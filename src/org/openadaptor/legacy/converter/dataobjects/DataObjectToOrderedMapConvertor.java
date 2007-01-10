/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.legacy.converter.dataobjects;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/processor/convertor/openadaptor/DataObjectToOrderedMapConvertorProcessor.java,v
 * 1.1 2006/07/19 15:16:20 higginse Exp $ Rev: $Revision: 1.1 $ Created Jul 13, 2006 by oa3 Core Team
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.auxil.convertor.AbstractConvertor;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DataObject;

/**
 * @author Russ Fennell
 */
public class DataObjectToOrderedMapConvertor extends AbstractConvertor {
  private static Log log = LogFactory.getLog(DataObjectToOrderedMapConvertor.class);

  /**
   * Takes an array of DataObjects and converts them into an Ordered Map
   * 
   * @return an array of one or more IOrderedMaps
   * 
   * @throws RecordException
   * 
   */
  protected Object convert(Object record) throws RecordException {

    DataObject[] dobs;
    // todo: does the transport strip out DO arrays into individual DO's

    if (record instanceof DataObject[])
      dobs = (DataObject[]) record;
    else if (record instanceof DataObject)
      dobs = new DataObject[] { (DataObject) record };
    else
      throw new RecordFormatException("Processor expects arrays of DataObjects - Supplied record:" + record);

    int mapSize = dobs.length;
    IOrderedMap maps = new OrderedHashMap(mapSize);
    log.debug("Processing " + mapSize + " DataObject(s)");

    for (int i = 0; i < mapSize; i++) {
      DataObject dob = dobs[i];
      String name = dob.getType().getName();
      log.debug("DataObject " + i + ": " + name);

      maps.put(name, asOrderedMap(dob));
    }

    log.debug("Map contains " + maps.size() + " sub-maps");

    return maps;
  }

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
          if (value == null) {
            log.debug("Attribute value : <null>");
          } else {
            if (value instanceof String) {
              log.debug("Attribute value :" + value + " [" + value.getClass().getName() + "]");
            } else {
              log.debug("Attribute value :" + value);
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
}
