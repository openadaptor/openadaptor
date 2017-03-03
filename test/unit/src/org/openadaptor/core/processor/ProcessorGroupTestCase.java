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
package org.openadaptor.core.processor;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.processor.ProcessorGroup;

public class ProcessorGroupTestCase extends TestCase {

  protected IDataProcessor[] processors;

  protected ProcessorGroup pg;

  protected Object record;

  protected MockProcessor p1, p2;

  private static final String ONE = "one";

  private static final String FIRST = "first";

  private static final String TWO = "two";

  private static final String SECOND = "second";

  // test data setup

  protected void setUp() throws Exception {
    super.setUp();
    Map expectedResults = new HashMap();
    expectedResults.put(ONE, FIRST);
    p1 = new MockProcessor();
    p1.setExpectedResults(expectedResults);
    p2 = new MockProcessor();
    expectedResults = new HashMap();
    expectedResults.put(FIRST, TWO);
    p2.setExpectedResults(expectedResults);
    processors = new IDataProcessor[] { p1, p2 };
    pg = new ProcessorGroup();
    pg.setProcessors(processors);
    record = ONE;
  }

  // Tests
  public Object[] invokeProcessRecord(Object record) {
    Object[] result = null;
    try {
      result = ((IDataProcessor) pg).process(record);
    } catch (RecordException re) {
      fail("Unexpected RecordException: " + re);
    }
    return result;
  }

  public void testSimpleCase() {
    Object[] processed = invokeProcessRecord(record);
    assertEquals(1, processed.length);
    assertEquals(TWO, processed[0]);
  }

  public void testCardinalityChange() {
    Map extraResults = new HashMap();
    extraResults.put(ONE, new String[] { FIRST, SECOND });
    p1.setExpectedResults(extraResults);
    Object[] processed = invokeProcessRecord(record);
    assertEquals(2, processed.length);
    assertEquals(TWO, processed[0]);
    assertEquals(SECOND, processed[1]);
  }
}
