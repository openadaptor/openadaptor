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

  private static String DIR = "build/test/unit/output";
  
  private static String TEST_FILE_CONTENT = "larry\ncurly\nmo\n";
  
  private File dir = null;
  
  protected void setUp() throws Exception {
    super.setUp();
    dir = new File(DIR);
    if (!dir.exists()) {
        dir.mkdir();
    }
  }

  public void testStringDataWriter() throws IOException {
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
    FileWriteConnector connector1 = new FileWriteConnector("writer");
    File tempFile = File.createTempFile("xyz", ".txt", new File(DIR));
    connector1.setFilename(tempFile.getAbsolutePath());
    runWriter(connector1);
    
    FileWriteConnector connector2 = new FileWriteConnector("writer");
    connector2.setFilename(tempFile.getAbsolutePath());
    connector2.setMoveExistingFileTo(tempFile.getAbsolutePath() + "_old");
    connector2.setAddTimestampToMovedFile(true);
    runWriter(connector2);
    String lastFileMovedTo = connector2.getLastFileMovedTo();
    
    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT);
    verifyFileContent(lastFileMovedTo, TEST_FILE_CONTENT);
    tempFile.delete();
  }
  
//  /**
//   * Tests if the existing file is rolled over when it reaches 
//   * certain size.
//   */
//  public void testRollover_size() throws IOException {
//    FileWriteConnector connector1 = new FileWriteConnector("writer");
//    File tempFile = File.createTempFile("xyz", ".txt", new File(DIR));
//    connector1.setFilename(tempFile.getAbsolutePath());
//    runWriter(connector1);
//    
//    FileWriteConnector connector2 = new FileWriteConnector("writer");
//    connector2.setFilename(tempFile.getAbsolutePath());
//    connector2.setRolloverSize("36b");
//    /* File is too small to be rolled over at this point. */
//    runWriter(connector2);
//    
//    /* 
//     * File to small to have been rolled over, it should contain output from both connectors.
//     */
//    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT + TEST_FILE_CONTENT);
//    
//    /*
//     * Size of the output file at this point will be around 36k.
//     * 'larry\n' should be written to the original file which should be 
//     * rolled over before writing 'curly\nmo\n'.
//     */
//    runWriter(connector2);
//    String lastFileMovedTo = connector2.getLastFileMovedTo();
//    assertNotNull(lastFileMovedTo);
//    verifyFileContent(lastFileMovedTo, TEST_FILE_CONTENT + TEST_FILE_CONTENT + "larry\n");
//    verifyFileContent(tempFile.getAbsolutePath(), "curly\nmo\n");
//    tempFile.delete();
//  }
//   
//  /**
//   * Tests if the existing file is rolled over when it reaches 
//   * certain age.
//   */
//  public void testRollover_age() throws Exception {
//    FileWriteConnector connector1 = new FileWriteConnector("writer");
//    File tempFile = File.createTempFile("xyz", ".txt", new File(DIR));
//    connector1.setFilename(tempFile.getAbsolutePath());
//    runWriter(connector1);
//    
//    FileWriteConnector connector2 = new FileWriteConnector("writer");
//    connector2.setFilename(tempFile.getAbsolutePath());
//    connector2.setRolloverDate("1s");
//    runWriter(connector2);
//    
//    /* 
//     * File wasn't old enough to be rolled over, output from second connector 
//     * should've been written to the same file.
//     */
//    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT + TEST_FILE_CONTENT);
//    
//    /*
//     * Wait long enough for the file to be old enough to be rolled over.
//     */
//    Thread.sleep(1100);
//    runWriter(connector2);
//    verifyFileContent(tempFile.getAbsolutePath(), TEST_FILE_CONTENT);
//    String lastFileMovedTo = connector2.getLastFileMovedTo();
//    verifyFileContent(lastFileMovedTo, TEST_FILE_CONTENT + TEST_FILE_CONTENT);
//    tempFile.delete();
//  }
  
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
