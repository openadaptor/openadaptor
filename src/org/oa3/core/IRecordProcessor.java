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
package org.oa3.core;

import org.oa3.core.exception.RecordException;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/processor/IRecordProcessor.java,v 1.5 2006/10/20 09:04:01 higginse Exp $
 * Rev:  $Revision: 1.5 $
 * Created May 09, 2006 by Eddy Higgins
 */
/**
 * This is the interface to be implemented by all components intending to process records.
 * <p>
 * Note:
 * Users intending to build their own custom processors are advised to
 * use this interface as their entry point.
 *
 * @author Eddy Higgins
 */

public interface IRecordProcessor {
  /**
   * Process a single input record.
   * <p>
   * Note:
   * A no-op implementation would be just to return the record unmolested (but wrapped in an Object[])
   * If a null object is provided, then a NullRecordException should be thrown.<br>
   * <p>
   * Note that if the result of calling process is <tt>null</tt>, or an empty <code>Object[]</code>, then
   * the calling INode interprets this to mean the record has been discarded, for the purposes of
   * routing discarded records.
   * <p>
   * Note that implementations should always return an Object[], even if empty. Currently a null return
   * value is treated similarily, but this behaviour is considered deprecated and is very likely to change
   * in future.
   *
   * @param  record - the input record to be processed.
   * @return Object[] with zero or more records, resulting from the processing
   *         operation.
   * @throws RecordException if the processing fails for any reason.
   */
  Object[] processRecord(Object record) throws RecordException;

  /**
   * Validates the configuration of this processor before use.
   * <p>
   * It provides a hook to perform any validation of the component properties required by the implementation.
   * <br>
   * Note that validate() should not have any impact outside of the processor itself.
   * <p>
   * Default behaviour should be a no-op that returns an empty Exception[].
   * validate() should never return <tt>null</tt>
   */
  public Exception[] validate();

  /**
   * Prepare this processor for use.
   * <p>
   * This method is provides a hook to perform any (first-time) initialisation required by the implementation.
   * This might include obtaining resources required etc.
   * initialise might have an external impact (clearing temporary files for example).
   * <p>
   * Default behaviour should be a no-op.
   */
  public void initialise();

  /**
   * Resets the state of this processor.
   * <p>
   * After a call to reset(), the processor should be ready to process records as if initialise() had
   * just been called.
   * <br>
   * It is essentially a hook to allow the processor to be reset during the lifetime of the processor.
   * It might be used , for example reset a record counter, or similar when receiving data from a new source.
   * <p>
   * Default behaviour should be a no-op.
   */
  public void reset();
}
