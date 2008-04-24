package org.openadaptor.auxil.connector.iostream.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.auxil.connector.iostream.reader.string.StringReader;
import org.openadaptor.util.ResourceUtil;

public class DirectoryReadConnectorTestCase extends MockObjectTestCase {

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
  
  public void testStringReader() throws IOException {

    List messageList = new ArrayList();
    List expectedMessageList = new ArrayList();
    
    DirectoryReadConnector connector = new DirectoryReadConnector("reader");
    String f = ResourceUtil.getResourcePath(this, "test/unit/src/", "test.xml");
    File file = new File(f);
    connector.setDirname(file.getParent());
    connector.setFilenameRegex(".*\\.xml");
    connector.setDataReader(new StringReader());
    connector.connect();
    
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      for (int i = 0; data != null && i < data.length; i++) {
        messageList.add(data[i]);
      }
    }
    // This test relies on the connector having read the files in the order below.
    expectedMessageList.add( ResourceUtil.readFileContents(this, "test.xml") );
    expectedMessageList.add( ResourceUtil.readFileContents(this, "test2.xml")) ;
    assertEquals(messageList, expectedMessageList);
  } 
  
  public void testValidate() {
    DirectoryReadConnector connector = new DirectoryReadConnector("reader");
    String f = ResourceUtil.getResourcePath(this, "test/unit/src/", "test.txt");
    File file = new File(f);
    connector.setDirname(file.getParent());
    connector.setFilenameRegex(".*\\.txt");
    connector.setDataReader(new LineReader());
    
    List exceptions = new ArrayList();
    connector.validate(exceptions);
    assertTrue("There should be no exceptions", exceptions.size() == 0);
  }
  
  public void testValidateFail() {
    DirectoryReadConnector connector = new DirectoryReadConnector("reader");
    // No directory set
    List exceptions = new ArrayList();
    connector.validate(exceptions);
    assertTrue("There should be one validation exception", exceptions.size() == 1);
    
    // set  a nonexistent directory.
    connector.setDirname("thisdoesnotexist");
    exceptions = new ArrayList();
    connector.validate(exceptions);
    assertTrue("There should be one validation exception", exceptions.size() == 1);
  }
  
  
  
}
