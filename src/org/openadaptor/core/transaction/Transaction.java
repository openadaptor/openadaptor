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

package org.openadaptor.core.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Transaction extends AbstractTransaction {

  private boolean rollbackOnly = false;
  private List resources =  new ArrayList();
  private Object LOCK = new Object();
  
  public Transaction(final long id, final long timeoutMs) {
  }
  
  public void commit() {
    if (!rollbackOnly) {
      synchronized (LOCK) {
        for (Iterator iter = resources.iterator(); iter.hasNext();) {
          ITransactionalResource resource = (ITransactionalResource) iter.next();
          resource.commit();
        }
      }
    } else {
      throw new RuntimeException("transaction has been marked for rollback only");
    }
  }

  public void delistForCommit(Object resource) {
  }

  public void delistForRollback(Object resource) {
  }

  public void enlist(Object resource) {
    if (resource instanceof ITransactionalResource) {
      synchronized (LOCK) {
        if (!resources.contains(resource)) {
          ((ITransactionalResource)resource).begin();
          resources.add(resources.size(), resource);
        }
      }
    } else {
      throw new RuntimeException("attempt to enlist resource that is not an instance of " + ITransactionalResource.class.getName());
    }
  }

  public void rollback() {
    synchronized (LOCK) {
      for (Iterator iter = resources.iterator(); iter.hasNext();) {
        ITransactionalResource resource = (ITransactionalResource) iter.next();
        resource.rollback(getErrorOrException());
      }
    }
  }

  public void setRollbackOnly() {
    rollbackOnly = true;
  }

}
