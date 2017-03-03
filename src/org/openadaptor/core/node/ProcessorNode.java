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

package org.openadaptor.core.node;

import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.exception.MessageException;

/**
 * This class should be used to "wrap" an IDataProcessor.
 * 
 * @author oa3 Core Team
 */
public final class ProcessorNode extends Node {

  private boolean stripOutExceptions = false;
  
  /**
   * Constructor.
   */
  public ProcessorNode() {
    super();
  }
 
  /**
   * Constructor.
   * 
   * @param id - the id of this node.
   */
  public ProcessorNode(String id) {
    super(id);
  }
  
  /**
   * Sets the behaviour of this node such that if any of the data it receives is a
   * {@link MessageException} then the data contained in the {@link MessageException}
   * is extracted before invoking the super class.
   */
  public void setStripOutExceptions(final boolean stripOut) {
    this.stripOutExceptions = stripOut;
  }
  
  /**
   * Returns its own Id. If that isn't set, returns Id of the processor it wraps.
   */
  public String getId() {
    String id = super.getId();
    return id != null ? id : getProcessorId();
  }
  
  /**
   * Returns Id of this Node.
   */
  public String toString() {
    return getId();
  }

  public Response process(Message msg) {
   if (stripOutExceptions) {
     Object[] data = msg.getData();
     Object[] newData = new Object[data.length];
     for (int i = 0; i < data.length; i++) {
       if (data[i] instanceof MessageException) {
         newData[i] = ((MessageException)data[i]).getData();
       } else {
         newData[i] = data[i];
       }
     }
     msg = new Message(newData, msg.getSender(), msg.getTransaction(), msg.getMetadata());
   }
   return super.process(msg);
  }
}
