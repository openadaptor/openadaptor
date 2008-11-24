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

package org.openadaptor.core.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.node.WriteNode;

/**
 * Basic (and default) implementation of {@link ITransaction} that collects and invokes
 * {@link ITransactionalResource}s.
 * 
 * @author perryj
 * 
 */
public class Transaction extends AbstractTransaction {
  
  private static final Log log = LogFactory.getLog(Transaction.class);

  private boolean rollbackOnly = false;
  private List resources =  new ArrayList();
  private List resourcesForRollback = new ArrayList();
  private Object LOCK = new Object();
  
  public Transaction(final long id, final long timeoutMs) {
  }
  
  public void commit() {
    if (!rollbackOnly) {
      synchronized (LOCK) {
        for (Iterator iter = resourcesForRollback.iterator(); iter.hasNext();) {
          ITransactionalResource resource = (ITransactionalResource) iter.next();
          resource.rollback(new RuntimeException("Marked for Rollback by Connector"));
        }
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
    if (resource != null) {
    synchronized (LOCK) {
      try {        
        boolean resourceWasEnlisted = resources.remove(resource); // Can only rollback resource if it was enlisted in the first place.
        if (resourceWasEnlisted) {
          resourcesForRollback.add(resource);
        }
      } catch (RuntimeException e) {
        throw new RuntimeException("Unable to mark transaction resource for Rollback: ["+resource+"]", e);
      }
    }
    }
  }

  public void enlist(Object resource) {
    if (resource != null) {
      if (resource instanceof ITransactionalResource) {
        synchronized (LOCK) {
          if (!resources.contains(resource)) {
            ((ITransactionalResource) resource).begin();
            resources.add(resources.size(), resource);
          }
        }
      } else {
        throw new RuntimeException(
            "attempt to enlist resource that is not an instance of "
                + ITransactionalResource.class.getName()
                + " it is an instance of " + resource.getClass().getName());
      }
    } else {
      log.warn("Not enlisting a NULL transaction resource.");
    }
  }

  public void rollback() {
    synchronized (LOCK) {
      for (Iterator iter = resourcesForRollback.iterator(); iter.hasNext();) {
        ITransactionalResource resource = (ITransactionalResource) iter.next();
        resource.rollback(getErrorOrException());
      }
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
