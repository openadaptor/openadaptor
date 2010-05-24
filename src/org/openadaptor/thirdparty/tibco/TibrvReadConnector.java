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

package org.openadaptor.thirdparty.tibco;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.exception.ConnectionException;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * Read Connector that subscribes to a tibco rendezvous topic / subject.
 * This has been completely rewritten as of release 3.4.5.
 * 
 * @since 3.4.5 Introduced as part of tibrv connector overhaul
 * 
 * @author Eddy Higgins (higginse)
 *
 */
public class TibrvReadConnector extends QueuingReadConnector implements TibrvMsgCallback {

  private static final Log log = LogFactory.getLog(TibrvReadConnector.class);

  private TibrvConnection connection;

  private Set topicNames = new HashSet();

  private Map listenerMap = new HashMap();

  private Dispatcher dispatcher; //Thread for interaction with tibrv

  //By default messages are just passed through opaquely.
  private ITibrvMessageDecoder decoder=null;

  public TibrvReadConnector() {
    super();
    super.setTransacted(false);
  }

  public TibrvReadConnector(String id) {
    super(id);
    super.setTransacted(false);
  }

  public void setTopic(final String topic) {
    topicNames.clear();
    topicNames.add(topic);
  }

  public void setTopics(final Set topics) {
    this.topicNames.clear();
    this.topicNames.addAll(topics);
  }

  public void setConnection(final TibrvConnection connection) {
    this.connection = connection;
  }

  /**
   * If a decoder {@link ITibrvMessageDecoder} is specified, it will be used to decode incoming raw TibrvMsg instances.
   * It is null by default, meaning that the raw message will be passed on through
   * the openadaptor pipeline.
   * @param decoder ITibrvMessageDecoder instance
   * 
   * @since 3.4.5 Introduced as part of rewrite of tibrv connectors
   */
  public void setDecoder(ITibrvMessageDecoder decoder) {
    this.decoder=decoder;
  }

  /**
   * if true then extracts string value from single field on incoming messages
   * otherwise queues actual TibrvMsg
   * @deprecated This only exists for backwards compatibility (pre 3.4.5)
   * 
   * @param decodeTibrvMsg
   */
  public void setDecodeTibrvMsg(final boolean decodeTibrvMsg) {
    OldTibrvMessageEncoderDecoder legacyDecoder;
    if (decoder==null) { //Create a legacy one
      setDecoder(new OldTibrvMessageEncoderDecoder());
    }
    else { //setFieldName must have already created it.
      if (! (decoder instanceof OldTibrvMessageEncoderDecoder)) {
        String msg="Misconfiguration - use EITHER decoder OR setDecodeTibRvMsg / fieldName ";
        log.warn(msg);
        throw new RuntimeException(msg);
      }
    }
  }

  /**
   * Set name of field which is expected to contain record data.
   * As of 3.4.5 this should no longer be used.
   * 
   * @deprecated This only exists for backwards compatibility with 
   *             oa versions before 3.4.5
   * @param fieldName
   */
  public void setFieldName(final String fieldName) {
    if (decoder==null) { //Set it up.
      setDecodeTibrvMsg(true);
    }
    //setDecodeTibrvMsg will catch misconfiguration below
    ((OldTibrvMessageEncoderDecoder)decoder).setFieldName(fieldName);
  }


  /**
   * Connect to Tibco Rendezvous and begin processing incoming messages.
   */
  public void connect() {
    for (Iterator iter = topicNames.iterator(); iter.hasNext();) {
      String topic = (String) iter.next();
      log.info("creating listener for " + topic);
      try {
        TibrvListener listener = connection.createListener(topic, this);
        listenerMap.put(topic, listener);
      } catch (TibrvException e) {
        throw new ConnectionException("failed to create listener", e, this);
      }
    }
    dispatcher=new Dispatcher(this,"tibrv_dispatch");
    dispatcher.setDaemon(true);
    dispatcher.start();
  }

  /**
   * Disconnect from Tibco Rendezvous. 
   */
  public void disconnect() {
    for (Iterator iter = listenerMap.values().iterator(); iter.hasNext();) {
      TibrvListener listener = (TibrvListener) iter.next();
      listener.destroy();
    }
    if (dispatcher!=null) {
      log.debug("Asking dispatcher to shutdown");
      dispatcher.shutdown();
    }
  }

  /**
   * process an incoming message (TibrvMsg) from Rendezvous.
   * The message will be decoded {@link #setDecoder(ITibrvMessageDecoder)}
   * if appropriate and made available to downstream oa components.
   * @param listener - a {@link #connect}ed rendezvous listener 
   * @param msg - the message received from the listener
   */
  public void onMsg(TibrvListener listener, TibrvMsg msg) {
    log.debug("Message received: "+msg);
    if (decoder!=null) {
      try {
        enqueue(decoder.decode(msg));
      }
      catch (TibrvException te) {
        fail("Failed to process TibrvMsg: "+msg,te);
      }
    }
    else {
      enqueue(msg);
    }
  }

  /**
   * Utility message for connection failures.
   * @param msg Message to log and throw
   * @param t Throwable which caused the failure.
   */
  protected void fail(String msg,Throwable t) {
    log.error(msg);
    throw new ConnectionException(msg, t);
  }

  /**
   * Validate the configuration state of this connector.
   * Add any issues to the working list of configuration issues.
   * 
   * @param exceptions - a list of outstanding exceptions encountered
   *        as part of configuration.
   */
  public void validate(List exceptions) {}

  /**
   * Thread to interact with Rendezvous.
   * This interfaces with it's message dispatcher.
   * it effectively loops makes blocking calls to
   * <code>Tibrv.defaultQueue().dispatch();</code>
   * @author higginse
   *
   */
  class Dispatcher extends Thread {
    private boolean running;
    private TibrvReadConnector connector;

    public void shutdown() {
      this.running=false;
    }
    public Dispatcher(TibrvReadConnector connector,String name) {
      super(name);
      this.connector=connector;
      this.setDaemon(true);
    }
    public void run(){
      running=true;
      log.debug("Dispatcher starting");
      while (running) {
        try {
          Tibrv.defaultQueue().dispatch();
        }
        catch (InterruptedException ie) {} //Can ignore this.
        catch (TibrvException te) {
          connector.fail("Exception dispatching tibrv message",te);
        }
      }
      log.debug("Dispatcher stopped");
    }
  }

}
