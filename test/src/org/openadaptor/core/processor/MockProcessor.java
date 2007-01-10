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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Simple Mock class for IRecordProcessor testing
 * 
 * @author OA3 Core Team
 */
public class MockProcessor implements IDataProcessor {

  private static final Log log = LogFactory.getLog(MockProcessor.class);

  private boolean throwException = false;

  private String exceptionClassName = null;

  private Map predefinedResults;

  private Object[] lastProcessedResult;

  public void setThrowException(boolean throwException) {
    this.throwException = throwException;
  }

  public void setExpectedResults(Map predefinedResuts) {
    this.predefinedResults = predefinedResuts;
  }

  public String getExceptionClassName() {
    return exceptionClassName;
  }

  public void setExceptionClassName(String exceptionClassName) {
    this.exceptionClassName = exceptionClassName;
  }

  /**
   * Process the message data. Doesn't do anything except log the data.
   * 
   * @param record -
   *          Object containing the record
   * @return record, untouched.
   */
  public Object[] process(Object record) throws RecordException {
    Object[] result = new Object[] { record };
    log.info("Processing: [" + record + "]");
    if (throwException) {
      throwRequiredRecordException(record);
    }
    if ((predefinedResults != null) && predefinedResults.containsKey(record)) {
      log.info("Retrieving predefined result for record - " + record);
      Object definedResult = predefinedResults.get(record);
      // Make sure it's an Object[]. If not, wrap it.
      result = (definedResult instanceof Object[]) ? (Object[]) definedResult : new Object[] { definedResult };
    } else {
      log.info("No predefined result, it will be the original object (wrapped in an Object[])");
    }
    lastProcessedResult = result;
    return result;
  }

  protected boolean throwRequiredRecordException(Object record) throws RecordException {
    if (getExceptionClassName() == null) {
      throw new RecordException("Mock RecordException for record - " + record);
    } else {
      Class exceptionClass = null;
      RecordException exception = null;
      try {
        exceptionClass = Class.forName(getExceptionClassName());
        exception = (RecordException) exceptionClass.getConstructor(new Class[] { Class.forName("java.lang.String") })
            .newInstance(new String[] { "Raising Exception: [" + getExceptionClassName() + "}" });
      } catch (ClassNotFoundException e) {
        log.error("Failed to throw configured exception. Reason [" + e + "]");
        return false;
      } catch (NoSuchMethodException e) {
        log.error("Failed to throw configured exception. Reason [" + e + "]");
        return false;
      } catch (IllegalAccessException e) {
        log.error("Failed to throw configured exception. Reason [" + e + "]");
        return false;
      } catch (InvocationTargetException e) {
        log.error("Failed to throw configured exception. Reason [" + e + "]");
        return false;
      } catch (InstantiationException e) {
        log.error("Failed to throw configured exception. Reason [" + e + "]");
        return false;
      }
      if (exception != null)
        throw exception;
    }
    return true;
  }

  public void validate(List exceptions) {
  }

  public void reset(Object context) {
  }

  /**
   * This is a utility method to allow unit test to verify results. i.e. use this to inspect the result after a call to
   * process.
   * 
   * @return the result of the last call to processRecord.
   */
  public Object[] getLastProcessedResult() {
    return lastProcessedResult;
  }
}
