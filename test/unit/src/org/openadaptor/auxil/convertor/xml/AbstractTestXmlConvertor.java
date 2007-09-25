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
package org.openadaptor.auxil.convertor.xml;

import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.util.ResourceUtil;

/**
 * This tests the XmlConvertors implementation.
 * 
 * @author OA3 Core Team
 */
public abstract class AbstractTestXmlConvertor extends AbstractTestIDataProcessor {

  protected String xmlString;

  protected Document xmlDocument;

  protected IOrderedMap om;

  //protected XmlToOrderedMapConvertor xml2om;

  //protected OrderedMapToXmlConvertor om2xml;

  protected void setUp() throws Exception {
    super.setUp();
    xmlString = ResourceUtil.readFileContents(this, "test.xml");
    xmlString = ResourceUtil.removeCarriageReturns(xmlString);
    xmlDocument = generateXmlDocument(xmlString);
    om = generateTestOrderedMap();
    //xml2om = new XmlToOrderedMapConvertor();
    //om2xml = new OrderedMapToXmlConvertor();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }


  //Utility methods
  protected static String docAsString(Document doc, String encoding) {
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

  protected IOrderedMap generateTestOrderedMap() {
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

  protected IOrderedMap generateTestOrderedMapWithSlashes() {
    IOrderedMap root = new OrderedHashMap();
    IOrderedMap parent = new OrderedHashMap();
    IOrderedMap child = new OrderedHashMap();
    IOrderedMap child2 = new OrderedHashMap();
    child.put("Y/Y", "Y_VAL");
    child.put("Z/Z", "Z_VAL");
    parent.put("B/B", child);
    child = new OrderedHashMap();
    child.put("Y/Y","Y_VAL");
    child.put("Z/Z", "Z_VAL");

    child2 = new OrderedHashMap();
    child2.put("Y/Y", "Y_VAL2");
    child2.put("Z/Z", "Z_VAL2");
    parent.put("C/C", new Object[] { child, child2 });

    root.put("A/A", parent);
    return root;
  }

  protected static Document generateXmlDocument(String xml) {
    Document document = null;
    try {
      return (DocumentHelper.parseText(xml));
    } catch (DocumentException de) {
      fail("Unit test problem - " + de.toString());
    }
    return document;
  }


}
