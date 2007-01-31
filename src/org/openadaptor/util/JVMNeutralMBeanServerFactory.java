/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to shield fact that 1.4 cannot reference java.lang.management classes.
 * Instead of directly calling ManagementFactory.getPlatformMBeanServer(), this class
 * uses reflections to access the ManagementFactory and similarly the method. This way
 * it will still compile under jdk 1.4 (as long at the appropriate jmx jars are available)
 * Otherwise the reference would break the 1.4 compilation process.
 * Crude, but it works.
 */
public class JVMNeutralMBeanServerFactory {
  private static Log log = LogFactory.getLog(JVMNeutralMBeanServerFactory.class);
  
  public static int DEFAULT_RMI_PORT=51410;
  public static final String FACTORY_1_5="java.lang.management.ManagementFactory";
  public static final String METHOD_1_5="getPlatformMBeanServer";
  public static final String FACTORY_1_4="javax.management.MBeanServerFactory";
  public static final String METHOD_1_4="createMBeanServer";

  private static MBeanServer server;  //Singleton instance of the server.
  private static JMXConnectorServer jmxConnectorServer_1_4;
  private static Registry rmiRegistry;

  /**
   * Use reflection to get an MBeanServer. Avoids compile issue where 1.4 jdk doesn't
   * have java.lang.management.ManagementFactory
   *
   * <pre>
   *
   * Note: For 1.4, an RMI registry may have to be manually started.
   * This may be achieved by something similar to ...
   * RJMX_LIB=<i>oa3_lib</i>
   * JMX_LIB=<i>oa3_lib</i>
   * CP=${RJMXLIB}/jmxremote.jar:${JMXLIB}/jmxri.jar
   * export CLASSPATH=.:$CP ; rmiregistry 51410 &
   *
   * Does <b>not</b> apply to 1.5+
   * </pre>
   * @return MBeanServer instance.
   */
  public static MBeanServer getMBeanServer() {
    String jvmVersion=System.getProperties().getProperty("java.version");
    log.info("Getting MBeanServer [for jvm "+jvmVersion+"]");
    boolean isjvm1_4=jvmVersion.startsWith("1.4.");
    String factory=isjvm1_4?FACTORY_1_4:FACTORY_1_5;
    String method=isjvm1_4?METHOD_1_4:METHOD_1_5;
    if (server==null) {
      server=getMBeanServer(factory,method);
      if (isjvm1_4) {
        //Todo: Add some kind of access mechanism to change the port!
        int port=DEFAULT_RMI_PORT;

        String serviceURLString="service:jmx:rmi:///jndi/rmi://localhost:"+port+"/server";
        try {
          log.info("starting rmi registry on "+  port);
          rmiRegistry = LocateRegistry.createRegistry(port);
          Runtime.getRuntime().addShutdownHook(new Thread () {
            public void run() {
            }
          });
        } catch (RemoteException e) {
          log.warn(e);
        }
        JMXServiceURL url;
        try {
          url = new JMXServiceURL(serviceURLString);
          //url = new JMXServiceURL("jmxmp", null, 5555);
          jmxConnectorServer_1_4 = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);

          // Start the RMI connector server
          log.info("Starting the RMI connector server");
          jmxConnectorServer_1_4.start();
          //Add a shutdownHook to make sure it stops also.
          addJMXShutdownHook();
          //ToDo: Remember to shut this baby down also!
          log.info("RMI connector server successfully started.");
          log.info("JMX clients may use the serviceURL: "+serviceURLString);

          //Start a Html Connection server - disabled for now.
          //It's just being explored.
          //startHtmlConnectorServer();
        }
        catch (Exception e) {
          log.warn("Failed to get RMI connector server started - "+e);
          e.printStackTrace();
        }
      }
    }
    return server;
  }

  private static MBeanServer getMBeanServer(String className,String methodName) {
    MBeanServer server=null;
    try {
      Class factory=Class.forName(className);
      Method method=factory.getMethod(methodName,null);
      server = (MBeanServer)method.invoke(factory,null);
    }
    catch (Exception e) {
      log.warn("Failed to invoke "+className+"."+methodName+"(). Reason - "+e);
    }
    return server;
  }
  /**
   * Stops the JMXConnector server, if it's not null.
   * Only applies to 1.4 instances.
   */
  public static void shutdown() {
    if (jmxConnectorServer_1_4!=null) {
      try {
        log.info("stopping JMXConnectionServer (jvm 1.4)");
        jmxConnectorServer_1_4.stop();
        if (rmiRegistry != null) {
          log.info("stopping rmi registry");
          UnicastRemoteObject.unexportObject(rmiRegistry, true);
        }
      }
      catch (IOException ioe) {
        log.warn("Failed to cleanly stop JMXConnectorServer (jvm 1.4)");
      }
    }
  }

  private static void addJMXShutdownHook() {
    log.debug("Adding shutdown hook for JMXConnectorServer.");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {shutdown();}}
    );
  }
}

