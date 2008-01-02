/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
 */
package org.openadaptor.core.connector;

import java.util.Date;

import org.openadaptor.core.IReadConnector;

import junit.framework.TestCase;

/**
 * System tests for {@link ThrottlingReadConnector}.
 * 
 * @author Kris Lachor
 */
public class ThrottlingReadConnectorTestCase extends TestCase {

  TestReadConnector reader = new TestReadConnector("reader");
  
  ThrottlingReadConnector throttlingReader = new ThrottlingReadConnector("throttlingReader");
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    reader.setDataString("foobar");
    throttlingReader.setDelegate(reader);
  }

  /**
   * Tests 'out of the box' behaviour.
   */
  public void testDefault() {    
    assertTrue(runThrottlingReader(throttlingReader, reader.getDataString()) == 1);
  }
  
  /**
   * Tests interval.
   * Sets pollIntervalMs.
   */
  public void testInterval() {   
    reader.setMaxSend(5);
    throttlingReader.setPollIntervalMs(100);
    Date start = new Date();
    assertTrue(runThrottlingReader(throttlingReader, reader.getDataString()) == 5);
    Date stop = new Date();
    long durationMs = stop.getTime() - start.getTime();
    assertTrue(durationMs >= 500);
    assertTrue(durationMs < 1000);
  }
  
  /**
   * Tests interval.
   * Sets pollIntervalMs and pauseOnlyAfterMsgs.
   */
  public void testInterval2() {   
    reader.setMaxSend(5);
    throttlingReader.setPollIntervalMs(100);
    throttlingReader.setPauseOnlyAfterMsgs(2);
    Date start = new Date();
    assertTrue(runThrottlingReader(throttlingReader, reader.getDataString()) == 5);
    Date stop = new Date();
    long durationMs = stop.getTime() - start.getTime();
    assertTrue(durationMs >= 200);
    assertTrue(durationMs < 500);
  }
  
  private int runThrottlingReader(IReadConnector poller, String dataString) {
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
