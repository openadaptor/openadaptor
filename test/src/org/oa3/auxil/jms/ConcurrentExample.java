package org.oa3.auxil.jms;

public class ConcurrentExample {

  public static void main(final String[] args) {
    
    Thread subThread = new Thread("sub") {
      public void run() {
        SubAdaptorExample.main(args);
      }
    };
    
    subThread.start();
    
    PubAdaptorExample.main(args);
  }
}
