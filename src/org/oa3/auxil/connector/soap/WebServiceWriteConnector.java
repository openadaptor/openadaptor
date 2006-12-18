package org.oa3.auxil.connector.soap;

import java.net.URL;

import org.codehaus.xfire.client.Client;
import org.oa3.core.connector.AbstractWriteConnector;
import org.oa3.core.exception.ComponentException;

/**
 * binds to a webservice endpoint and delivers data by calling a method
 * on this service.
 * 
 * @author perryj
 *
 */
public class WebServiceWriteConnector extends AbstractWriteConnector {

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
    } catch (Exception e) {
      throw new ComponentException("failed to connect", e, this);
    }
  }

  public Object deliver(Object[] data) {
    try {
      for (int i = 0; i < data.length; i++) {
        client.invoke(methodName, new Object[] {marshall(data[i])});
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
