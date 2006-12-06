package org.oa3.core.transaction;

import org.oa3.core.transaction.ITransactionalResource;


public class TestTransactionalResource implements ITransactionalResource {

  private int committed = 0;
  private int pending = 0;

  public void begin() {
  }

  public void commit() {
    committed += pending;
    pending = 0;
  }

  public void rollback() {
    pending = 0;
  }
  
  public int getCommittedCount() {
    return committed;
  }
  
  public void incrementRecordCount() {
    pending++;
  }
}
