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
package org.openadaptor.auxil.convertor.simplerecord;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.openadaptor.auxil.convertor.simplerecord.ToSimpleRecordConvertor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.auxil.simplerecord.ISimpleRecordAccessor;
import org.openadaptor.core.exception.RecordFormatException;

public class ToSimpleRecordConvertorTestCase extends MockObjectTestCase {

  protected ToSimpleRecordConvertor testSubject;
  protected Mock mockOutgoingSimpleRecord;
  protected Mock mockAccessor;
  protected ISimpleRecordAccessor accessor;
  protected ISimpleRecord outgoingSimpleRecord;
  protected Mock mockIncomingSimpleRecord;
  protected ISimpleRecord incomingSimpleRecord;


  protected void setUp() throws Exception {
    super.setUp();
    testSubject = createTestSubject();
    mockAccessor = new Mock(ISimpleRecordAccessor.class);
    accessor = (ISimpleRecordAccessor)mockAccessor.proxy();
    mockOutgoingSimpleRecord = new Mock(ISimpleRecord.class);
    outgoingSimpleRecord = (ISimpleRecord)mockOutgoingSimpleRecord.proxy();
    mockIncomingSimpleRecord = new Mock(ISimpleRecord.class);
    incomingSimpleRecord = (ISimpleRecord)mockIncomingSimpleRecord.proxy();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testSubject = null;
    mockOutgoingSimpleRecord = null;
    mockAccessor = null;
  }

  protected ToSimpleRecordConvertor createTestSubject() {
    ToSimpleRecordConvertor convertor = new ToSimpleRecordConvertor();
    return convertor;
  }

  // Tests

  public void testConvert() {
    Object testObject = new Object();
    testSubject.setSimpleRecordAccessor(accessor);
    mockAccessor.expects(once()).method("asSimpleRecord").with(eq(testObject)).will(returnValue(outgoingSimpleRecord));

    Object convertedObject = null;
    try {
      convertedObject = testSubject.convert(testObject);
    }
    catch(Exception e) {
      fail("Unexpected exception: " + e);
    }
    assertEquals("Didn't get the expected converted object", outgoingSimpleRecord, convertedObject);
  }

  public void testConvertNoAccessorSet() {
    Object testObject = new Object();
    testSubject.setSimpleRecordAccessor(null);
    try {
      testSubject.convert(testObject);
    }
    catch(RecordFormatException rfe) {
      // expected this
    }
    catch(Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  public void testConvertNoAccessorSetIncomingSimpleRecord() {
    Object testObject = incomingSimpleRecord;
    testSubject.setSimpleRecordAccessor(null);

    Object convertedObject = null;
    try {
      convertedObject = testSubject.convert(testObject);
    }
    catch(Exception e) {
      fail("Unexpected exception: " + e);
    }
    assertEquals("Data should be passed through unchanged.", testObject, convertedObject);
  }
}
