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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.openadaptor.auxil.connector.http.ServletContainer;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;
import org.openadaptor.core.transaction.ITransactionalResource;
import org.openadaptor.util.NetUtil;

public class ExceptionManager {

  private static final Log log = LogFactory.getLog(ExceptionManager.class);

  private ExceptionStore exceptionStore;

  private ReadConnectorWebService webService;

  private int port = 8080;
  
  public void setPort(int port) {
    this.port = port;
  }

  public void setExceptionStore(final ExceptionStore exceptionStore) {
    this.exceptionStore = exceptionStore;
  }

  public static void main(String[] args) {

    ExceptionManager mgr = new ExceptionManager();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-dir")) {
        mgr.setExceptionStore(new ExceptionFileStore(args[++i]));
      } else if (args[i].equalsIgnoreCase("-port")) {
        mgr.setPort(Integer.parseInt(args[++i]));
      }
    }

    mgr.run();
  }

  public void run() {

    Server server;
    
    try {
      server = new Server(port);
      server.start();
    } catch (Exception e) {
      log.error("failed to start jetty server", e);
      throw new RuntimeException("failed to start local jetty server", e);
    }

    // create web service to receive exceptions and store
    ServletContainer servletContainer = new ServletContainer(server);
    webService = new ReadConnectorWebService("WebService");
    webService.setServletContainer(servletContainer);
    webService.setServiceName("ExceptionManager");
    webService.setPath("/soap/*");
    webService.connect();

    // create servlet for browsing exceptions
    webService.addServlet(new ExceptionManagerServlet(exceptionStore), "/admin/*");
    log.info("admin interface at http://" + NetUtil.getLocalHostAddress() + ":" + port + "/admin");

    // poll webservice read connector for new data
    while (!webService.isDry()) {
      ITransactionalResource txnResource = (ITransactionalResource) webService.getResource();
      try {
        Object[] data = webService.next(1000);
        for (int i = 0; data != null && i < data.length; i++) {
          String id = exceptionStore.store(data[i].toString());
          log.debug("stored new exception " + id);
        }
        txnResource.commit();
      } catch (RuntimeException e) {
        txnResource.rollback(e);
      }
    }

  }
}
