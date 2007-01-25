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

package org.openadaptor.auxil.simplerecord;

import org.openadaptor.core.exception.RecordException;

/**
 * General Interface for helper (facade) classes which provide access to (<code>Object</code>) Records, as
 * ISimpleRecord implementations.
 * 
 * @author Eddy Higgins
 */
public interface ISimpleRecordAccessor {
  /**
   * Generate an <code>ISimpleRecord</code> representation of the supplied record <code>Object</code>.
   * <p>
   * Note: To get back the underlying Record object, use ISimpleRecord.getRecord()
   * 
   * @param record
   *          the object to be accessed as an ISimpleRecord
   * @return a view of the record as an ISimpleRecord.
   * @throws RecordException
   *           if the supplied record cannot be viewed as an <code>ISimpleRecord</code>
   */
  public ISimpleRecord asSimpleRecord(Object record) throws RecordException;

  /**
   * Return the type of the record being presented as an ISimpleRecord.
   * @return  Class
   */
  public Class getUnderlyingType();
}
