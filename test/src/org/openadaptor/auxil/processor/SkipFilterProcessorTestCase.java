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
package org.oa3.auxil.processor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oa3.core.exception.RecordException;

/**
 * Provides some tests for the SkipFilterProcessor.
 * <p />
 * 
 * The "skipRecords" property allows you to define a list of records that you do not wish to be processed.
 * 
 * 
 * @author Russ Fennell
 */
public class SkipFilterProcessorTestCase extends TestCase {
  private SkipFilterProcessor cp = new SkipFilterProcessor();

  /**
   * tests that the processor correctly reads in and processes the "skipRecords" details
   */
  public void testInitialiseResources() {
    // none
    cp.validate(null);
    assertEquals(0, cp.records2skip.size());

    cp.setSkipRecords(null);
    cp.validate(null);
    assertEquals(0, cp.records2skip.size());

    // simple single value
    cp.setSkipRecords(new String[] { "1" });
    cp.validate(null);
    assertEquals(1, cp.records2skip.size());
    assertEquals(new Integer(1), cp.records2skip.get(0));

    // multiple values
    cp.setSkipRecords(new String[] { "1", "2" });
    cp.validate(null);
    assertEquals(2, cp.records2skip.size());
    assertEquals(new Integer(1), cp.records2skip.get(0));
    assertEquals(new Integer(2), cp.records2skip.get(1));

    // list shorthand
    cp.setSkipRecords(new String[] { "1-3" });
    cp.validate(null);
    assertEquals(3, cp.records2skip.size());
    assertEquals(new Integer(1), cp.records2skip.get(0));
    assertEquals(new Integer(2), cp.records2skip.get(1));
    assertEquals(new Integer(3), cp.records2skip.get(2));

    // mixture
    cp.setSkipRecords(new String[] { "1-3", "4", "5" });
    cp.validate(null);
    assertEquals(5, cp.records2skip.size());
    assertEquals(new Integer(1), cp.records2skip.get(0));
    assertEquals(new Integer(2), cp.records2skip.get(1));
    assertEquals(new Integer(3), cp.records2skip.get(2));
    assertEquals(new Integer(4), cp.records2skip.get(3));
    assertEquals(new Integer(5), cp.records2skip.get(4));

    List validationExceptions=new ArrayList();
    // non existant records: 0
    cp.setSkipRecords(new String[] { "0" });
    cp.validate(validationExceptions);
    assertTrue("Failed to detect zero'th record cannot be skipped",validationExceptions.size()>0);

    validationExceptions.clear();
    // non existant records: -1
    cp.setSkipRecords(new String[] { "-1" });
    cp.validate(validationExceptions);
    assertTrue("Failed to detect negative record cannot be skipped",validationExceptions.size()>0);
  }

  private Object[] processBatch(Object[] recordBatch) {
    List results = new ArrayList();
    try {
      for (int i = 0; i < recordBatch.length; i++) {
        boolean skip = cp.doSkip(recordBatch[i]);
        if (!skip) {
          results.add(recordBatch[i]);
        }
      }
    } catch (RecordException re) {
      fail("processBatch failed unexpectedly - " + re);
    }
    return (results.size() == 0) ? new Object[0] : results.toArray(new Object[results.size()]);
  }

  /**
   * test that the processor actually skips the correct records
   */
  public void testSkipRecords() {
    Object[] records = new String[] { "1", "2", "3", "4", "5", "6" };
    Object[] results;
    // none
    cp.setSkipRecords(null);
    cp.validate(null);
    results = processBatch(records);
    assertEquals(6, results.length);
    assertEquals("1", results[0]);
    assertEquals("2", results[1]);
    assertEquals("3", results[2]);
    assertEquals("4", results[3]);
    assertEquals("5", results[4]);
    assertEquals("6", results[5]);

    // simple single value
    cp.setSkipRecords(new String[] { "1" });
    cp.validate(null);
    results = processBatch(records);
    assertEquals(5, results.length);
    assertEquals("2", results[0]);
    assertEquals("3", results[1]);
    assertEquals("4", results[2]);
    assertEquals("5", results[3]);
    assertEquals("6", results[4]);

    // multiple values
    cp.setSkipRecords(new String[] { "1", "2" });
    cp.validate(null);
    results = processBatch(records);
    assertEquals(4, results.length);
    assertEquals("3", results[0]);
    assertEquals("4", results[1]);
    assertEquals("5", results[2]);
    assertEquals("6", results[3]);

    // list shorthand
    cp.setSkipRecords(new String[] { "1-3" });
    cp.validate(null);
    results = processBatch(records);
    assertEquals(3, results.length);
    assertEquals("4", results[0]);
    assertEquals("5", results[1]);
    assertEquals("6", results[2]);

    // mixture
    cp.setSkipRecords(new String[] { "1-3", "4", "5" });
    cp.validate(null);
    results = processBatch(records);
    assertEquals(1, results.length);
    assertEquals("6", results[0]);
  }
}
