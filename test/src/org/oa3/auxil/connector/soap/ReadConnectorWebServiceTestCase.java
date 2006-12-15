package org.oa3.auxil.connector.soap;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.oa3.auxil.connector.soap.ReadConnectorWebService;

public class ReadConnectorWebServiceTestCase extends TestCase {

  public void testAxis() {

    ReadConnectorWebService service = new ReadConnectorWebService();

    try {
      // connect (this starts jetty)
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
        call.setOperationName(new QName("http://soap.connector.auxil.oa3.org/", "process"));
        for (int i = 0; i < data.length; i++) {
          call.invoke(new Object[] { data[i] });
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
