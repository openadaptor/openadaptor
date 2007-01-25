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

import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ComponentException;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

public class TibrvWriteConnector extends AbstractWriteConnector {

  // deliberately matches tibco examples tibrvlisten/send
  public static final String FIELD_NAME = "DATA";

  private TibrvConnection connection;
  
  private String subject;

  public TibrvWriteConnector() {
    super();
  }
  
  public TibrvWriteConnector(String id) {
    super(id);
  }
  
  public void setConnection(final TibrvConnection connection) {
    this.connection = connection;
  }

  public void setSubject(final String subject) {
    this.subject = subject;
  }

  public void connect() {
    if (connection == null) {
      throw new ComponentException("connection not set", this);
    }
  }

  public Object deliver(Object[] data) {
    for (int i = 0; i < data.length; i++) {
      TibrvMsg msg;
      try {
        if (data[i] instanceof TibrvMsg) {
          msg = (TibrvMsg)data[i];
        } else {
          msg = new TibrvMsg();
          msg.update(FIELD_NAME, data[i].toString());
        }
        setSendMessage(msg);
        connection.send(msg);
      } catch (TibrvException e) {
        throw new ComponentException("failed to send message", e, this);
      }
    }
    return null;
  }

  private void setSendMessage(TibrvMsg msg) throws TibrvException {
    if (msg.getSendSubject() == null) {
      if (subject != null) {
        msg.setSendSubject(subject);
      } else {
        throw new ComponentException("encountered message without a sendSubject and no subject is configured", this);
      }
    }
  }

  public void disconnect() {
  }

}
