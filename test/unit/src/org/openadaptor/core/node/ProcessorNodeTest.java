package org.openadaptor.core.node;

import junit.framework.TestCase;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.node.ProcessorNode;

public class ProcessorNodeTest extends TestCase {

  public void test() {
    Object[] data = new Object[] {"foo", "bar", "foobar"};
    Object[] exceptionData = new Object[] {
        new MessageException(data[0], new RuntimeException("test"), "test"),
        data[1],
        new MessageException(data[2], new RuntimeException("test"), "test")
    };
    
    ProcessorNode node = new ProcessorNode();
    node.setProcessor(IDataProcessor.NULL_PROCESSOR);
    
    {
      Response response = node.process(new Message(data, null, null));
      Object[] output = response.getCollatedOutput();
      assertTrue(equals(data, output));
    }

    {
      Response response = node.process(new Message(exceptionData, null, null));
      Object[] output = response.getCollatedOutput();
      assertFalse(equals(data, output));
    }

    node.setStripOutExceptions(true);
    {
      Response response = node.process(new Message(exceptionData, null, null));
      Object[] output = response.getCollatedOutput();
      assertTrue(equals(data, output));
    }

  }
  
  private boolean equals(Object[] a, Object[] b) {
    if (a != null && a != null && a.length == b.length) {
      for (int i = 0; i < a.length; i++) {
        if (!a[i].equals(b[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

}
