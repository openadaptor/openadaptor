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
package org.openadaptor.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.processor.script.ScriptProcessorTestCase;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;

/**
 * Abstracts whats common to testing IDataProcessor implementations.
 * 
 * @author Kevin Scully
 */
public abstract class AbstractTestIDataProcessor extends MockObjectTestCase {
  private static final Log log =LogFactory.getLog(ScriptProcessorTestCase.class);

  /**
   * The test processor.
   */
  protected IDataProcessor testProcessor;

  // Junit setup and teardown methods

  protected void setUp() throws Exception {
    super.setUp();
    createMocks();
    testProcessor = createProcessor();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testProcessor = null;
  }

  /**
   * Abstract method overridden to create any required mock objects.
   */
  protected void createMocks() {} //Default implementation doesn't do anything.

  protected void deleteMocks() {} //Default implementation doesn't do anything.

  /**
   * Override to create the basic test processor instance.
   * 
   * @return The test processor.
   */
  protected abstract IDataProcessor createProcessor();

  /**
   * Implement to perform the basic process record functionality.
   */
  abstract public void testProcessRecord();

  //abstract public void testValidation();

  /**
   * All IDataProcessors are implemented to expect a non-null record instance.
   * <p>
   * This test ensures that the correct NullRecordException is thrown when that is not the case.
   */
  public void testProcessNullRecord() {
    log.debug("--- BEGIN testProcessNullRecord ---");
    try {
      testProcessor.process(null);
    } catch (NullRecordException e) {
      log.debug("--- END testProcessNullRecord ---");
      return;
    } catch (RecordException e) {
      fail("Unexpected RecordException [" + e + "]");
    }
    fail("Did not catch expected NullRecordException");

    log.debug("--- END testProcessNullRecord ---");
  }

  public void testValidateNull() {
    log.debug("--- BEGIN testValidate(null) ---");
    try {
      testProcessor.validate(null);
    } catch (IllegalArgumentException e) {
      log.debug("--- END testValidate(null) ---");
      return;
    } catch (Throwable t) {
      fail("Unexpected Exceptione [" + t + "]");
    }
    fail("Did not catch expected IllegalArgumentException");

    log.debug("--- END testValidate(null) ---");
  }
}
