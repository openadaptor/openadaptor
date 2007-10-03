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

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ValidationException;

/**
 * 
 * @author Kris Lachor
 */
public abstract class AbstractHttpConnector extends Component {

  private static final Log log = LogFactory.getLog(AbstractHttpConnector.class);
  
  protected String url = null;
  
  protected String proxyHost = null;
  
  protected String proxyPort = null;
  
  protected HttpClient client = new HttpClient();
 
  
  /**
   * Default constructor.
   */
  public AbstractHttpConnector() {
  }

  /**
   * Constructor. 
   * 
   * @param id - the id
   */
  public AbstractHttpConnector(String id) {
    super(id);
  }
  
  protected void setHostConfiguration(){
    if(proxyHost!=null && proxyPort!=null){
      log.info("Setting Proxy: " + proxyHost + ":" + proxyPort);
      client.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));
    }else{
      log.info("Proxy not specified, using direct connection");
    }
  }
 
  /**
   * Validates mandatory properties: <code>url</code>.
   * 
   * @see {@link IReadConnector#validate(List)}
   */
  public void validate(List exceptions) {
    log.debug("Validating url: " + url);
    if(null==url || url.indexOf("http")==-1){
      exceptions.add(new ValidationException("Unknown URL", this));
    }
  }
  
  /**
   * Sets the <code>url</code> (mandatory).
   * 
   * @param url a valid URL of an HTTP resource
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Sets the proxy host (optional).
   * 
   * @param proxyHost the proxy host name
   */
  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  /**
   * Sets the proxy port (optional).
   * 
   * @param proxyPort the proxy port number
   */
  public void setProxyPort(String proxyPort) {
    this.proxyPort = proxyPort;
  }

  protected void setClient(HttpClient client) {
    this.client = client;
  }
  
}
