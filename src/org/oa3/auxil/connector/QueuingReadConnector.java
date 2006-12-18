package org.oa3.auxil.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.Component;
import org.oa3.core.IReadConnector;
import org.oa3.core.transaction.ITransactional;
import org.oa3.core.transaction.ITransactionalResource;

/**
 * base class for connectors that don't really poll. This provides core implementation
 * for queuing data as it arrives from the specific transport (socket, webservice, http, etc...)
 * If it is configured to be transactional then call to enqueue data blocks until transaction
 * completes (if the transaction does not commit then runtime exception is thrown).
 * 
 * @author perryj
 *
 */
public abstract class QueuingReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(QueuingReadConnector.class);
  private int batchSize = 1;
  private boolean isTransacted = false;
  private List queue = new ArrayList();
  private Object LOCK = new Object();
  private TransactionStatus transactionStatus = new TransactionStatus();
  
  protected QueuingReadConnector() {
  }

  protected QueuingReadConnector(String id) {
    super(id);
  }
  
  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }
  
  public void setTransacted(final boolean isTransacted) {
    this.isTransacted = isTransacted;
  }
  
  protected void enqueue(Object data) {
    synchronized (LOCK) {
      queue.add(queue.size(), data);
      if (log.isDebugEnabled()) {
        log.debug(getId() + " queued data"); 
      }
      LOCK.notify();
    }
    
    // if the controller is transacted then wait for transaction to complete
    if (isTransacted) {
      synchronized (transactionStatus) {
        try {
          transactionStatus.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException("thread interupted whilst waiting for transaction to complete");
        }
        if (transactionStatus.isRollback()) {
          throw new RuntimeException("transactioned rolledback, check server logs");
        }
        transactionStatus.reset();
      }
    }
  }
  
  protected boolean queueIsEmpty() {
    synchronized (LOCK) {
      return queue.isEmpty();
    }
  }
  
  public Object getReaderContext() {
    return null;
  }

  public boolean isDry() {
    return false;
  }

  public Object[] next(long timeoutMs) {
    synchronized (LOCK) {
      if (queue.size() == 0) {
        try {
          LOCK.wait(timeoutMs);
        } catch (InterruptedException e) {
        }
      }
      Object[] data = new Object[Math.min(batchSize, queue.size())];
      for (int i = 0; i < data.length; i++) {
        data[i] = queue.remove(0);
      }
      if (data.length > 0) {
        return data;
      } else {
        return null;
      }
    }
  }

  public Object getResource() {
    if (isTransacted) {
      return new MyTransactionalResource();
    } else {
      return null;
    }
  }

  //
  // inner class that integrates with oa3 transaction management
  //
  
  class MyTransactionalResource implements ITransactionalResource {

    public void begin() {
      transactionStatus.reset();
    }

    public void commit() {
      transactionStatus.setCommit();
    }

    public void rollback() {
      transactionStatus.setRollback();
    }
  }
  
  //
  // inner class used as flag to wait for transation to complete
  //
  
  private static final int COMMIT = 1;
  private static final int ROLLBACK = 2;
  
  class TransactionStatus {
    private int status = 0;
    
    synchronized void reset() {
      status = 0;
    }
    
    synchronized void setRollback() {
      status = ROLLBACK;
      this.notify();
    }
    synchronized void setCommit() {
      status = COMMIT;
      log.info("committed");
      this.notify();
    }

    boolean isCommit() {
      return status == COMMIT;
    }
    
    boolean isRollback() {
      return status == ROLLBACK;
    }
  }
}
