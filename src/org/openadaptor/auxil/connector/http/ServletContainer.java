/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.connector.http;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openadaptor.util.NetUtil;

/**
 * Class implements a simple HTTP server with a servlet container. 
 * Uses Jetty server.
 */
public class ServletContainer {

  private static final Log log = LogFactory.getLog(ServletContainer.class);
  
  private int port = 8080;
  
  private Server server;
  
  private String context = "/";
  
  private Context root;
  
  private boolean managed = false;
  
  private int refs;

  /**
   * Constructor.
   */
  public ServletContainer() {}
  
  public ServletContainer(final Server server) {
    this.server = server;
  }

  public void setPort(final int port) {
    if (server == null) {
      this.port = port;
    } else {
      throw new RuntimeException("jetty server already instantiated");
    }
  }

  public int getPort() {
    if (server != null) {
      Connector[] connectors = server.getConnectors();
      for (int i = 0; i < connectors.length; i++) {
        if (connectors[i] instanceof SocketConnector) {
          return ((SocketConnector)connectors[i]).getPort();
        }
      }
    }
    return port;
  }

  
  //EH - this did not make any sense whatsoever!
  //The only thing this may do is throw an exception if a server is supplied.
  //Possibly should have been if (this.server == null)  ?????

  public void setJettyServer(final Server server) {
    if (this.server == null) { //Only set if one is not already configured.
      this.server = server;
    } 
    else {
      String msg="Jetty server has aready been configured";
      log.warn(msg);
      throw new RuntimeException(msg);
    }
  }
  
  //EH - This is bad! Should not be setting managed flag here.
  //
  public Server getJettyServer() {
    if (server == null) {
      log.info("Starting Jetty server on port "+port);
      server = new Server(port);
      managed = true;
    }
    else {
      log.debug("Jetty server already configured - "+server);
    }
    return server;
  }
  
  private Context getRootContext() {
    getJettyServer();
    if (root == null) {
      Handler[] handlers = server.getHandlers();
      for (int i = 0; handlers != null && i < handlers.length; i++) {
        if (handlers[i] instanceof Context) {
          return (Context) handlers[i];
        }
      }
    }
    if (root == null) {
      root = new Context(server, context, Context.SESSIONS);
    }
    return root;
  }

  public String getContext() {
    return context;
  }

  public String getHost() {
    return NetUtil.getLocalHostname();  
  }
  
  public void addServlet(Servlet servlet, String path) {
    getRootContext().addServlet(new ServletHolder(servlet), path);
  }
  
  public void start() {
    getJettyServer();
    if (managed && ++refs == 1) {
      try {
        log.debug("Starting server "+server);
        server.start();
      } catch (Exception e) {
        log.error("failed to start jetty server", e);
        throw new RuntimeException("failed to start jetty server");
      }
    }
  }

  public void join() {
    try {
      server.join();
    } catch (InterruptedException e) {
    }
  }

  public void stop() {
    if (managed && --refs <= 0) {
      try {
        server.stop();
      } catch (Exception e) {
        log.error("failed to stop jetty server", e);
        throw new RuntimeException("failed to stop jetty server");
      }
    }
  }
}
