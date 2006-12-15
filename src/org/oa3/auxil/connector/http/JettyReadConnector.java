package org.oa3.auxil.connector.http;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.oa3.auxil.connector.QueuingReadConnector;
import org.oa3.auxil.connector.soap.ReadConnectorWebService;
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
  
  // TODO read this from the server instance (since jetty server can be non local)
  protected int getJettyPort() {
    return localJettyPort;
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
    Context root = new Context(getJettyServer(), context,Context.SESSIONS);
    root.addServlet(new ServletHolder(servlet), path);
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
