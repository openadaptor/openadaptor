/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.auxil.processor.GenericEnrichmentProcessor;
import org.openadaptor.core.exception.ValidationException;

/**
 * <p>
 * This class uses a different strategy for merging the enrichment data into the outgoing record,
 * but is otherwise the same as the <code>GenericEnrichmentProcessor</code>.
 * </p>
 * <p>
 * The strategy has three key differences:
 * </p>
 * <ul>
 *   <li>
 *     Only two fields from each row of the enrichment data are applied
 *     (the ones called <code>keyName</code> and <code>valueName</code>).
 *   </li>
 *   <li>
 *     The <b>value</b> in the enrichment data row of the <code>keyName</code> field
 *     is used as the <b>name</b> of the field in the outgoing record into which
 *     to write the <b>value</b> of the <code>keyValue</code> field from the same
 *     enrichment data row.
 *   </li>
 *   <li>
 *     If there are multiple rows of enrichment data then they are all applied
 *     to the same outgoing record (i.e. they are merged into the same
 *     outgoing record whereas the <code>GenericEnhancementProcessor</code>
 *     would emit one enriched record for each row of enrichment data).
 *   </li>
 * </ul>
 * <p>
 * It expects the enrichment data (for a single incoming record) to consist of a
 * <code>Map[]</code> (e.g. a SQL result set).
 * </p>
 * <p>
 * Each element of this enrichment data array will be a Map which includes the
 * two keys specified by the <code>keyName</code> and <code>valueName</code> properties
 * (if a key is missing it will be treated as if its value was <code>null</code>).
 * </p>
 * <p>
 * For example if your enrichment data was the following:
 * </p>
 * <table>
 *   <tr>
 *     <th>aspect</th>
 *     <th>data</th>
 *     <th><i>other field names...</i></th>
 *   </tr>
 *   <tr>
 *     <tr>manager</tr>
 *     <tr>Helen</tr>
 *     <th><i>other values...</i></th>
 *   </tr>
 *   <tr>
 *     <tr>secretary</tr>
 *     <tr>Tom</tr>
 *     <th><i>other values...</i></th>
 *   </tr>
 *   <tr>
 *     <tr>location</tr>
 *     <tr>London</tr>
 *     <th><i>other values...</i></th>
 *   </tr>
 * </table>
 * <p>
 * and you set:
 * </p>
 * <pre>
 *   <property name="keyName" value="aspect" />
 *   <property name="keyValue" value="data" />
 * </pre>
 * <p>
 * then your single outgoing record would look like the following had been executed:
 * </p>
 * <pre>
 *   enrichedRecord = incomingRecord.clone();
 *   enrichedRecord.put("manager","Helen");
 *   enrichedRecord.put("secretary","Tom");
 *   enrichedRecord.put("location","London");
 *   outgoingRecords = new Object[] { enrichedRecord };
 * <pre>
 *
 * @author Andrew Shire
 * @see org.openadaptor.auxil.processor.GenericEnrichmentProcessor
 */
public class ValueMapEnrichmentProcessor extends GenericEnrichmentProcessor
{
  private static final Log log = LogFactory.getLog(ValueMapEnrichmentProcessor.class);
  
  // bean properties:
  protected String keyName = null;
  protected String valueName = null;

  //BEGIN Bean getters/setters
  public void setKeyName(String name) { keyName = name; }
  public String getKeyName() { return keyName; }

  public void setValueName(String name) { valueName = name; }
  public String getValueName() { return valueName; }
  //END   Bean getters/setters
  
  /**
   * Checks that the mandatory properties have been set
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    super.validate(exceptions);
    if (keyName == null) {
      exceptions.add(new ValidationException("keyName property is not set", this));
    }
    if (valueName == null) {
      exceptions.add(new ValidationException("valueName property is not set", this));
    }
  }
  
  
  /**
   * Enriches input data with extra data from the reader.
   * If reader did not return any extra data, original data is not modified in any way.
   * 
   * @see org.openadaptor.core.IEnrichmentProcessor#enrich(Object, Object[]) 
   */
  public Object [] enrich(Object input, Object[] enrichmentData) {
    Object [] result = null;
    
    /* Return original input if the reader didn't find anything */
    if(null==enrichmentData || enrichmentData.length==0){
       result = new Object[]{input};
    }
    /* or add enrichment data as next element to input */
    else{    
      if(input instanceof OrderedHashMap){
        result = new Object[] { ((OrderedHashMap)input).clone() };
        for(int i=0; i<enrichmentData.length; i++){
          Object key   = ((Map)enrichmentData[i]).get(getKeyName());
          Object value = ((Map)enrichmentData[i]).get(getValueName());
          // log.debug(getKeyName() + " = " + key + ";  " + getValueName() + " = " + value);
          if (! ((Map)result[0]).containsKey(key)) {
            ((Map)result[0]).put(key, value);
          } else {
            Object existingValue = ((Map)result[0]).get(key);
            if (existingValue instanceof Object[]) {
              Object[] oldArray = (Object[])existingValue;
              Object[] newArray = new Object[oldArray.length + 1];
              for (int k=0; k<oldArray.length; k++) {
                newArray[k] = oldArray[k];
              }
              newArray[oldArray.length] = value;
              ((Map)result[0]).put(key, newArray);
            } else {
              ((Map)result[0]).put(key, new Object[] { existingValue, value });
            }
          }
        }
      }
      else{
        result = enrichmentData;
      }
    }
    return result;
  }
}