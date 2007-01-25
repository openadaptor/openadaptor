/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.thirdparty.tibco;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.exception.ComponentException;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

public class TibrvReadConnector extends QueuingReadConnector implements TibrvMsgCallback {

  private static final Log log = LogFactory.getLog(TibrvReadConnector.class);

  private TibrvConnection connection;
  
  private Set topicNames = new HashSet();

  private Map listenerMap = new HashMap();
  
  private DispathThread dispathThread = null;
  
  private boolean decodeTibrvMsg = true;

  private String fieldName = TibrvWriteConnector.FIELD_NAME;

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
   * if true then extracts string value from single field on incoming messages
   * otherwise queues actual TibrvMsg
   * @param decodeTibrvMsg
   */
  public void setDecodeTibrvMsg(final boolean decodeTibrvMsg) {
    this.decodeTibrvMsg = decodeTibrvMsg;
  }

  /**
   * name of field this component expects the data to be in
   * @param fieldName
   */
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public void connect() {
    for (Iterator iter = topicNames.iterator(); iter.hasNext();) {
      String topic = (String) iter.next();
      log.info("creating listener for " + topic);
      try {
        TibrvListener listener = connection.createListener(topic, this);
        listenerMap.put(topic, listener);
      } catch (TibrvException e) {
        throw new ComponentException("failed to create listener", e, this);
      }
    }
    dispathThread = new DispathThread();
    dispathThread.start();
  }

  public void disconnect() {
    for (Iterator iter = listenerMap.values().iterator(); iter.hasNext();) {
      TibrvListener listener = (TibrvListener) iter.next();
      listener.destroy();
    }
  }

  public void onMsg(TibrvListener listener, TibrvMsg msg) {
    if (decodeTibrvMsg) {
      try {
        enqueue(msg.getField(fieldName).data);
      } catch (TibrvException e) {
        log.error("failed to extract " + fieldName + " from message");
      }
    } else {
      enqueue(msg);
    }
  }

  class DispathThread extends Thread {
    boolean active = false;

    DispathThread() {
      super("tibrvdispatch");
      setDaemon(true);
    }

    public void run() {
      while (true) {
        try {
          Tibrv.defaultQueue().dispatch();
        } catch (InterruptedException e) {
        } catch (Throwable e) {
          log.error("unexpected exception dispatching tibrv message", e);
        }
      }
    }
  }
}
