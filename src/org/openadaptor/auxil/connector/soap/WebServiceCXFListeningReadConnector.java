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

/**
 * Draft of a CXF framework based Web service endpoint.
 * Not ready for use.
 * 
 * @author Kris Lachor
 */
public class WebServiceCXFListeningReadConnector extends QueuingReadConnector implements IStringDataProcessor {

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
   * Set up webservice
   */
  public void connect() {
    ServerFactoryBean svrFactory = new ServerFactoryBean();
    svrFactory.setServiceClass(IStringDataProcessor.class);
    svrFactory.setAddress("http://localhost:9999/" + serviceName);
    svrFactory.setServiceBean(this);
    svrFactory.create();
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

}
