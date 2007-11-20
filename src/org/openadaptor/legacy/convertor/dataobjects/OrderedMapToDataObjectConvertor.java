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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DOType;
import org.openadaptor.dataobjects.DataObject;
import org.openadaptor.dataobjects.InvalidParameterException;
import org.openadaptor.dataobjects.SDOType;
import org.openadaptor.dataobjects.SimpleDataObject;
import org.openadaptor.util.DateHolder;

/**
 * Convert OrderedMaps to DataObjects.
 * <BR>
 * 
 * Notes:
 *  <ul>
 *   <li>It requires that the legacy openadaptor jar (usually openadaptor.jar) is available on the classpath</li>
 *   <li>Date instances will convert to legacy DateHolder instances
 *  </ul>
 * <br>
 * 
 * @author Eddy Higgins
 */
public class OrderedMapToDataObjectConvertor extends AbstractLegacyConvertor {
  private static final Log log = LogFactory.getLog(OrderedMapToDataObjectConvertor.class);
  private static final char SEP='.'; //Separator for path entries in hierarchy.

  protected Map typeNameMap;

  protected boolean expandTypeNames=true;

  protected Map sdoTypeCache=new HashMap();
  
  public void setTypeNameMap(Map typeNameMap) {
    this.typeNameMap=typeNameMap;
  }

  public void setUseExpandedTypeNames(boolean expandTypeNames) {
    this.expandTypeNames=expandTypeNames;
  }


  public Object convert(Object record) throws RecordException {
    Object result=null;
    try {
      if (! (record instanceof IOrderedMap)) {
        throw new RecordFormatException("Expected IOrderedMap, got "+record.getClass().getName());
      }
      IOrderedMap map=(IOrderedMap)record;

      List keys=map.keys();
      if (keys.size()!=1) {
        String msg="Incoming map should have exactly one key, but has " +keys.size();
        log.warn(msg);
      }

      Object key=keys.get(0);
      //This it the current path, for use when mapping names.
      String path=key.toString();
      result=sdoFromMap(path,(IOrderedMap)map.get(key),null);

    } catch (InvalidParameterException ipe) {
      log.error("InvalidParameter: "+ ipe);
      ipe.printStackTrace();
      throw new RecordException("Failed to convert "+ipe.getMessage(),ipe);
    }
    return result;
  }


  private SimpleDataObject sdoFromMap(String name,IOrderedMap map,String path) throws InvalidParameterException {
    String mapPath=path==null?name:path+SEP+name;
    log.debug(mapPath+" sdoFromMap("+name+","+map+","+path+")");
    SDOType mapType=getSDOType(mapPath);
    log.debug(mapPath+" sdoFromMap() Creating new SDO with type= "+mapType.getName());
    SimpleDataObject sdo=new SimpleDataObject(mapType);

    Iterator it=map.keys().iterator(); 
    //Add each key/value as an attributeValue.
    log.debug(mapPath+" sdoFromMap() adding attributes...");
    while (it.hasNext()) {
      Object key=it.next();
      String attrName=key.toString();
      Object value=map.get(key);
      log.debug(mapPath+" sdoFromMap() adding attribute "+attrName+"->"+value);
      addAttribute(sdo,attrName,value,mapPath);
    }
    //Now add it to the parent sdo.
    //log.debug("sdoFromMap() returning sdo  "+sdo);
    log.debug(mapPath+" sdoFromMap() returning sdo of type "+sdo.getType().getName());
    return sdo;
  }

  private SimpleDataObject addAttribute(SimpleDataObject sdo,String name,Object value,String path) throws InvalidParameterException{
    //log.debug("addAttribute("+sdo+","+name+","+value+","+path+")");
    SDOType sdoType=SDOType.asSDOType(sdo.getType());
    if (!processAsPrimitive(sdo, sdoType, name, value)) { //It's a more complex type
      if (value instanceof IOrderedMap) { //Process an ordered map
        //log.debug("addAttribute() processing OM: "+value);
        SimpleDataObject mapSdo=sdoFromMap(name,(IOrderedMap)value,path);
        //log.debug("addAttribute() processed OM to sdo "+mapSdo);

        //Need to use getAttributeNamed() or non-existent throws Exception.
        if (sdoType.getAttributeNamed(name)!=null) { //Already have a value!
          log.debug("Adding value to existing attribute "+name);
          DataObject[] old=(DataObject[])sdo.getAttributeValue(name);
          int oldLength=old.length;
          DataObject[] modified=new DataObject[oldLength+1];
          System.arraycopy(old, 0, modified, 0, oldLength);
          modified[oldLength]=mapSdo;
          sdo.setAttributeValue(name, modified);
        }
        else { //No such attribute, but we need it now.
          log.debug("Adding new attribute "+ name);
          sdoType.addAttribute(name, mapSdo.getType());
          //Have to add it as a DataObject[]. Don't know why.
          sdo.setAttributeValue(name, new DataObject[] {mapSdo});
        }
      }
      else { //Not OM, how about an Array of 'em?
        if (value instanceof IOrderedMap[]) {
          IOrderedMap[] maps=(IOrderedMap[])value;
          log.debug(path+" addAttribute() processing DataObject["+maps.length+"]");
          for (int i=0;i<maps.length;i++) {
            addAttribute(sdo,name,maps[i],path);
          }
          return sdo;
        }
        else {
          String msg="Unable to handle value of type"+value.getClass().getName();
          log.error(msg);
          throw new RecordFormatException(msg);
        }
      }
    }
    //log.debug("addAttribute() returning sdo "+sdo);
    log.debug(path+" addAttribute() returning sdo of type "+sdo.getType().getName());
    return sdo;
  }

  private boolean processAsPrimitive(SimpleDataObject sdo,SDOType sdoType,String name,Object value) throws InvalidParameterException { 
    if (value==null) {
      log.debug("Value is <null> - nothing to do here");
      return true;
    }
    if (value instanceof Date) { //Date is special case.
      log.debug("Converting Date->DateHolder"); 
      value=asDateHolder((Date)value);
    }
    DOType primitiveType=SDOType.typeForValue(value);
    if (primitiveType!=null) {
      log.debug("addAttribute() Setting primitive ("+primitiveType.getName()+") "+name+"="+value);
      if(sdoType.getAttributeNamed(name) ==null) {
        log.debug("Adding new attribute "+name+" ("+primitiveType.getName()+") to type "+sdoType.getName());
        sdoType.addAttribute(name, primitiveType);
      }
      else {
        log.debug("Attribute "+name+" already exists in type "+sdoType.getName());
      }
      sdo.setAttributeValue(name, value);
      return true; //It was primitive, and has been processed.
    }
    return false; //Wasn't a primitive.
  }

  private SimpleDataObject addAttributeWorksExceptDates(SimpleDataObject sdo,String name,Object value,String path) throws InvalidParameterException{
    log.debug("addAttribute("+sdo+","+name+","+value+","+path+")");
    //SDOType sdoType=(SDOType)sdo.getType(); //Really hacky. Not legal to do this cast!
    SDOType sdoType=SDOType.asSDOType(sdo.getType());
    DOType valueType=getBasicDOType(value);
    if (valueType!=null) { //Can just add it with type
      log.debug("addAttribute() Setting primitive ("+valueType.getName()+") "+name+"="+value);
      sdoType.addAttribute(name, valueType);
      sdo.setAttributeValue(name, value);
    }
    else { //It's a more complex type
      if (value instanceof IOrderedMap) {
        log.debug("addAttribute() processing OM: "+value);
        SimpleDataObject mapSdo=sdoFromMap(name,(IOrderedMap)value,path);
        log.debug("addAttribute() processed OM to sdo "+mapSdo);
        sdoType.addAttribute(name, mapSdo.getType());
        //Have to add it as a DataObject[]. Don't know why.
        sdo.setAttributeValue(name, new DataObject[] {mapSdo});
        //sdo.addAttributeValue(name, mapSdo);
      }
      else {
        if (value instanceof IOrderedMap[]) {
          IOrderedMap[] maps=(IOrderedMap[])value;
          log.debug("addAttribute() processing DataObject["+maps.length+"]");
          for (int i=0;i<maps.length;i++) {
            addAttribute(sdo,name,maps[i],path);
          }
          return sdo;
        }
        else {
          throw new RecordFormatException("Unable to handle value of type"+value.getClass().getName());
        }
      }
    }
    log.debug("addAttribute() returning sdo "+sdo);
    return sdo;
  }


  private Object generate(SDOType type,Object object,String path) throws InvalidParameterException{
    Object result=null;
    if (object instanceof Object[]) {
      log.debug("Data is Object[]");
      Object[] objects=(Object[])object;
      Object[] output=new Object[objects.length];
      for (int i=0;i<objects.length;i++) {
        output[i]=generate(type,objects[i],path);
      }
    }
    else {
      if (object instanceof IOrderedMap) {
        result=generate(type,(IOrderedMap)object,path);
      }
      else {
        log.warn("Unexpected data: "+object);
      }
    }
    return result;
  }

  private Object generateShite(SDOType type,IOrderedMap map,String path) throws InvalidParameterException{
    SimpleDataObject sdo=new SimpleDataObject(type);
    Iterator it=map.keys().iterator(); 

    //Add each key/value
    while (it.hasNext()) {
      Object key=it.next();
      String attrName=key.toString();
      Object value=map.get(key);
      DOType attrType;
      log.debug("key="+key+"; value="+value);

      String newPath=path+SEP+attrName;

      if (value instanceof IOrderedMap) {
        log.debug("Processing OM");
        attrType=getSDOType(newPath);
        value=generate((SDOType)attrType,(IOrderedMap)value,newPath);
        log.debug("Adding attribute "+attrName+" to type "+attrType);
        type.addAttribute(attrName, attrType); //Add attribute to the parent type first.
        log.debug("Setting attribute "+attrName+" to "+value);
        sdo.setAttributeValue(attrName, value); //Then set the value.          
      }
      else {
        if (value instanceof IOrderedMap[]) {
          log.debug("Processing array");
          attrType=null;
          //Process an Object[]
          //Need to generate a corresponding DataObject[]
          IOrderedMap[] maps=(IOrderedMap[])value;
          DataObject[] doArray=new DataObject[ maps.length];
          for (int i=0;i<maps.length;i++) {
            doArray[i]=(DataObject)generate((SDOType)attrType,maps[i],newPath);
          }
          value=doArray;
          log.debug("Adding attribute "+attrName+" to type "+attrType);
          type.addAttribute(attrName, attrType); //Add attribute to the parent type first.
          log.debug("Setting attribute "+attrName+" to "+value);
          sdo.setAttributeValue(attrName, value); //Then set the value.          
        }
        else { //Gotta treat as a primitive...
          log.debug("Processing primitive key="+attrName+"; value="+value);
          attrType=SDOType.typeForValue(value);
          try {
            type.addAttribute(attrName,attrType);
          }
          catch (Throwable t){
            t.printStackTrace();
          }
          sdo.setAttributeValue(attrName, value);
        }
      }
    }   
    return sdo;
  }


  private DOType getBasicDOType(Object value) {
    if (value instanceof Date) { //Date is special case.
      log.debug("Converting Date->DateHolder"); 
      value=asDateHolder((Date)value);
    }
    return SDOType.typeForValue(value);   
  }


  private Object generateOrig(SDOType type,IOrderedMap map,String path) throws InvalidParameterException{
    SimpleDataObject sdo=new SimpleDataObject(type);
    Iterator it=map.keys().iterator(); 

    //Add each key/value as an attributeValue.
    while (it.hasNext()) {
      Object key=it.next();
      String attrName=key.toString();
      Object value=map.get(key);
      DOType attrType;
      log.debug("key="+key+"; value="+value);

      if ((value instanceof IOrderedMap) || (value instanceof IOrderedMap[])) {
        String newPath=path+SEP+attrName;
        attrType=getSDOType(newPath);
        if (value instanceof IOrderedMap[]) {
          //Need to generate a corresponding DataObject[]
          IOrderedMap[] maps=(IOrderedMap[])value;
          DataObject[] doArray=new DataObject[ maps.length];
          for (int i=0;i<maps.length;i++) {
            doArray[i]=(DataObject)generate((SDOType)attrType,maps[i],newPath);
          }
          value=doArray;
        }
        else { //Just an OM
          value=generate((SDOType)attrType,(IOrderedMap)value,newPath);
        }
      }
      else { 
        if (value instanceof Date) {
          log.debug("Converting Date->DateHolder"); 
          value=asDateHolder((Date)value);
        }
        attrType=SDOType.typeForValue(value);
        log.debug(type.getName()+": Setting DOType for name/value: "+attrName+"/"+value+" is: "+attrType);
      }
      log.debug("Adding attribute "+attrName+" to type "+attrType);
      type.addAttribute(attrName, attrType); //Add attribute to the parent type first.
      sdo.setAttributeValue(attrName, value); //Then set the value.
    }   
    return sdo;
  }

  private DateHolder asDateHolder(Date date) {
    DateHolder dateHolder=new DateHolder(date,null);
    return dateHolder;
  }

  /**
   * Generate an SDOType from a supplied name.
   * @param name
   * @return
   */
//private SDOType getSDOType(String name) {
//return getSDOType(name,null);
//}
//private SDOType getSDOType(String name,String path) {
//String unmappedName=path==null?name:path+"."+name;
//if ((typeNameMap!=null) &&(typeNameMap.containsKey(unmappedName))){
//name=typeNameMap.get(unmappedName).toString();
//}
//return new SDOType(name); 
//}

  private SDOType getSDOType(String path) {
    String typeName=path; //Default.
    if ((typeNameMap!=null) &&(typeNameMap.containsKey(path))){
      typeName=typeNameMap.get(path).toString();
    }
    else { //No mapping.
      if (!expandTypeNames){ //Strip path.
        int ofs=path.lastIndexOf(SEP);
        if (ofs>0) {
          typeName=path.substring(ofs+1);     
        }
      } 
    }
    SDOType type;
    if (!sdoTypeCache.containsKey(typeName)) {
      log.debug("Creating new SDOType for "+typeName);
      type=new SDOType(typeName);
      sdoTypeCache.put(typeName, type);
    }
    else {
      log.debug("Using cached SDOType for "+typeName);
      type=(SDOType)sdoTypeCache.get(typeName);
    }
    return type;
  }
}
