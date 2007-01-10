package org.openadaptor.auxil.connector.iostream;

import org.openadaptor.auxil.connector.iostream.reader.StringRecordReader;

import junit.framework.TestCase;

public class StringRecordReaderTestCase extends TestCase {

  public void testSingleExclude() {
    StringRecordReader reader = new StringRecordReader();
    reader.setExcludePattern("^#.*");
    assertTrue(reader.match("test"));
    assertTrue(reader.match("tes#t"));
    assertFalse(reader.match("#test"));
  }
  
  public void testSingleInclude() {
    StringRecordReader reader = new StringRecordReader();
    reader.setIncludePattern("^A.*");
    assertFalse(reader.match("test"));
    assertFalse(reader.match("abc"));
    assertTrue(reader.match("A"));
    assertTrue(reader.match("Abc"));
  }
  
  public void testMultipleInclude() {
    StringRecordReader reader = new StringRecordReader();
    reader.setIncludePatterns(new String[] {"^A.*", "^a.*"});
    assertFalse(reader.match("test"));
    assertTrue(reader.match("abc"));
    assertTrue(reader.match("A"));
    assertTrue(reader.match("Abc"));
  }
  
  public void testMultipleExclude() {
    StringRecordReader reader = new StringRecordReader();
    reader.setExcludePatterns(new String[] {"^#.*", "^//.*"});
    assertTrue(reader.match("test"));
    assertTrue(reader.match("tes#t"));
    assertTrue(reader.match("/test"));
    assertTrue(reader.match("tes//t"));
    assertFalse(reader.match("#test"));
    assertFalse(reader.match("//test"));
  }
  
  public void testExcludesAndInclude() {
    StringRecordReader reader = new StringRecordReader();
    reader.setExcludePatterns(new String[] {"^#.*", "^//.*"});
    reader.setIncludePatterns(new String[] {".*[aA].*"});
    assertFalse(reader.match("test"));
    assertTrue(reader.match("testA"));
    assertTrue(reader.match("atest"));
    assertTrue(reader.match("Atest"));
    assertFalse(reader.match("#test"));
    assertFalse(reader.match("//test"));
  }
}
