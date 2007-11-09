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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openadaptor.core.IWriteConnector;

/**
 * Makes a HTTP POST call to the specified URL.
 * 
 * The POST method is used to request that the destination server accept the data enclosed in the request. 
 * POST is designed to allow a uniform method to cover a variety of functions such as appending to a database, 
 * providing data to a data-handling process or posting to a message board.
 * It is generally expected that a POST request will have some side effect on the server such as writing 
 * to a database.
 * 
 * The writer allows for optional setting of an HTTP proxy.
 * 
 * @author Kris Lachor
 */
public class HttpWriteConnector extends AbstractHttpConnector implements IWriteConnector {

  private static final Log log = LogFactory.getLog(HttpWriteConnector.class);
  
  protected static final String RESPONSE_KEY = "data";
  
  /**
   * Default constructor.
   */
  public HttpWriteConnector() {
    log.info("Created new HTTP write connector");
  }

  /**
   * Constructor. 
   * 
   * @param id - the id
   */
  public HttpWriteConnector(String id) {
    super(id);
    log.info("Created new HTTP write connector");
  }

  /**
   * HTTP does not require a permanent connection.
   * 
   * @see IWriteConnector#connect()
   */
  public void connect(){
    setHostConfiguration();
  }

  /**
   * HTTP does not require a permanent connection.
   * 
   * @see IWriteConnector#disconnect()
   */
  public void disconnect(){
    log.info("HttpWriteConnector#disconnect - no action");
  }

  /**
   * @see IWriteConnector#deliver(Object[])
   * ToDo: handling of the response
   * ToDo: javadoc comments
   */
  public Object deliver(Object[] data) {
    log.info("HTTP write connector, delivering data..");
    Object result = new Object[data.length];
    PostMethod postMethod = null;
    for(int i=0; i<data.length; i++){
      NameValuePair [] nameValuePairs = { new NameValuePair(RESPONSE_KEY, data[i].toString())};
      postMethod = new PostMethod(url);
      postMethod.setRequestBody(nameValuePairs);   
      try {
        
        /* Execute the method and check the status */ 
        int statusCode = client.executeMethod(postMethod);
        if (statusCode != HttpStatus.SC_OK) {
          log.error("Method failed: " + postMethod.getStatusLine());
          return result;
        }     
        
//        /* Handle response */
//        InputStream in = postMethod.getResponseBodyAsStream();
//        
 
      } catch (IOException e) {
        log.error("Error while reading response from HTTP POST", e);
      } finally {
        postMethod.releaseConnection();
      }
    }
    return result;
  }
  
}
