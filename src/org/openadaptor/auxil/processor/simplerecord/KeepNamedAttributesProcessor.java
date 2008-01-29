/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.processor.simplerecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openadaptor.auxil.simplerecord.AbstractSimpleRecordProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Create a modified copy of a SimpleRecord containing only attributes named in a list.
 * <p>
 * Expects all named attributes to exist in the original Record. This Processor works with SimpleRecords.
 * 
 * @author Kevin Scully
 */
public class KeepNamedAttributesProcessor extends AbstractSimpleRecordProcessor {

  /** Strip all attributes apart from those named in this list */
  protected List attributesToKeep = new ArrayList();

  // Bean Properties

  /** Strip all attributes apart from those named in this list */
  public List getAttributesToKeep() {
    return attributesToKeep;
  }

  /** Strip all attributes apart from those named in this list */
  public void setAttributesToKeep(List attributesToKeep) {
    this.attributesToKeep = attributesToKeep;
  }

  // End Bean Properties

  /**
   * Process a simpleRecord, noting if it has already been cloned. NB This processor will always return a (heavily)
   * modified copy.
   * 
   * @param simpleRecord
   * @param alreadyCloned
   * @return record with only the named attributes remaining.
   * @throws RecordException
   * 
   */
  public Object[] processSimpleRecord(ISimpleRecord simpleRecord, boolean alreadyCloned) throws RecordException {
    ISimpleRecord outgoing = (ISimpleRecord) simpleRecord.clone();
    outgoing.clear();
    Iterator attributeIter = getAttributesToKeep().iterator();
    while (attributeIter.hasNext()) {
      Object nextkey = attributeIter.next();
      Object nextValue = simpleRecord.get(nextkey);
      if (nextValue != null) {
        outgoing.put(nextkey, nextValue);
      } else {
        throw new RecordFormatException("Requested attribute[" + nextkey + "] did not exist in original SimpleRecord");
      }
    }
    return new Object[]{outgoing} ;

  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    Exception e = checkMandatoryProperty("attributesToKeep", attributesToKeep != null);
    if (e != null)
      exceptions.add(e);
  }
}
