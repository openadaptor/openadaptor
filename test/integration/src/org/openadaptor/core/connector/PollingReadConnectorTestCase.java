package org.openadaptor.core.connector;

import org.openadaptor.core.connector.PollingReadConnector;

import junit.framework.TestCase;

public class PollingReadConnectorTestCase extends TestCase {
  //Cron expression for every second.
  private static final String CRON_EXP=
    "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,"+
    "30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59"+
    " * * * * ?";
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
    poller.setCronExpression(CRON_EXP);
    poller.setPollLimit(3);
    poller.setForceInitialPoll(true);
    assertTrue(runPoller(poller, reader.getDataString()) == 3);
  }

  public void testCronForceInitialPoll() {
    TestReadConnector reader = new TestReadConnector("reader");
    reader.setDataString("foobar");
    PollingReadConnector poller = new PollingReadConnector("poller");
    poller.setDelegate(reader);
    poller.setCronExpression(CRON_EXP);
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
