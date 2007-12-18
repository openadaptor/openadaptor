/**
 * 
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
	    try {
	      log.debug("Sleeping for " + intervalMs + " before next read.");	
	      Thread.sleep(intervalMs);
	    } catch (InterruptedException e) {
	      /* ignores errors */
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
	
}
