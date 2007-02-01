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

package org.openadaptor.auxil.convertor.simplerecord;

import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.auxil.simplerecord.ISimpleRecordAccessor;
import org.openadaptor.core.exception.RecordFormatException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Use an accessor to wrap incoming data in an ISimpleRecord view.
 */
public class ToSimpleRecordConvertor extends AbstractConvertor {

  private static final Log log = LogFactory.getLog(ToSimpleRecordConvertor.class);

  private ISimpleRecordAccessor simpleRecordAccessor;

  /**
   * Performs the the actual conversion. Returns the successfully converted record or throw a RecordException.
   *
   * @param data
   * @return Converted Record
   * @throws org.openadaptor.core.exception.RecordException
   *          if there was a problem converting the record
   */
  protected Object convert(Object data) {
    ISimpleRecord simpleRecord = null;
    if (simpleRecordAccessor != null) {
      simpleRecord = simpleRecordAccessor.asSimpleRecord(data);
    } else {
      if (data instanceof ISimpleRecord) { //Easy. We're done.
        simpleRecord = (ISimpleRecord) data;
      } else {
        log.warn("Incoming record is not an ISimpleRecord - perhaps a SimpleRecordAccessor must be specified?");
        throw new RecordFormatException("Expected ISimpleRecord . Got [" + data.getClass().getName() + "]");
      }
    }
    return simpleRecord;
  }

  // Bean

  public ISimpleRecordAccessor getSimpleRecordAccessor() {
    return simpleRecordAccessor;
  }

  public void setSimpleRecordAccessor(ISimpleRecordAccessor simpleRecordAccessor) {
    this.simpleRecordAccessor = simpleRecordAccessor;
  }
}
