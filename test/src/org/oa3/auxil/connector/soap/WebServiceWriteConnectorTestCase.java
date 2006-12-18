package org.oa3.auxil.connector.soap;

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
import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.connector.TestReadConnector;
import org.oa3.core.router.Router;
import org.oa3.core.router.RoutingMap;

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
