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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.transport.http.XFireServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openadaptor.auxil.connector.soap.WebServiceWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

public class WebServiceWriteConnectorTestCase extends TestCase {

  public void test() {
    
    MyServiceImpl impl = new MyServiceImpl();
    
    // run up webservice (under jetty)
    Server server = new Server(8191);
    Context root = new Context(server, "/",Context.SESSIONS);
    root.addServlet(new ServletHolder(new MyServlet(MyService.class, impl, "MyService")), "/*");
    try {
      server.start();
    } catch (Exception e) {
      fail("failed to start jetty");
    }
    
    // create adaptor to invoke webservice
    TestReadConnector inpoint = new TestReadConnector("in");
    inpoint.setDataString("foobar");
    
    WebServiceWriteConnector outpoint = new WebServiceWriteConnector("out");
    outpoint.setEndpoint("http://localhost:8191/MyService?wsdl");
    outpoint.setMethodName("process");
    
    Map map = new HashMap();
    map.put(inpoint, outpoint);
    RoutingMap routingMap = new RoutingMap();
    routingMap.setProcessMap(map);
    
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(new Router(routingMap));
    
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
    
    // check state of web service
    List results = impl.getData();
    assertTrue(results.size() == 1);
    assertTrue(results.get(0).equals("foobar"));
  }
  
  public interface MyService {
    void process(String s);
  }
  
  public class MyServiceImpl implements MyService {

    private List data = new ArrayList();
    
    public void process(String s) {
      System.err.println("got data " + s);
      data.add(s);
    }
    
    public List getData() {
      return Collections.unmodifiableList(data);
    }
  }
  
  public class MyServlet extends XFireServlet {

    private static final long serialVersionUID = 1L;

    private Class myService;
    private Object myServiceImpl;
    private String serviceName;
    
    MyServlet(final Class myService, final Object myServiceImpl, final String serviceName) {
      this.myService = myService;
      this.myServiceImpl = myServiceImpl;
      this.serviceName = serviceName;
    }
    
    public void init() throws ServletException
    {
      super.init();
      ObjectServiceFactory factory = new ObjectServiceFactory(getXFire().getTransportManager(), null);
      Service service = factory.create(myService, serviceName, null, null);
      service.setInvoker(new BeanInvoker(myServiceImpl));
      getController().getServiceRegistry().register(service);
    }
  }

}
