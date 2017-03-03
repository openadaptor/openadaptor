/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.thirdparty.mq;

/*
 * File: $Header$ Rev: $Revision$
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.transaction.ITransactional;

import java.util.List;

/**
 * Read Connector that gets data from an IBM MQ Series queue. Needs to be configured with an
 * {@link MqConnection}.
 * 
 * @see MqConnection
 * 
 */
public class MqReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(MqReadConnector.class);

  /**
   * MQ character set switch. If true, and message character set is 285 UK EBCDIC, then switch to character set 37 US
   * EBCDIC. This allows workaround if mq libraries do not support UK EBCDIC since numbers, letters, space, +, - and
   * fullstop are all the same
   */
  protected boolean _UK_US_EBCDIC_Switch = true;

  protected MqConnection connection = null;

  public MqReadConnector() {
  }

  public MqReadConnector(String id) {
    super(id);
  }

  public MqConnection getConnection() {
    return connection;
  }

  public void setConnection(MqConnection connection) {
    this.connection = connection;
  }

  public void connect() {
    if (getConnection() == null)
      throw new ConnectionException("No MqConnection configured", this);
    getConnection().setId(getId() + "_Connection");
    getConnection().connectToMQ(true);
    log.debug(getId() + " connected");
  }

  public void disconnect() {
    getConnection().close();
    log.debug(getId() + " disconnected");
  }

  public void validate(List exceptions) {
  }
  
  public Object getResource() {
    return getConnection().getResource();
  }

  public Object[] next(long timeoutMs) throws OAException {
    Object data = getConnection().nextRecord(timeoutMs);
    if (data != null) {
      log.debug(getId() + " got jms message");
    }
    return data != null ? new Object[] {data} : null;
  }

  public Object getReaderContext() {
    return null;
  }
  
  public void setReaderContext(Object context) {
  }

  public boolean isDry() {
    return false;
  }
}
