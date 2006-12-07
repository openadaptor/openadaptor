/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.auxil.convertor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.oa3.auxil.convertor.xml.OrderedMapToXmlConvertor;
import org.oa3.auxil.convertor.xml.XmlToOrderedMapConvertor;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;
import org.oa3.util.ResourceUtil;

/**
 * This tests the XmlConvertors implementation.
 * 
 * @author OA3 Core Team
 */
public class XmlConvertorTestCase extends TestCase {

  private String xmlString;

  private Document xmlDocument;

  private IOrderedMap om;

  private XmlToOrderedMapConvertor xml2om;

  private OrderedMapToXmlConvertor om2xml;

  protected void setUp() throws Exception {
    super.setUp();
    xmlString = ResourceUtil.readFileContents(this, "test.xml");
    xmlDocument = generateXmlDocument(xmlString);
    om = generateTestOrderedMap();
    xml2om = new XmlToOrderedMapConvertor();
    om2xml = new OrderedMapToXmlConvertor();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private IOrderedMap generateTestOrderedMap() {
    IOrderedMap root = new OrderedHashMap();
    IOrderedMap parent = new OrderedHashMap();
    IOrderedMap child = new OrderedHashMap();
    IOrderedMap child2 = new OrderedHashMap();
    child.put("Y", "Y_VAL");
    child.put("Z", "Z_VAL");
    parent.put("B", child);
    child = new OrderedHashMap();
    child.put("Y", "Y_VAL");
    child.put("Z", "Z_VAL");

    child2 = new OrderedHashMap();
    child2.put("Y", "Y_VAL2");
    child2.put("Z", "Z_VAL2");
    parent.put("C", new Object[] { child, child2 });

    root.put("A", parent);
    return root;
  }

  private static Document generateXmlDocument(String xml) {
    Document document = null;
    try {
      return (DocumentHelper.parseText(xml));
    } catch (DocumentException de) {
      fail("Unit test problem - " + de.toString());
    }
    return document;
  }

  public void testTwoWayConversion() {
    try {
      // IOrderedMap map=(IOrderedMap)xml2om.convert(xmlString);
      Object[] resultArray = xml2om.process(xmlString);
      assertTrue(resultArray.length == 1);
      IOrderedMap map = (IOrderedMap) resultArray[0];
      System.out.println(map);
      // String xml=(String)om2xml.convert(map);
      resultArray = om2xml.process(map);
      assertTrue(resultArray.length == 1);
      String xml = (String) resultArray[0];
      System.out.println(xml);
      assertEquals(xmlString, xml);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testXmlToOrderedMapConversion() {
    try {
      // IOrderedMap map=(IOrderedMap)xml2om.convert(xmlString);
      Object[] resultArray = xml2om.process(xmlString);
      assertTrue(resultArray.length == 1);
      IOrderedMap map = (IOrderedMap) resultArray[0];
      System.out.println(map);
      assertEquals(om.toString(), map.toString()); // Close enough
      // map=(IOrderedMap)xml2om.convert(xmlDocument);
      resultArray = xml2om.process(xmlDocument);
      assertTrue(resultArray.length == 1);
      map = (IOrderedMap) resultArray[0];
      assertEquals(om.toString(), map.toString()); // Close enough.
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testInvalidInputs() {
    try {
      // xml2om.convert(om);
      xml2om.process(om);
      fail("Convertor should not accept non String/Document value " + om.getClass().getName());
    } catch (RecordFormatException pe) {
      ;
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    try {
      om2xml.convert(xmlString);
      om2xml.process(xmlString);
      fail("Convertor should not accept non IOrderedMap value " + om.getClass().getName());
    } catch (RecordFormatException pe) {
      ;
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

  }

  public void testOrderedMapToXmlConversion() {
    try {
      // String xml=(String)om2xml.convert(om);

      Object[] resultArray = om2xml.process(om);
      assertTrue(resultArray.length == 1);
      String xml = (String) resultArray[0];

      assertEquals(xmlString, xml);

      om2xml.setReturnXmlAsString(false);
      // Document doc=(Document)om2xml.convert(om);
      resultArray = om2xml.process(om);
      assertTrue(resultArray.length == 1);
      Document doc = (Document) resultArray[0];

      String encoding = doc.getXMLEncoding();
      assertEquals(XmlConvertorTestCase.docAsString(xmlDocument, encoding), XmlConvertorTestCase.docAsString(doc,
          encoding));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  private static String docAsString(Document doc, String encoding) {
    StringWriter sw = new StringWriter();
    OutputFormat outputFormat = OutputFormat.createCompactFormat();
    if (encoding != null) {
      outputFormat.setEncoding(encoding); // This definitely sets it in the header!
    }
    XMLWriter writer = new XMLWriter(sw, outputFormat);
    try {
      writer.write(doc);
    } catch (IOException ioe) {
      fail("Failed to write XML as a String. Reason: " + ioe.toString());
    }
    return sw.toString();
  }
}
