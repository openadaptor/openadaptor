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
package org.openadaptor.auxil.processor.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.openadaptor.auxil.processor.xml.XsltProcessor;
import org.openadaptor.util.ResourceUtil;
import org.xml.sax.InputSource;

/**
 * @see XsltProcessor
 *
 * @author Russ Fennell
 */
public class XsltProcessorTestCase extends TestCase {

  private XsltProcessor processor = new XsltProcessor();

  /**
   * tests that the XSLT file can be read in
   */
  public void testValidateNoTransformFile() {
    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertFalse("Failed to detect that the XSLT file was not set in the properties", exceptions.isEmpty());
  }

  public void testValidateMissingTransformFile() {
    processor.setXsltFile("XXXX");
    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertFalse("Failed to detect that the XSLT file doesn't exist", exceptions.isEmpty());
  }

  /**
   * test dom transform
   */
  public void testDocumentTransform() {
    validateTransformFile();
    String xml = ResourceUtil.readFileContents(this, "input.xml");
    Object[] results = processor.process(createDOM(xml));
    assertEquals(1, results.length);
    assertTrue(results[0] instanceof String);
    checkOutput((String) results[0]);
  }

  /**
   * test string transform
   */
  public void testStringTransform() {
    validateTransformFile();
    Object[] results = null;
    try {
      String xml = ResourceUtil.readFileContents(this, "input.xml");
      results = processor.process(xml);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    assertEquals(1, results.length);
    assertTrue(results[0] instanceof String);
    checkOutput((String) results[0]);
  }

  private void validateTransformFile() {
    String s = ResourceUtil.getResourcePath(this, "transform.xsl");
    processor.setXsltFile(s);
    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("validation has failed", exceptions.isEmpty());
  }

  private void checkOutput(String output) {
    String expected = ResourceUtil.readFileContents(this, "output.xml");
    output = output.replaceAll("\r\n", "\n");
    assertEquals(expected, output);
  }

  private Document createDOM(String xml) {
    try {
      SAXReader reader = new SAXReader();
      InputSource is = new InputSource(new StringReader(xml));
      return reader.read(is);
    } catch (DocumentException e) {
      fail("Failed to parse sample XML: " + e.getMessage());
    }

    return null;
  }
}
