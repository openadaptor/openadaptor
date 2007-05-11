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
package org.openadaptor.auxil.processor.map;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
/**
 * Unit tests for AbstractMapProcessor.
 * 
 * Incomplete.
 * @author higginse
 * @since Introduced Post 3.2.1
 */
public abstract class AbstractTestMapProcessor extends TestCase {
  public static Log log = LogFactory.getLog(AbstractTestMapProcessor.class);
  protected AbstractMapProcessor processor;

  protected Object[] input;

  protected Object[] processedResults;
  

  protected void setUp() throws Exception {
    super.setUp();
    processor=createInstance();
    input=new Object[] {createOrderedMap(),createHashMap(),createXmlDocument()};
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    processor=null;
    input=null;
  }

  abstract protected AbstractMapProcessor createInstance();

  protected IOrderedMap createOrderedMap() {
    OrderedHashMap map=new OrderedHashMap();
    return map;
  }

  protected Map createHashMap() {
    Map map=new HashMap();
    return map;
  }

  protected Document createXmlDocument() {
    Document doc = DocumentHelper.createDocument();
    Element root=DocumentHelper.createElement("root");
    Element first=DocumentHelper.createElement("first");
    root.add(first);
    doc.setRootElement(root);
    return doc;
  }

  /**
   * Test basic process functionality with the default reference and expected maps. Note most processors will be
   * configurable. If this is the case additional tests should be written in the appropriate test subclasses.
   */
  public void testProcess() {
    for (int i=0;i<input.length;i++) {
      try {
        processedResults = processor.process(input[i]);
      } catch (Exception e) {
        fail("Unexpected failure with input "+input[i]);
      }
    }
    /*
    if (unexpected != null)
      fail("Unexpected exception processing record [" + unexpected + "]");
    assertArrayCardinality(processedResults, 1); // Cardinality currently does not change for Modify Processors

    IOrderedMap processedMapResult = (IOrderedMap) processedResults[0];
    assertEquals("Modify doesn't produce expected map", expectedMap, processedMapResult);
     */
  }

  /**
   * Test for appropriate behaviour given a Null Record.
   */
  public void testProcessNull() {
    try {
      processedResults = processor.process(null);
      fail("Expected NullRecordException not thrown");
    } catch (NullRecordException e) {
      ; // All is well
    } catch (Exception e) {
      fail("Caught an Exception that wasn't a NullRecordException [" + e + "]");
    }
  }

  /**
   * Test for appropriate behaviour given a Record that is not an OrderedMap.
   */
  public void testProcessNotMap() {
    try {
      processedResults = processor.process("I am not a Map :-)");
      fail("Expected RecordException not thrown");
    } catch (RecordException re) {
      ; // All is well.
    } catch (Exception e) {
      fail("Unexpected excption thrown [" + e + "]");
    }
  }
  protected void logTest(String name) {
    log.info("--- Testing "+name+" ---");
  }

}
