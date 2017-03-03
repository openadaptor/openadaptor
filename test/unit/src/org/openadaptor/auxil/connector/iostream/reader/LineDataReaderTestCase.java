package org.openadaptor.auxil.connector.iostream.reader;

import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;

import junit.framework.TestCase;

public class LineDataReaderTestCase extends TestCase {

  public void testSingleExclude() {
    LineReader reader = new LineReader();
    reader.setExcludeRegex("^#.*");
    assertTrue(reader.match("test"));
    assertTrue(reader.match("tes#t"));
    assertFalse(reader.match("#test"));
  }
  
  public void testSingleInclude() {
    LineReader reader = new LineReader();
    reader.setIncludeRegex("^A.*");
    assertFalse(reader.match("test"));
    assertFalse(reader.match("abc"));
    assertTrue(reader.match("A"));
    assertTrue(reader.match("Abc"));
  }
  
  public void testMultipleInclude() {
    LineReader reader = new LineReader();
    reader.setIncludeRegexs(new String[] {"^A.*", "^a.*"});
    assertFalse(reader.match("test"));
    assertTrue(reader.match("abc"));
    assertTrue(reader.match("A"));
    assertTrue(reader.match("Abc"));
  }
  
  public void testMultipleExclude() {
    LineReader reader = new LineReader();
    reader.setExcludeRegexs(new String[] {"^#.*", "^//.*"});
    assertTrue(reader.match("test"));
    assertTrue(reader.match("tes#t"));
    assertTrue(reader.match("/test"));
    assertTrue(reader.match("tes//t"));
    assertFalse(reader.match("#test"));
    assertFalse(reader.match("//test"));
  }
  
  public void testExcludesAndInclude() {
    LineReader reader = new LineReader();
    reader.setExcludeRegexs(new String[] {"^#.*", "^//.*"});
    reader.setIncludeRegexs(new String[] {".*[aA].*"});
    assertFalse(reader.match("test"));
    assertTrue(reader.match("testA"));
    assertTrue(reader.match("atest"));
    assertTrue(reader.match("Atest"));
    assertFalse(reader.match("#test"));
    assertFalse(reader.match("//test"));
  }
}
