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

package org.openadaptor.util;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Utility class to provide access to Spring AOP proxy class.
 * <br>
 * Note that it uses reflection to avoid any dependency on 
 * the Spring libraries, but still permit core classes to make
 * use of it if desired.
 * <br>
 * If the Aopcontext.currentProxy() method is available, and if
 * exposeProxy property has been enabled in the proxy creator., and
 * can be executed, then calls to getProxy(Object) will return
 * the current proxy, if any.
 * If it fails, it will just return the supplied object.
 * <br>
 * For more detail, constult the Spring documentation.
 * @author higginse
 * @since 3.3
 */
public class ProxyChecker {
  private static final Log log =LogFactory.getLog(ProxyChecker.class);
  private static final String AOP_CONTEXT_CLASS_NAME="org.springframework.aop.framework.AopContext";
  private static final String CURRENT_PROXY_METHOD_NAME="currentProxy";
  private static final Class[] NULL_CLASS_ARRAY=(Class[])null;
  private static final Object[] NULL_OBJECT_ARRAY=(Object[])null;

  private static Class aopContextClass;
  private static Method currentProxyMethod;

  public synchronized static Object getProxy(Object orig) {
    Object proxyObject=orig; //Default is no-proxy
    try {
      if (aopContextClass==null) { //get the method from it first.
        aopContextClass = Class.forName(AOP_CONTEXT_CLASS_NAME);
        currentProxyMethod=aopContextClass.getMethod(CURRENT_PROXY_METHOD_NAME, NULL_CLASS_ARRAY);
      }
      proxyObject=currentProxyMethod.invoke(null, NULL_OBJECT_ARRAY);
    } 
    catch (Throwable t) {
      log.debug("Proxy not available for "+orig+" (Reason: "+t.getMessage()+")");
    }
    return proxyObject;
  }
}
