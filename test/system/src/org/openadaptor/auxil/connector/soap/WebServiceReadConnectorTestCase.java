/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.auxil.connector.soap;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.server.http.XFireHttpServer;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.test.Echo;
import org.codehaus.xfire.test.EchoImpl;

/**
 * Unit tests for {@link WebServiceReadConnector}.
 * 
 * @author Kris Lachor
 */
public class WebServiceReadConnectorTestCase extends TestCase {

  public static final int PORT_NR = 9998;
  
  public static final String URL_PREFIX = "http://localhost:" + PORT_NR;
  
  public static final String TEST_SERVICE_NAME = "getInt";
  
  private ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
  
  private XFireProxyFactory proxyFactory = new XFireProxyFactory();
 
  private ServiceStarter serviceServer = null;
  
  private WebServiceReadConnector wsConnector = new WebServiceReadConnector();
 
  protected void setUp() throws Exception {
    super.setUp();
    serviceServer = new ServiceStarter();
    serviceServer.start();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    serviceServer.stop();
  }

  /**
   * Tests that web services can be set up locally.
   */
  public void testLocalWebServiceServer() throws Exception{
    /*  Create a service model for the client and a client proxy */
    Service serviceModel = serviceFactory.create(Echo.class);
    Echo echoService = (Echo) proxyFactory.create(serviceModel, URL_PREFIX + "/Echo");
    Assert.assertEquals("Test local webservice", echoService.echo("Test local webservice"));
  }
  
  /**
   * Tests local random int generating web service.
   */
  public void testRandomIntWebService() throws Exception{
    Service serviceModel = serviceFactory.create(IRandomIntegerGeneratorWS.class);
    IRandomIntegerGeneratorWS service = (IRandomIntegerGeneratorWS) proxyFactory.create(serviceModel, URL_PREFIX + "/IRandomIntegerGeneratorWS");
    checkRandomInteger(service.getInt());
  }
  
  /**
   * Tests local random int generating web service using 
   * XFire ServiceFactory/ProxyFactory and Java reflection API.
   */
  public void testRandomIntWebServiceClientThroughReflection() throws Exception{
    String serviceInterfaceName = "org.openadaptor.auxil.connector.soap.IRandomIntegerGeneratorWS";
    String methodName = TEST_SERVICE_NAME;
    Class serviceInterfaceClass = Class.forName(serviceInterfaceName);
    Service serviceModel = serviceFactory.create(serviceInterfaceClass);
    Object service = proxyFactory.create(serviceModel, URL_PREFIX + "/IRandomIntegerGeneratorWS");
    Method method = service.getClass().getMethod(methodName, new Class[]{});
    Object result = method.invoke(service, new Object[]{});
    checkRandomInteger(result);
  }
  
  /**
   * Tests local random int generating web service using XFire Client API.
   */
  public void testRandomIntWebServiceClient() throws Exception{
    Client client;
    client = new Client(new URL(URL_PREFIX + "/IRandomIntegerGeneratorWS"  + "?wsdl"));
    Object obj = client.invoke(TEST_SERVICE_NAME, new Object[] {});
    Object [] res = (Object []) obj;
    for(int i=0; i<res.length; i++){
      checkRandomInteger(res[i]);
    }
    client.close();
  }
  
  private void checkRandomInteger(Object obj){
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj instanceof Integer);
    Integer intObj = (Integer) obj;
    Assert.assertTrue(intObj.intValue() > 0);
    Assert.assertTrue(intObj.intValue() < 100);
  }
  
  public void testWebServiceReadConnector1(){
    wsConnector.setWsEndpoint(URL_PREFIX + "/IRandomIntegerGeneratorWS"  + "?wsdl");
    wsConnector.setServiceName(TEST_SERVICE_NAME);
    wsConnector.connect();
    new Thread(){
      public void run() {
        wsConnector.invoke();  
      }
    }.start();
    Object[] data = wsConnector.next(0);
    assertTrue(data.length == 1);
    checkRandomInteger(data[0]);
    wsConnector.disconnect();
  }
  
  public void testWebServiceReadConnectorWithParameters(){
    List parameters = new ArrayList(3) {
      {
	add(new Integer(100));
	add(new Long(100));
	add(new Float(100.0));
      }
    };
    wsConnector.setWsEndpoint(URL_PREFIX + "/IRandomIntegerGeneratorWS"  + "?wsdl");
    wsConnector.setServiceName("sum");
    wsConnector.setParameters(parameters);
    wsConnector.connect();
    new Thread(){
      public void run() {
        wsConnector.invoke();  
      }
    }.start();
    Object[] data = wsConnector.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0] instanceof Double);
    assertEquals(Double.parseDouble(data[0].toString()), 300.0d, 0d);
    wsConnector.disconnect();
  }
  
  public void testValidateNoEndpoint(){
    wsConnector.setServiceName(TEST_SERVICE_NAME);
    List exceptions = new ArrayList();
    wsConnector.validate(exceptions);
    assertTrue(exceptions.size()==1);
    wsConnector.setWsEndpoint(URL_PREFIX + "/IRandomIntegerGeneratorWS");
    wsConnector.validate(exceptions);
    assertTrue(exceptions.size()==2);
    wsConnector.setWsEndpoint(URL_PREFIX + "/IRandomIntegerGeneratorWS" + "?wsdl");
    wsConnector.validate(exceptions);
    assertTrue(exceptions.size()==2);
  }
  
  public void testValidateNoServiceName(){
    wsConnector.setWsEndpoint(URL_PREFIX + "/IRandomIntegerGeneratorWS"  + "?wsdl");
    List exceptions = new ArrayList();
    wsConnector.validate(exceptions);
    assertTrue(exceptions.size()==1);
    wsConnector.setServiceName(TEST_SERVICE_NAME);
    wsConnector.validate(exceptions);
    assertTrue(exceptions.size()==1);
  }
    
}

/**
 * Creates sample web services and exposes them via HTTP.
 */
class ServiceStarter{
    XFireHttpServer server;
    
    public void start() throws Exception{
        XFire xfire = XFireFactory.newInstance().getXFire();
      
        /* Create XFire Services and register them in ServiceRegistry */
        ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
        Service service = serviceFactory.create(Echo.class);
        service.setInvoker(new BeanInvoker(new EchoImpl()));
        xfire.getServiceRegistry().register(service);
        service = serviceFactory.create(IRandomIntegerGeneratorWS.class);
        service.setInvoker(new BeanInvoker(new RandomIntegerGeneratorWS()));
        xfire.getServiceRegistry().register(service);
        
        /* Start the HTTP server */
        server = new XFireHttpServer();
        server.setPort(WebServiceReadConnectorTestCase.PORT_NR);
        server.start();
    }
    
    public void stop() throws Exception{
        server.stop();
    }
}
