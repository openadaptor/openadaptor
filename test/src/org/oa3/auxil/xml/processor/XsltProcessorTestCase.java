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
package org.oa3.auxil.xml.processor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.oa3.auxil.xml.XsltProcessor;
import org.oa3.util.FileUtils;

/**
 * @see XsltProcessor
 *
 * @author Russ Fennell
 */
public class XsltProcessorTestCase extends TestCase {

  public static final String PATH = "test/src/org/oa3/auxil/xml/processor";

  private static final String INPUT_FILENAME = PATH + "/input.xml";

  private static final String XSL_FILENAME = PATH + "/transform.xsl";

  private static final String OUTPUT_FILENAME = PATH + "/output.xml";

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
    processor.setXsltFile(XSL_FILENAME + "-XXXX");
    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertFalse("Failed to detect that the XSLT file doesn't exist", exceptions.isEmpty());
  }

  /**
   * test dom transform
   */
  public void testDocumentTransform() {
    validateTransformFile();
    Object[] results = processor.process(createDOMFromFile());
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
      results = processor.process(FileUtils.getFileContents(INPUT_FILENAME));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    assertEquals(1, results.length);
    assertTrue(results[0] instanceof String);
    checkOutput((String) results[0]);
  }

  private void validateTransformFile() {
    processor.setXsltFile(XSL_FILENAME);
    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("validation has failed", exceptions.isEmpty());
  }

  private void checkOutput(String output) {
    try {
      String expected = FileUtils.getFileContents(OUTPUT_FILENAME);
      if (expected.length() < output.length()) {
        expected = expected.replaceAll("\n", "\r\n");
      }
      assertEquals(expected, output);
    } catch (IOException e) {
      fail("Failed to read file contents: " + e.getMessage());
    }
  }

  private Document createDOMFromFile() {
    try {
      URL url = FileUtils.toURL(INPUT_FILENAME);
      if (url == null)
        fail("Failed to find sample XML: ");

      SAXReader reader = new SAXReader();
      return reader.read(url);
    } catch (DocumentException e) {
      fail("Failed to parse sample XML: " + e.getMessage());
    }

    return null;
  }

}
