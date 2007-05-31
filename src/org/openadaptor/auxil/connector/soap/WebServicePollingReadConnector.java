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

package org.openadaptor.auxil.connector.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.client.Client;
import org.openadaptor.core.connector.QueuingReadConnector;

/**
 * ReadConnector that connects to a webservice, polls it and saves the returned data
 * in a queue.
 * 
 * @author lachork
 */
public class WebServicePollingReadConnector extends QueuingReadConnector {

  private static final Log log = LogFactory.getLog(WebServicePollingReadConnector.class);

  private String wsEndpoint = null;
  
  private String serviceName = null;
  
  private Client client = null;
  
  /**
   * Default constructor.
   */
  public WebServicePollingReadConnector() {}

  /**
   * Constructor. 
   * 
   * @param id
   */
  public WebServicePollingReadConnector(String id) {
    super(id);
  }

  /**
   * Connects to web service.
   */
  public void connect() {
    log.debug("About to connect.");
    try {
      client = new Client(new URL(wsEndpoint));
    } catch (MalformedURLException e) {
      log.error("Wrong web service endpoint URL: " + wsEndpoint + " " + serviceName, e);
    } catch (Exception e) {
      log.error("Connection to web service failed: " + wsEndpoint + " " + serviceName, e);
    }
  }
  
  
  public void disconnect(){
    log.debug("About to disconnect.");
    client.close();
  }
  
  
  /**
   * Invokes webservice, puts results to a queue.
   */
  public void invoke(){
    Object [] result = null;
    try {
      result = client.invoke(serviceName, new Object[] {});
    } catch (Exception e) {
      log.error("Call to web service failed: " + wsEndpoint + " " + serviceName);
      return;
    } 
    for(int i=0; i<result.length; i++){
      enqueue(result[i]);
    }
  }


  public void validate(List exceptions) {
    log.debug("No validation rules.");
  }

  /**
   * Set name of web service
   * 
   * @param name Service name
   */
  public void setServiceName(final String name) {
    serviceName = name;
  }

  public String getWsEndpoint() {
    return wsEndpoint;
  }

  public void setWsEndpoint(String wsEndpoint) {
    this.wsEndpoint = wsEndpoint;
  }

  public String getServiceName() {
    return serviceName;
  }
 
}
