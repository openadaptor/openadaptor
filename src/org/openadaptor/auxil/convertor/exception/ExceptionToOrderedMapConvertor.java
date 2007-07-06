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
 */
public class ExceptionToOrderedMapConvertor extends AbstractConvertor {
  
  private static final Log log = LogFactory.getLog(ExceptionToOrderedMapConvertor.class);
  
  /* JDBC mapping constants */
  private static final String TIMESTAMP = "timestamp";
  private static final String EXCEPTION_CLASS = "exceptionClass";
  private static final String COMPONENT = "originatingComponent";
  private static final String DATA = "data";

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
   * @return 
   */
  protected Object convert(Object record) {
      if(! (record instanceof MessageException)){
        log.error("Exception handling error.");   
        return null;
      }
      MessageException messageException = (MessageException) record;
      IOrderedMap map = new OrderedHashMap();
      map.put(TIMESTAMP, new java.util.Date().toString());
      map.put(EXCEPTION_CLASS, messageException.getException().getClass().getName());
      String component = messageException.getOriginatingModule();
      map.put(COMPONENT, null==component ? "Unknown" : component);
      map.put(DATA, messageException.getData());
      return map;       
  }
}