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

package org.openadaptor.legacy.convertor.dataobjects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DOAttribute;
import org.openadaptor.dataobjects.DOType;
import org.openadaptor.dataobjects.DataObject;
import org.openadaptor.dataobjects.InvalidParameterException;
import org.openadaptor.dataobjects.SDOType;
import org.openadaptor.dataobjects.SimpleDataObject;
import org.openadaptor.util.DateHolder;
import org.openadaptor.util.DateTimeHolder;

/**
 * Convert OrderedMaps to DataObjects, using template DOXML to define the
 * output types.
 * <BR/>
 * 
 * @author Eddy Higgins
 */
public class OrderedMapToDataObjectConvertor extends AbstractLegacyConvertor {
  private static final Log log = LogFactory.getLog(OrderedMapToDataObjectConvertor.class);

  private DOXmlToDataObjectConvertor templateConvertor=new DOXmlToDataObjectConvertor();

  protected String templateDOXmlFilename;

  protected Map typeCache; //Cache of known types from template objects

  protected boolean debug=log.isDebugEnabled(); //Cached debug flag.

  public void setTemplateDOXmlFilename(String templateDOXmlFilename) {
    this.templateDOXmlFilename=templateDOXmlFilename;
  }

  /**
   * Convert an IOrderedMap into a DataObject.
   * <br>
   * The incoming ordered map is expected to have a single top level
   * entry, whose name will be used as the type for the output DataObject.
   * 
   */
  public Object convert(Object record) throws RecordException {
    // Update debug flag (in case it has changed)
    debug=log.isDebugEnabled();

    if (typeCache==null) {
      typeCache=cacheTypes(loadTemplate(templateDOXmlFilename));
    }
    SimpleDataObject[] dobs=null;
    if (! (record instanceof IOrderedMap)) {
      throw new RecordFormatException("Expected IOrderedMap, got "+record.getClass().getName());
    }
    IOrderedMap map=(IOrderedMap)record;

    Object[] keys=map.keys().toArray(new Object[map.size()]);    
    dobs=new SimpleDataObject[keys.length];

    for (int i=0;i<dobs.length;i++) {
      Object key=keys[i];
      Object value=map.get(key);
      String name=key.toString();
      log.debug("convert() is processing "+name+"->"+value);
      if (typeCache.containsKey(name)) { //We have type
        DOType type=(DOType)typeCache.get(name);
        try {
        dobs[i]=dataObjectFromMap(type,(IOrderedMap)value);
        }
        catch (InvalidParameterException ipe) {
          String msg="Convert failed "+ipe;
          log.warn(msg);
          throw new RecordException(msg);               
        }
      }
      else { //Unknown type
        String msg="No type registered for field named "+name;
        log.warn(msg);
        throw new RecordFormatException(msg);   
      }
    }
    return dobs;
  }

  private void setAttribute(SimpleDataObject parent,DOType type,String name,Object value) throws InvalidParameterException {
    //if (debug) {log.debug("sdo["+parent.getType().getName()+"] "+name+"("+type.getName()+") -> "+value);}
    if (value instanceof Object[]) {
      Object[] values=(Object[])value;
      for (int i=0;i<values.length;i++) {
        setOneAttribute(parent,type,name,values[i]);
      }
    }
    else {
      setOneAttribute(parent,type,name,value);
    }
  }

  private void setOneAttribute(SimpleDataObject parent,DOType type,String name,Object value) throws InvalidParameterException {
    if (type.isPrimitive()) {
      if (debug) {log.debug("sdo["+parent.getType().getName()+"] "+name+"("+type.getName()+") -> "+value+ " primitive");}
      if (SDOType.DATETIME.equals(type) && (null!=value)) {
        if (debug) {log.debug("Converting Date value "+value+" to DateTimeHolder");}
        value=asDateTimeHolder((Date)value);
      }
      else
        if (SDOType.DATE.equals(type) && (null!=value)) {
          if (debug) {log.debug("Converting Date value "+value+" to DateHolder");}
          value=asDateHolder((Date)value);        
        }
      parent.addAttributeValue(name, value);
    }
    else { //Complex.
      if (debug) {log.debug("sdo["+parent.getType().getName()+"] "+name+"("+type.getName()+") -> "+value+ " complex");}
      if (value!=null) { 
        //Generate an SDO for the value.
        SimpleDataObject child=dataObjectFromMap(type,(IOrderedMap)value);

        //Need to first check if it already has a value. If so add this one to it.
        DataObject[] newVal; //DataObject[] which will hold the value(s)
        //Get the old value, if any first.
        int oldSize=0;
        DataObject[]  oldVal=(DataObject[])parent.getAttributeValue(name);
        if (oldVal!=null) {
          oldSize=oldVal.length;
          newVal=new DataObject[oldSize+1];
          System.arraycopy(oldVal, 0, newVal, 0, oldSize);
          newVal[oldSize]=child;
        }  
        else {
          newVal=new DataObject[] {child};
        } 
       parent.addAttributeValue(name, newVal);
      }
      else {
        if (debug) {log.debug("Complex attribute "+name+" has null value - not adding");}
      }
    }
  }

  private SimpleDataObject dataObjectFromMap(DOType type,IOrderedMap map) throws InvalidParameterException{
    SimpleDataObject sdo=new SimpleDataObject(type);
    DOAttribute[] attrs=type.getAttributes();
    for (int i=0;i<attrs.length;i++) {
      DOAttribute attr=attrs[i];
      String attrName=attr.getName();
      if (map.containsKey(attrName)) {
        Object value=map.get(attrName);
        setAttribute(sdo,attr.getType(),attrName,value);
      }
      else {
        if (debug) {log.debug("Map does not contain attribute "+attrName);}
      }
    }
    return sdo;
  }

  protected DateHolder asDateHolder(Date date) {
    return new DateHolder(date,null);
  }

  protected DateTimeHolder asDateTimeHolder(Date date) {
    return new DateTimeHolder(date,null);
  }

  /**
   * Cache all of the non-primitive types defined by the
   * supplied DataObject array.
   * 
   * @param templateDataObjects template DataObject[] to be used
   * @return Map containing all of the non-primitive DOTypes.
   */
  private Map cacheTypes(DataObject[] templateDataObjects) {
    IOrderedMap cache=new OrderedHashMap();
    for (int i=0;i<templateDataObjects.length;i++) {
      updateCache(cache,templateDataObjects[i].getType());
    }
    return cache;
  }

  /**
   * Add the supplied type to the supplied Map.
   * <br>
   * Primitive types are ignored.
   * Non-primitive types are added to the cache, unless
   * they already exist.
   * The type of each attribute is then recursively added
   * similarly.
   * @param cache IOrderedMap to contain the types.
   * @param type contains the type to be added
   */
  private void updateCache(IOrderedMap cache,DOType type) {
    if (!type.isPrimitive()) { //We can ignore primitives.
      String typeName=type.getName();
      if (cache.containsKey(typeName)) { 
        //log.warn("Ignoring duplicate type name "+typeName) ;
      }
      else {
        log.debug("Caching type"+typeName);
        cache.put(typeName, type);
        DOAttribute[] attributes=type.getAttributes();
        for (int i=0;i<attributes.length;i++) {
          updateCache(cache,attributes[i].getType());
        }
      }
    }
  }

  /**
   * Load template DataObject[] from supplied filename
   * <br>
   * File is expected to contain valid DOXML for template
   * DataObjects.
   * @param filename path to file to read
   * @return DataObject[] from the supplied DOXML
   */
  private DataObject[] loadTemplate(String filename) {
    DataObject[] dobs = null;
    if (filename==null) {
      throw new OAException("templateDOXmlFilename not configured, but is mandatory");
    }
    try {
      String text=readFile(filename);
      dobs=(DataObject[])templateConvertor.convert(text);
    }
    catch (IOException ioe) {
      String msg="Failed to load template DataObjects from DOXML file "+filename;
      log.error(msg);
      throw new RuntimeException(msg,ioe);
    }
    return dobs;
  }

  /**
   * Read complete contents of a text file into a String.
   * @param filePath  file to be read
   * @return String containing contents of the file
   * @throws IOException if any I/O problems are encountered.
   */
  private static String readFile(String filePath) throws IOException{
    StringBuffer fileData = new StringBuffer();
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    char[] buf = new char[2048];
    int read=0;
    while((read=reader.read(buf)) != -1){
      String s = String.valueOf(buf, 0, read);
      fileData.append(s);
    }
    reader.close();
    return fileData.toString();
  }
}
