/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
/**
 * Utility class which provides a generic cloning mechanism.
 * 
 * @author higginse
 * @since 3.3
 */
public class ObjectCloner {
  private static final Log log =LogFactory.getLog(ObjectCloner.class);
  private static final String CLONE_METHOD_NAME="clone";
  private static final Class[] NULL_CLASS_ARRAY=(Class[])null;
  private static final Object[] NULL_OBJECT_ARRAY=(Object[])null;

  //These are classes we are very likely to encounter!
  private static final Class[] seedClonables=  {OrderedHashMap.class,HashMap.class};

  private Map cache;

  private boolean forgiving=true;
  /**
   * Flag to indicate that failure to clone won't cause an exception.
   * If true (the default) then failure to clone will just result
   * in a reference to the original object being returned.
   * If false, then a RuntimeException will be thrown if an attempt
   * is made, but fails to clone an object.
   * 
   */
  public void setForgiving(boolean forgiving) {
    this.forgiving=forgiving;
  }
  /**
   * Flag to indicate that failure to clone won't cause an exception.
   * If true (the default) then failure to clone will just result
   * in a reference to the original object being returned.
   * If false, then a RuntimeException will be thrown if an attempt
   * is made, but fails to clone an object.
   * 
   */
  public boolean getForgiving() {
    return this.forgiving;
  }

  /**
   * Property which allows user configuration of cloning.
   * The supplied map should contain a map of
   * fully qualified class names paired with the name of
   * a no-arg method which may be used to generate clones
   * of Objects of the named class.
   * <br>
   * Any duplicate existing mapping keys will be overriden.
   * @param cloneMethodMap
   */
  public void setCloneMethodMapping(Map cloneMethodMap) {
    if (cloneMethodMap!=null) {
      Iterator it=cloneMethodMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry entry=(Map.Entry)it.next();
        try {
          String className=entry.getKey().toString();
          String methodName=entry.getValue().toString();
          Class cl=Class.forName(className);
          Method method=cl.getDeclaredMethod(methodName, NULL_CLASS_ARRAY);
          if (cache.containsKey(className)) {
            log.info("Overriding cache value for "+className);
            //cache.remove(className);
          }
          cache.put(className, method);
        }
        catch (Throwable t) {
          log.warn("Failed to register map entry - "+t.getMessage());
        }
      }
    }
  }

  public ObjectCloner() {
    cache=new HashMap();
    initialiseDefaultCache();
  }

  private void initialiseDefaultCache() {
    for (int i=0;i<seedClonables.length;i++) {
      getCloneForClass(seedClonables[i]);
    }
  }

  /**
   * retrieve the appropriate clone method for a given class.
   * <br>
   * It will first consult it's cache of class->method mappings.
   * On a hit, the cached method will be returned.
   * On a miss, it will attempt to get the 'clone()' method for
   * that class.
   * @param theClass
   * @return Method for cloning.
   */ 
  private Method getCloneForClass(Class theClass) {
    //Try and resolve it quickly for a cache hit!
    String className=theClass.getName();
    Method method=(Method)cache.get(className);
    if (method==null) { //Then we have a cache miss. 
      //Shouldn't matter if it's expensive - should only happen once for
      //each new class encountered.
      try {
        log.debug("Caching method "+CLONE_METHOD_NAME+" for class "+className);
        method=theClass.getMethod(CLONE_METHOD_NAME,NULL_CLASS_ARRAY);
        cache.put(className,method);
      }
      catch(NoSuchMethodException nsme) {
        log.warn("Cannot find method "+CLONE_METHOD_NAME+" for class "+className);
        checkFail(nsme);
      }
    }
    else { //Cache hit. Log if debug on.
      if (log.isDebugEnabled()) {
        log.debug("Cache hit ["+className+"] for method  "+CLONE_METHOD_NAME);
      }
    }
    return method;
  }

  /**
   * This will clone the supplied object if possible.
   * <br>
   * If it cannot clone the object it will, by default
   * return a reference to the object without cloning.
   * <br>
   * Set the <code>forgiving</code> property to false to force a
   * RuntimeException instead.
   * @param object
   * @return clone of the original object, or the original object if forgiving
   *         flag is set, and cloning is not possible.
   */
  public  Object clone(Object object) { 
    Object clone=object;
    if (object!=null) {
      Class objectClass=object.getClass();
      if (!(String.class==objectClass)) { //String is a special case - it's immutable, and safe.
        //Should really check if it's cloneable first :-)
        if (!(object instanceof Cloneable)) { //Must be for us
          log.warn("Object does not implement Cloneable - clone() might not work!");
        }
        Method method=getCloneForClass(objectClass);
        if (method!=null) { //Cache hit!
          try {
            clone=method.invoke(object, NULL_OBJECT_ARRAY);
          }
          catch (IllegalArgumentException e) {
            checkFail(e);
          } catch (IllegalAccessException e) {
            checkFail(e);
          } catch (InvocationTargetException e) {
            checkFail(e);
          }
        }
        else {//cache miss!
          checkFail(new Exception("Failed to get appropriate clone method"));
          if (log.isDebugEnabled()) {
            log.debug("Failed to clone(). Returning reference to original object!");
          }
        }
      }
    }
    return clone;
  }

  private void checkFail(Throwable t) {
    if (!forgiving) {
      throw new RuntimeException("Failed to clone Object: "+t.getMessage(),t);
    }
    else {
      if (log.isDebugEnabled()){
        log.debug("Failed to clone Object: "+t.getMessage()+" but forgiving=true");
      }
    }
  }

}
