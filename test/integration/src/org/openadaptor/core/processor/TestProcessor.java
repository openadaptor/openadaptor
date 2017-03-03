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

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.ProcessingException;

import java.util.List;

public class TestProcessor extends Component implements IDataProcessor {

  private OAException exception = new ProcessingException("test", this);

  private int exceptionFrequency = 0;

  private int discardFrequency = 0;

  private int gobbleFrequency = 0;

  private int count = 0;

  public TestProcessor() {
  }

  public TestProcessor(String id) {
    super(id);
  }

  public void setExceptionFrequency(int frequency) {
    this.exceptionFrequency = frequency;
  }

  public void setException(OAException exception) {
    this.exception = exception;
  }

  public void setDiscardFrequency(int frequency) {
    this.discardFrequency = frequency;
  }

  public void setGobbleFrequency(int frequency) {
    this.gobbleFrequency = frequency;
  }

  public Object[] process(Object data) {
    count++;

    if (exceptionFrequency > 0 && (count % exceptionFrequency == 0)) {
      throw exception;
    }

    if (discardFrequency > 0 && (count % discardFrequency == 0)) {
      return null;
    }

    if (gobbleFrequency > 0 && (count % gobbleFrequency == 0)) {
      return new Object[0];
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append(getId() != null ? getId() : TestProcessor.class.getName());
    buffer.append("(").append(data.toString()).append(")");
    return new Object[] { buffer.toString() };
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
    count = 0;
  }

}
