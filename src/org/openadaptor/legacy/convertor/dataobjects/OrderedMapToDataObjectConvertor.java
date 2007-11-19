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

  protected Map typeNameMap;
  
  protected boolean expandTypeNames=true;

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
      SDOType doType=getSDOType(path);
      result=generate(doType,map.get(key),path);

    } catch (InvalidParameterException ipe) {
      log.error(ipe.getMessage());
      throw new RecordException("Failed to convert "+ipe.getMessage(),ipe);
    }

    return result;
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

  private Object generate(SDOType type,IOrderedMap map,String path) throws InvalidParameterException{
    SimpleDataObject sdo=new SimpleDataObject(type);
    Iterator it=map.keys().iterator(); 

    //Add each key/value
    while (it.hasNext()) {
      Object key=it.next();
      String attrName=key.toString();
      Object value=map.get(key);
      DOType attrType;
      log.debug("key="+key+"; value="+value);

      if ((value instanceof IOrderedMap) || (value instanceof IOrderedMap[])) {
        attrType=getSDOType(attrName,path);
        String newPath=path+"."+attrName;
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
  private SDOType getSDOType(String name) {
    return getSDOType(name,null);
  }
  private SDOType getSDOType(String name,String path) {
    String unmappedName=path==null?name:path+"."+name;
    if ((typeNameMap!=null) &&(typeNameMap.containsKey(unmappedName))){
      name=typeNameMap.get(unmappedName).toString();
    }
    return new SDOType(name); 
  }
}
