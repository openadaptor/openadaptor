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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link HttpReadConnector}. 
 * 
 * @author Kris Lachor
 */
public class HttpReadConnectorUnitTestCase extends MockObjectTestCase{
  
  private HttpReadConnector httpReadConnector = new HttpReadConnector();
  
  protected Mock mockHttpMethod;
  
  protected MockHttpClient mockHttpClient = new MockHttpClient();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    httpReadConnector.setId("Test Read Connector");
    httpReadConnector.setUrl("http://foo.bar");
    mockHttpMethod =  new Mock(HttpMethod.class);
    httpReadConnector.setClient(mockHttpClient);
  }

  /**
   * Tests {@link JDBCReadConnector#connect}.
   * With no proxy params set.
   */
  public void testConnect(){
    assertFalse(httpReadConnector.isDry());
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    assertTrue(httpReadConnector.getMethod()==null);
    httpReadConnector.connect();
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    assertTrue(httpReadConnector.getMethod()!=null);
    assertFalse(httpReadConnector.isDry());
  }
  
  /**
   * Tests {@link JDBCReadConnector#connect}.
   * With not all proxy params set.
   */
  public void testConnect2(){
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    assertTrue(httpReadConnector.getMethod()==null);
    httpReadConnector.setProxyHost("testProxyHost");
    /* no proxy port set, should igore proxy */
    httpReadConnector.connect();
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    assertTrue(httpReadConnector.getMethod()!=null);
  }
  
  /**
   * Tests {@link JDBCReadConnector#connect}.
   * With proxy params set.
   */
  public void testConnectWithProxy(){
    assertTrue(mockHttpClient.hostConfigurationCounter==0);
    assertTrue(httpReadConnector.getMethod()==null);
    httpReadConnector.setProxyHost("testProxyHost");
    httpReadConnector.setProxyPort("1111");
    httpReadConnector.connect();
    assertTrue(mockHttpClient.hostConfigurationCounter==1);
    assertTrue(httpReadConnector.getMethod()!=null);
  }
  
  /**
   * Tests {@link HttpReadConnector#validate}.
   */
  public void testValidate(){
    List exceptions = new ArrayList();
    httpReadConnector.validate(exceptions);
    assertTrue(exceptions.isEmpty());
    httpReadConnector.setUrl(null);
    httpReadConnector.validate(exceptions);
    assertTrue(exceptions.size()==1);
  }
  
  /**
   * Tests {@link HttpReadConnector#next}.
   * HttpClient returns with status==OK.
   */
  public void testNextCorrectReturnStatus(){
    httpReadConnector.connect();
    assertFalse(httpReadConnector.isDry());
    assertTrue(mockHttpClient.executeMethodCounter==0);
    mockHttpMethod.expects(once()).method("getResponseBody").will(returnValue(new byte[] {'a','b'}));
    mockHttpMethod.expects(once()).method("releaseConnection");
    httpReadConnector.setMethod((HttpMethod) mockHttpMethod.proxy()); 
    httpReadConnector.next(1000);
    assertTrue(mockHttpClient.executeMethodCounter==1);
    assertTrue(httpReadConnector.isDry());
  }
  
  /**
   * Tests {@link HttpReadConnector#next}.
   * HttpClient returns with status!=OK.
   */
  public void testNextWithWrongReturnStatus(){
    mockHttpClient = new MockHttpClientReturnsError();
    httpReadConnector.setClient(mockHttpClient);
    httpReadConnector.connect();
    assertFalse(httpReadConnector.isDry());
    assertTrue(mockHttpClient.executeMethodCounter==0);
    mockHttpMethod.expects(once()).method("getStatusLine");
    mockHttpMethod.expects(once()).method("releaseConnection");
    httpReadConnector.setMethod((HttpMethod) mockHttpMethod.proxy()); 
    httpReadConnector.next(1000);
    assertTrue(mockHttpClient.executeMethodCounter==1);
    assertTrue(httpReadConnector.isDry());
  }
  
  /**
   * Tests {@link HttpReadConnector#next}.
   * Tests the isDry property after calling to #connect, #next, #disconnect and #connect again.
   */
  public void testConnectNextAndReconnect(){
    testConnect();
    testNextCorrectReturnStatus();
    assertTrue(httpReadConnector.isDry());
    httpReadConnector.disconnect();
    assertTrue(httpReadConnector.isDry());
    httpReadConnector.connect();
    assertFalse(httpReadConnector.isDry());
  }
  
}

/**
 * Mock version of the HttpClient class.
 * executeMethod returns status==OK
 */
class MockHttpClient extends HttpClient{
  protected int hostConfigurationCounter = 0;
  protected int executeMethodCounter = 0;

  public synchronized HostConfiguration getHostConfiguration() {
    hostConfigurationCounter++;
    return super.getHostConfiguration();
  }

  public int executeMethod(HttpMethod arg0) throws IOException, HttpException {
    executeMethodCounter++;
    return HttpStatus.SC_OK;
  }
}

/**
 * Mock version of the HttpClient class.
 * executeMethod returns status!=OK
 */
class MockHttpClientReturnsError extends MockHttpClient{
  public int executeMethod(HttpMethod arg0) throws IOException, HttpException {
    executeMethodCounter++;
    return HttpStatus.SC_OK + 1;
  }
}
