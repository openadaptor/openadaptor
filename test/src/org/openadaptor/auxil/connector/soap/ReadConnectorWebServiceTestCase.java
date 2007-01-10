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

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;

public class ReadConnectorWebServiceTestCase extends TestCase {

  public void testAxis() {

    ReadConnectorWebService service = new ReadConnectorWebService();

    try {
      // connect (this starts jetty)
      service.setLocalJettyPort(9999);
      service.setTransacted(false);
      service.connect();

      // invoke webservice (using axis)
      AxisThread thread = new AxisThread(service.getEndpoint(), new String[] { "foo", "bar" });
      thread.start();
      try {
        thread.join();
      } catch (InterruptedException e) {
      }

      // poll service and check results
      Object[] data = service.next(1);
      assertTrue(data.length == 1);
      assertTrue(data[0].equals("foo"));

      data = service.next(1);
      assertTrue(data.length == 1);
      assertTrue(data[0].equals("bar"));

    } finally {
      // stop jetty
      service.disconnect();
    }
  }

  class AxisThread extends Thread {

    private String endpoint;

    private String[] data;

    AxisThread(final String endpoint, final String[] data) {
      this.endpoint = endpoint;
      this.data = data;
    }

    public void run() {
      try {
        Service webService = new Service();
        Call call = (Call) webService.createCall();
        endpoint = endpoint.substring(0, endpoint.indexOf("?wsdl"));
        call.setTargetEndpointAddress(new java.net.URL(endpoint));
        call.setOperationName(new QName("http://soap.connector.auxil.openadaptor.org/", "process"));
        for (int i = 0; i < data.length; i++) {
          call.invoke(new Object[] { data[i] });
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
