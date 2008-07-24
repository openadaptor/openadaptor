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
package org.openadaptor.thirdparty.w3c;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.w3c.dom.Document;

/**
 * Converts XML strings to W3C {@link Document}s.
 *
 * @author cawthorng
 */
public class XmlToW3CDocumentConvertor extends AbstractConvertor {

  private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

  /* (non-Javadoc)
   * @see org.openadaptor.auxil.convertor.AbstractConvertor#convert(java.lang.Object)
   */
  protected Object convert(Object record) throws RecordException {
    if (!(record instanceof String)) {
      throw new RecordFormatException("Record is not an XML string. Record: " + record);
    }

    Document dom = null;
    
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      dom = db.parse(new ByteArrayInputStream(((String) record).getBytes()));
    } catch (Exception e) {
      throw new RecordException("Could not parse document", e);
    }
    
    return dom;
  }
}