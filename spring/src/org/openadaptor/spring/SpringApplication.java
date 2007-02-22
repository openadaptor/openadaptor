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

package org.openadaptor.spring;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.jmx.Administrable;
import org.openadaptor.util.Application;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.UrlResource;

public class SpringApplication {

  private static Log log = LogFactory.getLog(SpringApplication.class);

  private static String getOptionValue(String[] args,int index){
    if (args.length>index+1){
      return args[index+1];
    }
    else {
      throw new RuntimeException("Option "+args[index]+" requires a value, which has not been supplied");
    }
  }
  /**
   * read cmd line args and run SpringApplication
   * 
   * @param args
   */
  public static void main(String[] args) {
    String configUrl = null;
    String propsUrl = null;
    String beanName = null;
    int jmxPort = 0;

    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-config")) {
          configUrl = getOptionValue(args, i++);//args[++i];
        } else if (args[i].equals("-props")) {
          propsUrl = getOptionValue(args, i++);//args[++i];
        } else if (args[i].equals("-bean")) {
          beanName = getOptionValue(args, i++);//args[++i];
        } else if (args[i].equals("-jmx")) {
          String jmxPortString =getOptionValue(args, i++);
          try {
            jmxPort = Integer.parseInt(jmxPortString);
            if ((jmxPort<=0) || (jmxPort>65535))  {
              throw new RuntimeException("Illegal jmx port specified: "+jmxPort+". Valid range is [1-65535]");
            }
          }
          catch (NumberFormatException nfe) {
            throw new RuntimeException("-jmx option requires a integer port number");
          }
        } else {
          throw new RuntimeException("unrecognised cmd line arg " + args[i]);
        }
      }
      if (configUrl==null) {
        throw new RuntimeException("Cannot start: -config option is required");
      }
      if (beanName==null) {
        throw new RuntimeException("Cannot start: -bean option is required");
      }
    } catch (RuntimeException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      usage(System.err);
      System.exit(1);
    }

    System.setProperty(Application.PROPERTY_CONFIG_URL, configUrl);
    if (propsUrl != null) {
      System.setProperty(Application.PROPERTY_PROPS_URL, propsUrl);
    }
    System.setProperty(Application.PROPERTY_COMPONENT_ID, beanName);

    try {
      runXml(configUrl, propsUrl, beanName, jmxPort);
      System.exit(0);
    } catch (RuntimeException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void usage (PrintStream ps) {
    ps.println("usage: java " + SpringApplication.class.getName() + "\n  -config <url> "
        + "\n  -bean <id> " + "\n  [-props <url>] "
        + "\n  [-jmx <http port>]");  	
  }

  public static ListableBeanFactory getBeanFactory(String configUrl, String propsUrl) {
    return getBeanFactory(configUrl, propsUrl, 0);
  }

  public static ListableBeanFactory getBeanFactory(String configUrl, String propsUrl, int jmxPort) {
    try {
      // filthy hack for the moment, originally we prefixed the config with file: if
      // it did not contain a colon, however we need to allow for fully qualified
      // windows paths with drive mappings
      if (configUrl.indexOf(":") < 2) {
        configUrl = "file:" + configUrl;
      }
      XmlBeanFactory factory = new XmlBeanFactory(new UrlResource(configUrl));
      preProcessFactory(propsUrl, factory);
      setComponentIds(factory);
      configureMBeanServer(factory, jmxPort, configUrl, propsUrl);
      return factory;
    } catch (BeansException e) {
      log.error("error", e);
      throw new RuntimeException("BeansException : " + e.getMessage());
    } catch (MalformedURLException e) {
      log.error("error", e);
      throw new RuntimeException("MalformedUrlException : " + e.getMessage());
    }
  }

  public static void runXml(String configUrl, String propsUrl, String beanId) {
    runXml(configUrl, propsUrl, beanId, 0);
  }

  /**
   * run a spring application, creates Xml bean factory from url post processes factory using optional properties url
   * and system properties gets runnable bean and calls run
   * 
   * @param configUrl
   *          url that points to xml spring config
   * @param propsUrl
   *          url that points to properties file
   * @param beanId
   *          id of bean that implements Runnable
   */
  public static void runXml(String configUrl, String propsUrl, String beanId, int jmxPort) {
    try {
      ListableBeanFactory factory = getBeanFactory(configUrl, propsUrl, jmxPort);
      Runnable runnerBean = (Runnable) factory.getBean(beanId);
      Thread.currentThread().setName(beanId);
      runnerBean.run();
    } catch (BeansException e) {
      log.error("bean exception", e);
      throw new RuntimeException("BeansException : " + e.getMessage());
    }
  }

  private static void preProcessFactory(String propsUrl, XmlBeanFactory factory) throws MalformedURLException {
    PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
    if (propsUrl != null) {
      if (propsUrl.indexOf(":") == -1) {
        propsUrl = "file:" + propsUrl;
      }
      configurer.setLocation(new UrlResource(propsUrl));
    }
    configurer.setProperties(System.getProperties());
    configurer.postProcessBeanFactory(factory);
  }

  private static void setComponentIds(XmlBeanFactory factory) {
    String[] beanNames = factory.getBeanDefinitionNames();
    for (int i = 0; i < beanNames.length; i++) {
      Object bean = factory.getBean(beanNames[i]);
      if (bean instanceof IComponent) {
        IComponent component = (IComponent) bean;
        if (component.getId() == null) {
          component.setId(beanNames[i]);
          log.debug("setting IComponent id for " + beanNames[i]);
        } else {
          log.debug("IComponent id is already set for " + beanNames[i]);
        }
      } else {
        log.debug("bean " + beanNames[i] + " is not an IComponent");
      }
    }
  }

  private static void configureMBeanServer(XmlBeanFactory factory, int jmxPort, String configUrl, String propsUrl) {
    MBeanServer mbeanServer = (MBeanServer) getFirstBeanOfType(factory, MBeanServer.class);
    if (mbeanServer == null && jmxPort != 0) {
      mbeanServer = new org.openadaptor.core.jmx.MBeanServer(jmxPort);
    }
    if (mbeanServer != null) {
      attemptToRegisterBean(new FactoryConfig(configUrl, propsUrl), mbeanServer, "Config");
      String[] beanNames = factory.getBeanDefinitionNames();
      for (int i = 0; i < beanNames.length; i++) {
        Object bean = factory.getBean(beanNames[i]);
        if (bean instanceof Administrable) {
          bean = ((Administrable)bean).getAdmin();
        }
        attemptToRegisterBean(bean, mbeanServer, beanNames[i]);
      }
    }
  }

  private static void attemptToRegisterBean(Object bean, MBeanServer mbeanServer, String beanName) {
    if (!(bean instanceof MBeanServer)) {
      try {
        ObjectName name = new ObjectName("beans:id=" + beanName);
        mbeanServer.registerMBean(bean, name);
        log.info("registered bean " + beanName);
      } catch (NotCompliantMBeanException e) {
        log.debug("bean " + beanName + " is not compliant : " + e.getMessage());
      } catch (Exception e) {
        log.error("failed to register mbean " + beanName, e);
      }
    }
  }

  public static Object getFirstBeanOfType(DefaultListableBeanFactory factory, Class beanClass) {
    Map beanMap = factory.getBeansOfType(MBeanServer.class);
    for (Iterator iter = beanMap.values().iterator(); iter.hasNext();) {
      return iter.next();
    }
    return null;
  }
}
