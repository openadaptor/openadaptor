/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.auxil.connector.soap;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.client.Client;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ComponentException;

/**
 * binds to a webservice endpoint and delivers data by calling a method
 * on this service.
 * 
 * @author perryj
 *
 */
public class WebServiceWriteConnector extends AbstractWriteConnector {

  private static final Log log = LogFactory.getLog(WebServiceWriteConnector.class);

  private Client client;

  private String methodName;

  private String endpoint;

  public WebServiceWriteConnector() {
    super();
  }

  public WebServiceWriteConnector(String id) {
    super(id);
  }

  public void setEndpoint(final String endpoint) {
    this.endpoint = endpoint;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  public void connect() {
    try {
      client = new Client(new URL(endpoint));
      log.info(getId() + " bound to endpoint " + endpoint);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Malformed Url exception ", e);
    } catch (Exception e) {
      throw new ComponentException("failed to connect", e, this);
    }
  }

  public Object deliver(Object[] data) {
    if (client == null) {
      connect();
    }
    try {
      for (int i = 0; i < data.length; i++) {
        try {
          client.invoke(methodName, new Object[] { marshall(data[i]) });
        } catch (RuntimeException e) {
          client = null;
          throw e;
        }
      }
      return null;
    } catch (Exception e) {
      throw new ComponentException("webservice call failed", e, this);
    }
  }

  protected Object marshall(Object data) {
    return data.toString();
  }

  public void disconnect() {
    client = null;
  }

}
