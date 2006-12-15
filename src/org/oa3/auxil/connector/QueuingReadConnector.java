package org.oa3.auxil.connector;

import java.util.ArrayList;
import java.util.List;

import org.oa3.core.Component;
import org.oa3.core.IReadConnector;

public abstract class QueuingReadConnector extends Component implements IReadConnector {

  private int batchSize = 1;
  private List queue = new ArrayList();
  private Object LOCK = new Object();
  
  protected QueuingReadConnector() {
  }

  protected QueuingReadConnector(String id) {
    super(id);
  }
  
  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }
  
  protected void enqueue(Object data) {
    synchronized (LOCK) {
      queue.add(queue.size(), data);
      LOCK.notify();
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
      while (queue.size() == 0) {
        try {
          LOCK.wait(timeoutMs);
        } catch (InterruptedException e) {
        }
      }
      Object[] data = new Object[Math.min(batchSize, queue.size())];
      for (int i = 0; i < data.length; i++) {
        data[i] = queue.remove(0);
      }
      return data;
    }
  }

}
