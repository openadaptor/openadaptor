/*
 **
 ** Copyright (C) 2001 The Software Conservancy as Trustee. All rights
 ** reserved.
 ** 
 ** Permission is hereby granted, free of charge, to any person obtaining a
 ** copy of this software and associated documentation files (the
 ** "Software"), to deal in the Software without restriction, including
 ** without limitation the rights to use, copy, modify, merge, publish,
 ** distribute, sublicense, and/or sell copies of the Software, and to
 ** permit persons to whom the Software is furnished to do so, subject to
 ** the following conditions:
 ** 
 ** The above copyright notice and this permission notice shall be included
 ** in all copies or substantial portions of the Software.
 ** 
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 ** OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 ** MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 ** NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 ** LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 ** OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 ** WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ** 
 ** Nothing in this notice shall be deemed to grant any rights to
 ** trademarks, copyrights, patents, trade secrets or any other intellectual
 ** property of the licensor or any contributor except as expressly stated
 ** herein. No patent license is granted separate from the Software, for
 ** code that you delete from the Software, or for combinations of the
 ** Software with other software or hardware.
 **
 */

package org.openadaptor.dataobjects;

import org.openadaptor.StubException;

/**
 * Stub for legacy openadaptor code so that legacy components can
 * be compiled and distributed
 */

public class DOTypeHolder {
  /** Creates a new DOTypeHolder containing all the types found in dobs.
   * Searches through the DataObject array dobs for all the distinct DOTypes
   * in it, and stores them in the new DOTypeHolder.
   * @param dobs DataObject array.
   * @exception IncompatibleTypeException if different DOTypes with the same
   * name are found in dobs.
   */
  public DOTypeHolder(DataObject[] dobs) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  /** Returns the number of types held in the receiver.
   * @return Number of types held in the receiver.
   */
  public int numberOfTypes() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  /** Returns an array containing all the DOTypes contained in self.
   */
  public DOType[] getTypes(boolean includePrimitives) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  /** Returns whether a type <I>name</I> is held in the receiver.
   * @param name String name of DOType.
   * @return true if a type <I>name</I> is held in receiver, otherwise false.
   */
  public boolean holdsTypeNamed(String name) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  /** Returns DOType <I>type</I> held in the receiver.
   * @param name String name of DOType requested.
   * @return DOType named name.
   * @exception DataObjectException If no DOType with the same name is
   * held in the receiver.
   */
  public DOType getTypeNamed(String name) throws DataObjectException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }
  /** Removes DOType <I>type</I> from the receiver, if held in the receiver.
   * @param type DOType to be removed.
   * @return the DOType being removed, or null if not held or
   * <CODE>type</CODE> is null.
   */
  synchronized public DOType removeType(DOType type) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }
}
