package org.openadaptor.core.transaction;

import org.openadaptor.core.transaction.ITransactionalResource;


public class TestTransactionalResource implements ITransactionalResource {

  private int committed = 0;
  private int rolledBack = 0;
  private int pending = 0;
  
  private String name;

  public TestTransactionalResource() {
  }
  
  public TestTransactionalResource(String name) {
    this.name = name;
  }

  public void begin() {
  }

  public void commit() {
    committed += pending;
    pending = 0;
  }

  public void rollback(Throwable t) {
    rolledBack += pending;
    pending = 0;
  }
  
  public int getCommittedCount() {
    return committed;
  }
  
  public int getRolledBackCount() {
    return rolledBack;
  }
  
  public void incrementRecordCount() {
    pending++;
  }
}
