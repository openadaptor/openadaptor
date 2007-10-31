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


import java.util.HashMap;
import java.util.Map;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.auxil.processor.AbstractEnhancementProcessor;


/**
 * Skeleton of the new JNDI enhancement processor, eventually meant to 
 * replace the existing {@link JNDIEnhancementProcessor}.
 * 
 * Early draft version, do not use.
 * 
 * @author Kris Lachor
 * @TODO javadocs
 * @TODO unit tests, system tests
 */
public class NewJNDIEnhancementProcessor extends AbstractEnhancementProcessor {

  
  public IOrderedMap prepareParameters(Object input) {
    IOrderedMap params = new OrderedHashMap();
    if(input instanceof IOrderedMap){
      IOrderedMap inputMap = (IOrderedMap) input;
      if(getCommaSeparatedFieldNames() != null){
        Object value = inputMap.get(getCommaSeparatedFieldNames());
        params.put(getCommaSeparatedFieldNames(), value);
      }else{
        params = inputMap;
      }
    }
    return params;
  }
  
  /**
   * @TODO this needs to be an abstract method - no generic way of combining input data with additional    
   */
  public Object [] enhance(Object input, Object[] additionalData) {
    Object [] result = null;
    if(null == additionalData){
      result = new Object[]{input};
    }else{
     
//      if(input instanceof IOrderedMap ){
//        result = new Object[]{input};
//        for(int i=1; i<=additionalData.length; i++){
//          IOrderedMap addEl = (IOrderedMap)additionalData[i];
//          ((IOrderedMap)input).put(addEl., value)
//        }
//      }else{
        result = new Object[additionalData.length + 1];
        result[0] = input;
        for(int i=1; i<=additionalData.length; i++){
          result[i]=additionalData[i-1];
        }
      }
      
     
//    }
    return result;
  }
   
}
