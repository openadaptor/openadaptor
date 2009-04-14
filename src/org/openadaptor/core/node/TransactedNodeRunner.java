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

package org.openadaptor.core.node;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Response;
import org.openadaptor.core.Message;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionManager;
import org.openadaptor.core.transaction.ITransactionInitiator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 30, 2007 by oa3 Core Team
 */

public class TransactedNodeRunner extends AbstractNodeRunner implements ITransactionInitiator {

  private static final Log log = LogFactory.getLog(TransactedNodeRunner.class);
  private ITransactionManager transactionManager;

  public void run() {
    if (!isStillRunning()) {
      log.warn(getId() + " has not been started");
      exitCode = 0;
    }
    ITransaction transaction = null;
    try {
      log.info(getId() + " running");
      while (isStillRunning()) {
        if ((transaction == null) && (getTransactionManager() != null)) {
          transaction = getTransactionManager().getTransaction();
        }
        Response response = target.process(new Message(new Object[]{}, null, transaction));        
        log.debug("Response is: " + response);
        if (transaction != null) {
          if (transaction.getErrorOrException() == null) {
            log.debug(getId() + " committing transaction");
            transaction.commit();
          } else {
            log.info(getId() + " rolling back transaction");
            transaction.rollback();
          }
          transaction = null;
        }
      }
    }
    catch (Throwable e) {
      exitCode = 1;
      exitThrowable = e;
      log.error(getId() + " uncaught exception, rolling back transaction and stopping", e);
      if (transaction != null) {
        transaction.setErrorOrException(e);
        transaction.rollback();       
      }
      stop();
    }
    finally {
      log.info(getId() + " no longer running");
      stop();
    }
  }

  public void setTransactionManager(ITransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public ITransactionManager getTransactionManager() {
    return transactionManager;
  }
 
}
