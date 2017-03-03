package org.openadaptor.auxil.connector.socket;

public class SocketWriteConnectorTest {

  public static void main(String[] args) {
    SocketWriteConnector connector = new SocketWriteConnector("writer");
    connector.setRemoteHostname("localhost");
    connector.setPort(9000);
    connector.connect();
    connector.deliver(new String[] {"mary", "had", "a", "little", "lamb"});
    connector.disconnect();
  }
}
