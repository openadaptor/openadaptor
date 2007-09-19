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

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ValidationException;

/**
 * Makes a HTTP GET call to the specified URL.
 * 
 * @author Kris Lachor
 */
public class HttpReadConnector extends Component implements IReadConnector {

  private static final Log log = LogFactory.getLog(HttpReadConnector.class);
  
  private String url = null;
  
  private String proxyHost = null;
  
  private String proxyPort = null;
  
  private HttpClient client = new HttpClient();
  
  private HttpMethod method = null;
  
  private boolean isDry = false;
  
  /**
   * Default constructor.
   */
  public HttpReadConnector() {
    log.info("Created new HTTP read connector");
  }

  /**
   * Constructor. 
   * 
   * @param id - the id
   */
  public HttpReadConnector(String id) {
    super(id);
  }

  /**
   * HTTP does not require a permanent connection.
   * 
   * @see IReadConnector#connect()
   */
  public void connect(){
    if(proxyHost!=null && proxyPort!=null){
      log.info("Setting Proxy: " + proxyHost + ":" + proxyPort);
      client.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));
    }else{
      log.info("Proxy not specified, using direct connection");
    }
    method = new GetMethod(url);
    /* Provide custom retry handler */
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
  }
  
  /**
   * HTTP does not require a permanent connection.
   * 
   * @see IReadConnector#disconnect()
   */
  public void disconnect(){
    log.info("HttpReadConnector#disconnect - no action");
  }
  
  /**
   * Makes an HTTP GET call.
   * 
   * @see IReadConnector#next(long)
   */
  public Object[] next(long timeoutMs) {
    Object [] result = null;
    try {
      
      /* Execute the method and check the status */ 
      int statusCode = client.executeMethod(method);
      if (statusCode != HttpStatus.SC_OK) {
        log.error("Method failed: " + method.getStatusLine());
        return new Object[]{};
      }

      /* 
       * Read the response body and deal with the response.
       * Use caution: ensure correct character encoding and is not binary data
       */
      byte[] responseBody = method.getResponseBody();
      result = new String [] {new String(responseBody)};
            
    } catch (HttpException e) {
      log.error("Fatal protocol violation.", e);
      isDry = true;
    } catch (IOException e) {
      log.error("Fatal transport error.", e);
      isDry = true;
    } finally {
      isDry = true;
      
      /* Release the connection */
      method.releaseConnection();
    }  
    return result;
  }

  /**
   * @see IReadConnector#isDry()
   * @return false
   */
  public boolean isDry() {
    return isDry;
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
   * @see {@link IReadConnector#getReaderContext()}
   * @return null
   */
  public Object getReaderContext() {
    return null;
  }
  
  /**
   * @see {@link IReadConnector#setReaderContext(Object)}
   */
  public void setReaderContext(Object context) {
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

  /*
   * protected accessors for unit testing:
   */
  
  protected void setMethod(HttpMethod method) {
    this.method = method;
  }

  protected HttpMethod getMethod() {
    return method;
  }

  protected void setClient(HttpClient client) {
    this.client = client;
  }
  
}
