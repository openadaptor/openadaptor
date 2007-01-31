package org.openadaptor.auxil.connector.iostream.reader;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.util.ResourceUtil;

public class DirectoryReadConnectorTestCase extends TestCase {

  public void testLineReader() throws IOException {
    
    StringBuffer buffer = new StringBuffer();
    
    DirectoryReadConnector connector = new DirectoryReadConnector("reader");
    String f = ResourceUtil.getResourcePath(this, "test/unit/src/", "test.txt");
    File file = new File(f);
    connector.setDirname(file.getParent());
    connector.setFilenameRegex(".*\\.txt");
    connector.setDataReader(new LineReader());
    connector.connect();
    
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      for (int i = 0; data != null && i < data.length; i++) {
        buffer.append(data[i] + "\n");
      }
    }
    
    String s = ResourceUtil.readFileContents(this, "test.txt") 
      + ResourceUtil.readFileContents(this, "test2.txt");
    s = ResourceUtil.removeCarriageReturns(s);
    assertEquals(buffer.toString(), s);
  }
}
