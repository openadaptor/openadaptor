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

package org.openadaptor.core.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.transaction.ITransactional;
import org.openadaptor.core.transaction.ITransactionalResource;

/**
 * Base class for connectors that don't really poll. This provides core implementation
 * for queuing data as it arrives from the specific transport (socket, webservice, http, etc...)
 * The data is dequeued by calls to next().
 * 
 * By default it is configured to be transactional which mean that calls to enqueue data block 
 * until the transaction completes (if the transaction does not commit then runtime exception is thrown).
 * 
 * By default the queue size is unlimited, but a queue limit can be set, as can the behaviour
 * for if the queue size ever reaches the that limit.
 * 
 * @author perryj
 *
 */
public abstract class QueuingReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(QueuingReadConnector.class);
  private int batchSize = 1;
  private boolean isTransacted = true;
  private List queue = new ArrayList();
  private int queueLimit  = 0;
  private boolean blockOnQueue = true;
  private Object LOCK = new Object();
  private MyTransactionalResource resource = new MyTransactionalResource();
  
  protected QueuingReadConnector() {
  }

  protected QueuingReadConnector(String id) {
    super(id);
  }

  public void validate(List exceptions) {
  }
  
  /**
   * The max number of data elements to dequeue in a single call to next(), defaults to 1
   * @param batchSize
   */
  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }
  
  /**
   * if true then calls to enqueue block until the current transaction is completed
   * @param isTransacted
   */
  public void setTransacted(final boolean isTransacted) {
    this.isTransacted = isTransacted;
  }
  
  /**
   * set the max number of data elements that can be queue, the behaviour of what happens
   * when this is reached is controlled by the blockOnQueue property. Defaults to zero
   * which means unlimited queue size.
   * @param limit
   */
  public void setQueueLimit(int limit) {
    this.queueLimit = limit;
  }
  
  /**
   * if set then the call to enqueue data will block if the queue is full. 
   * @param block
   */
  public void setBlockOnQueue(boolean block) {
    this.blockOnQueue = block;
  }
  
  /**
   * currently synchronized because there is only one transactional resource
   * @param data
   */
  protected synchronized void enqueue(Object data) {
    synchronized (LOCK) {
      while (queueLimit > 0 && queue.size() >= queueLimit) {
        if (blockOnQueue) {
          try {
            log.debug("queue full");
            LOCK.wait();
          } catch (InterruptedException e) {
          }
        } else {
          log.error(getId() + " queue size has exceeded limit, discarding data");
          return;
        }
      }
      queue.add(queue.size(), data);
      if (log.isDebugEnabled()) {
        log.debug(getId() + " queued data"); 
      }
      LOCK.notify();
    }
    
    // if the controller is transacted then wait for transaction to complete
    if (isTransacted) {
      synchronized (resource) {
        try {
          resource.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException("thread interupted whilst waiting for transaction to complete");
        }
        if (resource.rolledBack()) {
          throw new RuntimeException(resource.getErrorOrException());
        }
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
        LOCK.notify();
        return data;
      } else {
        return null;
      }
    }
  }

  public Object getResource() {
    if (isTransacted) {
      return resource;
    } else {
      return null;
    }
  }

  //
  // inner class that integrates with openadaptor transaction management
  //
  
  class MyTransactionalResource implements ITransactionalResource {

    Throwable errorOrException = null;
    
    public void begin() {
      errorOrException = null;
    }

    public synchronized void commit() {
      notify();
    }

    public synchronized void rollback(Throwable t) {
      errorOrException = t != null ? t : new RuntimeException("rolled back with no exception chek logs");
      notify();
    }
    
    public Throwable getErrorOrException() {
      return errorOrException;
    }
    
    public boolean rolledBack() {
      return errorOrException != null;
    }
  }
}
