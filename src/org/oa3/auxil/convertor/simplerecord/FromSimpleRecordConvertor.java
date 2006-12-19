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
package org.oa3.auxil.convertor.simplerecord;

import org.oa3.auxil.convertor.AbstractConvertor;
import org.oa3.auxil.simplerecord.ISimpleRecord;
import org.oa3.core.exception.RecordFormatException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Dec 18, 2006 by oa3 Core Team
 */

/**
 * Unwrap an ISimpleRecord returning the underlying data that it is a view on.
 */
public class FromSimpleRecordConvertor extends AbstractConvertor {

  private static final Log log = LogFactory.getLog(FromSimpleRecordConvertor.class);

  /**
   * Performs the the actual conversion. Returns the successfully converted record or throw a RecordException.
   *
   * @param data
   * @return Converted Record
   * @throws org.oa3.core.exception.RecordException
   *          if there was a problem converting the record
   */
  protected Object convert(Object data) {
    if (! (data instanceof ISimpleRecord )) {
      log.warn("Incoming record is not an ISimpleRecord - Cannot get original record.");
      throw new RecordFormatException("Expected ISimpleRecord . Got [" + data.getClass().getName() + "]");
    }
    return ((ISimpleRecord)data).getRecord();
  }
}