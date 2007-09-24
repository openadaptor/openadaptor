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

package org.openadaptor.auxil.processor.simplerecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openadaptor.auxil.simplerecord.AbstractSimpleRecordProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Filter an ISimpleRecord based the presence of all of a list of Attributes.
 * <p>
 * This Processor is a bit of a one off as it can behave as a Filter or as a Processor. It is intended to cover a gap
 * in the expression handling where it is not possible (yet) to refernce attributes that don't exist in a record. This
 * sometimes occurs. This processor is (very) likely to be deprecated once this is no longer an issue. Any function it still
 * serves at that point is likely to be factored into a single function processor or filter.
 * <p>
 * Default behaviour is to discard any records for which attributes on the <code>mandatoryIncomingRecordAttributes</code> list are missing.
 * <p>
 * If either of <code>throwExceptionOnMissingAttribute</code> or <code>createOnMissingAttribute</code> are set then the
 * behaviour is different:
 * <p>
 * <code>throwExceptionOnMissingAttribute</code> set to true:<br>
 * Throw a RecordFormatException instead of discarding records.
 * <p>
 * <code>createOnMissingAttribute</code> set to true:<br>
 * Add the attribute to the reocrd with a null value. NB in this case the Processor acts as a "Processor" rather
 * than a "Filter".
 * <p>
 */
public class SimpleRecordAttributeExistsProcessor extends AbstractSimpleRecordProcessor {

  /** List of attributes to test for. */
  protected List mandatoryAttributes = new ArrayList();

  /** If true throws RecordFormatException instead of discarding records. Defaults to false. */
  protected boolean throwExceptionOnMissingAttribute = false;

  /** If true then add the missing attribute with a null value. */
  protected boolean createOnMissingAttribute = false;

  /**
   * Flag indicating if matches are passed or blocked.
   * <p>
   * By default, matches are blocked, and non-matches are passed. In practice this means that by default records
   * with missing attributes are blocked (i.e. discarded).
   */
  protected boolean discardMatches = true;

  // Bean stuff

  /**
   * List of attributes to test for.
   * @return List of attributes.
   */
  public List getMandatoryAttributes() {
    return mandatoryAttributes;
  }

  /**
   * List of attributes to test for.
   * @param mandatoryAttributes List of attributes.
   */
  public void setMandatoryAttributes(List mandatoryAttributes) {
    this.mandatoryAttributes = mandatoryAttributes;
  }

  /**
   * If true throws RecordFormatException instead of discarding records.
   * @return Whether to throw a RecordFormatException.
   */
  public boolean isThrowExceptionOnMissingAttribute() {
    return throwExceptionOnMissingAttribute;
  }

  /**
   * If true throws RecordFormatException instead of discarding records.
   * @param throwExceptionOnMissingAttribute Whether to throw a RecordFormatException
   */
  public void setThrowExceptionOnMissingAttribute(boolean throwExceptionOnMissingAttribute) {
    this.throwExceptionOnMissingAttribute = throwExceptionOnMissingAttribute;
  }

  /**
   * Set flag indicating if matches will cause records to be passed or blocked.
   * <p>
   * Default behaviour is to pass matches and block non-matches.
   *
   * @param discardMatches boolean flag, where <tt>true</tt> means block matching records.
   */
  public void setDiscardMatches(boolean discardMatches) {
    this.discardMatches = discardMatches;
  }

  /**
   * Get flag indicating if matches will cause records to be passed or blocked.
   *
   * @return boolean, <tt>true</tt> if matches are to be blocked, <tt>false</tt> otherwise.
   */
  public boolean getDiscardMatches() {
    return discardMatches;
  }

  /**
   * If true then add the missing attribute with a null value.
   * @return If true then add the missing attribute with a null value.
   */
  public boolean isCreateOnMissingAttribute() {
    return createOnMissingAttribute;
  }

  /**
   * If true then add the missing attribute with a null value.
   * @param createOnMissingAttribute  If true then add the missing attribute with a null value.
   */
  public void setCreateOnMissingAttribute(boolean createOnMissingAttribute) {
    this.createOnMissingAttribute = createOnMissingAttribute;
  }

  // End of bean stuff

  /**
   * Process a SimpleRecord noting if it has already been cloned.
   * <p>
   * If any mandatory records are missing then:
   * <ol>
   * <li>If CreateOnMissingAttribute set then create the missing attributes with null values and return the resulting SimpleRecord.</li>
   * <li>if ThrowExceptionOnMissingAttribute set then throw a RecordFormatException.</li>
   * <li>Otherwise based on value of DiscardMatches discard teh record (or don't).</li>
   * </ol>
   * <p>
   * This is all a bit of a bodge. This processor will probably be superceeded by better expression handling and/or dedicated
   * processors in the near future.
   *
   * @param simpleRecord
   * @param alreadyCloned
   * @return Object[] containing the results of processing the record.
   * @throws RecordException
   *
   */
  public Object[] processSimpleRecord(ISimpleRecord simpleRecord, boolean alreadyCloned) throws RecordException {
    ISimpleRecord outgoingRecord = simpleRecord;
    Object[] outgoingArray = new Object[] {};
    if (!alreadyCloned && isCreateOnMissingAttribute()) { // Only clone if the intention is to modify the incoming record.
      outgoingRecord = (ISimpleRecord) simpleRecord.clone(); //We are to act as a processor so we clone if it hasn't neen done already.
    }

    Iterator attributeIterator = getMandatoryAttributes().iterator();
    List missingAttributes = new ArrayList();
    while (attributeIterator.hasNext()) {
      String nextAttribute = (String) attributeIterator.next();
      if (!outgoingRecord.containsKey(nextAttribute)) {
        if (isCreateOnMissingAttribute()) {
          outgoingRecord.put(nextAttribute, null); // could set a default value. not going there yet.
        } else {
          missingAttributes.add(nextAttribute);
        }
      }
    }
    // Behave as a processor. Ignore discardMatches.
    // Outgoing record has already been updated with missing attributes
    // So we just return it.
    if (isCreateOnMissingAttribute()) {
      outgoingArray = new Object[] { outgoingRecord };
    }
    //Always throw  exception if there are missing attributes. Ignore discardMatches
    else if (isThrowExceptionOnMissingAttribute()) {
      if (!missingAttributes.isEmpty()) {
        if (missingAttributes.size() == 1) {
          throw new RecordFormatException("Attribute " + missingAttributes + " is missing.");
        } else {
          throw new RecordFormatException("Attributes " + missingAttributes + " are missing.");
        }
      } else {
        outgoingArray = new Object[] { simpleRecord };
      }
    }
    // Here we can act as a filter. Our default mode. NB This is the only time we heed the value of discardMatches.
    // Note: The filter tests as true if the missingAttributes is NOT empty.
    else if (!missingAttributes.isEmpty()) {
      if (discardMatches) {
        outgoingArray = new Object[] {};
      } else {
        outgoingArray = new Object[] { simpleRecord };
      }
    } else {
      if (discardMatches) {
        outgoingArray = new Object[]{simpleRecord};
      } else {
        outgoingArray = new Object[]{};
      }
    }
    return outgoingArray;

  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    Exception e = checkMandatoryProperty("mandatoryAttributes", mandatoryAttributes != null);
    if (e != null) {
      exceptions.add(e);
    }

    // Slightly unusual -
    // Discardin the rocord when attributes are missing is the default.
    // if either of throwExceptionOnMissingAttribute or createOnMissingAttribute is set to true then we do that.
    // if both are set we have a problem.

    if (throwExceptionOnMissingAttribute && createOnMissingAttribute) {
      e = new ValidationException("Cannot set both of throwExceptionOnMissingAttribute and createOnMissingAttribute to true.", this);
      exceptions.add(e);
    }
  }
}
