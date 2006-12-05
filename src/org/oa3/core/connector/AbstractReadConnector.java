/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.core.connector;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/AbstractReadConnector.java,v 1.24 2006/10/18 14:32:51 kscully Exp $
 * Rev: $Revision: 1.24 $ Created Jul 26, 2005 by Kevin Scully
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.Component;
import org.oa3.core.IReadConnector;
import org.oa3.core.adaptor.IAdaptorInpoint;
import org.oa3.core.exception.OAException;

/**
 * Abstract superclass that implements common functionality for Read Connectors.
 * 
 * @author OA3 Core Team
 */
public abstract class AbstractReadConnector extends Component implements IReadConnector {
  private static final Log log = LogFactory.getLog(AbstractReadConnector.class);

  public static final int DEFAULT_TIMEOUT = 60 * 1000;

  protected boolean connected;

  protected int timeout = DEFAULT_TIMEOUT;

  /**
   * This holds the polling configuration for this connector. It may be null, in which case polling is not configured.
   */
  protected IPollingConfiguration pollingConfiguration;

  /**
   * Receive next Message - needs to be reviewed as part of analysing the Connector-to-InPoint interfaces.
   * 
   * @return an array of records to be processed
   */
  public Object[] next(long timeoutMs) throws OAException {
    log.debug("Fetching next record...");
    Object[] result = nextRecord(timeoutMs);

    // If we didn't get a resut, maybe we should be polling
    boolean polling = (result == null) && (pollingConfiguration != null);
    if (polling) {
      // Reset the poll counter
      pollingConfiguration.resetPollCount();
    }
    while (polling) {
      log.debug("No data fetched, attempting refresh...");
      refreshData(); // Attempt to load new data
      result = nextRecord(timeoutMs); // Get the next record
      polling = result == null;
      if (polling) { // Nothing there wait for poll interval
        log.debug("Still no data. Waiting for poll period ...");
        // If pollWait returns false, then maxPollWait has been reached.
        polling = pollingConfiguration.pollWait();
      } else {
        // ToDo: could add enforced wait here - for situations where refresh always returns data.
        // it ends up as a tight loop if misconfigured.
      }
    }
    return result;
  }

  public void refreshData() throws OAException {
    log.info("Default refreshData() behaviour is a no-op");
  }

  /**
   * Receive next Message - needs to be reviewed as part of analysing the Connector-to-InPoint interfaces.
   * 
   * @return Object[] containing the next batch of records from this connector.
   */
  public abstract Object[] nextRecord(long timeoutMs) throws OAException;

  /**
   * Set the Source instance used as entry point to the Pipeline.
   * 
   * @param source
   */
  public void setInpoint(IAdaptorInpoint source) {
  }

  public IAdaptorInpoint getInpoint() {
    return null;
  }

  /**
   * Establish a connection to external message transport. If already connected then do nothing.
   * 
   * @throws org.oa3.control.OAException
   */
  public void connect() {
    log.debug("Connector: [" + getId() + "] connecting ....");
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   * 
   * @throws org.oa3.control.OAException
   */
  public void disconnect() {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
    connected = false;
    log.info("Connector: [" + getId() + "] disconnected");
  }

  /**
   * True if connected.
   * 
   * @return true if connected.
   */
  public boolean isConnected() {
    return connected;
  }

  public int getTimeout() {
    return timeout;
  }

  /**
   * returns the name of the component rather than Java's default class thingy
   */
  public String toString() {
    return getId();
  }

  /**
   * Defines the polling configuration in use for this connector
   * 
   * @see IPollingConfiguration for more details.
   * @return The configured PollingConfiguration.
   * @deprecated - Use PollManager instead
   */
  public IPollingConfiguration getPollingConfiguration() {
    return pollingConfiguration;
  }

  /**
   * 
   * @param pollingConfiguration
   * @deprecated - Use PollManager instead
   */
  public void setPollingConfiguration(IPollingConfiguration pollingConfiguration) {
    log.info("Setting polling configuration to :" + pollingConfiguration == null ? "<null>" : pollingConfiguration
        .toString());
    this.pollingConfiguration = pollingConfiguration;
  }

}