/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.core;

import java.util.List;

import org.openadaptor.core.exception.ValidationException;

/**
 * Implemented by classes that are capable of doing some sort of processing.
 * Implementations typically cast the data to the expected types(s). The core
 * method is {@link #process(Object)}.
 * 
 * @author perryj
 * 
 */
public interface IDataProcessor {

  /**
   * Implementations of {@link IDataProcessor} are typically beans with zero-arg
   * constructors. This method checks that the current state of an
   * implementation is "meaningful". Implementations are encouraged to add
   * exception to the list parameter rather than throwing them. This allows the
   * calling code to collate the exceptions. If the implementation is an
   * {@link IComponent} then the exceptions should be an
   * {@link ValidationException}.
   * 
   * @param exceptions
   *          collection to which exceptions should be added
   */
  void validate(List exceptions);

  /**
   * Allows calling code to communicate context information, the implementation
   * will cast the context to an expected type.
   * 
   * @param context
   * @see IReadConnector#getReaderContext()
   */
  void reset(Object context);

  /**
   * Processes some data and return the results. Results must be in an array, an
   * empty array is valid but null is not. It is assumed that implementations
   * will cast the incoming data to an expected type(s). Exceptions should be
   * thrown as RuntimeException, if the implementation is an {@link IComponent}
   * then this should be a subclass of {@link ComponentException}.
   * 
   * @return output, null indicates that data was disgarded, this is distinct
   *         from empty array which indicates no return value
   */
  Object[] process(Object data);

  /**
   * static field that does nothing to the data. Can be used by code to assign a
   * default value to and field / variable so that code can refer to that field /
   * variable without checking for null.
   */
  public static final IDataProcessor NULL_PROCESSOR = new IDataProcessor() {

    public Object[] process(Object data) {
      return new Object[] { data };
    }

    public void reset(Object context) {
    }

    public void validate(List exceptions) {
    }
  };

}
