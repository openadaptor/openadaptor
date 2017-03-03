package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class DelimitedReaderTestCase extends TestCase {

  public void testDelimiterChar() throws IOException {
    DelimitedReader reader = new DelimitedReader();
    reader.setDelimiterCharCodes(new int[] {10});
    String s = "mary had a little lamb\nit's fleece was white as snow\nand every where that mary went\nthe lamb was sure to go";
    ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
    reader.setInputStream(is);
    Object data;
    StringBuffer buffer = new StringBuffer();
    while ((data = reader.read()) != null) {
      buffer.append(data.toString());
    }
    assertEquals(s, buffer.toString());
  }
  
  public void testDelimiterChars() throws IOException {
    DelimitedReader reader = new DelimitedReader();
    reader.setDelimiterCharCodes(new int[] {69, 78, 68});
    String s = "mary had a little lambENDit's fleece was white as snowENDand every where that mary wentENDthe lamb was sure to goEND";
    ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
    reader.setInputStream(is);
    reader.setIncludeDelimiter(false);
    Object data;
    StringBuffer buffer = new StringBuffer();
    while ((data = reader.read()) != null) {
      buffer.append(data.toString() + "END");
    }
    assertEquals(s, buffer.toString());
  }
  
  public void testDelimiter() throws IOException {
    DelimitedReader reader = new DelimitedReader();
    reader.setDelimiter("</record>");
    String s = "<record><field>larry</field></record>\n"
      + "<record><field>curly</field></record>\n"
      + "<record><field>mo</field></record>";
    ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
    reader.setInputStream(is);
    Object data;
    StringBuffer buffer = new StringBuffer();
    while ((data = reader.read()) != null) {
      buffer.append(data.toString());
    }
    assertEquals(s, buffer.toString());
  }
}
