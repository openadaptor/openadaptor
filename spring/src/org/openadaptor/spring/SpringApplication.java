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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.jmx.Administrable;
import org.openadaptor.util.Application;
import org.openadaptor.util.ResourceUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;

/**
 * Helper class for launching openadaptor application based on spring.
 * @author perryj
 *
 */
public class SpringApplication {

  private static Log log = LogFactory.getLog(SpringApplication.class);

  private ArrayList configUrls = new ArrayList();

  private String beanId;

  private int jmxPort;

  public static void main(String[] args) {
    try {
      SpringApplication app = new SpringApplication();
      app.parseArgs(args);
      app.run();
      System.exit(0);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      usage(System.err);
      System.exit(1);
    }
  }

  protected String getBeanId() {
    return beanId;
  }

  public void setBeanId(final String beanId) {
    this.beanId = beanId;
  }

  protected ArrayList getConfigUrls() {
    return configUrls;
  }

  public void setConfigUrls(final List configUrls) {
    this.configUrls.clear();
    this.configUrls.addAll(configUrls);
  }

  public void addConfigUrl(String configUrl) {
    this.configUrls.add(configUrl);
  }

  protected int getJmxPort() {
    return jmxPort;
  }

  public void setJmxPort(final int jmxPort) {
    this.jmxPort = jmxPort;
  }

  protected void parseArgs(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-config")) {
        configUrls.add(getOptionValue(args, i++));
      } else if (args[i].equals("-bean")) {
        beanId = getOptionValue(args, i++);
      } else if (args[i].equals("-jmxport")) {
        String jmxPortString = getOptionValue(args, i++);
        try {
          jmxPort = Integer.parseInt(jmxPortString);
          if ((jmxPort <= 0) || (jmxPort > 65535)) {
            throw new RuntimeException("Illegal jmx port specified: " + jmxPort + ". Valid range is [1-65535]");
          }
        } catch (NumberFormatException nfe) {
          throw new RuntimeException("-jmx option requires a integer port number");
        }
      } else {
        throw new RuntimeException("unrecognised cmd line arg " + args[i]);
      }
    }
  }

  protected String getConfigUrlsString() {
    StringBuffer buffer = new StringBuffer();
    for (Iterator iter = configUrls.iterator(); iter.hasNext();) {
      buffer.append(buffer.length() > 0 ? "," : "").append(iter.next());
    }
    return buffer.toString();
  }
  
  public void run() {
    Runnable bean = getRunnableBean(createBeanFactory());
    if (bean instanceof Application) {
      ((Application)bean).setConfigData(getConfigUrlsString());
    }
    Thread.currentThread().setName(beanId);
    bean.run();
  }

  protected Runnable getRunnableBean(ListableBeanFactory factory) {
    String beanId = getBeanId();
    if (beanId == null) {
      throw new RuntimeException("No bean specified");
    }
    return (Runnable) factory.getBean(beanId);
  }
  
  protected static void usage(PrintStream ps) {
    ps.println("usage: java " + SpringApplication.class.getName() 
        + "\n  -config <url> [ -config <url> ]" 
        + "\n  -bean <id> "
        + "\n  [-jmxport <http port>]"
        + "\n\n"
        + " e.g. java " + SpringApplication.class.getName() + " -config file:test.xml -bean Application");
  }

  private ListableBeanFactory createBeanFactory() {
    if (configUrls.isEmpty()) {
      throw new RuntimeException("no config urls specified");
    }
    GenericApplicationContext context = new GenericApplicationContext();
    loadBeanDefinitions("classpath:" + ResourceUtil.getResourcePath(this, "", ".openadaptor-spring.xml"), context);
    for (Iterator iter = configUrls.iterator(); iter.hasNext();) {
      String configUrl = (String) iter.next();
      loadBeanDefinitions(configUrl, context);
    }
    context.refresh();
    setComponentIds(context);
    configureMBeanServer(context);
    return context;
  }

  protected void loadBeanDefinitions(String url, GenericApplicationContext context) {
    String protocol = "";
    if (url.indexOf(':') != -1) {
      protocol = url.substring(0, url.indexOf(':'));
    }
    
    if (protocol.equals("file") || protocol.equals("http")) {
      loadBeanDefinitionsFromUrl(url, context);
    } else if (protocol.equals("classpath")) {
      loadBeanDefinitionsFromClasspath(url, context);
    } else {
      loadBeanDefinitions("file:" + url, context);
    }
  }

  private void loadBeanDefinitionsFromClasspath(String url, GenericApplicationContext context) {
    String resourceName = url.substring(url.indexOf(':') + 1);
    BeanDefinitionReader reader = null;
    if (url.endsWith(".xml")) {
      reader = new XmlBeanDefinitionReader(context);
    } else if (url.endsWith(".properties")) {
      reader = new PropertiesBeanDefinitionReader(context);
    }
    
    if (reader != null) {
      reader.loadBeanDefinitions(new ClassPathResource(resourceName));
    } else {
      throw new RuntimeException("No BeanDefinitionReader associated with " + url);
    }
  }

  private void loadBeanDefinitionsFromUrl(String url, GenericApplicationContext context) {
    BeanDefinitionReader reader = null;
    if (url.endsWith(".xml")) {
      reader = new XmlBeanDefinitionReader(context);
    } else if (url.endsWith(".properties")) {
      reader = new PropertiesBeanDefinitionReader(context);
    }
    
    if (reader != null) {
      try {
        reader.loadBeanDefinitions(new UrlResource(url));
      } catch (BeansException e) {
        log.error("error", e);
        throw new RuntimeException("BeansException : " + e.getMessage());
      } catch (MalformedURLException e) {
        log.error("error", e);
        throw new RuntimeException("MalformedUrlException : " + e.getMessage());
      }
    } else {
      throw new RuntimeException("No BeanDefinitionReader associated with " + url);
    }
  }

  private static void setComponentIds(ListableBeanFactory factory) {
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

  private void configureMBeanServer(ListableBeanFactory factory) {
    MBeanServer mbeanServer = (MBeanServer) getFirstBeanOfType(factory, MBeanServer.class);
    if (mbeanServer == null && jmxPort != 0) {
      mbeanServer = new org.openadaptor.core.jmx.MBeanServer(jmxPort);
    }
    if (mbeanServer != null) {
      attemptToRegisterBean(new FactoryConfig(configUrls), mbeanServer, "Config");
      String[] beanNames = factory.getBeanDefinitionNames();
      for (int i = 0; i < beanNames.length; i++) {
        Object bean = factory.getBean(beanNames[i]);
        if (bean instanceof Administrable) {
          bean = ((Administrable) bean).getAdmin();
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

  private static Object getFirstBeanOfType(ListableBeanFactory factory, Class beanClass) {
    Map beanMap = factory.getBeansOfType(MBeanServer.class);
    for (Iterator iter = beanMap.values().iterator(); iter.hasNext();) {
      return iter.next();
    }
    return null;
  }

  private static String getOptionValue(String[] args, int index) {
    if (args.length > index + 1) {
      return args[index + 1];
    } else {
      throw new RuntimeException("Option " + args[index] + " requires a value, which has not been supplied");
    }
  }
}
