/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.legacy.converter.dataobjects;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.RFC2279;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DataObject;
import org.openadaptor.dataobjects.InvalidParameterException;
import org.openadaptor.dataobjects.SimpleDataObject;

/**
 * Convert OrderedMaps to DataObjects
 * 
 * --- WARNING ----- TODO: This class is still at the prototype stage and should not be used, not least because it is
 * incomplete!
 * 
 * 
 * 
 * @author Eddy Higgins
 */
public class OrderedMapToDataObjectConvertor extends AbstractConvertor implements RFC2279 {
  private static final Log log = LogFactory.getLog(OrderedMapToDataObjectConvertor.class);

  // BEGIN Bean getters/setters
  // END Bean getters/setters

  // BEGIN Abstract Convertor Processor implementation

  public OrderedMapToDataObjectConvertor() {
    log.warn("--- THIS CONVERTOR IS STILL IN DEVELOPMENT - DO NOT USE ---");
  }

  /**
   * Convert an OrderedMap into a DataObject
   * 
   * 
   * @param record
   * 
   * @return DataObject representation of the OrderedMap
   * 
   * @throws RecordException
   *           if the conversion fails
   */
  protected Object convert(Object record) throws RecordException {
    if (!(record instanceof IOrderedMap))
      throw new RecordFormatException("Record is not an IOrderedMap. Record: " + record);

    log.warn("--- USING PROTOTYPE OM->DO CONVERTOR IS UNWISE ---");
    return convertOrderedMapToDataObject((IOrderedMap) record);
  }

  // END Abstract Convertor Processor implementation

  private Object convertOrderedMapToDataObject(IOrderedMap map) throws RecordException {

    Object result = null;

    // Create a Document to hold the data.
    DataObject sdo = new SimpleDataObject();
    String key = null;
    try {
      Iterator it = map.keys().iterator();
      while (it.hasNext()) {
        key = (String) it.next();
        Object value = map.get(key);
        if (value instanceof IOrderedMap) { // Recurse
          sdo.setAttributeValue(key, convertOrderedMapToDataObject((IOrderedMap) value));
        } else {
          if (value instanceof IOrderedMap[]) {
            IOrderedMap[] maps = (IOrderedMap[]) value;
            DataObject[] dataObjects = new DataObject[maps.length];
            for (int i = 0; i < maps.length; i++) {
              dataObjects[i] = (DataObject) convertOrderedMapToDataObject(maps[i]);
            }
            sdo.setAttributeValue(key, dataObjects);
          }

          else {
            sdo.setAttributeValue(key, value);
          }
        }
      }
    } catch (InvalidParameterException ipe) {
      throw new RecordException("Failed to process attribute [" + key + "]: " + ipe, ipe);
    }
    return result;
  }

}
