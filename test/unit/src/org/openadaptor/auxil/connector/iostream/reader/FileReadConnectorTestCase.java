package org.openadaptor.auxil.connector.iostream.reader;

import java.io.IOException;

import junit.framework.TestCase;

import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.auxil.connector.iostream.reader.string.RegexMultiLineReader;
import org.openadaptor.auxil.connector.iostream.reader.string.RegexReader;
import org.openadaptor.auxil.connector.iostream.reader.string.StringReader;
import org.openadaptor.util.ResourceUtil;

public class FileReadConnectorTestCase extends TestCase {

  public void testStringReader() throws IOException {
    FileReadConnector connector = new FileReadConnector("reader");
    connector.setFilename(ResourceUtil.getResourcePath(this, "test/unit/src/", "test.txt"));
    connector.setDataReader(new StringReader());
    connector.connect();
    String s = (String) connector.next(1)[0];
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.txt");
    assertTrue(s.equals(fileContents));
  }
  
  public void testLineReader() throws IOException {
    StringBuffer buffer = new StringBuffer();
    FileReadConnector connector = new FileReadConnector("reader");
    connector.setFilename(ResourceUtil.getResourcePath(this, "test/unit/src/", "test.txt"));
    connector.setDataReader(new LineReader());
    connector.connect();
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      if (data != null && data.length > 0) {
        buffer.append(data[0] + "\n");
      }
    }
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.txt");
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    assertTrue(buffer.toString().equals(fileContents));
  }
  
  public void testLineReader2() throws IOException {
    StringBuffer buffer = new StringBuffer();
    FileReadConnector connector = new FileReadConnector("reader");
    connector.setFilename(ResourceUtil.getResourcePath(this, "test/unit/src/", "test2.txt"));
    LineReader dataReader = new LineReader();
    dataReader.setExcludeRegex("^#.*");
    connector.setDataReader(dataReader);
    connector.connect();
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      if (data != null && data.length > 0) {
        buffer.append(data[0] + "\n");
      }
    }
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.txt");
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    assertTrue(buffer.toString().equals(fileContents));
  }
  
  public void testRegexReader() throws IOException {
    StringBuffer buffer = new StringBuffer();
    FileReadConnector connector = new FileReadConnector("reader");
    connector.setFilename(ResourceUtil.getResourcePath(this, "test/unit/src/", "test.xml"));
    RegexReader dataReader = new RegexReader();
    dataReader.setRegex("(<record>.*</record>)\r\n");
    connector.setDataReader(dataReader);
    connector.connect();
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      if (data != null && data.length > 0) {
        buffer.append(data[0] + "\n");
      }
    }
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.xml");
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    assertTrue(buffer.toString().equals(fileContents));
  }
  
  public void testRegexReader2() throws IOException {
    StringBuffer buffer = new StringBuffer();
    FileReadConnector connector = new FileReadConnector("reader");
    connector.setFilename(ResourceUtil.getResourcePath(this, "test/unit/src/", "test2.xml"));
    RegexReader dataReader = new RegexReader();
    dataReader.setRegex(".*<record>(.*)</record>.*");
    connector.setDataReader(dataReader);
    connector.connect();
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      if (data != null && data.length > 0) {
        buffer.append(data[0] + "\n");
      }
    }
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.txt");
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    assertTrue(buffer.toString().equals(fileContents));
  }
  
  public void testRegexMultiLineReader() throws IOException {
    StringBuffer buffer = new StringBuffer();
    FileReadConnector connector = new FileReadConnector("reader");
    connector.setFilename(ResourceUtil.getResourcePath(this, "test/unit/src/", "test2.xml"));
    RegexMultiLineReader dataReader = new RegexMultiLineReader();
    dataReader.setStartLineRegex("<doc>.*");
    dataReader.setEndLineRegex("</doc>.*");
    dataReader.setIncludeRecordDelimiters(false);
    connector.setDataReader(dataReader);
    connector.connect();
    while (!connector.isDry()) {
      Object[] data = connector.next(1);
      if (data != null && data.length > 0) {
        buffer.append(data[0] + "\n");
      }
    }
    connector.disconnect();
    String fileContents = ResourceUtil.readFileContents(this, "test.xml");
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    assertTrue(buffer.toString().equals(fileContents));
  }
}
