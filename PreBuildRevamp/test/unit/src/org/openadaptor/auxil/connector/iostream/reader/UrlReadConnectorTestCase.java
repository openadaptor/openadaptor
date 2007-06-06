package org.openadaptor.auxil.connector.iostream.reader;

import java.io.IOException;

import junit.framework.TestCase;

import org.openadaptor.auxil.connector.iostream.reader.string.StringReader;
import org.openadaptor.util.ResourceUtil;

public class UrlReadConnectorTestCase extends TestCase {

  public void testStringReader() throws IOException {
    URLReadConnector connector = new URLReadConnector("reader");
    connector.setUrl("file:" + ResourceUtil.getResourcePath(this, "test/unit/src/", "test.txt"));
    connector.setDataReader(new StringReader());
    connector.connect();
    String s = (String) connector.next(1)[0];
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.txt");
    assertTrue(s.equals(fileContents));
  }
}
