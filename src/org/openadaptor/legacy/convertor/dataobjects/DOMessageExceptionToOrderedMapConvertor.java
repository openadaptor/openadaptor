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