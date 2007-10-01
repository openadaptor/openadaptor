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

import org.apache.commons.httpclient.HttpMethod;
//import org.apache.commons.httpclient.NameValuePair;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link HttpWriteConnector}. 
 * 
 * The {@link HttpWriteConnector#validate(java.util.List)} method is unit tested 
 * in {@link HttpReadConnectorUnitTestCase#testValidate()} as validation for both connectors is identical.
 * 
 * @author Kris Lachor
 */
public class HttpWriteConnectorUnitTestCase extends MockObjectTestCase{
  
  private HttpWriteConnector httpWriteConnector = new HttpWriteConnector();
  
  protected Mock mockHttpMethod;
  
  protected MockHttpClient mockHttpClient = new MockHttpClient();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    httpWriteConnector.setId("Test Write Connector");
    httpWriteConnector.setUrl("http://foo.bar");
    mockHttpMethod =  new Mock(HttpMethod.class);
    httpWriteConnector.setClient(mockHttpClient);
  }

  /**
   * Tests {@link HttpWriteConnector#connect}.
   * With no proxy params set.
   */
  public void testConnect(){
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    httpWriteConnector.connect();
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
  }
  
  /**
   * Tests {@link HttpWriteConnector#connect}.
   * With one proxy param set, one missing.
   */
  public void testConnect2(){
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    httpWriteConnector.setProxyHost("testProxyHost");
    /* no proxy port set, should igore proxy */
    httpWriteConnector.connect();
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
  }
  
  /**
   * Tests {@link HttpWriteConnector#connect}.
   * With proxy params set.
   */
  public void testConnectWithProxy(){
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    httpWriteConnector.setProxyHost("testProxyHost");
    httpWriteConnector.setProxyPort("1111");
    httpWriteConnector.connect();
    assertTrue(mockHttpClient.hostConfigurationCounter==1);
  }
  
  /**
   * Tests {@link HttpWriteConnector#deliver(Object[])}.
   */
  public void testDeliverWithCorrectReturnStatus(){
    httpWriteConnector.connect();
    assertTrue(mockHttpClient.executeMethodCounter==0);
// can't easily test here as PostMethod is a concrete class...
//    NameValuePair [] nameValuePairs = { new NameValuePair(HttpWriteConnector.RESPONSE_KEY, "test")};
//    mockHttpMethod.expects(once()).method("setRequestBody").with(eq(nameValuePairs));
//    httpWriteConnector.setPostMethod((HttpMethod) mockHttpMethod.proxy()); 
    httpWriteConnector.deliver(new String[]{"test"});
    assertTrue(mockHttpClient.executeMethodCounter==1);
  }
     
}