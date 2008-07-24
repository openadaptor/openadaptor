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

import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Unit test for the {@link XmlToW3CDocumentConvertor}.
 *
 * @author cawthorng
 */
public class XmlToW3CDocumentConvertorTest extends TestCase {

  private XmlToW3CDocumentConvertor convertor = new XmlToW3CDocumentConvertor();
  
  public void testConvertorRejectsNonStringInput() throws Exception {
    try {
      convertor.convert(new Integer(1));
      Assert.fail("convert() should have thrown a RecordFormatException");
    } catch (RecordFormatException e) {
      assertTrue(e.getMessage().contains("Record is not an XML string"));
    }
  }
  
  public void testConvertorRejectsPoorlyFormedXML() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><row></column>";
    
    try {
      convertor.convert(xml);
      Assert.fail("convert() should have thrown a RecordException");
    } catch (RecordException e) {
      assertTrue(e.getMessage().contains("Could not parse document"));
    }
  }
  
  public void testConvertorConvertsWellFormedXML() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><row>Dummy</row>";

    Object record = convertor.convert(xml);
    assertTrue(record instanceof Document);
      
    Document dom = (Document) record;
    assertEquals(dom.getChildNodes().getLength(), 1);
    
    Element element = dom.getDocumentElement();
    assertEquals(element.getNodeName(), "row");
    assertEquals(element.getTextContent(), "Dummy");
  }
}
