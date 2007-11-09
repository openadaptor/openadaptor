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

package org.openadaptor.auxil.convertor.exception;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.MessageException;

/**
 * Converts a MessageException to an ordered map.
 * 
 * @author Kris Lachor
 * TODO java doc
 */
public class ExceptionToOrderedMapConvertor extends AbstractConvertor {
  
  private static final Log log = LogFactory.getLog(ExceptionToOrderedMapConvertor.class);
  
  /* Default field names */
  
  static final String TIMESTAMP = "TIMESTAMP";
  
  static final String EXCEPTION_CLASS = "EXCEPTION_CLASS_NAME";
  
  static final String COMPONENT = "ORIGINATING_COMPONENT";
  
  static final String DATA = "DATA";
  
  static final String FIXED = "FIXED";
  
  static final String REPROCESSED = "REPROCESSED";
  
  
  /* Field names, initialised to defaults */
  
  private String timestampColName = TIMESTAMP;
  
  private String exceptionClassColName = EXCEPTION_CLASS;
  
  private String componentColName = COMPONENT;
  
  private String dataColName = DATA;
  
  private String fixedColName = FIXED;
  
  private String reprocessedColName = REPROCESSED;
  
  
  // the format the exception timestamp will have in the ordered map
  // default to the java.util.Date().toString() value
  private SimpleDateFormat timestampFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");

  public ExceptionToOrderedMapConvertor() {
    super();
  }

  public ExceptionToOrderedMapConvertor(String id) {
    super(id);
  }
  
  /**
   * Converts the <code>record</code> into an <code>IOrderedMap</code> .
   *
   * @param a record, expected to be a MessageException.
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
      String component = messageException.getOriginatingModule();
      map.put(componentColName, null==component ? "Unknown" : component);
      map.put(dataColName, messageException.getData());
      map.put(fixedColName, "false");
      map.put(reprocessedColName, "false");
      return map;       
  }
  
  public void setTimestampFormat(String timestampFormat) { 
    this.timestampFormat = new SimpleDateFormat(timestampFormat);
  }

  public void setComponentColName(String componentColName) {
    this.componentColName = componentColName;
  }

  public void setDataColName(String dataColName) {
    this.dataColName = dataColName;
  }

  public void setExceptionClassColName(String exceptionClassColName) {
    this.exceptionClassColName = exceptionClassColName;
  }

  public void setFixedColName(String fixedColName) {
    this.fixedColName = fixedColName;
  }

  public void setReprocessedColName(String reprocessedColName) {
    this.reprocessedColName = reprocessedColName;
  }

  public void setTimestampColName(String timestampColName) {
    this.timestampColName = timestampColName;
  }

}