package org.openadaptor.core.connector;

import org.openadaptor.core.connector.PollingReadConnector;

import junit.framework.TestCase;

public class PollingReadConnectorTestCase extends TestCase {

  public void testDefault() {
    TestReadConnector reader = new TestReadConnector("reader");
    reader.setDataString("foobar");
    PollingReadConnector poller = new PollingReadConnector("poller");
    poller.setDelegate(reader);
    assertTrue(runPoller(poller, reader.getDataString()) == 1);
  }

  public void testLimit() {
    TestReadConnector reader = new TestReadConnector("reader");
    reader.setDataString("foobar");
    PollingReadConnector poller = new PollingReadConnector("poller");
    poller.setDelegate(reader);
    poller.setPollLimit(5);
    assertTrue(runPoller(poller, reader.getDataString()) == 5);
  }

//  public void testInterval() {
//    TestReadConnector reader = new TestReadConnector("reader");
//    reader.setDataString("foobar");
//    PollingReadConnector poller = new PollingReadConnector("poller");
//    poller.setDelegate(reader);
//    poller.setPollLimit(5);
//    poller.setPollIntervalSecs(2);
//    assertTrue(runPoller(poller, reader.getDataString()) == 5);
//  }

  public void testCron() {
    TestReadConnector reader = new TestReadConnector("reader");
    reader.setDataString("foobar");
    PollingReadConnector poller = new PollingReadConnector("poller");
    poller.setDelegate(reader);
    poller.setCronExpression("0,5,10,15,20,25,30,35,40,45,50,55 * * * * ?");
    poller.setPollLimit(6);
    poller.setForceInitialPoll(true);
    assertTrue(runPoller(poller, reader.getDataString()) == 6);
  }

  public void testCronForceInitialPoll() {
    TestReadConnector reader = new TestReadConnector("reader");
    reader.setDataString("foobar");
    PollingReadConnector poller = new PollingReadConnector("poller");
    poller.setDelegate(reader);
    poller.setCronExpression("0,5,10,15,20,25,30,35,40,45,50,55 * * * * ?");
    poller.setForceInitialPoll(true);
    poller.setPollLimit(2);
    assertTrue(runPoller(poller, reader.getDataString()) == 2);
  }

  private int runPoller(PollingReadConnector poller, String dataString) {
    int count = 0;
    poller.connect();
    while (!poller.isDry()) {
      Object[] data = poller.next(1000);
      if (data != null) {
        assertTrue(data.length == 1);
        assertTrue(data[0].equals(dataString));
        count++;
      } else {
      }
    }
    return count;
  }

}
