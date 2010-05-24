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

package org.openadaptor.auxil.connector.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ConnectionException;

/**
 * Binds to a webservice endpoint and delivers data by calling a method
 * on this service.
 * 
 * This connector is now subsumed by the CXF based {@link WebServiceCXFWriteConnector}.
 * 
 * @author perryj, Kris Lachor
 */
public class WebServiceWriteConnector extends AbstractWriteConnector {

  private static final Log log = LogFactory.getLog(WebServiceWriteConnector.class);

  private Client client;

  private String methodName;

  private String endpoint;

  /**
   * Constructor.
   */
  public WebServiceWriteConnector() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id descriptive identifier.
   */
  public WebServiceWriteConnector(String id) {
    super(id);
  }

  /**
   * Sets the <code>endpoint</code> with WSDL definition.
   */
  public void setEndpoint(final String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * TODO manual setting of method name might be unnecessary.
   */
  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }
 
  /**
   * Creates a web service client based on <code>endpoint</code> WSDL definition.
   */
  public void connect() {
    try {
      client = new Client(new URL(endpoint));
      log.info(getId() + " bound to endpoint " + endpoint);
      
      /* If method name not supplied in config, read from WSDL */
      if(methodName==null){
        Service service = client.getService();
        log.info("Service name: " + service.getSimpleName());
        Collection col = service.getServiceInfo().getOperations();
        Iterator it = col.iterator();
        if(it.hasNext()){
          OperationInfo operationInfo = (OperationInfo) it.next();
          log.info("Operation name: " + operationInfo.getName());
          methodName = operationInfo.getName();
        }
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Malformed Url exception ", e);
    } catch (Exception e) {
      throw new ConnectionException("failed to connect", e, this);
    }
  }
  
  /**
   * Sets the web service client to null.
   */
  public void disconnect() {
    client = null;
  }

  /**
   * Invokes the web service defined in <code>endpoint</code> WSDL.
   * Call the web service as many times as there are elements in the
   * <code>data</code> array.
   * 
   * @param data - an array with data to be passed to the web service.
   * TODO review the fact of connecting and disconnecting the client here - this
   *      is also implemented in {@link #connect()}, {@link #disconnect()} and
   *      called by the framework.
   */
  public Object deliver(Object[] data) {
    if (client == null) {
      log.info("Connecting web service client.");
      connect();
    }
    try {
      for (int i = 0; i < data.length; i++) {
        try {
          client.invoke(methodName, new Object[] { marshall(data[i]) });
        } catch (RuntimeException e) {
          client = null;
          throw e;
        }
      }
      return null;
    } catch (Exception e) {
      throw new ConnectionException("webservice call failed", e, this);
    }
  }

  protected Object marshall(Object data) {
    String result = null;
    if(data instanceof Properties){
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Properties props = (Properties) data;
      try {
        props.store(bos, "");
      } catch (IOException e) {
        log.error("Error while marshalling properties.",e);
      }
      result = bos.toString();
    }
    else{
      result = data.toString();
    }
    return result;
  }

}
