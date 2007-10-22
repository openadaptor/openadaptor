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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DataObject;

/**
 * Converts arrays of DataObjects (legacy data format from previous versions of openadaptor)
 * to and array of objects that implement {@link IOrderedMap}.
 * 
 * @author Russ Fennell
 * @author higginse
 */
public class DataObjectToOrderedMapConvertor extends AbstractConvertor {
  private static Log log = LogFactory.getLog(DataObjectToOrderedMapConvertor.class);
  private static final Class DATE_HOLDER_CLASS=getLegacyClass("org.openadaptor.util.DateHolder");
  private static final Method DATE_HOLDER_METHOD=getMethod(DATE_HOLDER_CLASS,"asDate",(Class[])null);

  private boolean convertDateHolderToDate=true;

  public void setConvertDateHolderToDate(boolean convertDateHolderToDate) {
    this.convertDateHolderToDate=convertDateHolderToDate;
  }

  public void validate(List exceptions) {
    super.validate(exceptions);

    if (convertDateHolderToDate) {
      if (DATE_HOLDER_METHOD!=null){
        log.info("Legacy DateHolder instances (including subclasses) will be converted to java.util.Date");
      }
      else { 
        log.warn("Unable to get asDate() method from DateHolderClass - DateHolder instances will NOT be converted to java.util.date");
        setConvertDateHolderToDate(false); //Force it to false.
      }
    }
  }

  /**
   * Convert a single DataObject into a Single OrderedMap
   * 
   * @return an IOrderedMaps
   * 
   * @throws RecordException
   * 
   */
  protected Object convert(Object record) throws RecordException {
    if (! (record instanceof DataObject)) {                        
      throw new RecordFormatException("Expected DataObject - Supplied record:" + record);
    }
    IOrderedMap map=new OrderedHashMap();
    DataObject dataObject= (DataObject)record;
    String name = dataObject.getType().getName();
    map.put(name, asOrderedMap(dataObject));
    return map;
  }

// Old convert would fail for an array of DataObjects.
//  /**
//   * Takes an array of DataObjects and converts them into an Ordered Map
//   * 
//   * @return an array of one or more IOrderedMaps
//   * 
//   * @throws RecordException
//   * 
//   */
//  protected Object convertOld(Object record) throws RecordException {
//
//    DataObject[] dobs;
//    // todo: does the transport strip out DO arrays into individual DO's
//
//    if (record instanceof DataObject[])
//      dobs = (DataObject[]) record;
//    else if (record instanceof DataObject)                         
//      dobs = new DataObject[] { (DataObject) record };
//    else
//      throw new RecordFormatException("Processor expects arrays of DataObjects - Supplied record:" + record);
//
//    int mapSize = dobs.length;
//    IOrderedMap maps = new OrderedHashMap(mapSize);
//    log.debug("Processing " + mapSize + " DataObject(s)");
//
//    for (int i = 0; i < mapSize; i++) {
//      DataObject dob = dobs[i];
//      String name = dob.getType().getName();
//      log.debug("DataObject " + i + ": " + name);
//
//      maps.put(name, asOrderedMap(dob));
//    }
//
//    log.debug("Map contains " + maps.size() + " sub-maps");
//
//    return maps;
//  }

  
  /**
   * Renderers the supplied DataObject as an OrderedMap. Loops through all the attributes and adds them to the map. If
   * the attribute is anything other than a Java primitive then we recursively create another OrderedMap and add it. In
   * this way we get a tree of maps corresponding to the structure of the DataObject
   */
  private IOrderedMap asOrderedMap(DataObject dob) throws RecordException {
    IOrderedMap map = new OrderedHashMap();
    // ToDo: decide if empty map is really better than null here.
    if (dob == null)
      return map;

    String[] attrs = dob.getType().getAttributeNames();

    for (int i = 0; i < attrs.length; i++) {
      String attr = attrs[i];
      log.debug("  Processing attribute [" + attr + "]");

      try {
        Object value = dob.getAttributeValue(attr);
        // If it's a dataobject, then recurse call asOrderedMap recursively
        if (value instanceof DataObject) {
          log.debug("  - Attribute is DataObject. Will traverse");
          map.put(attr, asOrderedMap((DataObject) value));
        }
        // If it's an array of DataObjects, then generate an array of ordered maps.
        else if (value instanceof DataObject[]) {
          log.debug("  - Attribute is DataObject array. Will traverse each one");
          DataObject[] dobs = (DataObject[]) value;
          IOrderedMap[] maps = new IOrderedMap[dobs.length];
          for (int j = 0; j < dobs.length; j++)
            maps[j] = asOrderedMap(dobs[j]);

          map.put(attr, maps);
        }
        // Not a structural part - must be an actual value.
        else {
          if (value==null) {
            log.debug("Attribute value : <null>");
          }
          else {
            if (log.isDebugEnabled()) {
              log.debug("Attribute value :" + value + " [" + value.getClass().getName() + "]");
            }  
            //Need to check if it's a legacy dataobject type - if so we need to 'clean' it.
            if (convertDateHolderToDate) {
              value=clean(value);
            }
          }
          map.put(attr, value); // Might be null
        }
      } catch (Exception e) {
        throw new RecordException("Failed to process attribute [" + attr + "]: " + e.getMessage());
      }
    }

    return map;
  }
  /**
   * Change DateHolder types to Date.
   * Remove proprietary DataObjects Date types.
   * @param incoming
   * @return 
   */
  private static Object clean(Object incoming) {
    Object outgoing=incoming;
    Class cl=incoming.getClass();
    //Catches DateHolder (and DateTimeHolder subclass)
    if (DATE_HOLDER_CLASS.isAssignableFrom(cl)){
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
   * Use reflection to derive legacy class.
   * <br>
   * Don't want to pollute openadaptor3 build with requirement for legacy jar.
   * If it fails, it will just return null.
   * @param className
   * @return
   */
  private static Class getLegacyClass(String className) {
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

}
