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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

/**
 * Unit tests for {@link WebServiceCXFReadConnectorTestCase}. Checks compatibility 
 * between  {@link WebServiceCXFReadConnectorTestCase} and  {@link WebServiceReadConnectorTestCase}
 * (its XFire based equivalent).
 * 
 * Essentially runs the same tests as defined in superclass but with the CXF based connector.
 * 
 * @author Kris Lachor
 */
public class WebServiceCXFReadConnectorTestCase extends WebServiceReadConnectorTestCase {
    
  private WebServiceCXFReadConnector wsConnector = new WebServiceCXFReadConnector();
 
  private static final String PREFIX = "http://soap.connector.auxil.openadaptor.org";
  
  public static final QName TEST_SERVICE_NAME = new QName(PREFIX, "getInt");
  
  /**
   * Overrides the test from superclass not to execute twice.
   */
  public void testLocalWebServiceServer() throws Exception{
  }
  
  /**
   * Overrides the test from superclass not to execute twice.
   */
  public void testRandomIntWebService() throws Exception{
  }
  
  /**
   * Overrides the test from superclass not to execute twice.
   */
  public void testRandomIntWebServiceClientThroughReflection() throws Exception{
  }
  
  /**
   * Overrides the test from superclass not to execute twice.
   */
  public void testRandomIntWebServiceClient() throws Exception{
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
    wsConnector.setServiceName(new QName(PREFIX, "sum"));
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

