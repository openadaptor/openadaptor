/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.jmx;

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.util.JVMNeutralMBeanServerFactory;

import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * Implementation of MBeanServer that delegates to sun reference impl
 * @author perryj
 * @author higginse
 *
 */
public class MBeanServer implements javax.management.MBeanServer {
  
  public static final String OBJECT_NAME_STRING="jmx:id=http";
  
  private static Log log = LogFactory.getLog(MBeanServer.class);
  
  private javax.management.MBeanServer mServer;
  
  private HtmlAdaptorServer html=null;
  
  /**
   * Constructor.
   * Uses the jvm-neutral 'Factory' to get at the real mbean server.
   * For 1.5+ it should yield the same as: 
   * mServer = javax.management.MBeanServerFactory.createMBeanServer();
   */
  public MBeanServer() {
    log.info("Getting MBeanServer (note: Http server will not start unless property 'port' is configured)");
    mServer=JVMNeutralMBeanServerFactory.getMBeanServer();
  }
	
  /**
   * Constructor. Has a side effect of starting HTTP server.
   * 
   * @param httpPort the HTTP port.
   */
  public MBeanServer(int httpPort) {
    this();
    setPort(httpPort);
  }
    
  /**
   * set the httpPort for this MBeanServer.
   * <br>
   * This isn't perfect. Strictly, setting a bean property shouldn't
   * have unexpected side effects; this will, however, start the
   * http server on the supplied port.
   * 
   * @param httpPort
   */
  public void setPort(int httpPort) {
    if (html!=null) { //It's already configured.
      int port=html.getPort();
      String msg="HtmlAdaptorServer is already configured for http port "+port;
      if (httpPort!=port) { //Trying to change port - not allowd if already running.
        throw new RuntimeException(msg+"; cannot change http port");
      }
      else {
        log.warn(msg+"; port will not change");
      }
    }
    else { 
      if (httpPort>=0) { //Need to create one and start it
        html=new HtmlAdaptorServer(httpPort);
        startHtmlServerAdaptor(html);
      }
    }
  }    
  
  private void startHtmlServerAdaptor(HtmlAdaptorServer html) {
    try {
      ObjectName name=new ObjectName(OBJECT_NAME_STRING);
      log.info("Registering MBean for htmlAdaptorServer with name "+name.getCanonicalName());
      mServer.registerMBean(html, name);
      log.info("starting jmx http adaptor on port " + html.getPort());
      html.start();
    } catch(Exception e) {
      log.error("failed to start HtmlAdaptorServer", e);
    }   
  }

  public void addNotificationListener(ObjectName arg0,
  		NotificationListener arg1, NotificationFilter arg2, Object arg3)
  		throws InstanceNotFoundException {
  	mServer.addNotificationListener(arg0, arg1, arg2, arg3);
  }
  
  public void addNotificationListener(ObjectName arg0, ObjectName arg1,
  		NotificationFilter arg2, Object arg3) throws InstanceNotFoundException {
  	mServer.addNotificationListener(arg0, arg1, arg2, arg3);
  }
  
  public ObjectInstance createMBean(String arg0, ObjectName arg1)
  		throws ReflectionException, InstanceAlreadyExistsException,
  		MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
  	return mServer.createMBean(arg0, arg1);
  }
  
  public ObjectInstance createMBean(String arg0, ObjectName arg1,
  		ObjectName arg2) throws ReflectionException,
  		InstanceAlreadyExistsException, MBeanRegistrationException,
  		MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
  	return mServer.createMBean(arg0, arg1, arg2);
  }
  
  public ObjectInstance createMBean(String arg0, ObjectName arg1,
  		Object[] arg2, String[] arg3) throws ReflectionException,
  		InstanceAlreadyExistsException, MBeanRegistrationException,
  		MBeanException, NotCompliantMBeanException {
  	return mServer.createMBean(arg0, arg1, arg2, arg3);
  }
  
  public ObjectInstance createMBean(String arg0, ObjectName arg1,
  		ObjectName arg2, Object[] arg3, String[] arg4)
  		throws ReflectionException, InstanceAlreadyExistsException,
  		MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
  		InstanceNotFoundException {
  	return mServer.createMBean(arg0, arg1, arg2, arg3, arg4);
  }

  /**
   * @deprecated 
   */
  public ObjectInputStream deserialize(ObjectName arg0, byte[] arg1)
  		throws InstanceNotFoundException, OperationsException {
  	throw new RuntimeException("deprecated");
  }
  
  /**
   * @deprecated 
   */
  public ObjectInputStream deserialize(String arg0, byte[] arg1)
  		throws OperationsException, ReflectionException {
  	throw new RuntimeException("deprecated");
  }
  
  /**
   * @deprecated 
   */
  public ObjectInputStream deserialize(String arg0, ObjectName arg1, byte[] arg2)
  		throws InstanceNotFoundException, OperationsException,
  		ReflectionException {
  	throw new RuntimeException("deprecated");
  }
  
  public Object getAttribute(ObjectName arg0, String arg1)
  		throws MBeanException, AttributeNotFoundException,
  		InstanceNotFoundException, ReflectionException {
  	return mServer.getAttribute(arg0, arg1);
  }
  
  public AttributeList getAttributes(ObjectName arg0, String[] arg1)
  		throws InstanceNotFoundException, ReflectionException {
  	return mServer.getAttributes(arg0, arg1);
  }
  
  public ClassLoader getClassLoader(ObjectName arg0)
  		throws InstanceNotFoundException {
  	return mServer.getClassLoader(arg0);
  }
  
  public ClassLoader getClassLoaderFor(ObjectName arg0)
  		throws InstanceNotFoundException {
  	return mServer.getClassLoaderFor(arg0);
  }
  
  public ClassLoaderRepository getClassLoaderRepository() {
  	return mServer.getClassLoaderRepository();
  }
  
  public String getDefaultDomain() {
  	return mServer.getDefaultDomain();
  }
  
  public String[] getDomains() {
  	return mServer.getDomains();
  }
  
  public Integer getMBeanCount() {
  	return mServer.getMBeanCount();
  }
  
  public MBeanInfo getMBeanInfo(ObjectName arg0)
  		throws InstanceNotFoundException, IntrospectionException,
  		ReflectionException {
  	return mServer.getMBeanInfo(arg0);
  }
  
  public ObjectInstance getObjectInstance(ObjectName arg0)
  		throws InstanceNotFoundException {
  	return mServer.getObjectInstance(arg0);
  }
  
  public Object instantiate(String arg0) throws ReflectionException,
  		MBeanException {
  	return mServer.instantiate(arg0);
  }
  
  public Object instantiate(String arg0, ObjectName arg1)
  		throws ReflectionException, MBeanException, InstanceNotFoundException {
  	return mServer.instantiate(arg0, arg1);
  }
  
  public Object instantiate(String arg0, Object[] arg1, String[] arg2)
  		throws ReflectionException, MBeanException {
  	return mServer.instantiate(arg0, arg1, arg2);
  }
  
  public Object instantiate(String arg0, ObjectName arg1, Object[] arg2,
  		String[] arg3) throws ReflectionException, MBeanException,
  		InstanceNotFoundException {
  	return mServer.instantiate(arg0, arg1, arg2, arg3);
  }
  
  public Object invoke(ObjectName arg0, String arg1, Object[] arg2,
  		String[] arg3) throws InstanceNotFoundException, MBeanException,
  		ReflectionException {
  	return mServer.invoke(arg0, arg1, arg2, arg3);
  }
  
  public boolean isInstanceOf(ObjectName arg0, String arg1)
  		throws InstanceNotFoundException {
  	return mServer.isInstanceOf(arg0, arg1);
  }
  
  public boolean isRegistered(ObjectName arg0) {
  	return mServer.isRegistered(arg0);
  }
  
  public Set queryMBeans(ObjectName arg0, QueryExp arg1) {
  	return mServer.queryMBeans(arg0, arg1);
  }
  
  public Set queryNames(ObjectName arg0, QueryExp arg1) {
  	return mServer.queryNames(arg0, arg1);
  }
  
  public ObjectInstance registerMBean(Object arg0, ObjectName arg1)
  		throws InstanceAlreadyExistsException, MBeanRegistrationException,
  		NotCompliantMBeanException {
  	return mServer.registerMBean(arg0, arg1);
  }
  
  public void removeNotificationListener(ObjectName arg0, ObjectName arg1)
  		throws InstanceNotFoundException, ListenerNotFoundException {
  	mServer.removeNotificationListener(arg0, arg1);
  }
  
  public void removeNotificationListener(ObjectName arg0,
  		NotificationListener arg1) throws InstanceNotFoundException,
  		ListenerNotFoundException {
  	mServer.removeNotificationListener(arg0, arg1);
  }
  
  public void removeNotificationListener(ObjectName arg0, ObjectName arg1,
  		NotificationFilter arg2, Object arg3) throws InstanceNotFoundException,
  		ListenerNotFoundException {
  	mServer.removeNotificationListener(arg0, arg1, arg2, arg3);
  }
  
  public void removeNotificationListener(ObjectName arg0,
  		NotificationListener arg1, NotificationFilter arg2, Object arg3)
  		throws InstanceNotFoundException, ListenerNotFoundException {
  	mServer.removeNotificationListener(arg0, arg1, arg2, arg3);
  }
  
  public void setAttribute(ObjectName arg0, Attribute arg1)
  		throws InstanceNotFoundException, AttributeNotFoundException,
  		InvalidAttributeValueException, MBeanException, ReflectionException {
  	mServer.setAttribute(arg0, arg1);
  }
  
  public AttributeList setAttributes(ObjectName arg0, AttributeList arg1)
  		throws InstanceNotFoundException, ReflectionException {
  	return mServer.setAttributes(arg0, arg1);
  }
  
  public void unregisterMBean(ObjectName arg0)
  		throws InstanceNotFoundException, MBeanRegistrationException {
  	mServer.unregisterMBean(arg0);
  }

}
