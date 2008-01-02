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
import org.openadaptor.core.IEnrichmentProcessor;

/**
 * A enrichment processor with generic implementations of prepering parameters and 
 * enhancing input data. 
 * 
 * Written to replace the (depricated) {@link JNDIEnhancementProcessor}.
 * 
 * @author Kris Lachor
 * @since Post 3.3
 */
public class GenericEnrichmentProcessor extends AbstractEnrichmentProcessor {
  
  /**
   * If input is not an IOrderedMap, returns an empty list of parameters. 
   * If input is an IOrderedMap, checks specific field names or field indexes
   * have been requested (by setting properties on {@link AbstractEnrichmentProcessor}.
   * If no specific fields were requested, returns the original input, otherwise returns
   * an IOrderedMap with specified fields.
   * 
   * @see IEnrichmentProcessor#prepareParameters(Object)
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
   * Enriches input data with extra data from the reader.
   * If reader did not return any extra data, original data is not modified in any way.
   * 
   * @see IEnrichmentProcessor#enrich(Object, Object[]) 
   */
  public Object [] enrich(Object input, Object[] enrichmentData) {
    Object [] result = null;
    
    /* Retun original input if the reader didn't find anything */
    if(null==enrichmentData || enrichmentData.length==0){
       result = new Object[]{input};
    }
    /* or add enrichment data as next element to input */
    else{    
      if(input instanceof OrderedHashMap){
        result = new Object[enrichmentData.length];
        for(int i=0; i<enrichmentData.length; i++){
          result[i]=((OrderedHashMap)input).clone();   
          ((Map)result[i]).putAll((Map)enrichmentData[i]);
        }
      }
      else{
        result = enrichmentData; 
      }  
    }
    return result;
  }
  
}
