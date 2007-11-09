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

package org.openadaptor.legacy.convertor.dataobjects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.StubException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DataObject;

/**
 * Utilities for conversion of legacy types.
 * <BR>
 * These include DataObject, DataObject[] and DOXML.
 * 
 * @author higginse
 * 
 */
public class LegacyUtils  {
  //This should match SpringConfigValidateTask.IGNORE_STUB_EXCEPTION_FLAG
  //It doesn't reference it as it would create a dependency on the build tools.
  public static final String IGNORE_STUB_EXCEPTION_FLAG="openadaptor.exception.stub.ignore";
  private static final Log log = LogFactory.getLog(LegacyUtils.class);

  private static final String LEGACY_SET_ATTRIBUTE_METHOD_NAME="setAttributeValue";
  private static final Class[] LEGACY_SET_ATTRIBUTE_METHOD_ARGS= {String.class,String.class};

  //Legacy DateHolder Class, obtained by reflection to avoid compile-time dependency.
  private static final Class DATE_HOLDER_CLASS=getLegacyClass("org.openadaptor.util.DateHolder");
  //Legacy DateHolder asDate() method, by reflecation. As above.
  private static final Method DATE_HOLDER_METHOD=getNonStubMethod(DATE_HOLDER_CLASS,"asDate",(Class[])null);


  /**
   * Checks if dateHolder is available.
   * <br>
   * More specifically, it checks that the 
   * asDate() Method is available.
   * @return
   */
  public static boolean dateHolderAvailable() {
    return (null !=DATE_HOLDER_METHOD);
  }

  public static boolean ignoreStubExceptions() {
    boolean ignore=false;
    String flag=System.getProperty(IGNORE_STUB_EXCEPTION_FLAG);
    if (null != flag) {
      ignore=Boolean.valueOf(flag).booleanValue();
    }
    return ignore;
  }

  /**
   * Box or cast supplied Object (DataObject, or DataObject[]) as DataObject[] 
   * 
   * @throws RecordFormatException if record is anything but DataObject or DataObject[]
   */
  public static DataObject[] asDataObjectArray(Object record) throws RecordFormatException {
    DataObject[] dobs;
    //check that supplied record is a DataObject or DataObject[]
    //If it's a single DataObject, then wrap it as a DataObject[]
    if (record instanceof DataObject[]) {
      dobs = (DataObject[]) record;
    }
    else {
      if (record instanceof DataObject)  { 
        //Wrap it in any array.
        dobs = new DataObject[] { (DataObject) record };
      }
      else {
        throw new RecordFormatException("Expected DataObject or DataObject[]. Got:" + record);
      }
    }
    //Let the concrete subclass do the actual conversion.
    return dobs;
  }

  /**
   * Try and convert a legacy dateHolder instance to a Date instance.
   * <NB>
   * It does <em>not</em> check that the DATE_HOLDER_CLASS is avaiable.
   * The caller may check this via dateHolderAvailable()
   * If the incoming object is not a DateHolder instance it will just 
   * return the incoming object.
   * 
   * @param incoming
   * @return
   * @throws RecordFormatException if incoming object is not a DateHolderInstance.
   */
  public static Object convertDateHolderToDate(Object incoming) {
    Object outgoing=incoming;
    //Catches DateHolder (and DateTimeHolder subclass)
    if (DATE_HOLDER_CLASS.isAssignableFrom(incoming.getClass())){
      try {
        outgoing=DATE_HOLDER_METHOD.invoke(incoming, (Object[])null);
      } catch (Exception e) {
        String msg="Failed to convert DateHolder to java.util.Date";
        log.warn(msg+". Exception: "+e);
      }
    }
    return outgoing;
  }



  /**
   * Assign attributes for the legacy convertor compenent.
   * Consult legacy openadaptor documentation for details on
   * possible attributes.
   * @param attributeMap
   */ 
  public static void setAttributes(Object legacyOpenadaptorObject, Map attributeMap)  {
    Method method=getMethod(legacyOpenadaptorObject.getClass(),LEGACY_SET_ATTRIBUTE_METHOD_NAME,LEGACY_SET_ATTRIBUTE_METHOD_ARGS);
    for (Iterator iter = attributeMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      try {
        method.invoke(legacyOpenadaptorObject,new Object[] {(String) entry.getKey(), (String) entry.getValue()});
      } 
      //ToDo: Revisit this.
      catch (StubException se) { //Ignore the stub warning, it would break springcheck ant task for now
        log.warn(se);
        if (!ignoreStubExceptions()) {
          throw new RuntimeException(se.getMessage(),se);
        }
      }
      catch (InvocationTargetException ite) {
        Throwable cause=ite.getCause();
        log.debug("InvocationTargetException cause: "+cause);
        if (cause instanceof StubException) {
          if (ignoreStubExceptions()){
            log.warn("Ignoring StubException generated on setAttributes");
          }
          else {
            log.error("Stub code invoked - "+cause.getMessage());
            throw (StubException)cause;
          }
        }
        else {
          String msg="Failed to setAttributes - "+cause;
          throw new RuntimeException(msg,cause);
        }
      }
      catch (Exception e) {
        String msg="Failed to setAttributes. Exception was: "+e;
        log.warn(msg);
        throw new RuntimeException(msg,e);
      } 
    }  
  }

  /**
   * Use reflection to derive legacy class.
   * <br>
   * Don't want to pollute openadaptor3 build with requirement for legacy jar.
   * If it fails, it will just return null.
   * @param className
   * @return
   */
  public static Class getLegacyClass(String className) {
    Class result=null;
    try {
      result= Class.forName(className);
    } catch (ClassNotFoundException e) {
      String msg="Unable to resolve "+className+". Is legacy openadaptor jar available on the classpath?";
      log.warn(msg+". Exception: "+e);
    } 
    return result;
  }
  /**
   * Reflection to get a legacy method without requiring openadaptor3 to know about it.
   * <br>
   * If class is null, then it won't bother trying.
   * @param cl
   * @param methodName
   * @param argTypes
   * @return
   */
  private static Method getMethod(Class cl,String methodName,Class[] argTypes) {
    Method method=null;
    if (cl!=null) {
      try {
        method= cl.getMethod(methodName, argTypes);
      } catch (SecurityException e) {
        String msg="Unable to resolve "+methodName+". Is legacy openadaptor jar available on the classpath?";
        log.warn(msg+". Exception: "+e);
      } catch (NoSuchMethodException e) {
        String msg="Unable to resolve "+methodName+". Is legacy openadaptor jar available on the classpath?";
        log.warn(msg+". Exception: "+e);
      }
    }
    return method;
  }


  private static Method getNonStubMethod(Class cl, String methodName,Class[] argTypes) {
    Method method=getMethod(cl, methodName, argTypes);
    if (method!=null) {
      try {
        method.invoke(new Object(),new Object[argTypes.length]);
      }    
      catch (StubException se) {
        log.warn(se);
        method=null;
      }
      catch (Exception e) {} //Can ignore other invocation ones.
    }
    return method;
  }
}

