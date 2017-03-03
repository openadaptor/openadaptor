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
package org.openadaptor.auxil.processor.orderedmap;

import junit.framework.TestCase;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.processor.orderedmap.OrderedMapModifyProcessor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordFormatException;

public abstract class AbstractTestOrderedMapModifyProcessor extends TestCase {
  protected OrderedMapModifyProcessor testInstance;

  protected IOrderedMap referenceMap;

  protected IOrderedMap expectedMap;

  protected IOrderedMap processMap;

  protected Object[] processedResults;

  protected void setUp() throws Exception {
    super.setUp();
    testInstance = createInstance();
    referenceMap = createReferenceMap();
    expectedMap = createExpectedMap();
    processMap = (IOrderedMap) referenceMap.clone();
    processedResults = new Object[] {};
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testInstance = null;
    referenceMap = null;
    expectedMap = null;
    processMap = null;
  }

  abstract protected OrderedMapModifyProcessor createInstance();

  abstract protected IOrderedMap createReferenceMap();

  abstract protected IOrderedMap createExpectedMap();

  protected void assertArrayCardinality(Object[] testArray, int expectedCardinality) {
    assertTrue("Record Array Cardinality should be [" + expectedCardinality + "] after processing, not ["
        + testArray.length + "]", testArray.length == expectedCardinality);
  }

  /**
   * Test basic process functionality with the default reference and expected maps. Note most processors will be
   * configurable. If this is the case additional tests should be written in the appropriate test subclasses.
   */
  public void testProcessRecord() {
    Object unexpected = null;
    try {
      processedResults = testInstance.process(processMap);
    } catch (Exception e) {
      unexpected = e;
    }

    if (unexpected != null)
      fail("Unexpected exception processing record [" + unexpected + "]");
    assertArrayCardinality(processedResults, 1); // Cardinality currently does not change for Modify Processors

    IOrderedMap processedMapResult = (IOrderedMap) processedResults[0];
    assertEquals("Modify doesn't produce expected map", expectedMap, processedMapResult);
  }

  /**
   * Test for appropriate behaviour given a Null Record.
   */
  public void testProcessNull() {
    try {
      processedResults = testInstance.process(null);
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
  public void testProcessNotOrderedMap() {
    try {
      processedResults = testInstance.process("I am not an OrderedMap");
      fail("Expected RecordFormatException not thrown");
    } catch (RecordFormatException rfe) {
      ; // All is well.
    } catch (Exception e) {
      fail("Unexpected excption thrown [" + e + "]");
    }
  }

}
