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
package org.openadaptor.auxil.processor.jndi;

import java.util.List;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.processor.orderedmap.AbstractOrderedMapProcessor;
import org.openadaptor.core.IEnhancementProcessor;
import org.openadaptor.core.IEnhancementReadConnector;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Skeleton of the new JNDI enhancement processor, eventually meant to 
 * replace the existing {@link JNDIEnhancementProcessor}.
 * 
 * Early draft version, do not use.
 * 
 * @author Kris Lachor
 * @TODO rename to OrderedMapEnhancementProcessor? 
 */
public class NewJNDIEnhancementProcessor extends AbstractOrderedMapProcessor implements IEnhancementProcessor {

  IEnhancementReadConnector readConnector;
  
  /**
   * @see org.openadaptor.core.IEnhancementProcessor#getReadConnector()
   */
  public IEnhancementReadConnector getReadConnector() {
    return readConnector;
  }

  public void setReadConnector(IEnhancementReadConnector readConnector) {
    this.readConnector = readConnector;
  }

  /**
   * Validates: 
   * - is read connector set
   * - does read connector itself validates
   * 
   * @see org.openadaptor.core.IDataProcessor#validate(java.util.List)
   */
  public void validate(List exceptions) {
    if(readConnector==null){
      throw new ValidationException("Set an IReadConnector", this);
    }
    readConnector.validate(exceptions);
  }


  /**
   * @see AbstractOrderedMapProcessor#processOrderedMap(IOrderedMap)
   * @TODO remove downcasting
   * @TODO fix the timeout
   */
  public Object[] processOrderedMap(IOrderedMap orderedMap) throws RecordException {
//    return ((NewJNDIReadConnector) readConnector).processOrderedMap(orderedMap);
    Object [] additionalData = readConnector.next(orderedMap, 1000);
    return  enhance(orderedMap, additionalData);
  }

  
  public Object [] enhance(Object input, Object[] additionalData) {
    Object [] result = null;
    if(null == additionalData){
      result = new Object[]{input};
    }else{
      result = new Object[additionalData.length + 1];
      result[0] = input;
      for(int i=1; i<additionalData.length; i++){
        result[i]=additionalData[i-1];
      }
    }
    return result;
  }

}
