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
package org.oa3.auxil.processor.orderedmap;

import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.core.exception.RecordFormatException;

public class OMValueTypeModifyProcessorTestCase extends AbstractTestOrderedMapModifyProcessor {

  private static String ATT_STRING_NAME = "ATT_STRING";

  private static String ATT_INT_NAME = "ATT_INTEGER";

  private static String ATT_DOUBLE_NAME = "ATT_DOUBLE";

  private static String STRING_VAL = "I am a String";

  private static String STRING_INTEGER_VAL = "10";

  private static String STRING_DOUBLE_VAL = "10.0";

  private static Integer INTEGER_VAL = new Integer(10);

  private static Double DOUBLE_VAL = new Double(10);

  // Default tests processing a String to Integer transform.
  protected OrderedMapModifyProcessor createInstance() {
    OMValueTypeModifyProcessor processor = new OMValueTypeModifyProcessor();
    processor.setAttribute(ATT_INT_NAME);
    processor.setType("java.lang.Integer");
    return processor;
  }

  protected IOrderedMap createReferenceMap() {
    IOrderedMap map = new OrderedHashMap();
    map.put(ATT_STRING_NAME, STRING_VAL);
    map.put(ATT_INT_NAME, STRING_INTEGER_VAL);
    map.put(ATT_DOUBLE_NAME, STRING_DOUBLE_VAL);
    return map;
  }

  protected IOrderedMap createExpectedMap() {
    IOrderedMap map = new OrderedHashMap();
    map.put(ATT_STRING_NAME, STRING_VAL);
    map.put(ATT_INT_NAME, INTEGER_VAL);
    map.put(ATT_DOUBLE_NAME, STRING_DOUBLE_VAL);
    return map;
  }

  // Extra tests

  public void testNonExistentAttribute() {
    // Set the test processor instance to convert the "DOUBLE" attribute from String to Double
    OMValueTypeModifyProcessor testProcessor = (OMValueTypeModifyProcessor) testInstance;
    testProcessor.setAttribute("NOT IN MAP");
    testProcessor.setType("java.lang.Integer"); // 

    // Do the test
    try {
      processedResults = testInstance.process(processMap);
      fail("Did not raise an excepion as expected");
    } catch (RecordFormatException rfe) {
      ; // All is well
    } catch (Exception e) {
      fail("Threw an unexpected exception [" + e + "]");
    }
  }

  public void testIntToString() {
    // Set the test processor instance to convert the "DOUBLE" attribute from String to Double
    OMValueTypeModifyProcessor testProcessor = (OMValueTypeModifyProcessor) testInstance;
    testProcessor.setAttribute(ATT_INT_NAME);
    testProcessor.setType("java.lang.String");

    // Set the reference map the way we want

    referenceMap.put(ATT_INT_NAME, INTEGER_VAL);

    // Set expected map the way we want it
    expectedMap.put(ATT_INT_NAME, STRING_INTEGER_VAL);
    expectedMap.put(ATT_DOUBLE_NAME, STRING_DOUBLE_VAL);
    // Do the test
    testProcessRecord();
  }

  public void testStringToDouble() {
    // Set the test processor instance to convert the "DOUBLE" attribute from String to Double
    OMValueTypeModifyProcessor testProcessor = (OMValueTypeModifyProcessor) testInstance;
    testProcessor.setAttribute(ATT_DOUBLE_NAME);
    testProcessor.setType("java.lang.Double");

    // Set expected map the way we want it
    expectedMap.put(ATT_INT_NAME, STRING_INTEGER_VAL);
    expectedMap.put(ATT_DOUBLE_NAME, DOUBLE_VAL);
    // Do the test
    testProcessRecord();
  }

  public void testDoubleToString() {
    // Set the test processor instance to convert the "DOUBLE" attribute from String to Double
    OMValueTypeModifyProcessor testProcessor = (OMValueTypeModifyProcessor) testInstance;
    testProcessor.setAttribute(ATT_DOUBLE_NAME);
    testProcessor.setType("java.lang.String");

    // Set the reference map the way we want

    referenceMap.put(ATT_DOUBLE_NAME, DOUBLE_VAL);

    // Set expected map the way we want it
    expectedMap.put(ATT_INT_NAME, STRING_INTEGER_VAL);
    expectedMap.put(ATT_DOUBLE_NAME, STRING_DOUBLE_VAL);
    // Do the test
    testProcessRecord();
  }

}
