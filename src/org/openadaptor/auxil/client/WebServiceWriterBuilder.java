/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.client;

import org.openadaptor.auxil.connector.soap.WebServiceWriteConnector;
import org.openadaptor.core.IWriteConnector;

/**
 * A <code>WriterBuilder</code> that creates a <code>WebServiceWriteConnector</code>.
 * 
 * @author Kris Lachor
 * @see SpringWriterBuilder
 * @see OAClient
 */
public class WebServiceWriterBuilder implements WriterBuilder {
  
  private static final String DEFAULT_ENDPOINT = "http://localhost:9999/OAService?wsdl";
  
  private static final String DEFAULT_WRITER_NAME = "OAWebServiceClient";
  
  private WebServiceWriteConnector webServiceWriteConnector;
  
  private String endpoint = DEFAULT_ENDPOINT;
  
  private String writerName = DEFAULT_WRITER_NAME;
  
  /**
   * Constructor.
   * 
   * Assumes the default endpoint.
   */
  public WebServiceWriterBuilder() {
  }
  
  /**
   * Constructor.
   * 
   * @param endpoint ws endpoint.
   */
  public WebServiceWriterBuilder(String endpoint) {
    this.endpoint = endpoint;
  }
  
  /**
   * Constructor.
   * 
   * @param endpoint ws endpoint.
   */
  public WebServiceWriterBuilder(String endpoint, String writerName) {
    this.endpoint = endpoint;
    this.writerName = writerName;
  }

  /**
   * Creates a <code>WebServiceWriteConnector</code>.
   */
  public IWriteConnector getWriter(){
    if(webServiceWriteConnector!=null){
      return webServiceWriteConnector;
    }
    webServiceWriteConnector = new WebServiceWriteConnector(writerName);
    webServiceWriteConnector.setEndpoint(endpoint);
    return webServiceWriteConnector;
  }

  /**
   * Allows to overwrite the default endpoint;
   * 
   * @param endpoint new ws endpoint.
   */
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

}
