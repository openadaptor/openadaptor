package org.openadaptor.core.connector;

import org.openadaptor.core.connector.CronnablePollingReadConnector;

import junit.framework.TestCase;

/**
 * System tests for {@link CronnablePollingReadConnector}.
 * 
 * @author OA3 Core Team
 */
public class CronnablePollingReadConnectorTestCase extends TestCase {
  //Default cron expression to use for the tests...
	//public static final String DEFAULT_CRON_EXPRESSION="0,5,10,15,20,25,30,35,40,45,50,55 * * * * ?";
	public static final String DEFAULT_CRON_EXPRESSION="0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58 * * * * ?";
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
    poller.setCronExpression(DEFAULT_CRON_EXPRESSION);
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
    poller.setCronExpression(DEFAULT_CRON_EXPRESSION);
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
      } 
      else {
      }
    }
    return count;
  }

}
