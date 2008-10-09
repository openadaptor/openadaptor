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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ConnectionException;

/**
 * Binds to a webservice endpoint and delivers data by calling a method
 * on this service.
 * 
 * @author perryj, Kris Lachor
 */
public class WebServiceCXFWriteConnector extends AbstractWriteConnector {

  private static final Log log = LogFactory.getLog(WebServiceCXFWriteConnector.class);

  private Client client;

  private String methodName;

  private String endpoint;
  
  protected String httpProxyHost;
  
  protected String httpProxyPort;
  
  private DynamicClientFactory dcf = DynamicClientFactory.newInstance();

  /**
   * Constructor.
   */
  public WebServiceCXFWriteConnector() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id descriptive identifier.
   */
  public WebServiceCXFWriteConnector(String id) {
    super(id);
  }

  /**
   * Sets the <code>endpoint</code> with WSDL definition.
   */
  public void setEndpoint(final String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Sets the web service method name. If not set, the name will
   * be read from the WSDL definition.
   */
  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }
 
  /**
   * Creates a web service client based on <code>endpoint</code> WSDL defition.
   */
  public void connect() {
    try {
      client = dcf.createClient(endpoint);
      log.info(getId() + " bound to endpoint " + endpoint);
      
      /* If method name not supplied in config, read from WSDL */
      if(methodName==null){
        Service service = client.getEndpoint().getService();
        Collection serviceInfos = service.getServiceInfos();
        Iterator serviceInfosIt = serviceInfos.iterator();
        
        if(serviceInfosIt.hasNext()){         
          ServiceInfo serviceInfo = (ServiceInfo) serviceInfosIt.next();
          InterfaceInfo interfaceInfo = serviceInfo.getInterface();
          Collection operations = interfaceInfo.getOperations();
          Iterator operationsIt = operations.iterator();
          if(operationsIt.hasNext()){
            methodName = ((OperationInfo) operationsIt.next()).getName().getLocalPart() ;
            log.info("Operation name read from WSDL: " + methodName);
          }     
        }
      }
      else{
        log.info("Operation name from adaptor's config: " + methodName);
      }
      
      
    } catch (Exception e) {
      log.error("Error while instantiating CXF client", e);
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
   *      is also implemetned in {@link #connect()}, {@link #disconnect()} and
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
    return data.toString();
  }

  public void setHttpProxyHost(String httpProxyHost) {
    this.httpProxyHost = httpProxyHost;
  }

  public void setHttpProxyPort(String httpProxyPort) {
    this.httpProxyPort = httpProxyPort;
  }

}
