/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.exception.ExceptionToOrderedMapConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.dataobjects.DOType;
import org.openadaptor.dataobjects.DataObject;

/**
 * Custom MesssageException to IOrderedMap convertor that in addition to 
 * standard {@link ExceptionToOrderedMapConvertor} extracts certain fields 
 * specific to DataObject payload.
 */
public class DOMessageExceptionToOrderedMapConvertor extends
    ExceptionToOrderedMapConvertor {

  private static final Log log = LogFactory.getLog(DOMessageExceptionToOrderedMapConvertor.class);

  private static final String ID_DO_ATTRIB = "id";
  
  private String dataObjectTypeNameColName = "DATA_OBJECT_TYPE";  
  
  private String dataObjectIdColName = "DATA_OBJECT_ID";
  
  /**
   * First runs conversion from the superclass. Then downcasts the <code>record</code>
   * to a DataObject and extracts name of the type of the DataObject and
   * 'id' attribute.
   * 
   * @param record - expects a MessageException that carries a DataObject
   * @return an map with data necessary to populate exception db schema 
   */
  protected Object convert(Object record) {
    Object result = super.convert(record);
    
    /* 
     * Checks for null and correct data types. An uncaught 
     * exception will shut down the adaptor.
     */
    if(result==null){
      log.warn("MessageException -> IOrderedMap failed");
      return result;
    }
    Object data = null;
    if((record instanceof MessageException)){
      data = ((MessageException) record).getData();
      if(data==null || !(data instanceof DataObject)){
        log.error("Payload is not DataObject");
        return result;
      }
    }else{
      log.warn("Record not MessageException");
      return result;
    }
    
    /* Downcast and extract relevant fields */
    IOrderedMap orderedMap = (IOrderedMap) result;
    DataObject dataObject = (DataObject) data;
    DOType type = dataObject.getType();
    String typeName = null;
    if(type!=null){
      typeName = type.getName();
    }
    Object msgId = null;
    try {
      msgId = dataObject.getAttributeValue(ID_DO_ATTRIB);
    } catch (Exception e) {
      log.error("Error while reading attributes from DataObject", e);
    }
    String idStr = null;
    if(msgId!=null){
      idStr = msgId.toString();
    }
    
    /* Populate the map */
    orderedMap.put(dataObjectTypeNameColName, typeName);
    orderedMap.put(dataObjectIdColName, idStr);
    return orderedMap;
  }

}
