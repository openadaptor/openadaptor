package org.openadaptor.core.connector;

import junit.framework.TestCase;
import java.util.Date;

import org.openadaptor.core.IReadConnector;

/**
 * System tests for {@link LoopingPollingReadConnector}.
 * 
 * @author Kris Lachor
 */
public class LoopingPollingReadConnectorTestCase extends TestCase {
  
  TestReadConnector reader = new TestReadConnector("reader");
  
  LoopingPollingReadConnector poller = new LoopingPollingReadConnector("poller");
  
  protected void setUp() throws Exception {
    super.setUp();
    reader.setDataString("foobar");
    poller.setDelegate(reader);
  }

  /**
   * Tests 'out of the box' behaviour. Polls one time and quits.
   */
  public void testDefault() {    
    assertTrue(runPoller(poller, reader.getDataString()) == 1);
  }

  /**
   * Tests poll limit.
   */
  public void testLimit() {
    poller.setPollLimit(5);
    assertTrue(runPoller(poller, reader.getDataString()) == 5);
  }

  /**
   * Tests poll interval.
   */
  public void testInterval() {
    poller.setPollLimit(2);
    poller.setPollIntervalSecs(1);
    Date start = new Date();
    assertTrue(runPoller(poller, reader.getDataString()) == 2);
    Date stop = new Date();
    long durationMs = stop.getTime() - start.getTime();
    /* Polling should've taken at least 2 secs */
    assertTrue(durationMs >= 2000);
  }
  
  /**
   * Tests reconnectDelegateBetweenPolls = true (default).
   */
  public void testReconnectDelegateBetweenPollsTrue() {
    poller.setPollLimit(2);
    reader.setExpectedConnectCount(2);
    assertTrue(runPoller(poller, reader.getDataString()) == 2);
    reader.checkConnectCount();
  }
  
  /**
   * Tests reconnectDelegateBetweenPolls = false.
   */
  public void testReconnectDelegateBetweenPollsFalse() {
    poller.setPollLimit(2);
    poller.setReconnectDelegateBetweenPolls(false);
    reader.setExpectedConnectCount(1);
    /* The delegate will return one record only; after going dry it won't return anyting. */
    assertTrue(runPoller(poller, reader.getDataString()) == 1);
    reader.checkConnectCount();
  }

  
  private int runPoller(IReadConnector poller, String dataString) {
    int count = 0;
    poller.connect();
    while (!poller.isDry()) {
      System.out.println("Test: " + new Date());
      Object[] data = poller.next(500);
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
