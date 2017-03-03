package org.openadaptor.auxil.connector.socket;

public class SocketReadConnectorTest  {

  public static void main(String[] args) {
    SocketReadConnector connector = new SocketReadConnector("test");
    connector.setPort(9000);
    connector.connect();
    boolean die = false;
    while (!connector.isDry() && !die) {
      Object[] data = connector.next(1000);
      for (int i = 0; data != null && i < data.length; i++) {
        System.err.println(data[i]);
        die |= data[i].equals("die");
      }
    }
    connector.disconnect();
    System.err.println("exited");
  }
}
