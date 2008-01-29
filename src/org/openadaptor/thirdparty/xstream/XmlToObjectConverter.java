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

package org.openadaptor.thirdparty.xstream;

import org.dom4j.Document;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;

/**
 * Converts incoming xml (string or dom4j document) to a POJO using XStream.
 * @author perryj
 *
 */
public class XmlToObjectConverter extends XStreamConverter implements IDataProcessor {

  public XmlToObjectConverter() {
    super();
  }

  public XmlToObjectConverter(String id) {
    super(id);
  }

  /**
   * converts xml data to a POJO. If data is dom4j document then autoconverts to
   * XML string, otherwsie calls toString() on data.
   * 
   * @throws ProcessingException if data is neither a string nor a dom4j document
   */
  public Object[] process(Object data) {
    if (data instanceof Document) {
      return new Object[] {toObject(((Document)data).asXML())};
    } else {
      return new Object[] {toObject(data.toString())};
    }
  }
}
