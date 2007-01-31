package org.openadaptor.auxil.connector.iostream.writer;

import java.io.File;
import java.io.IOException;

import org.openadaptor.util.ResourceUtil;

import junit.framework.TestCase;

public class FileWriteConnectorTestCase extends TestCase {

  public void testStringDataWriter() throws IOException {
    FileWriteConnector connector = new FileWriteConnector("writer");
    File tempFile = File.createTempFile("xyz", ".txt", new File("test/unit/output"));
    connector.setFilename(tempFile.getAbsolutePath());
    connector.connect();
    connector.deliver(new Object[] {"larry"});
    connector.deliver(new Object[] {"curly", "mo"});
    connector.disconnect();
    
    String fileContents = ResourceUtil.readFileContents(tempFile.getAbsolutePath());
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    
    assertEquals(fileContents, new String("larry\ncurly\nmo\n"));
  }
}
