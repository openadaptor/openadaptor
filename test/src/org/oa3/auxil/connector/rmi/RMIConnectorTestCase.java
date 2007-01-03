package org.oa3.auxil.connector.rmi;

import junit.framework.TestCase;

public class RMIConnectorTestCase extends TestCase {

  public void xtest() {
    RMIReadConnector reader = new RMIReadConnector("reader");
    reader.setCreateRegistry(true);
    reader.connect();
  }
  
}
