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

import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Represents a class that can send / write some data to an external resource.
 * 
 * @author perryj
 *
 */
public interface IWriteConnector {

  /**
   * Implementations of {@link IWriteConnector} are typically beans with zero-arg
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
   * This should be called before {@link #deliver(Object[])} is called.
   * Implementations should use this method to establish connections to external
   * resources and prepare to accept calls to {@link #deliver(Object[])}.
   * Exceptions should be thrown as RuntimeExceptions. If the implementation is
   * an {@link IComponent} then it should throw {@link ConnectionException}.
   */
  void connect();

  /**
   * This should be called before the implementation is unreferenced.
   * Implementations should use this method to "cleanup" connections to external
   * resources and reset state. Exceptions should be thrown as
   * RuntimeExceptions. If the implementation is an {@link IComponent} then it
   * should throw {@link ConnectionException}.
   */
  void disconnect();

  /**
   * Delivers some data to the external resource to which the implementation is connected.
   * It is assumed that the implementation will cast the data to the expected type(s).
   * Exceptions should be thrown as RuntimeExceptions.
   * Implementation that are {@link IComponent}s should throw {@link ConnectionException}s.
   * @param data the data to process
   * @return the resopnse from the external resource
   */
  Object deliver(Object[] data);
}
