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

package org.openadaptor.core.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.writer.JDBCWriteConnector;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;

/**
 * Extends JDBCWriteConnector by first converting the records into 
 * IOrderedMap. Assumes the data are 
 * 
 * @author Kris Lachor
 */
public class HospitalDBWriter extends JDBCWriteConnector {
  
  private static final Log log = LogFactory.getLog(HospitalDBWriter.class);
  
  /* JDBC mapping constants */
  private static final String TIMESTAMP = "timestamp";
  private static final String EXCEPTION_CLASS = "exceptionClass";
  private static final String COMPONENT = "originatingComponent";
  private static final String DATA = "data";
  
  /**
   * Converts elements of <code>data</code> into an <code>IOrderedMap</code> 
   * and delegates to {@link JDBCWriteConnector#deliver(Object[])}.
   *
   * @param data the source of the data for the prepared statement as an 
   * @return null
   * @see JDBCWriteConnector#deliver(Object[])
   */
  public Object deliver(Object[] data) throws ComponentException {
     Object [] convertedData = new Object[data.length];
     for(int i=0; i<data.length; i++){
       if(! (data[i] instanceof MessageException)){
         log.error("Exception handling error.");   
       }
       MessageException messageException = (MessageException) data[i];
       IOrderedMap map = new OrderedHashMap();
       map.put(TIMESTAMP, new java.util.Date().toString());
       map.put(EXCEPTION_CLASS, messageException.getException().getClass().getName());
       map.put(COMPONENT, messageException.getOriginatingModule());
       map.put(DATA, messageException.getData());
       convertedData[i]=map;       
     }
     return super.deliver(convertedData);
  }


}
