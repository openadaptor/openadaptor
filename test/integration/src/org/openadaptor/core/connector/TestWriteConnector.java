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

package org.openadaptor.core.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.lifecycle.LifecycleComponent;
import org.openadaptor.core.transaction.ITransactional;
import org.openadaptor.core.transaction.TestTransactionalResource;

public class TestWriteConnector extends LifecycleComponent implements IWriteConnector, ITransactional {

  private static final Log log = LogFactory.getLog(TestWriteConnector.class);

  private boolean connected = false;

  private final List output = new ArrayList();

  private List expectedOutput = new ArrayList();

  private int exceptionFrequency = 0;

  private int count;

  private int expectedCommitCount = -1;
  
  private int expectedRollbackCount = -1;

  private TestTransactionalResource transactionalResource;

  public TestWriteConnector() {
  }

  public TestWriteConnector(String id) {
    super(id);
  }

  public void setExpectedOutput(Object output) {
    expectedOutput.clear();
    if (output instanceof Collection) {
      expectedOutput.addAll((Collection) output);
    } else {
      expectedOutput.add(output);
    }
  }

  public void setExceptionFrequency(int frequency) {
    this.exceptionFrequency = frequency;
  }

  public synchronized Object deliver(Object[] records) {

    for (int i = 0; i < records.length; i++) {
      count++;
      record(records[i].toString());
      if (exceptionFrequency > 0 && (count % exceptionFrequency == 0)) {
        throw new RuntimeException("test");
      }
    }
    return null;
  }

  public synchronized void connect() {
    output.clear();
    count = 0;
    connected = true;
  }

  public synchronized void disconnect() {
    if (connected) {
      if (!expectedOutput.equals(output)) {
        log.error(getId() + " output was not expected output, switch debug on to see more detail");
        if (log.isDebugEnabled()) {
          log.debug(getId() + " output was...");
          for (Iterator iter = output.iterator(); iter.hasNext();) {
            log.debug(getId() + " " + iter.next());

          }
          log.debug(getId() + " expected output was...");
          for (Iterator iter = expectedOutput.iterator(); iter.hasNext();) {
            log.debug(getId() + " " + iter.next());

          }
        }
        throw new RuntimeException("output was not expected output");
      }

      if (transactionalResource != null) {
        checkCommitCount();
      }
    }

  }

  public void checkCommitCount() {
    if (expectedCommitCount > 0 && transactionalResource.getCommittedCount() != expectedCommitCount) {
      throw new RuntimeException("expected commit count = " + expectedCommitCount + " actual = "
          + transactionalResource.getCommittedCount());
    }
  }
  
  public void checkRollbackCount() {
    if (expectedRollbackCount > 0 && transactionalResource.getRolledBackCount() != expectedRollbackCount) {
      throw new RuntimeException("expected rollback count = " + expectedCommitCount + " actual = "
          + transactionalResource.getRolledBackCount());
    }
  }

  public int getCommitCount() {
    return transactionalResource.getCommittedCount();
  }

  public int getRollbackCounter(){
    return transactionalResource.getRolledBackCount();
  }

  private void record(String line) {
    log.debug("received [" + line + "]");
    output.add(line);
    if (transactionalResource != null) {
      transactionalResource.incrementRecordCount();
    }
  }

  public Object getResource() {
    return transactionalResource;
  }

  public void setExpectedCommitCount(int expectedCommitCount) {
    this.expectedCommitCount = expectedCommitCount;
  }

  public void setExpectedRollbackCount(int expectedRollbackCount) {
    this.expectedRollbackCount = expectedRollbackCount;
  }
  
  public void setTransacted(boolean transactional) {
    if (transactional) {
      transactionalResource = new TestTransactionalResource("Transactional resource for " + getId());
    } else {
      transactionalResource = null;
    }
  }

}
