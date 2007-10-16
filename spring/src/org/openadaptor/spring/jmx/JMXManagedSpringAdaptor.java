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
package org.openadaptor.spring.jmx;

import org.openadaptor.spring.SpringAdaptor;

/**
 * A SpringAdaptor exposable as a JMX mbean compliant with JBoss service contract.
 * This is meant to be used when OA is deployed to a JEE application server (tested
 * on JBoss) and allow to start the adaptor via a JMX console.
 * 
 * @author Kris Lachor
 */
public class JMXManagedSpringAdaptor extends SpringAdaptor implements JMXManagedSpringAdaptorMBean {
  
  private boolean isStarted = false;
  
  /**
   * @param name of the Spring config file that defines the adaptor
   * @see org.openadaptor.spring.jmx.JMXManagedSpringAdaptorMBean#runSpringAdaptor()
   */
  public void runSpringAdaptor(String configFileName){
    String [] args = new String[] {CONFIG, configFileName};
    SpringAdaptor app = new SpringAdaptor();
    app.execute(args);
  }
 
  /**
   * @see org.openadaptor.spring.jmx.JMXManagedSpringAdaptorMBean#isStarted()
   */
  public boolean isStarted() {
    return isStarted;
  }

  /**
   * @see org.openadaptor.spring.jmx.JMXManagedSpringAdaptorMBean#start()
   */
  public void start() {
    isStarted = true;
  }

  /**
   * @see org.openadaptor.spring.jmx.JMXManagedSpringAdaptorMBean#stop()
   */
  public void stop() {
    isStarted = false;
  }
}
