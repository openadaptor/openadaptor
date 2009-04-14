/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

import org.openadaptor.core.connector.QueuingReadConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for read connectors that require a http server with a servlet container.
 * 
 * @see org.openadaptor.auxil.connector.soap.WebServiceListeningReadConnector
 * @author perryj
 */
public abstract class ServletServingReadConnector extends QueuingReadConnector {

  private static final Log log = LogFactory.getLog(ServletServingReadConnector.class);
  
  private ServletContainer servletContainer = new ServletContainer();
  
  private String path = "/*";
  
  private Servlet servlet;
  
  protected ServletServingReadConnector() {}

  protected ServletServingReadConnector(String id) {
    super(id);
  }
  
  public void connect() {
    log.info("Starting servlet container "+servletContainer);
    servletContainer.start();
    addServlet(servlet, path);
  }
  
  public void disconnect() {
    log.info("Stopping servlet container.");
    servletContainer.stop();
  }
  
  public void addServlet(Servlet servlet, String path) {
    servletContainer.addServlet(servlet, path);
  }
  
  public void setServletContainer(final ServletContainer servletContainer) {
    this.servletContainer = servletContainer;
  }

  public ServletContainer getServletContainer() {
    return servletContainer;
  }
  
  public void setPort(final int port) {
    servletContainer.setPort(port);
  }

  public void setServlet(final Servlet servlet) {
    this.servlet = servlet;
  }
  
  public void setPath(final String path) {
    this.path = path;
  }
  
  protected String getPath() {
    return path;
  }  
}
