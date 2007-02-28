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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.transaction.ITransactional;
import org.openadaptor.core.transaction.ITransactionalResource;

/**
 * Base class for connectors that don't really poll. This provides the core
 * implementation for queuing data as it arrives from the specific transport
 * (socket, webservice, http, etc...) The data is dequeued by calls to next().
 * 
 * Calls to {@link #enqueue} block until the data is dequeued. If data
 * is dequeued within a transaction (i.e.
 * {@link ITransactionalResource#begin()} has been called on this component's 
 * {@link ITransactionalResource}) then the call to enqueue will block until the
 * transaction completes.
 * 
 * By default the queue size is unlimited, but a queue limit can be set, as can
 * the behaviour for if the queue size ever reaches that limit.
 * 
 * @author perryj
 * 
 */
public abstract class QueuingReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(QueuingReadConnector.class);
  
  /**
   * max number of elements in the data array to process in one go
   * @see #next(long)
   */
  private int batchSize = 1;
  
  /**
   * controls whether this components provides an {@link ITransactionalResource} that
   * can be enlisted in a transaction
   * @see #isTransacted
   */
  private boolean isTransacted = true;
  
  /**
   * internal queue where the data is held
   */
  private List queue = new ArrayList();
  
  /**
   * maximum size of the queue
   */
  private int queueLimit  = 0;
  
  /**
   * controls whether calls to enqueue block when the queue is full
   * @see #enqueue(Object)
   */
  private boolean blockOnQueue = true;

  /**
   * implementation of {@link ITransactionalResource}, which if the component is
   * transacted will be returned when {@link #getResource()} is called
   */
  private QueueTransactionalResource resource = new QueueTransactionalResource();
  
  
  protected QueuingReadConnector() {
  }

  protected QueuingReadConnector(String id) {
    super(id);
  }

  /**
   * The max number of data elements to dequeue in a single call to next(), defaults to 1
   */
  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }
  
  /**
   * if true then this instance implements {@link ITransactional} by 
   * return a {@link ITransactionalResource}
   */
  public void setTransacted(final boolean isTransacted) {
    this.isTransacted = isTransacted;
  }
  
  /**
   * set the max number of items that can be queued, the behaviour of what happens
   * when this is reached is controlled by the blockOnQueue property. Defaults to zero
   * which means unlimited queue size.
   */
  public void setQueueLimit(int limit) {
    this.queueLimit = limit;
  }
  
  /**
   * if set then the call to enqueue data will block if the queue is full, 
   * otherwise and exception is thrown.
   */
  public void setBlockOnQueue(boolean block) {
    this.blockOnQueue = block;
  }
  
  /**
   * adds some data to the queue, blocks until the data is dequeued
   */
  protected void enqueue(Object data) {
    
    QueueItem item = new QueueItem(data);
    
    synchronized (queue) {
      while (queueLimit > 0 && queue.size() >= queueLimit) {
        if (blockOnQueue) {
          try {
            log.debug("queue full");
            queue.wait();
          } catch (InterruptedException e) {
            throw new RuntimeException("thread interupted whilst waiting queue data");
          }
        } else {
          throw new RuntimeException("queue size has exceeded limit, discarding data");
        }
      }
      queue.add(queue.size(), item);
      if (log.isDebugEnabled()) {
        log.debug(getId() + " queued data"); 
      }
      queue.notify();
    }
    
    item.waitForDequeueNotification();
  }

  /**
   * this dequeues the data
   */
  public Object[] next(long timeoutMs) {
    synchronized (queue) {
      
      if (queue.size() == 0) {
        try {
          queue.wait(timeoutMs);
        } catch (InterruptedException e) {
        }
      }

      Object[] data = new Object[Math.min(batchSize, queue.size())];
      for (int i = 0; i < data.length; i++) {
        QueueItem item = (QueueItem) queue.remove(0);
        resource.add(item);
        data[i] = item.data;
      }
      
      if (data.length > 0) {
        queue.notify();
        return data;
      } else {
        return null;
      }
    }
  }

  protected boolean queueIsEmpty() {
    synchronized (queue) {
      return queue.isEmpty();
    }
  }
  
  public Object getReaderContext() {
    return null;
  }

  public boolean isDry() {
    return false;
  }

  public Object getResource() {
    if (isTransacted) {
      return resource;
    } else {
      return null;
    }
  }

  /**
   * {@link ITransactionalResource} implementation. One of these is returned if
   * the ReadConnector is transacted. When {@link QueueItem}s are dequeued they
   * are added to an instance of this class. This class will set the processing
   * state of the QueueItem and allow the queueing thread block for that event.
   * If a transaction has not begun then it simply sets the state to COMMITTED
   * straight away. However if a transaction has been begin then the state will
   * only be set once commit/rollback is called on this resource.
   */
  class QueueTransactionalResource implements ITransactionalResource {

    private boolean inTransaction = false;
    private List items = new ArrayList();
    
    public synchronized void begin() {
      if (inTransaction) {
        throw new RuntimeException("attempt to begin a transaction whilst one is already in progress");
      }
      inTransaction = true;
    }

    public synchronized void commit() {
      for (Iterator iter = items.iterator(); iter.hasNext();) {
        QueueItem item = (QueueItem) iter.next();
        item.complete();
      }
      items.clear();
      inTransaction = false;
    }

    public synchronized void rollback(Throwable t) {
      for (Iterator iter = items.iterator(); iter.hasNext();) {
        QueueItem item = (QueueItem) iter.next();
        item.fail(t);
      }
      items.clear();
      inTransaction = false;
    }
    
    private synchronized void add(QueueItem item) {
      if (inTransaction) {
        items.add(item);
      } else {
        item.complete();
      }
    }
  }
  
  /**
   * wrapper class that holds data to be processed, allows queueing and dequeing thread
   * to synchronise processing and communicate status.
   */

  class QueueItem {
    
    Object data;
    boolean completed = false;
    boolean failed = false;
    Throwable throwable = null;
    
    QueueItem(Object data) {
      this.data = data;
    }
    
    synchronized void complete() {
      completed = true;
      notifyAll();
    }
    
    synchronized void fail(Throwable t) {
      throwable = t;
      completed = true;
      failed = true;
      notifyAll();
    }
    
    synchronized void waitForDequeueNotification() {
      while (!completed) {
        try {
          log.debug("waiting for processing to complete");
          wait();
        } catch (InterruptedException e) {
          throw new RuntimeException("interupted waiting for processing to complete", e);
        }
      }
      if (failed) {
        throw new RuntimeException("processing failed", throwable);
      }
    }
  }
}
