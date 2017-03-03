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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

/**
 * System tests for {@link WebServiceCXFWriteConnectorTestCase}.
 * Also test compatibility between {@link WebServiceWriteConnector} and {@link WebServiceCXFWriteConnector}.
 */
public class WebServiceCXFWriteConnectorTestCase extends WebServiceWriteConnectorTestCase {

  /**
   * Starts up web service server.
   * Programmatically assembles an adaptor with one read node and WebServiceCXFWriteConnector
   * as the write node.
   * Runs the adaptor, ensures the server received expected data.
   */
  public void test() {
    
    /* run up webservice (under jetty) */
    MyServiceImpl impl = runUpWebService();
    
    /* Create adaptor to invoke webservice */
    TestReadConnector readNode = new TestReadConnector("in");
    readNode.setDataString("foobar");
    WebServiceCXFWriteConnector writeNode = new WebServiceCXFWriteConnector("out");
    writeNode.setEndpoint("http://localhost:8191/MyService?wsdl");
    writeNode.setMethodName("process");
    
    /* Run adaptor */
    Map map = new HashMap();
    map.put(readNode, writeNode);
    RoutingMap routingMap = new RoutingMap();
    routingMap.setProcessMap(map);
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(new Router(routingMap));
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
    
    /* Check state of web service */
    List results = impl.getData();
    assertTrue(results.size() == 1);
    assertTrue(results.get(0).equals("foobar"));
  }

}
