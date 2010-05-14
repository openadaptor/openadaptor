/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.thirdparty.tibco;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ConnectionException;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvRvdTransport;
import com.tibco.tibrv.TibrvTransport;
/**
 * This class models a Tibco Rendezvous connection.
 * See the Tibco Rendevous documentation for further information.
 * 
 * @author Eddy Higgins (higginse)
 */
public class TibrvConnection extends Component {
  private static final Log log = LogFactory.getLog(TibrvConnection.class);
  private String service;

  private String network;

  private String daemon;

  private TibrvTransport transport;

  public void setDaemon(final String daemon) {
    this.daemon = daemon;
  }

  public void setNetwork(final String network) {
    this.network = network;
  }

  public void setService(final String service) {
    this.service = service;
  }

  /**
   * Connect to Tibco Rendezvous transport
   */
  private void initialiseTransport() {
    if (transport == null) {
      log.info("Initialising transport");
      try {
        Tibrv.open(Tibrv.IMPL_NATIVE);
      } 
      catch (TibrvException e) {
        throw new ConnectionException("failed to initialise rendezvous, ensure PATH / LD_LIBRARY_PATH contains tibco resources", e, this);
      }
      try {
        transport = new TibrvRvdTransport(service, network, daemon);
      } 
      catch (TibrvException e) {
        throw new ConnectionException("failed to create tibrv transport", e, this);
      }
      log.info("Transport initialiased");
    }
  }

  public void connect() {
    initialiseTransport();
  }
  public void disconnect() {
    if (transport!=null) {
      transport.destroy();
    }
  }

  public TibrvListener createListener(String topic, TibrvReadConnector connector) throws TibrvException  {
    if (transport==null) {
      initialiseTransport();
    }
    return new TibrvListener(Tibrv.defaultQueue(), connector, transport, topic, null);
  }
  /**
   * Send a message via the transport.
   * Note that for efficiency,it assumes that the transport has already been initialised
   * 
   * @param msg TibrvMsg instance to send
   * @throws TibrvException if send fails for any reason
   */
  public void send(TibrvMsg msg) throws TibrvException {
    transport.send(msg);
  }
}
