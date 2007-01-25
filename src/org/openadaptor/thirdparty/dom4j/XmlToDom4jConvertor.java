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

package org.openadaptor.thirdparty.dom4j;

import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.RecordException;

/**
 * Converts XML documents (as Strings) to Dom4j Documents
 * 
 * @author Eddy Higgins
 */
public class XmlToDom4jConvertor extends AbstractConvertor {
  //private static final Log log = LogFactory.getLog(XmlToDom4jConvertorProcessor.class);

  /**
   * Convert an incoming record into a dom4j Document. Valid input records may contain an XML String,a Dom4j Document,
   * or null. If not, a RecordFormatException will be thrown. Note that if a null input record is supplied, null will be
   * returned. The real work is done by Dom4jUtils.getDocument()
   * 
   * @param record
   *          containing an XML Document (dom4j) or a String containing an XML Document
   * 
   * @return Dom4J Document representing the XML
   * 
   * @throws RecordException
   *           if conversion fails
   * 
   * @see Dom4jUtils
   */
  protected Object convert(Object record) throws RecordException {
    return (Dom4jUtils.getDocument(record));
  }
}
