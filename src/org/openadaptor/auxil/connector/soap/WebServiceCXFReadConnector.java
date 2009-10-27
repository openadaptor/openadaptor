/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.IEnrichmentReadConnector;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.ValidationException;

/**
 * WebServiceReadConnector calls a web service with a specified name and specified
 * endpoint. Returns web service's result for processing in subsequent nodes. At the moment 
 * it has fairly limited capability - for instance using an HTTP proxy server is not supported. 
 * Delegates to CXF to handle low level Web service/SOAP.
 * 
 * This connector may be used in multiple threads running concurrently (adaptors with multiple
 * {@link IReadConnector}s) when embedded in an {@link IEnrichmentReadConnector}.
 * 
 * Initial version of this class was meant to be fully compatible with {@link WebServiceReadConnector}
 * (a XFire equivalent). Going forwards, Web service connectors development will involve CXF connectors 
 * only, XFire based connectors are to be gradually phased out.
 * 
 * NOTE: Class requires JVM 1.5.
 * NOTE: Use of the Dynamic compiler requires a full JDK to be available. 
 *       It generates java code and calls off to "javac" to compile the code.
 * 
 * @author Kris Lachor
 */
public class WebServiceCXFReadConnector extends Component implements IEnrichmentReadConnector {

  private static final Log log = LogFactory.getLog(WebServiceCXFReadConnector.class);
 
  protected static Object[] NO_PARAMETERS = new Object[0];

  private String wsEndpoint = null;
  private QName serviceName = null;
  private List parameters;
  private Client client;
  
  /**
   * See comments in {@link WebServiceCXFWriteConnector%dcf}.
   */
  private DynamicClientFactory dcf = DynamicClientFactory.newInstance();
  
  /**
   * Default constructor.
   */
  public WebServiceCXFReadConnector() {
    log.info("Created new CXF web service read connector");
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
      String msg = "Connection to web service failed: " + getWsEndpoint() + " " + getServiceName();
      log.error(msg, e);
      throw new ConnectionException(msg, e);
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
   * Calls {@link #invoke(List)} with parameters set in the {@link #parameters} property.
   * 
   * @see #invoke(List)
   */
  public Object [] invoke(){
	  return invoke(getParameters());
  }
  
  /**
   * Invokes webservice via CXF client.
   * 
   * Groovy Web services support for dynamic clients (comming soon, accoring to CXF's website)
   * may provide alternative ways of calling the service in the future. 
   */
  public Object [] invoke(List wsParameters){
    Object [] result = null;
    try {
      result = client.invoke(getServiceName(), wsParameters != null ? 
    		  wsParameters.toArray(new Object[wsParameters.size()]) : NO_PARAMETERS);
      log.debug("Invoked the service. Result = " + (result != null && result.length > 0 ? result[0] : result));
    } catch (Exception e) {
      String msg = "Call to web service failed: " + getWsEndpoint() + " " + getServiceName();
      log.error(msg, e);
      throw new OAException(msg, e);     
    } 
    return result;
  }

  /**
   * Ensures that <code>wsEndpoint</code> and <code>serviceName</code> properties
   * have been set.
   */
  public void validate(List exceptions) {
    log.debug("Validating wsEndpoing: " + wsEndpoint);
    if(null==wsEndpoint || wsEndpoint.indexOf("wsdl")==-1){
      exceptions.add(new ValidationException("Unknown web service endpoint", this));
    }
    if(null==serviceName){
      exceptions.add(new ValidationException("Unknown web service endpoint", this));
    }
  }
  
  /**
   * Invokes the web service.
   * 
   * @see IReadConnector#next(long)
   * @see #invoke()
   */
  public Object[] next(long timeoutMs) {
    return invoke();
  }
  
  /**
   * Uses values from <code>inputParameters</code> to set parameters
   * for the web service. Invokes the web service.
   * 
   * Method is synchronized for thread safety when the connector is run by multiple threads, such as a 
   * fan-in to an enrichment processor with this connector.
   * 
   * @see IEnrichmentReadConnector#next(IOrderedMap, long)
   * @see #invoke()
   */
  public synchronized Object[] next(IOrderedMap inputParameters, long timeout) {
	List dynamicParams = new ArrayList();
    log.debug("WS reader received dynamic parameters: " + inputParameters);
    if(parameters==null){
      dynamicParams.addAll(parameters);
    }
    dynamicParams.addAll(inputParameters.values());
    return invoke(dynamicParams);
  }
  
  /**
   * Reader has no specific context at the moment. Always returns null.
   * 
   * @see IReadConnector#getReaderContext()
   * @return null
   */
  public Object getReaderContext() {
    return null;
  }
  
  /**
   * Always returns false.
   * 
   * @see IReadConnector#isDry()
   */
  public boolean isDry() {
    return false;
  }
  
  /**
   * Does nothing.
   * 
   * @see IReadConnector#setReaderContext(Object)
   */
  public void setReaderContext(Object context) {
  }
  
  /**
   * Set name of web service
   * 
   * @param name Service name
   */
  public void setServiceName(final QName name) {
    serviceName = name;
  }

  /**
   * Getter for Web Service endpoint.
   * 
   * @return the Web Service endpoint.
   */
  public String getWsEndpoint() {
    return wsEndpoint;
  }

  /**
   * Sets web service endpoint.
   * 
   * @param wsEndpoint - web service end point.
   */
  public void setWsEndpoint(String wsEndpoint) {
    this.wsEndpoint = wsEndpoint;
  }

  /**
   * Getter for service name.
   * 
   * @return the service name.
   */
  public QName getServiceName() {
    return serviceName;
  }

  /**
   * Returns the {@link List} of parameters.
   * 
   * @return the parameters
   */
  public List getParameters() {
    return parameters;
  }

  /**
   * Set the parameters that will be passed to the web service.
   * <p>
   * The types of the parameters must match the types of the 
   * parameters to the target method, in the correct order. 
   * 
   * @param parameters the parameters to set
   */
  public void setParameters(List parameters) {
    this.parameters = parameters;
  }
  
}