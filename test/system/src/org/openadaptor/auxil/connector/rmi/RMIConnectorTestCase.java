package org.openadaptor.auxil.connector.rmi;

import org.openadaptor.auxil.connector.rmi.RMIReadConnector;

import junit.framework.TestCase;

public class RMIConnectorTestCase extends TestCase {

  public void test() {
    RMIReadConnector reader = new RMIReadConnector("reader");
    reader.setCreateRegistry(true);
    reader.connect();
  }
  
}
