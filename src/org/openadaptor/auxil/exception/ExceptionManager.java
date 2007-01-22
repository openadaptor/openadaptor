/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.auxil.exception;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.SessionHandler;
import org.openadaptor.auxil.connector.http.ServletContainer;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;
import org.openadaptor.core.transaction.ITransactionalResource;
import org.openadaptor.util.NetUtil;

public class ExceptionManager {

  private static final Log log = LogFactory.getLog(ExceptionManager.class);

  private Server server;
  
  private ExceptionStore exceptionStore;

  private ReadConnectorWebService webService;

  public ExceptionManager() {
  }
  
  protected String[] processArgs(String[] args) {

    int port = 8080;
    String realmFile = null;
    boolean unsecured = false;
    
    HashUserRealm realm = createDefaultRealm();
    
    ArrayList unprocessedArgs = new ArrayList();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-port")) {
        port = Integer.parseInt(args[++i]);
      } else if (args[i].equalsIgnoreCase("-unsecured")) {
        unsecured = true;
      } else if (args[i].equalsIgnoreCase("-realm")) {
        realmFile = args[++i];
      } else {
        unprocessedArgs.add(args[i]);
      }
    }

    if (!unsecured) {
      if (realmFile == null) {
        realm = createDefaultRealm();
      } else {
        try {
          realm = new HashUserRealm("Realm", realmFile);
        } catch (IOException e) {
          throw new RuntimeException("failed to create user realm, " + e.getMessage(), e);
        }
      }
    }
    setServer(createDefaultServer(port, realm));
    return (String[]) unprocessedArgs.toArray(new String[unprocessedArgs.size()]);
  }

  public void setExceptionStore(final ExceptionStore exceptionStore) {
    this.exceptionStore = exceptionStore;
  }

  public void setServer(final Server server) {
    this.server = server;
  }
  
  protected static HashUserRealm createDefaultRealm() {
    HashUserRealm realm = new HashUserRealm();
    realm.put("test", "password");
    realm.addUserToRole("test", "view");
    realm.put("testadmin", "password");
    realm.addUserToRole("testadmin", "view");
    realm.addUserToRole("testadmin", "admin");
    return realm;
  }
  
  protected static Server createDefaultServer(int port, HashUserRealm realm) {
    SecurityHandler securityHandler = null;
    
    if (realm != null) {
      Constraint viewConstraint = new Constraint();
      viewConstraint.setName(Constraint.__BASIC_AUTH);
      viewConstraint.setRoles(new String[] {"view"});
      viewConstraint.setAuthenticate(true);
      
      ConstraintMapping viewMapping = new ConstraintMapping();
      viewMapping.setConstraint(viewConstraint);
      viewMapping.setPathSpec("/admin/*");
      
      securityHandler = new SecurityHandler();
      securityHandler.setUserRealm(realm);
      securityHandler.setConstraintMappings(new ConstraintMapping[] {viewMapping});
    }
    
    Context context = new Context();
    context.setContextPath("/");
    if (securityHandler != null) {
      context.addHandler(new SessionHandler());
    }
    context.addHandler(securityHandler);
    
    Server server = new Server(port);
    server.setHandlers(new Handler[] {context, new DefaultHandler()});
    
    return server;
  }
  
  public void run() {

    // start jetty
    if (!server.isStarted() && !server.isStarting()) {
      try {
        server.start();
      } catch (Exception e) {
        log.error("failed to start jetty server", e);
        throw new RuntimeException("failed to start local jetty server", e);
      }
    }

    // create web service to receive exceptions and store
    ServletContainer servletContainer = new ServletContainer(server);
    webService = new ReadConnectorWebService("WebService");
    webService.setServletContainer(servletContainer);
    webService.setServiceName("ExceptionManager");
    webService.setPath("/soap/*");
    webService.connect();

    // create servlet for browsing and managing exceptions
    webService.addServlet(new ExceptionManagerServlet(exceptionStore), "/admin/*");
    log.info("admin interface at http://" + NetUtil.getLocalHostAddress() + ":" + getServerPort(server) + "/admin");

    // receive incoming exceptions and store them
    while (!webService.isDry()) {
      ITransactionalResource txnResource = (ITransactionalResource) webService.getResource();
      try {
        Object[] data = webService.next(1000);
        for (int i = 0; data != null && i < data.length; i++) {
          exceptionStore.store(data[i].toString());
          log.debug("stored new exception");
        }
        txnResource.commit();
      } catch (RuntimeException e) {
        txnResource.rollback(e);
      }
    }

  }

  private int getServerPort(Server server) {
    Connector[] connectors = server.getConnectors();
    for (int i = 0; i < connectors.length; i++) {
      if (connectors[i] instanceof SocketConnector) {
        return ((SocketConnector)connectors[i]).getPort();
      }
    }
    return 0;
  }
}
