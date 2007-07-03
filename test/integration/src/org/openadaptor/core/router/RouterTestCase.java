/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */

package org.openadaptor.core.router;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.ResourceUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.UrlResource;


/**
 * Integration tests for {@link Router}.
 * 
 * @author Kris Lachor
 */
public class RouterTestCase extends TestCase {

  protected static final String RESOURCE_LOCATION = "test/integration/src/";

  ListableBeanFactory factory;

  public RouterTestCase() throws BeansException, MalformedURLException {
    factory = new XmlBeanFactory(new UrlResource("file:" + 
        ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "router.xml")));
  }
  
  private Router getRouter(String testName) {
    return (Router) factory.getBean(testName);
  }
	
  private Object getNode(String beanName) {
      return factory.getBean(beanName);
  }

  /**
   * Checks that exceptionHandler is not autoboxed twice - first time when 
   * it's saved to RoutingMap.processMap and second time in Router#setExceptionProcessor
   * (this problem used to make creation of composite exceptionProcessors impossible).
   */
  public void testExceptionHandlerAutoboxing(){
    Router router = getRouter("router");
    assertNotNull(router);
    RoutingMap routingMap = router.getRoutingMap();
    Object o = getNode("exceptionHandler");
    Object boxedExceptionHandler = routingMap.getIfAlreadyAutoboxed(o);
    assertNotNull(boxedExceptionHandler);
    Map exceptionMap = routingMap.getExceptionMap();
    assertNotNull(exceptionMap);
    assertTrue(exceptionMap.size()==1);
    Object exceptionToProcessorsMapObj = exceptionMap.get("*");
    assertNotNull(exceptionToProcessorsMapObj);
    RoutingMap.OrderedExceptionToProcessorsMap exceptionToProcessorsMap = 
      (RoutingMap.OrderedExceptionToProcessorsMap) exceptionToProcessorsMapObj;
    List exceptionHandlers = (List) exceptionToProcessorsMap.get(Exception.class);
    assertNotNull(exceptionHandlers);
    assertTrue(exceptionHandlers.size()==1);
    Object exceptionHandler = exceptionHandlers.get(0);
    assertNotNull(exceptionHandler); 
    assertTrue(exceptionHandler.equals(boxedExceptionHandler));
    List destinations = routingMap.getProcessDestinations((IMessageProcessor) boxedExceptionHandler);
    assertNotNull(destinations);
    assertTrue(destinations.size() == 1);
  }
  
  
  /*Dummy implementations of read and write connectors and data processor */
  public static final class DummyDataProcessor implements IDataProcessor {
    public Object[] process(Object data) { return null; }
    public void reset(Object context) {}
    public void validate(List exceptions) {}
  }
  
  public static final class DummyReadConnector implements IReadConnector {
    public void connect() {}
    public void disconnect() {}
    public Object getReaderContext() {return null;}
    public boolean isDry() { return false;}
    public Object[] next(long timeoutMs) { return null; }
    public void validate(List exceptions) {}
  }
    
  public static final class DummyWriteConnector implements IWriteConnector {
    public Object deliver(Object[] data) {return null;}
    public void connect() {}
    public void disconnect() {}
    public void validate(List exceptions) {}
  }

}
