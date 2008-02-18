package org.openadaptor.core.connector;

import org.openadaptor.core.connector.CronnablePollingReadConnector;

import junit.framework.TestCase;

/**
 * System tests for {@link CronnablePollingReadConnector}.
 * 
 * @author Fred Perry, Kris Lachor
 */
public class CronnablePollingReadConnectorTestCase extends TestCase {

  TestReadConnector reader = new TestReadConnector("reader");
  
  CronnablePollingReadConnector poller = new CronnablePollingReadConnector("poller");
  
  protected void setUp() throws Exception {
    super.setUp();
    reader.setDataString("foobar");
    poller.setDelegate(reader);
  }
  
  /**
   * Tests 'out of the box' behaviour.
   */
//  public void testDefault() {    
//    assertTrue(runPoller(poller, reader.getDataString()) == 1);
//  }
  
  public void testCron() {
    poller.setCronExpression("0,5,10,15,20,25,30,35,40,45,50,55 * * * * ?");
    poller.setPollLimit(2);
    poller.setForceInitialPoll(false);
//    Date start = new Date();
    assertTrue(runPoller(poller, reader.getDataString()) == 2);
//    Date stop = new Date();
//    long durationMs = stop.getTime() - start.getTime();
//    /* Polling should've taken at least 2 secs */
//    assertTrue(durationMs >= 5000);
  }

  public void testCronForceInitialPoll() {
    poller.setCronExpression("0,5,10,15,20,25,30,35,40,45,50,55 * * * * ?");
    poller.setPollLimit(2);
    poller.setForceInitialPoll(true);
    assertTrue(runPoller(poller, reader.getDataString()) == 2);
  }

  private int runPoller(CronnablePollingReadConnector poller, String dataString) {
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
