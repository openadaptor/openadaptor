/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.ValidationException;

public class SkipFilterProcessor extends Component implements IDataProcessor {

  private static final Log log = LogFactory.getLog(SkipFilterProcessor.class);

  // internal state:
  // the current record number
  protected int currentRecordNumber = 0;

  // actual list of the record numbers (ie. with the shorthand lists expanded)
  protected ArrayList records2skip = new ArrayList();

  // bean properties:
  /**
   * list of the records to be skipped. Each one may contain an individual record number or a list shorthand made up of
   * the starting record number, a dash and the ending record number (eg. 2-14).
   */
  protected String[] skipRecords;

  /**
   * Flag indicating if matches are discarded or retained (discarded or passed).
   * <p>
   * By default, matches are discarded, and non-matches are passed.
   */
  protected boolean discardMatches = true;

  // BEGIN Properties
  public void setSkipRecords(String[] s) {
    this.skipRecords = s;
  }

  public String[] getSkipRecords() {
    return skipRecords;
  }

  public boolean isDiscardMatches() {
    return discardMatches;
  }

  public void setDiscardMatches(boolean discardMatches) {
    this.discardMatches = discardMatches;
  }

  // END Properties

  /**
   * Process a single input record. <p/> Note: A no-op implementation would be just to return the record unmolested (but
   * wrapped in an Object[]) If a null object is provided, then a NullRecordException should be thrown.<br>
   * <p/> Note that if the result of calling process is <tt>null</tt>, or an empty <code>Object[]</code>, then the
   * calling INode interprets this to mean the record has been discarded, for the purposes of routing discarded records.
   * <p/> Note that implementations should always return an Object[], even if empty. Currently a null return value is
   * treated similarily, but this behaviour is considered deprecated and is very likely to change in future.
   * 
   * @param data -
   *          the input record to be processed.
   * @return Object[] with zero or more records, resulting from the processing operation.
   * @throws RecordException
   *           if the processing fails for any reason.
   */
  public Object[] process(Object data) {

    if (doSkip(data)) {
      log.debug("Filter test passed");
      if (isDiscardMatches()) {
        log.debug("'discardMatches' is 'true'. Blocking Record");
        return new Object[] {};
      } else {
        log.debug("'discardMatches' is 'false'. Passing Record");
        return new Object[] { data };
      }
    } else {
      log.debug("Filter test passed");
      if (isDiscardMatches()) {
        log.debug("'discardMatches' is 'true'. Passing Record");
        return new Object[] { data };
      } else {
        log.debug("'discardMatches' is 'false'. Blocking Record");
        return new Object[] {};
      }
    }
  }

  /**
   * Validates the configuration of this processor before use. <p/> It provides a hook to perform any validation of the
   * component properties required by the implementation. <br>
   * Note that validate() should not have any impact outside of the processor itself. <p/> Default behaviour should be a
   * no-op that returns an empty Exception[]. validate() should never return <tt>null</tt>
   */
  public void validate(List exceptions) {
    // need to reset any fields that maintain state
    records2skip = new ArrayList();

    // loop through the skip records and expand any lists
    if (skipRecords != null) {
      for (int i = 0; i < skipRecords.length; i++) {
        String s = skipRecords[i];

        // sanity check
        if (s.equals("0"))
          exceptions.add(new ValidationException("Cannot skip the zero'th record", this));

        // not a list so must be a record number
        if (s.indexOf("-") == -1) {
          records2skip.add(new Integer(s));
          continue;
        }

        // add all elements in the list
        String[] extreems = s.split("-");

        if (extreems.length != 2)
          exceptions.add(new ValidationException("Failed to parse skipRecord [" + s + "]", this));

        int start, end;
        try {
          start = Integer.parseInt(extreems[0]);
          end = Integer.parseInt(extreems[1]);

          for (int j = start; j <= end; j++)
            records2skip.add(new Integer(j));
        } catch (NumberFormatException e) {
          exceptions.add(new ValidationException("Failed to parse skipRecord [" + s + "]: " + e.getMessage(), this));
        }
      }

      log.info("SkipFilterProcessor will skip " + records2skip.size() + " record(s)");
      currentRecordNumber = 0;
    }
  }

  /**
   * True if the object is to be skipped, false otherwise.
   * 
   * @return true if the object is to be skipped, false otherwise
   */
  protected boolean doSkip(Object testObject) throws RecordException {
    currentRecordNumber++; // todo: make this thread safe

    if (isSkipRecord()) {
      log.debug("Skipping record " + currentRecordNumber);
      return true;
    }
    return false;
  }

  /**
   * Perform initialisation of this bean from the bean properties.
   * 
   * Called by adaptor as part of the start-up. Processes the skip records converting the list shortcuts into actual
   * record numbers.
   * 
   * Check pre-condition of properties required to be set, and compile pattern.
   * 
   * @throws org.openadaptor.core.exception.OAException
   *           if the preconditions are not met
   */

  public void reset(Object context) {
    log.info("Resetting convertors.");
    currentRecordNumber = 0;
    log.info("SkipFilterProcessor will ignore " + records2skip.size() + " record(s)");
  }

  /**
   * returns true if the current record is to be skipped
   */
  private boolean isSkipRecord() {
    return records2skip.contains(new Integer(currentRecordNumber));
  }
}
