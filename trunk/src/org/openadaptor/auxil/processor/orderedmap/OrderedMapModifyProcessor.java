/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.processor.orderedmap;

import java.util.List;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedMapUtils;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * An Abstract Processor which applies a transform to an IOrderedMap record.
 * 
 * @author OA3 Core Team
 */
public abstract class OrderedMapModifyProcessor extends Component implements IDataProcessor {

  protected Object attribute;

  // Bean Implementation
  public Object getAttribute() {
    return attribute;
  }

  public void setAttribute(Object attribute) {
    this.attribute = attribute;
  }

  // End Bean Implementation

  /** Override to apply modification */
  protected abstract IOrderedMap applyToMap(IOrderedMap map) throws RecordException;

  /**
   * Process a single record, which must be an instance of an IOrderedMap. Final because this is where the IOrderedMap
   * checking is done. If you don't want this, implement IRecordProcessor directly instead.
   * 
   * @param record
   * @return the array of records which result from processing the incoming record.
   * @throws RecordException
   */
  public final Object[] process(Object data) {
    IOrderedMap inMap = OrderedMapUtils.extractOrderedMap(data);
    IOrderedMap modifiedMap = applyToMap((IOrderedMap) inMap.clone());
    return new Object[] { modifiedMap };
  }

  public void validate(List exceptions) {
  }

  public void reset(Object context) {
  }
}
