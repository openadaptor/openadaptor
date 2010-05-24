/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.legacy.convertor.dataobjects;

import org.openadaptor.core.exception.RecordException;
import org.openadaptor.dataobjects.DataObject;

/**
 * Abstract Convertor which converts from DataObjects into other formats
 * 
 * @author higginse
 * 
 */
public abstract class AbstractDataObjectConvertor extends AbstractLegacyConvertor {

  /**
   * Convert DataObject, or DataObject[] into some other format.
   * <br>
   * It will call the concrete subclass convert method to do
   * the actual conversion (the subclasses will determine the output format).
   * 
   * This class checks that the supplied record is a DataObject[],
   * or, if a DataObject, it will wrap it as a DataObject[] before
   * calling the concrete convert implementation.
   */
  protected final Object convert(Object record) throws RecordException {
    //Let the concrete subclass do the actual conversion.
    return convert(LegacyUtils.asDataObjectArray(record));
  }

  /**
   * Convert the supplied dataobject array into some other representation.
   * <br>
   * The output format is dictated by the implementing subclass.
   * @param dobs
   * @return Object containing the result of conversion
   */
  protected abstract Object convert(DataObject[] dobs);

}

