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

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IReadConnector;

/**
 * Makes a HTTP GET call to the specified URL.
 * 
 * HTTP specifications give the usage recommendation that (in a simplification) "GET" is 
 * for just getting (retrieving) data whereas "POST" may involve anything, like storing or updating data.
 * 
 * At present the verbatim content of the retrieved page is returns as a result.
 * Future extentions might attempt to parse certain elements from the returned page (header, body, ...).
 * 
 * The reader allows for optional setting of an HTTP proxy.
 * 
 * @author Kris Lachor
 */
public class HttpReadConnector extends AbstractHttpConnector implements IReadConnector {

  private static final Log log = LogFactory.getLog(HttpReadConnector.class);
  
  private boolean isDry = false;
  
  protected HttpMethod method = null;
  
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
    log.info("Created new HTTP read connector");
  }

  /**
   * HTTP does not require a permanent connection.
   * 
   * @see IReadConnector#connect()
   */
  public void connect(){
    setHostConfiguration();
    method = new GetMethod(url);
    /* Provide custom retry handler */
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
    isDry=false;
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
   * Makes an HTTP GET call. If the status of the returned page equates to an error, an 
   * empty array is returned. The same happens if an Exception is throws during page retrieval.
   * If the return status is correct, the verbatim result is returned.
   * 
   * @see IReadConnector#next(long)
   */
  public Object[] next(long timeoutMs) {
    Object [] result = new Object []{};
    try {
      
      /* Execute the method and check the status */ 
      int statusCode = client.executeMethod(method);
      if (statusCode != HttpStatus.SC_OK) {
        log.error("Method failed: " + method.getStatusLine());
        return result;
      }

      /* 
       * Read the response body and deal with the response.
       * Use caution: ensure correct character encoding and is not binary data
       */
      byte[] responseBody = method.getResponseBody();
      result = new String [] {new String(responseBody)};
            
    } catch (HttpException e) {
      log.error("Fatal protocol violation.", e);
    } catch (IOException e) {
      log.error("Fatal transport error.", e);
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
  
  /*
   * protected accessors for unit testing:
   */
  
  protected void setMethod(HttpMethod method) {
    this.method = method;
  }

  protected HttpMethod getMethod() {
    return method;
  }
  
}
