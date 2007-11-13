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
package org.openadaptor.auxil.processor;

import java.util.Map;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor;
import org.openadaptor.core.IEnhancementProcessor;

/**
 * A generic enhancement processor. Attempt at a 'generic' implementation
 * of prepering parameters and enhancing input data.
 * 
 * Written to eventually replace the {@link JNDIEnhancementProcessor}.
 * 
 * @author Kris Lachor
 * @since Post 3.3
 * ToDo: javadocs
 * ToDo: unit tests (prepareParameters - OK, enhance not tested), system tests
 */
public class GenericEnhancementProcessor extends AbstractEnhancementProcessor {
  
  /**
   * If input is not an IOrderedMap, returns an empty list of parameters. 
   * If input is an IOrderedMap, checks specific field names or field indexes
   * have been requested (by setting properties on {@link AbstractEnhancementProcessor}.
   * If no specific fields requested, return the whole input, otherwise returns
   * an IOrderedMap with specified fields.
   * 
   * @see IEnhancementProcessor#prepareParameters(Object)
   */
  public IOrderedMap prepareParameters(Object input) {
    IOrderedMap params = new OrderedHashMap();
    if(input instanceof IOrderedMap){
      IOrderedMap inputMap = (IOrderedMap) input;
      String [] paramsFieldNames = getParamsFieldNames();
      if(paramsFieldNames != null){
        for(int i=0; i<paramsFieldNames.length; i++){
          Object value = inputMap.get(paramsFieldNames[i]);
          params.put(paramsFieldNames[i], value);  
        }
      }else{
        params = inputMap;
      }
    }
    return params;
  }
  
  /**
   * Enhances input data with extra data from the reader.
   * If reader did not return any extra data, original data is not modified in any way.
   * 
   * @see IEnhancementProcessor#enhance(Object, Object[]) 
   */
  public Object [] enhance(Object input, Object[] additionalData) {
    Object [] result = null;
    
    /* Retun original input if the reader didn't find anything */
    if(null == additionalData){
       result = new Object[]{input};
    }
    /* or add enhancement data as next element to input */
    else{    
         result = new Object[additionalData.length + 1];
         result[0] = input;
         for(int i=1; i<=additionalData.length; i++){
           result[i]=additionalData[i-1];
         }
         
         if(input instanceof IOrderedMap && additionalData.length == 1){
           Object additionalDataObj = additionalData[0];
           if(additionalDataObj instanceof IOrderedMap){
             Map additionalDataMap = (Map) additionalDataObj;
             ((Map)input).putAll(additionalDataMap);
             result = new Object[]{input};
           }
         }    
    }
    return result;
  }
  
}
