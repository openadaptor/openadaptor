package org.openadaptor.auxil.connector.iostream.writer;

import java.io.File;
import java.io.IOException;

import org.openadaptor.core.IWriteConnector;
import org.openadaptor.util.ResourceUtil;

import junit.framework.TestCase;

/**
 * System tests for {@link FileWriteConnector}.
 * 
 * @author OA3 Core Team
 */
public class FileWriteConnectorTestCase extends TestCase {

  private static String DIR = "test/unit/output";
  
  private static String TEST_FILE_CONTENT = "larry\ncurly\nmo\n";
  
  public void testStringDataWriter() throws IOException {
  	File dir = new File(DIR);
  	if (!dir.exists()) {
  		dir.mkdir();
  	}
    FileWriteConnector connector = new FileWriteConnector("writer");    
    File tempFile = File.createTempFile("xyz", ".txt", dir);
    connector.setFilename(tempFile.getAbsolutePath());
    runWriter(connector);
    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT);
  }
  
  /**
   * Tests if the existing file is renamed before a new file is created.
   */
  public void testMoveTo() throws IOException {
  	File dir = new File(DIR);
  	if (!dir.exists()) {
  		dir.mkdir();
  	}
    FileWriteConnector connector1 = new FileWriteConnector("writer");
    File tempFile = File.createTempFile("xyz", ".txt", new File(DIR));
    connector1.setFilename(tempFile.getAbsolutePath());
    runWriter(connector1);
    
    FileWriteConnector connector2 = new FileWriteConnector("writer");
    connector2.setFilename(tempFile.getAbsolutePath());
    connector2.setMoveExistingFileTo(tempFile.getAbsolutePath() + "_old");
    runWriter(connector2);
    
    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT);
    verifyFileContent(tempFile.getAbsolutePath() + "_old", TEST_FILE_CONTENT);
    tempFile.delete();
  }
  
  /**
   * Tests if the existing file is renamed (by adding a timestamp) 
   * before a new file is created.
   */
  public void testMoveTo_WithTimeStamp() throws IOException {
  	File dir = new File(DIR);
  	if (!dir.exists()) {
  		dir.mkdir();
  	}
    FileWriteConnector connector1 = new FileWriteConnector("writer");
    File tempFile = File.createTempFile("xyz", ".txt", new File(DIR));
    connector1.setFilename(tempFile.getAbsolutePath());
    runWriter(connector1);
    
    FileWriteConnector connector2 = new FileWriteConnector("writer");
    connector2.setFilename(tempFile.getAbsolutePath());
    connector2.setMoveExistingFileTo(tempFile.getAbsolutePath() + "_old");
    connector2.setAddTimestampToMovedFile(true);
    runWriter(connector2);
    String moveExistingFileTo = connector2.getMoveExistingFileTo();
    
    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT);
    verifyFileContent(moveExistingFileTo, TEST_FILE_CONTENT);
  }
  
  private void runWriter(IWriteConnector connector) throws IOException{
    connector.connect();
    connector.deliver(new Object[] {"larry"});
    connector.deliver(new Object[] {"curly", "mo"});
    connector.disconnect();
  }
  
  private void verifyFileContent(String filename, String expectedContent){
    String fileContents = ResourceUtil.readFileContents(filename);
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    assertEquals(fileContents, expectedContent);
  }
}
