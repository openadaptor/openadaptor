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

package org.openadaptor.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.transaction.ITransaction;

/**
 * Class that encapsulates the input data to be processed by an {@link IMessageProcessor}.
 * Also encapsulates the metadata related to the data. The metadata may optionally
 * store message history.
 *
 * @see IMessageProcessor
 */
public class Message {

  /** Key under which a message history will (optionally) be stored in the metadata. */	
  public static final String MESSAGE_HISTORY_KEY = "MessageHistory";	
  
  /** Constant to be used in message history for message processors that were not given a name */
  public static final String UNNAMED_MESSAGE_PROCESSOR = "UNNAMED_MESSAGE_PROCESSOR";
	
  /**
   * A reference to the object that initiated the processing, this is not necessarily the
   * caller of {@link IMessageProcessor#process} nor is it necessarily a IMessageProcessor.
   */
  private Object sender;
  
  /**
   * The data that should be processed as a single transaction.
   */
  private Object[] data;
  
  /**
   * Optional metadata that describes or relates to this message.
   */
  private Map metadata;
  
  /**
   * The {@link ITransaction} that is associated with the processing of this Message.
   */
  private ITransaction transaction;
	
  /**
   * Constructor. 
   */
  public Message(final Object[] data, final Object sender, final ITransaction transaction, final Map metadata) {
    this.data = data;
    this.sender = sender;
    this.transaction = transaction;
    this.metadata = metadata!=null?metadata:new HashMap();
  }

  /**
   * Constructor. 
   */
  public Message(final List data, final Object sender, final ITransaction transaction, final Map metadata) {
    this((Object[]) data.toArray(new Object[data.size()]),sender,transaction, metadata);
  }
  
  /**
   * Constructor. 
   */
  public Message(final Object data, final Object sender, final ITransaction transaction, final Map metadata) {
    this(new Object[] {data},sender,transaction, metadata);
  }
  
  public Object getSender() {
  	return sender;
  }
  
  /**
   * @return the input data to be processed by an {@link IMessageProcessor}.
   */
  public Object[] getData() {
  	return data;
  }

  /** 
   * @return metadata that describes or relates to this message.
   */
  public Map getMetadata() {
    return metadata;
  }

  /**
   * Sets metadata that describes or relates to this message.
   * 
   * @param metadata
   */
  public void setMetadata(Map metadata) {
    this.metadata = metadata;
  }
  
  /**
   * @return a transaction related to this message.
   */
  public ITransaction getTransaction() {
    return transaction;
  }
}
