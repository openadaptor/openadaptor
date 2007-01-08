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

package org.oa3.auxil.connector.http;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.oa3.auxil.connector.soap.ReadConnectorWebService;
import org.oa3.core.connector.QueuingReadConnector;
import org.oa3.core.exception.ComponentException;
import org.oa3.util.ResourceUtils;

/**
 * base class for read connectors that require an http server
 * @see ReadConnectorWebService
 * @author perryj
 *
 */
public abstract class JettyReadConnector extends QueuingReadConnector {

  private static final Log log = LogFactory.getLog(JettyReadConnector.class);
  
  private int localJettyPort = 8080;
  private Server server;
  private String context = "/";
  private String path = "/*";
  private Servlet servlet;
  private Context root;
  
  protected JettyReadConnector() {
  }

  protected JettyReadConnector(String id) {
    super(id);
  }
  
  public void setJettyServer(final Server server) {
    this.server = server;
    localJettyPort = 0;
  }
  
  public void setLocalJettyPort(final int port) {
    if (server != null) {
      throw new ComponentException("attempt to configure jettyServer and localJettyPort", this);
    }
    localJettyPort = port;
  }

  public void setContext(final String context) {
    this.context = context;
  }
  
  public void setServlet(final Servlet servlet) {
    this.servlet = servlet;
  }
  
  public void setPath(final String path) {
    this.path = path;
  }
  
  protected Server getJettyServer() {
    return server;
  }
  
  protected int getJettyPort() {
    Connector[] connectors = server.getConnectors();
    for (int i = 0; i < connectors.length; i++) {
      if (connectors[i] instanceof SocketConnector) {
        return ((SocketConnector)connectors[i]).getLocalPort();
      }
    }
    return localJettyPort;
  }
  
  public Context getRootContext() {
    if (root == null) {
      Handler[] handlers = getJettyServer().getHandlers();
      for (int i = 0; handlers != null && i < handlers.length; i++) {
        if (handlers[i] instanceof Context) {
          return (Context) handlers[i];
        }
      }
    }
    if (root == null) {
      root = new Context(getJettyServer(), context, Context.SESSIONS);
    }
    return root;
  }
  
  protected String getJettyHost() {
    return ResourceUtils.getLocalHostname();
  }
  
  protected String getContext() {
    return context;
  }
  
  protected String getPath() {
    return path;
  }

  protected Servlet getServlet() {
    return servlet;
  }
  
  public void connect() {
    if (localJettyPort > 0 && server == null) {
      server = new Server(localJettyPort);
      try {
        server.start();
      } catch (Exception e) {
        log.error("failed to start jetty server", e);
        throw new ComponentException("failed to start local jetty server", this);
      }
    }
    getRootContext().addServlet(new ServletHolder(servlet), path);
  }

  public void disconnect() {
    if (localJettyPort > 0 && server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        log.error("failed to stop jetty server", e);
      }
    }
  }

}
