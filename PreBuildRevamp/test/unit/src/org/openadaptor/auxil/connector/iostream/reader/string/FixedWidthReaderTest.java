package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class FixedWidthReaderTest extends TestCase {

  public void test() throws IOException {
    String s = "larry \ncurly \nmo    \n";
    ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
    FixedWidthReader reader = new FixedWidthReader();
    reader.setBufferLength(7);
    reader.setInputStream(is);
    Object data;
    StringBuffer buffer = new StringBuffer();
    while ((data = reader.read()) != null) {
      buffer.append(data);
    }
    assertEquals(s, buffer.toString());
  }
}
