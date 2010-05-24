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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ConnectionException;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * Write Connector to publish messages to tibco Rendezvous.
 * This has been completely rewritten as of release 3.4.5
 * 
 * @since 3.4.5 Introduced as part of tibrv connector overhaul
 * 
 * @author Eddy Higgins (higginse)
 *
 */
public class TibrvWriteConnector extends AbstractWriteConnector {
  private static final Log log = LogFactory.getLog(TibrvWriteConnector.class);

  private TibrvConnection connection;

  private String subject;

  private ITibrvMessageEncoder encoder=null;

  public TibrvWriteConnector() {
    super();
  }

  public TibrvWriteConnector(String id) {
    super(id);
  }

  /**
   * Associate  a TibrvConnection for this connector
   * @param connection TibrvConnection instance
   */
  public void setConnection(final TibrvConnection connection) {
    this.connection = connection;
  }

  /**
   * Set the subject for outgoing messages.
   * Note that incoming messages (which are already TibrvMsg instances)
   * may already have a subject set, in which case the subject
   * configured here will NOT be used.
   * 
   * @param subject Rendezvous subject for outgoing messages.
   */
  public void setSubject(final String subject) {
    this.subject = subject;
  }

  /**
   * Associate an encoder to encode incoming messages as TibrvMsg instances.
   * If an {@link ITibrvMessageEncoder} is specified, it will be used to encode incoming messages as TibrvMsg instances.
   * As of 3.4.5 a backwards-compatibility encoder is implicitly associated.
   * Note that if the connector receives messages which are already TibrvMsg instances,
   * then the encoder is not used - see {@link #deliver(Object[])}
   * 
   * but this is subject to chance whereby it will become mandatory.
   * @param encoder {@link ITibrvMessageEncoder} instance
   * 
   * @since 3.4.5 Introduced as part of rewrite of tibrv connectors
   */
  public void setEncoder(ITibrvMessageEncoder encoder) {
    this.encoder=encoder;
  }
  /**
   * Connect to Tibco Rendezvous
   */
  public void connect() {
    if (connection == null) {
      throw new ConnectionException("connection not set", this);
    }
    else {
      connection.connect();
    }
  }

  /**
   * Publish incoming messages on Rendezvous.
   * This will take incoming messages and process them
   * sequentially as follows.
   * If the current message is already a TibrvMsg instance, it is 
   * published directly; otherwise the configured encoder
   * is used to encode the data record as a TibrvMsg instance.
   * The configured subject is applied if the TibrvMsg instance does
   * not already have a subject.
   */
  public Object deliver(Object[] data) {
    for (int i = 0; i < data.length; i++) {
      Object record=data[i];
      try {
        if (record instanceof TibrvMsg) {
          deliver((TibrvMsg)record);
        }
        else {
          if (encoder!=null) {
            deliver(encoder.encode(record));
          }
          else {
            throw new RuntimeException("No encoder configured - this is probably a misconfiguration of encoder property");
          }
        }
      }
      catch (TibrvException te) {
        throw new ConnectionException("Failed to send message",te, this);
      }
    }
    return null;
  }

  /**
   * Publish a TibrvMsg.
   * It will assign the configured subject if any, assuming 
   * the message does not already have a subject set.
   * @param msg Message to be published
   * @throws TibrvException if Rendezvous fails to publish.
   */
  private void deliver(TibrvMsg msg) throws TibrvException {
    String existingSubject=msg.getSendSubject();
    if (existingSubject==null) {
      if (subject==null) {    
        String err="Required message subject is missing (and not configured)";
        log.debug(err);
        throw new ConnectionException(err, this);      
      }
      else {
        msg.setSendSubject(subject);
      }
    }
    else {
      if (log.isDebugEnabled() && subject!=null) {
        log.debug("TibrvMsg not overwriting existing subject (" +existingSubject+") with configured ("+subject+")");
      }
    }
    connection.send(msg);
  }

  /**
   * Apply a subject if not already configured.
   * Will throw a ConnectionException if a subject cannot be set.
   * @param msg TibrvMsg instance
   * @throws TibrvException if Rendezvous fails to assign the subject
   * @throws ConnectionException if a subject is not defined and thus
   *         cannot be set
   */
  private void setMessageSubject(TibrvMsg msg) throws TibrvException {
    if (msg.getSendSubject() == null) {
      if (subject != null) {
        msg.setSendSubject(subject);
      } else {
        throw new ConnectionException("encountered message without a sendSubject and no subject is configured", this);
      }
    }
  }

  /**
   * Validate the configuration state of this connector
   * Add any exceptions encountered to the working list of
   * configuration issues
   * @param exceptions - a list of outstanding exceptions encountered
   *        as part of configuration.
   */

  public void validate(List exceptions) {
    if (encoder==null) {
      encoder=new OldTibrvMessageEncoderDecoder();
      log.warn("Defaulting encoder to legacy "+encoder.getClass().getName()+" -future releases are likey to provide a different default");
    }
  }

  /**
   * Disconnect from Tibco Rendezvous
   */
  public void disconnect() {
    connection.disconnect();
  }
}
