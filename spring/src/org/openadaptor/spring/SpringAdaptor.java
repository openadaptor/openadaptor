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

package org.openadaptor.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Helper class for launching OpenAdaptor processes based on Spring 
 * configuration.
 * <p>
 * This class will automatically register {@link Adaptor} and {@link Router} 
 * beans where none are explicitly defined in the Spring configuration, 
 * allowing adaptors to be defined implicitly by simply declaring a
 * number of beans that implement {@link IComponent} in the desired order
 * of execution.  
 */
public class SpringAdaptor extends SpringApplication {
  private static Log log = LogFactory.getLog(SpringAdaptor.class);

  public static void main(String[] args) {
    SpringAdaptor springAdaptor = new SpringAdaptor();
    int exitCode = springAdaptor.execute(args);
    log.debug("Exiting with code "+exitCode);
    System.exit(exitCode);
  }
  
  public int execute(String [] args){
    int exitCode=0;
    try {  
      parseArgs(args);
      exitCode=run();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      usage(System.err);
      exitCode=1;
    } 
    return exitCode;
  }
  
  /**
   * Returns the {@link Adaptor} bean from the Spring context.
   * 
   * @see org.openadaptor.spring.SpringApplication#getRunnableBean(org.springframework.beans.factory.ListableBeanFactory)
   */
  protected Runnable getRunnableBean(ListableBeanFactory factory) {
    if (getBeanId() == null) {
      String[] ids = factory.getBeanNamesForType(Adaptor.class);
      if (ids.length == 1) {
        setBeanId(ids[0]);
      } else if (ids.length == 0){
        registerAdaptor(factory);
      } else if (ids.length > 1) {
        throw new RuntimeException("Mulitple Adaptor beans found in config");
      }
    }
    return (Adaptor) factory.getBean(getBeanId());
  }
  
  /**
   * Registers an instance of an {@link Adaptor} with the Spring context.
   * <p>
   * Common OpenAdaptor configurations include an {@link Adaptor} with a 
   * {@link Router} which defines an ordered list of {@link IComponent}s. If 
   * no adaptor bean is explicitly registered, this method will auto-configure 
   * and register one.
   * <p>
   * If a {@link Router} is explicitly registered in the Spring context, that
   * will be autowired as the {@link Adaptor}'s <code>messageProcessor</code>.
   * Otherwise, a {@link Router} will be automatically configured and registered.
   * 
   * @param factory the ListableBeanFactory Spring context factory
   */
  protected void registerAdaptor(ListableBeanFactory factory) {
    String[] ids = factory.getBeanNamesForType(Router.class);
    Router router = null;
    
    if (ids.length == 1) {
      router = (Router) factory.getBean(ids[0]);
    } else if (ids.length == 0) {
      router = registerRouter(factory);
    }
    
    MutablePropertyValues properties = new MutablePropertyValues();
    properties.addPropertyValue("messageProcessor", router);
    RootBeanDefinition beanDefinition = new RootBeanDefinition(Adaptor.class);
    beanDefinition.setPropertyValues(properties);
    ((GenericApplicationContext) factory).registerBeanDefinition("Adaptor", beanDefinition);
    setBeanId("Adaptor");
  }
  
  /**
   * Registers an instance of a {@link Router} with the Spring context. 
   * <p>
   * This method adds all beans registered in the Spring context that are
   * instances of {@link IComponent} to the router's <code>processors</code> 
   * list in <strong>the order in which they are declared in the Spring 
   * configuration</strong>.
   * <p>
   * The only exception is any bean with an id of <code>ExceptionProcessor</code>,
   * which is set as the <code>exceptionProcessor</code> property of the
   * {@link Router}.
   * <p>
   * Note that this means only basic, single pipeline router configurations
   * may be auto-configured in this way. Complex configurations that require
   * a <code>processMap</code> must be defined explicitly. 
   * 
   * @param factory the ListableBeanFactory Spring context factory
   * @return a configured Router
   */
  protected Router registerRouter(ListableBeanFactory factory) {
    String[] ids = factory.getBeanNamesForType(IComponent.class);
    
    if (ids.length == 0) {
      throw new RuntimeException("No Component beans found in config");
    }
    
    List processors = new ArrayList(ids.length);
    Object exceptionProcessor = null;
    
    for (int i = 0; i < ids.length; i++) {
      if (!ids[i].equals("ExceptionProcessor")) {
	processors.add(factory.getBean(ids[i]));
      } else {
	exceptionProcessor = factory.getBean(ids[i]);
      }
    }
    
    MutablePropertyValues properties = new MutablePropertyValues();
    properties.addPropertyValue("processors", processors);
    
    if (exceptionProcessor != null) {
      properties.addPropertyValue("exceptionProcessor", exceptionProcessor);
    }
    
    RootBeanDefinition beanDefinition = new RootBeanDefinition(Router.class);
    beanDefinition.setPropertyValues(properties);
    ((GenericApplicationContext) factory).registerBeanDefinition("Router", beanDefinition);
    
    return (Router) factory.getBean("Router");
  }
}
