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

package org.openadaptor.auxil.convertor.exception;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.exception.MessageException;

/**
 * The class is often useful as part of the exception processing pipeline.  
 * It converts a {@link MessageException} to an ordered map. This is to allow for further
 * custom processing of exceptions, such as persisting them in a database, to achieve
 * behaviour known in the legacy Openadaptor as The Hospital (section 13c of the tutorial 
 * provides more details on The Hospital).
 * 
 * @author Kris Lachor
 */
public class ExceptionToOrderedMapConvertor extends AbstractConvertor {
  
  private static final Log log = LogFactory.getLog(ExceptionToOrderedMapConvertor.class);
  
  private static final String NO_CAUSE_EXCEPTION     = "No cause exception detected.";
  private static final String UNKNOWN_ADAPTOR_NAME   = "Unknown";
  private static final String UNKNOWN_COMPONENT_NAME = "Unknown";
  
  /* Messges for missing non-critical data, initialised to defaults. */
  private String noCauseException     = NO_CAUSE_EXCEPTION;
  private String unknownAdaptorName   = UNKNOWN_ADAPTOR_NAME;
  private String unknownComponentName = UNKNOWN_COMPONENT_NAME;
  
  /* 
   * Default ordered map field names. Ideally, these should correspond to column names
   * in the database - public setters allow for overriding the defaults.
   */
  static final String TIMESTAMP               = "TIMESTAMP";
  static final String EXCEPTION_CLASS         = "EXCEPTION_CLASS_NAME";
  static final String EXCEPTION_MESSAGE       = "EXCEPTION_MESSAGE";
  static final String CAUSE_EXCEPTION_CLASS   = "CAUSE_EXCEPTION_CLASS_NAME";
  static final String CAUSE_EXCEPTION_MESSAGE = "CAUSE_EXCEPTION_MESSAGE";
  static final String STACK_TRACE             = "STACK_TRACE";
  static final String ADAPTOR_NAME            = "ADAPTOR_NAME";
  static final String COMPONENT               = "ORIGINATING_COMPONENT";
  static final String THREAD_NAME             = "THREAD_NAME";
  static final String DATA_TYPE               = "DATA_TYPE";
  static final String DATA                    = "DATA";
  static final String METADATA                = "METADATA";
  static final String FIXED                   = "FIXED";
  static final String REPROCESSED             = "REPROCESSED";
  
  /* Field names, initialised to defaults defined above. */
  private String timestampColName             = TIMESTAMP;
  private String exceptionClassColName        = EXCEPTION_CLASS;
  private String exceptionMessageColName      = EXCEPTION_MESSAGE;
  private String causeExceptionClassColName   = CAUSE_EXCEPTION_CLASS;
  private String causeExceptionMessageColName = CAUSE_EXCEPTION_MESSAGE;
  private String stackTraceColName            = STACK_TRACE;  
  private String adaptorColName               = ADAPTOR_NAME;
  private String componentColName             = COMPONENT;
  private String dataTypeColName              = DATA_TYPE;
  private String dataColName                  = DATA;
  private String metadataColName              = METADATA;
  private String fixedColName                 = FIXED;
  private String reprocessedColName           = REPROCESSED;
  private String threadNameColName            = THREAD_NAME;
  
  /* 
   * Default values of FIXED and REPROCESSED column are set to "false" (Strings).
   * Some databases such as Hypersonic will automatically convert them to boolean values,
   * but others such as Postgres need to have them set directly as booleans.
   */
  private Object FIXED_COLUMN_DEFAUL_VALUE        = new Boolean(false);
  private Object REPROCESSED_COLUMN_DEFAULT_VALUE = new Boolean(false);
  
  /* 
   * Values of FIXED and PREPROCESSED columns set to their defaults. Can be overwritten
   * via setters.
   */
  private Object fixedColumnValue       = FIXED_COLUMN_DEFAUL_VALUE;
  private Object reprocessedColumnValue = REPROCESSED_COLUMN_DEFAULT_VALUE;
  
  /* Optional property allowing to retrieve an adaptor's name */
  private IComponent adaptor;
  
  /** 
   * the format the exception timestamp will have in the ordered map
   * default to the java.util.Date().toString() value
   */
  private SimpleDateFormat timestampFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");

  private boolean convertPayloadToString = true;
  
  /**
   * Default constructor.
   */
  public ExceptionToOrderedMapConvertor() {
    super();
  }

  /**
   * Constructor. 
   * 
   * @param id the component id.
   */
  public ExceptionToOrderedMapConvertor(String id) {
    super(id);
  }
  
  /**
   * Converts the <code>record</code> into an <code>IOrderedMap</code> .
   *
   * @param record Object which should be a MessageException instance
   * @return an IOrderedMap representation of the MessageException contents
   */
  protected Object convert(Object record) {
    if(! (record instanceof MessageException)){
      log.error("Exception handling error.");   
      return null;
    }
    MessageException messageException = (MessageException) record;
    IOrderedMap map = new OrderedHashMap();
    map.put(timestampColName, timestampFormat.format(new java.util.Date()));
    map.put(exceptionClassColName, messageException.getException().getClass().getName());
    map.put(exceptionMessageColName, messageException.getException().getMessage());
    
    Throwable cause = messageException.getException().getCause();
    if(cause!=null){
  	map.put(causeExceptionClassColName, cause.getClass().getName());
      map.put(causeExceptionMessageColName, cause.getMessage());
    }
    else{
  	map.put(causeExceptionClassColName, noCauseException);
      map.put(causeExceptionMessageColName, noCauseException);
    }
    
    String stackTrace = getStackTraceAsString(messageException.getException());
    map.put(stackTraceColName, stackTrace);
    
    String adaptorName = null==adaptor ? unknownAdaptorName : adaptor.getId();
    map.put(adaptorColName, adaptorName);
    String component = messageException.getOriginatingModule();
    map.put(componentColName, null==component ? unknownComponentName : component);
            
    Object data = messageException.getData();
    String dataType = null;  
    if(data!=null){
      dataType = data.getClass().getName();
    }
    map.put(dataTypeColName, dataType); 
    
    /* data has to be String */
    if(data!=null && !(data instanceof String) && convertPayloadToString){
      data = data.toString();
    }
    map.put(dataColName, data);
    
    /* metadata has to be Map */
    Object metadata = messageException.getMetadata();
    if(metadata!=null){
      metadata = metadata.toString();
    }
    map.put(metadataColName, metadata);
 
    map.put(fixedColName, fixedColumnValue);
    map.put(reprocessedColName, reprocessedColumnValue);
    map.put(threadNameColName, messageException.getOriginatingThreadName());
    messageException.getException().getCause();
    return map;       
  }
  
  /**
   * Converts stack trace of an exception (and its root cause) to a String.
   * @param exception the processed exception.
   * @return stack trace as a String.
   */
  private String getStackTraceAsString(Exception exception){
    StringBuffer stackTraceBuf = new StringBuffer();
    StackTraceElement [] stackTrace = exception.getStackTrace();
    for(int i=0; i<stackTrace.length; i++){
    stackTraceBuf.append(stackTrace[i]);
    stackTraceBuf.append("\n"); 
    }
    /* Append cause exception stack trace */
    Throwable cause = exception.getCause();
    if(cause!=null){
      stackTraceBuf.append("\n\n");
      stackTrace = cause.getStackTrace();
      for(int i=0; i<stackTrace.length; i++){
        stackTraceBuf.append(stackTrace[i]);
        stackTraceBuf.append("\n");   
      }
    }
    return stackTraceBuf.toString();
  }
  
  public void setTimestampFormat(String timestampFormat) { 
    this.timestampFormat = new SimpleDateFormat(timestampFormat);
  }

  /**
   * Overrides the default component column name.
   * 
   * @param componentColName the component column name.
   */
  public void setComponentColName(String componentColName) {
    this.componentColName = componentColName;
  }

  /**
   * Overrides the default data column name.
   * 
   * @param dataColName the data column name.
   */
  public void setDataColName(String dataColName) {
    this.dataColName = dataColName;
  }
  
  /**
   * Overrides the default metadata column name.
   * 
   * @param metadataColName the data column name.
   */
  public void setMetadataColName(String metadataColName) {
    this.metadataColName = metadataColName;
  }

  /**
   * Overrides the default exception class column name.
   * 
   * @param exceptionClassColName the exception class column name.
   */
  public void setExceptionClassColName(String exceptionClassColName) {
    this.exceptionClassColName = exceptionClassColName;
  }

  /**
   * Overrides the default 'fixed' column name.
   * 
   * @param fixedColName the 'fixed' column name.
   */
  public void setFixedColName(String fixedColName) {
    this.fixedColName = fixedColName;
  }

  /**
   * Overrides the default 'reprocessed' column name.
   * 
   * @param reprocessedColName the 'reprocessed' column name.
   */
  public void setReprocessedColName(String reprocessedColName) {
    this.reprocessedColName = reprocessedColName;
  }

  /**
   * Overrides the default timestamp column name.
   * 
   * @param timestampColName the timestamp column name.
   */
  public void setTimestampColName(String timestampColName) {
    this.timestampColName = timestampColName;
  }

  /**
   * Optional property that allows to retrieve the name of the adaptor. 
   * 
   * @param adaptor
   */
  public void setAdaptor(IComponent adaptor) {
    this.adaptor = adaptor;
  }

  /**
   * @return if true then message payload will always be converted to a String. Otherwise it'll
   *         be stored in the ordered map in its original format.
   */
  public boolean isConvertPayloadToString() {
    return convertPayloadToString;
  }

  /**
   * Allows to overwrite the default value of <code>convertPayloadToString</code>.
   * 
   * @param convertPayloadToString if true then message payload will always be converted to a String. 
   *        Otherwise it'll be stored in the ordered map in its original format.
   */
  public void setConvertPayloadToString(boolean convertPayloadToString) {
    this.convertPayloadToString = convertPayloadToString;
  }

  /**
   * Allows to overwrite the default message when no cause exception was found.
   * 
   * @param noCauseException new message
   */
  public void setNoCauseException(String noCauseException) {
    this.noCauseException = noCauseException;
  }

  /**
   * Allows to overwrite the default message when the name of the adaptor could not be determined.
   * 
   * @param unknownAdaptorName new message
   */
  public void setUnknownAdaptorName(String unknownAdaptorName) {
    this.unknownAdaptorName = unknownAdaptorName;
  }

  /**
   * Allows to overwrite the default message when the name of the component that threw the 
   * exception could not be determined.
   * 
   * @param unknownComponentName new message
   */
  public void setUnknownComponentName(String unknownComponentName) {
    this.unknownComponentName = unknownComponentName;
  }

  /**
   * Default values of FIXED and REPROCESSED column are set to "false" (Strings).
   * Some databases such as Hypersonic will automatically convert them to boolean values,
   * but others such as Postgres need to have them set directly as booleans.
   */
  public void setFixedColumnValue(Object fixedColumnValue) {
    this.fixedColumnValue = fixedColumnValue;
  }

  /** 
   * Default values of FIXED and REPROCESSED column are set to "false" (Strings).
   * Some databases such as Hypersonic will automatically convert them to boolean values,
   * but others such as Postgres need to have them set directly as booleans.
   */
  public void setReprocessedColumnValue(Object reprocessedColumnValue) {
    this.reprocessedColumnValue = reprocessedColumnValue;
  }
  
}
