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
import org.openadaptor.util.DateTimeHolder;

/**
 * Convert OrderedMaps to DataObjects.
 * <BR>
 * 
 * Notes:
 *  <ul>
 *   <li>It requires that the legacy openadaptor jar (usually openadaptor.jar) is available on the classpath</li>
 *   <li>Date instances will convert to legacy DateTimeHolder instances
 *  </ul>
 * <br>
 * 
 * @author Eddy Higgins
 */
public class OrderedMapToDataObjectConvertor extends AbstractLegacyConvertor {
  private static final Log log = LogFactory.getLog(OrderedMapToDataObjectConvertor.class);
  private static final char SEP='.'; //Separator for path entries in hierarchy.

  //Mapping between auto-generated 'element' name and values to substitute for them
  protected Map typeNameMap;

  protected boolean expandTypeNames=true;

  //Cache of already created type names.
  protected Map sdoTypeCache=new HashMap();
  
  /**
   * Map to allow substitution of type names in outgoing DOTypes.
   * <p>
   * By default (sub) Map values will be turned into DOTypes with
   * attributes for each of the contained fields.<br>
   * These types are names according to their hierarchy within the
   * map supplied to {@link #convert()}.<br>
   * When creating new DOTypes, this map is checked to see if the
   * candidate type name has an entry here. If it does, then the
   * String value of the map entry is instead used.<br>
   * For example, with an entry like:
   * <pre>
   * Bike.Manufacturer -> ManuType
   * </pre>
   * Incoming ordered map with top level name Bike and subMap Manufacturer
   * will have the DOType of ManuType instead of Bike.Manufacturer.
   * Take a look at the spring example 
   * <code>example/spring/legacy/doxml_do_om_do_doxml.xml</code>
   * to see it in action.
   * 
   * @param typeNameMap a Map of hierarchynames and substitutions pairs.
   */
  public void setTypeNameMap(Map typeNameMap) {
    this.typeNameMap=typeNameMap;
  }

  /**
   * Flag to indicate if full hierarchical DOType names should be createed.
   * <p>
   * If true (the default), then DOTypes are created with names which correspond
   * to the location in the ordered map hierarchy where the type is being
   * created from (e.g. Bike.Manufacturer.Country)
   * Otherwise, just the name will be used (Country in the example shown).
   * <br>
   * Note: Note that clashing DOType names may be possible if set to false.<br>
   * Note: TypeNameMap will *always* expect the full hierarchical name even
   * if expandTypeNames is false.
   * @param expandTypeNames
   */
  public void setUseExpandedTypeNames(boolean expandTypeNames) {
    this.expandTypeNames=expandTypeNames;
  }


  /**
   * Convert an IOrderedMap into a DataObject.
   * <br>
   * The incoming ordered map is expected to have a single top level
   * entry, whose name will be used as the type for the output DataObject.
   * 
   */
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

 /**
  * Create a SimpleDataObject from an incoming ordered map.
  * <br>
  * Note: The legacy exceptions do not provide much in the way of diagnostic
  * information.
  * 
  * @param name The type name for the SDO
  * @param map the map to be converted.
  * @param path Current path within the hierarchy 
  * @return SimpleDataObject representation of the incoming map.
  * @throws InvalidParameterException if legacy operations fail for some reason.
  */
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

  /**
   * Add an attribute to an existing SimpleDataObject.
   * <br>
   * If the incoming value is primitive, it is added directly.
   * If the incoming value is an IOrderedMap, then a child SDO is created,
   * and recursively added.
   * If the incoming value is an IOrderedMap[], then each in turn is
   * individually (recursively) handled.
   * @param sdo SimpleDataObject to which an attribute must be added.
   * @param name The name of the attribute to add
   * @param value The attribute value.
   * @param path Current location in the incoming ordered map hierarchy.
   * @return the incoming sdo, with attribute added.
   * @throws InvalidParameterException on legacy exception.
   */
  private SimpleDataObject addAttribute(SimpleDataObject sdo,String name,Object value,String path) throws InvalidParameterException{
    //log.debug("addAttribute("+sdo+","+name+","+value+","+path+")");
    SDOType sdoType=SDOType.asSDOType(sdo.getType());
    if (!addAttributeAsPrimitive(sdo, sdoType, name, value)) { //It's a more complex type
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

  /**
   * This will attempt to add an attribute as a primitive value.
   * If will return true if it succeeds, false otherwise.
   * @param sdo SDO to which the attribute is to be added
   * @param sdoType ToDo - check if we still need this.
   * @param name
   * @param value
   * @return true if it was primitive, and added, false otherwise.
   * @throws InvalidParameterException if legacy exception occurs.
   */
  private boolean addAttributeAsPrimitive(SimpleDataObject sdo,SDOType sdoType,String name,Object value) throws InvalidParameterException { 
    if (value==null) {
      log.debug("Value is <null> - nothing to do here");
      return true;
    }
    if (value instanceof Date) { //Date is special case.
      log.debug("Converting Date->DateTimeHolder"); 
      value=asDateTimeHolder((Date)value);
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

 
  protected DateHolder asDateHolder(Date date) {
    return new DateHolder(date,null);
  }
  

  protected DateTimeHolder asDateTimeHolder(Date date) {
    return new DateTimeHolder(date,null);
  }

  /**
   * Generate an SDOType from a supplied name.
   * @param name
   * @return
   */

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
