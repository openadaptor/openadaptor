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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IPollingReadConnector;
import org.openadaptor.core.IReadConnector;

/**
 * A polling read connector that calls to underlying read connector at fixed time 
 * intervals. When this connector polls it calls the following on the IReadConnector it is
 * wrapping...
 * {@link IReadConnector#next(long)}
 * 
 * The throttling occurs between calls to {@link IReadConnector#next(long)}.
 * Note that the difference between this connector and {@link ILoopingPollingReadConnector}
 * and {@link ICronnablePolingReadConnector} is that this connector operates between
 * single calls to #next(long) by the underlying reader whereas the other two treat 
 * all calls #next(long) until #isDry() is true, as one read operation. 
 * This connector does not connect or disconnect the underlying reader.
 * 
 * @author Kris Lachor
 * @see CronnablePollingReadConnector
 * @see LoopingPollingReadConnector
 */
public class ThrottlingReadConnector implements IPollingReadConnector {

	private static final Log log = LogFactory.getLog(ThrottlingReadConnector.class);
	
	private IReadConnector delegate;
	
	private long intervalMs = -1;
	
	private long pauseOnlyAfterMsgs = -1;
	
	private long msgCounter = 0;
	
	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IPollingReadConnector#getDelegate()
	 */
	public IReadConnector getDelegate() {
		return this.delegate;
	}

	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IReadConnector#connect()
	 */
	public void connect() {
		delegate.connect();
	}

	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IReadConnector#disconnect()
	 */
	public void disconnect() {
		delegate.disconnect();
	}

	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IReadConnector#getReaderContext()
	 */
	public Object getReaderContext() {
		return delegate.getReaderContext();
	}

	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IReadConnector#isDry()
	 */
	public boolean isDry() {
		return delegate.isDry();
	}

	/**
	 * 
	 * @see org.openadaptor.core.IReadConnector#next(long)
	 */
	public Object[] next(long timeoutMs) {	
		if(intervalMs != -1){
		    try {
		      log.debug("Sleeping for " + intervalMs + " before next read.");	
		      Thread.sleep(intervalMs);
		    } catch (InterruptedException e) {
		      /* ignores errors */
		    }
		}
		if(pauseOnlyAfterMsgs != -1){
		    msgCounter ++;	
		}
		return delegate.next(timeoutMs);
	}

	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IReadConnector#setReaderContext(java.lang.Object)
	 */
	public void setReaderContext(Object context) {
		delegate.setReaderContext(context);
	}

	/**
	 * Forwards the call to the underlying reader.
	 * 
	 * @see org.openadaptor.core.IReadConnector#validate(java.util.List)
	 */
	public void validate(List exceptions) {
		delegate.validate(exceptions);
	}
	
	/**
   * Optional.
   * 
   * @param intervalMs set the adaptor to poll every X milliseconds
   */
  public void setPollIntervalMs(long intervalMs) {
    this.intervalMs = intervalMs;
  }

  /**
   * Optional
   * 
   * @param interval set the adaptor to poll ever X seconds
   */
  public void setPollIntervalSecs(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalSecs] will take precedence");

    this.intervalMs = interval * 1000;
  }
  
  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X minutes
   */
  public void setPollIntervalMins(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalMins] will take precedence");

    this.intervalMs = interval * 60 * 1000;
  }

  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X hpurs
   */
  public void setPollIntervalHours(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalHours] will take precedence");

    this.intervalMs = interval * 60 * 60 * 1000;
  }
  
  /**
   * @return time interval between two subsequent calls to the underlying connector 
   *         #next() (in milliseconds)
   */
  public long getPollIntervalMs() {
    return intervalMs;
  }

  /**
   * Sets the reader connector that this reader wraps.
   * 
   * @param delegate
   */
  public void setDelegate(IReadConnector delegate) {
	this.delegate = delegate;
  }
	
}
