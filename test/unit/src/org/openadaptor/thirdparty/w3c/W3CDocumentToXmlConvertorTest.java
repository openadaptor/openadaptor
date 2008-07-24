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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openadaptor.core.exception.RecordFormatException;
import org.w3c.dom.Document;

/**
 * Unit test for the {@link W3CDocumentToXmlConvertor}.
 * 
 * @author cawthorng
 */
public class W3CDocumentToXmlConvertorTest extends TestCase {

  private W3CDocumentToXmlConvertor toXmlConverter = new W3CDocumentToXmlConvertor();
  private XmlToW3CDocumentConvertor toW3CConverter = new XmlToW3CDocumentConvertor();

  private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record><row>Dummy</row></record>";
  private static final String PRETTY_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record>\r\n   <row>Dummy</row>\r\n</record>\r\n";

  public void testConvertorRejectsNonDocumentInput() throws Exception {
    try {
      toXmlConverter.convert(new Integer(1));
      Assert.fail("convert() should have thrown a RecordFormatException");
    } catch (RecordFormatException e) {
      assertTrue(e.getMessage().contains("Record is not an org.w3c.dom.Document"));
    }
  }

  public void testConvertorConvertsDocumentToFlatXML() throws Exception {
    Document dom = (Document) toW3CConverter.convert(XML);
    Object record = toXmlConverter.convert(dom);
    assertTrue(record instanceof String);
    String convertedXml = (String) record;
    assertEquals(XML, convertedXml);
  }

  public void testConvertorConvertsDocumentToPrettyPrintXML() throws Exception {
    Document dom = (Document) toW3CConverter.convert(XML);
    toXmlConverter.setPrettyPrint(true);
    String convertedXml = (String) toXmlConverter.convert(dom);
    assertEquals(PRETTY_XML, convertedXml);
  }
}
