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
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

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
 * This connector subsumes the XFire based {@link WebServiceWriteConnector}.
 * Because XFire based connector may be phased out in the future this 
 * connector doesn't extend {@link WebServiceWriteConnector}, event though
 * big part of functionality is the same. 
 * 
 * NOTE: Class requires JVM 1.5.
 * NOTE: Use of the Dynamic compiler requires a full JDK to be available. 
 *       It generates java code and calls off to "javac" to compile the code.
 * 
 * @author Kris Lachor
 */
public class WebServiceCXFWriteConnector extends AbstractWriteConnector {

  private static final Log log = LogFactory.getLog(WebServiceCXFWriteConnector.class);

  private Client client;

  private String methodName;

  private String endpoint;
  
  /**
   * HTTP proxy is not yet supported.
   */
  protected String httpProxyHost;
  
  /**
   * HTTP proxy is not yet supported.
   */
  protected String httpProxyPort;
  
  /**
   * DynamicClientFactory, when used to create a CXF Client, will go an extra 
   * step compared to the ClientFactoryBean, and generate and compile JAXB POJOs
   * in the background. Although not used by OA at the moment, in the 
   * future they may be combined with a use of dynamic languages such as Groovy.
   * 
   * CXF documentation is not clear on how to use ClientFactoryBean at this time.
   */
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
   * Creates a web service client based on <code>endpoint</code> WSDL definition.
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
    client.destroy();
  }

  /**
   * Invokes the web service defined in <code>endpoint</code> WSDL.
   * Call the web service as many times as there are elements in the
   * <code>data</code> array.
   * 
   * NOTE:  Groovy Web services support for dynamic clients (comming soon, 
   * accoring to CXF's website) may provide alternative ways of calling 
   * the service in the future. 
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

  /**
   * HTTP proxy is not yet supported.
   */
  public void setHttpProxyHost(String httpProxyHost) {
    this.httpProxyHost = httpProxyHost;
  }

  /**
   * HTTP proxy is not yet supported.
   */
  public void setHttpProxyPort(String httpProxyPort) {
    this.httpProxyPort = httpProxyPort;
  }

}
