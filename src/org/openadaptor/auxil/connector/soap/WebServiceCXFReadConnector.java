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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.openadaptor.core.IEnrichmentReadConnector;
import org.openadaptor.core.IReadConnector;

/**
 * WebServiceReadConnector calls a web service with a specified name and specified
 * endpoint. Returns web service's result for processing in subsequent nodes. At the moment 
 * it has fairly limited capability - for instance using an HTTP proxy server is not supported. 
 * Delegates to CXF to handle low level Web service/SOAP.
 * 
 * Initial version of this class is meant to be fully compatible with {@link WebServiceReadConnector}
 * (a XFire equivalent). Future Web service connectors development will involve CXF connectors only,
 * XFire based connectors are to be gradually phased out.
 * 
 * @author Kris Lachor
 */
public class WebServiceCXFReadConnector extends WebServiceReadConnector implements IEnrichmentReadConnector {

  private static final Log log = LogFactory.getLog(WebServiceCXFReadConnector.class);
 
  private Client client;
  
  private DynamicClientFactory dcf = DynamicClientFactory.newInstance();
  
  /**
   * Default constructor.
   */
  public WebServiceCXFReadConnector() {
    log.info("Created new web service read connector");
  }

  /**
   * Constructor. 
   * 
   * @param id
   */
  public WebServiceCXFReadConnector(String id) {
    super(id);
  }

  /**
   * Connects to web service using CXF Client.
   * 
   * @see IReadConnector#connect()
   */
  public void connect(){
    log.debug("About to connect.");
    try {
      client = dcf.createClient(getWsEndpoint());
      log.info(getId() + " bound to endpoint " + getWsEndpoint());
    } catch (Exception e) {
      log.error("Connection to web service failed: " + getWsEndpoint() + " " + getServiceName(), e);
    }
  }
  
  /**
   * Closes the CXF web service Client.
   * 
   * @see IReadConnector#disconnect()
   */
  public void disconnect(){
    log.debug("About to disconnect.");
    client.destroy();
  }

  /**
   * Invokes webservice via CXF client.
   */
  public Object [] invoke(){
    Object [] result = null;
    try {
      result = client.invoke(getServiceName(), getParameters() != null ? 
    		      getParameters().toArray(new Object[getParameters().size()]) : NO_PARAMETERS);
      log.debug("Invoked the service. Result = " + (result != null && result.length > 0 ? result[0] : result));
    } catch (Exception e) {
      log.error("Call to web service failed: " + getWsEndpoint() + " " + getServiceName(), e);
      return null;
    } 
    return result;
  }

}
