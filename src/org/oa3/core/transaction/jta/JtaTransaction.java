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

package org.oa3.core.transaction.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.oa3.core.transaction.AbstractTransaction;

public class JtaTransaction extends AbstractTransaction {

  protected Transaction transaction = null;

  protected JtaTransaction(final Transaction transaction) {
    this.transaction = transaction;
  }

  public void commit() {

    try {
      transaction.commit();
    } catch (RollbackException e) {
      rollback();
    } catch (HeuristicMixedException e) {
      throw new RuntimeException("HeuristicMixedException during commit", e);
    } catch (HeuristicRollbackException e) {
      throw new RuntimeException("HeuristicRollbackException during commit", e);
    } catch (SystemException e) {
      throw new RuntimeException("SystemException during transaction commit", e);
    }
  }

  public void delistForCommit(Object resource) {
    if (resource instanceof XAResource) {
      try {
        transaction.delistResource((XAResource) resource, XAResource.TMSUCCESS);
      } catch (SystemException e) {
        throw new RuntimeException("SystemException during delistment", e);
      }
    } else {
      throw new RuntimeException("resource is not XAResource");
    }
  }

  public void delistForRollback(Object resource) {
    if (resource instanceof XAResource) {
      try {
        transaction.delistResource((XAResource) resource, XAResource.TMFAIL);
      } catch (SystemException e) {
        throw new RuntimeException("SystemException during delistment", e);
      }
    } else {
      throw new RuntimeException("resource is not XAResource");
    }
  }

  public void enlist(Object resource) {
    if (resource instanceof XAResource) {
      try {
        transaction.enlistResource((XAResource) resource);
      } catch (SystemException e) {
        throw new RuntimeException("SystemException during enlist", e);
      } catch (IllegalStateException e) {
        throw new RuntimeException(
            "IllegalStateException exception during enlist, e");
      } catch (RollbackException e) {
        throw new RuntimeException("Rollback exception during enlist, e");
      }
    } else {
      throw new RuntimeException("resource is not XAResource");
    }
  }

  public void rollback() {
    try {
      transaction.rollback();
    } catch (SystemException e) {
      throw new RuntimeException("SystemException during transaction rollback", e);
    }
  }

  public void setRollbackOnly() {
    try {
      transaction.setRollbackOnly();
    } catch (SystemException e) {
      throw new RuntimeException("SystemException during enlist", e);
    } catch (IllegalStateException e) {
      throw new RuntimeException(
          "IllegalStateException exception during enlist, e");
    }
  }

}
