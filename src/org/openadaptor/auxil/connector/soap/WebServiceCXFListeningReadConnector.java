/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.soap;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.jmx.Administrable;

/**
 * ReadConnector that exposes a webservice which allows external clients to send it data.
 * This connector subsumes the XFire based {@link WebServiceListeningReadConnector}.
 * 
 * @author Kris Lachor
 */
public class WebServiceCXFListeningReadConnector extends QueuingReadConnector implements IStringDataProcessor,
           Administrable{

  private static final Log log = LogFactory.getLog(WebServiceCXFListeningReadConnector.class);

  private String serviceName = "openadaptorws";
  
  private String namespace = "http://www.openadaptor.org";

  /**
   * Default constructor.
   */
  public WebServiceCXFListeningReadConnector() {
    super();
  }

  /**
   * Constructor.
   */
  public WebServiceCXFListeningReadConnector(String id) {
    super(id);
  }

  /**
   * Set name of web service
   * @param name Service name
   */
  public void setServiceName(final String name) {
    serviceName = name;
  }

  /**
   * Sets up a CXF webservice.
   */
  public void connect() {
    ServerFactoryBean svrFactory = new ServerFactoryBean();
    svrFactory.setServiceClass(IStringDataProcessor.class);
    svrFactory.setAddress("http://localhost:9999/" + serviceName);
    svrFactory.setServiceBean(this);
    svrFactory.create();
    log.info("Started WS Endpoint");
  }

  /**
   * Process requests
   *
   * @param s request string
   */
  public void process(String s) {
    enqueue(s);
  }


  public void validate(List exceptions) {
  }

  public void disconnect() { 
  }
  
  /**
   * @see Administrable
   */
  public Object getAdmin() {
    return new Admin();
  }

  /**
   * @see Administrable
   */
  public interface AdminMBean {
    int getQueueLimit();

    int getQueueSize();
  }
  
  public class Admin implements AdminMBean {

    public int getQueueLimit() {
      return WebServiceCXFListeningReadConnector.this.getQueueLimit();
    }

    public int getQueueSize() {
      return WebServiceCXFListeningReadConnector.this.getQueueSize();
    }
  }

}
