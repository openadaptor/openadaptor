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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.dataobjects.DataObject;
import org.openadaptor.util.DateHolder;

/**
 * Converts arrays of DataObjects (legacy data format from previous versions of openadaptor)
 * to and array of objects that implement {@link IOrderedMap}.
 * 
 * @author Russ Fennell
 * @author higginse
 */
public class DataObjectToOrderedMapConvertor extends AbstractDataObjectConvertor {
  private static Log log = LogFactory.getLog(DataObjectToOrderedMapConvertor.class);

  //Flag to indicate that DataHolders could be converted to java.util.Date instances.
  private boolean convertDateHolderToDate=true;

  /**
   * Flag to indicate that DateHolder instances should be converted to Date instances.
   * <br>
   * The default is 'true'.
   * @param convertDateHolderToDate boolean flag
   */
  public void setConvertDateHolderToDate(boolean convertDateHolderToDate) {
    this.convertDateHolderToDate=convertDateHolderToDate;
  }

  public void validate(List exceptions) {
    super.validate(exceptions);

    if (convertDateHolderToDate) {
      if (LegacyUtils.dateHolderIsStub()){
        log.warn("DateHolder is stub Class - DateHolder instances will NOT be converted to java.util.date");
        setConvertDateHolderToDate(false); //Force it to false.
      }
      else { 
        log.info("Legacy DateHolder instances (including subclasses) will be converted to java.util.Date");
      }
    }
  }

  /**
   * Converts DataObjects into IOrderedMaps.
   * 
   * @return an array of one or more IOrderedMaps
   * 
   * @throws RecordException
   * 
   */
  protected Object convert(DataObject[] dobs) throws RecordException {
    int mapSize = dobs.length;
    log.debug("Processing " + mapSize + " DataObject(s)");
    IOrderedMap[] maps = new OrderedHashMap[mapSize];

    for (int i = 0; i < mapSize; i++) {
      maps[i]=convertDataObject(dobs[i]);
    }
    //If there's only a single map, don't wrap it in an array.
    //The base class will do it for us.
    if (mapSize==1) {
      return maps[0];
    }
    else {
      return maps;
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
  protected IOrderedMap convertDataObject(DataObject dataObject)  {
    IOrderedMap map=new OrderedHashMap();
    String name = dataObject.getType().getName();
    map.put(name, asOrderedMap(dataObject));
    return map;
  }

  /**
   * Recursively convert the supplied DataObject as an OrderedMap.
   * <BR> 
   * Traverses each attribute and adds them to the map. If attribute is:
   * <UL>
   *  <LI> A DataObject - recursively call this method with it.
   *  <LI> A DataObject[] call this method for each, and add an array of results 
   *  <LI> Anything else - add as a primitive attribute value.
   * </UL>
   * This should result in a hierarchy of ordered maps which reflects the original
   * structure of the DataObject
   */
  private IOrderedMap asOrderedMap(DataObject dob) throws RecordException {
    IOrderedMap map = new OrderedHashMap();
    // ToDo: decide if empty map is really better than null here.
    if (dob == null)
      return map;

    String[] attrs = dob.getType().getAttributeNames();

    for (int i = 0; i < attrs.length; i++) {
      String attr = attrs[i];
      try {
        Object value = dob.getAttributeValue(attr);
        // If it's a dataobject, then recurse call asOrderedMap recursively
        if (value instanceof DataObject) {
          if (log.isDebugEnabled()){
            log.debug(attr+"->"+((DataObject)value).getType().getName());
          }
          map.put(attr, asOrderedMap((DataObject) value));
        }
        // If it's an array of DataObjects, then generate an array of ordered maps.
        else if (value instanceof DataObject[]) {
          DataObject[] dobs = (DataObject[]) value;
          if (log.isDebugEnabled()){
            log.debug("BEGIN "+attr+"->DataObject["+dobs.length+"]");
          }
          IOrderedMap[] maps = new IOrderedMap[dobs.length];
          for (int j = 0; j < dobs.length; j++){
            maps[j] = asOrderedMap(dobs[j]);
          }
          if (log.isDebugEnabled()){
            log.debug("END   "+attr);
          }

          map.put(attr, maps);
        }
        // Not a structural part - must be an actual value.
        else {
          if ((value instanceof DateHolder) && convertDateHolderToDate) {
            value=((DateHolder)value).asDate();
          }
          if (log.isDebugEnabled()) {
            String valString=(value==null)?"<null>":value + " [" + value.getClass().getName() + "]";           
            log.debug(attr+"->" + valString);
          }
          map.put(attr, value); // Might be null
        }
      } catch (Exception e) {
        throw new RecordException("Failed to process attribute [" + attr + "]: " + e.getMessage());
      }
    }

    return map;
  }

  
}
