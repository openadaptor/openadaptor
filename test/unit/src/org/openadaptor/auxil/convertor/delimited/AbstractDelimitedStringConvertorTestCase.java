package org.openadaptor.auxil.convertor.delimited;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AbstractDelimitedStringConvertor}.
 */
public class AbstractDelimitedStringConvertorTestCase extends TestCase {
  
  /* Need an instance of the tested class */
  AbstractDelimitedStringConvertor convertor = new AbstractDelimitedStringConvertor(){
      protected Object convert(Object record) {
        throw new UnsupportedOperationException("Method not implemetned");
      }};

  /**
   * Test AbstractDelimitedStringConvertor#extractValues. 
   * Not quoted fields, single char delimiter. 
   */	
  public void testNotQuotedFields_SingleCharDelimiter(){
    /* Comma delimiter */
    convertor.setDelimiter(",");
    convertor.setProtectQuotedFields(false);
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(false);
    notQuotedCommaDelimiter();
    convertor.setProtectQuotedFields(true);
    notQuotedCommaDelimiter();
    convertor.setDelimiterAlwaysLiteralString(true);
    notQuotedCommaDelimiter();
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(true);
    notQuotedCommaDelimiter();
    
    /* Pipe delimiter */
    convertor.setDelimiter("|");
    convertor.setProtectQuotedFields(false);
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(false);
    notQuotedPipeDelimiter();
    convertor.setProtectQuotedFields(true);
    notQuotedPipeDelimiter();
    convertor.setDelimiterAlwaysLiteralString(true);
    notQuotedPipeDelimiter();
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(true);
    check(convertor.extractValues("a|b|c"),
        new String[] {"", "a", "|", "b", "|", "c", ""});
  }
  
  private void notQuotedCommaDelimiter(){
    check(convertor.extractValues("a,b,c"),
        new String[] {"a", "b", "c"});
    check(convertor.extractValues("a,b,c,"),
        new String[] {"a", "b", "c", ""});
    check(convertor.extractValues("a,b,c"),
        new String[] {"a", "b", "c"});
    check(convertor.extractValues("a,b,c,"),
        new String[] {"a", "b", "c", ""});
    check(convertor.extractValues(",,a,b,c,"),
        new String[] {"", "", "a", "b", "c", ""});
    check(convertor.extractValues(",,aa,,b,c,"),
        new String[] {"", "", "aa", "", "b", "c", ""});
  }
  
  private void notQuotedPipeDelimiter(){
    check(convertor.extractValues("a|b|c"),
        new String[] {"a", "b", "c"});
    check(convertor.extractValues("a|b|c|"),
        new String[] {"a", "b", "c", ""});
    check(convertor.extractValues("a|b|c"),
        new String[] {"a", "b", "c"});
    check(convertor.extractValues("a|b|c|"),
        new String[] {"a", "b", "c", ""});
    check(convertor.extractValues("||a|b|c|"),
        new String[] {"", "", "a", "b", "c", ""});
    check(convertor.extractValues("||aa||b|c|"),
        new String[] {"", "", "aa", "", "b", "c", ""});
  }

  /**
   * Test AbstractDelimitedStringConvertor#extractValues. 
   * Quoted fields, single char delimiter. 
   */
  public void testQuotedFields_SingleCharDelimiter(){
    /* Comma delimiter */
    convertor.setDelimiter(",");
    convertor.setProtectQuotedFields(true);
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(false);
    quotedCommaDelimiter();
    convertor.setDelimiterAlwaysRegExp(true);
    quotedCommaDelimiter(); 
  }
  
  private void quotedCommaDelimiter(){
    check(convertor.extractValues("\"a\",\"b\",\"c\""),    //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(convertor.extractValues("\"a\",b,\"c\""),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(convertor.extractValues("\"a\",\"b\",\"c\","),   //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(convertor.extractValues("\"a\",b,\"c\""),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(convertor.extractValues("\"a\",\"b\",\"c\""),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(convertor.extractValues("\"a\",\"b\",\"c\","),    //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(convertor.extractValues("\"a\",,b,\"c\""),       //"a",,b,"c"
        new String[] {"\"a\"", "", "b", "\"c\""});
    check(convertor.extractValues("\"a\",\"\",\"c\""),      //"a","","c"
        new String[] {"\"a\"", "\"\"", "\"c\""});
    check(convertor.extractValues("\" a \",\" b\",\"c\""),  //" a "," b","c"
        new String[] {"\" a \"", "\" b\"", "\"c\""});  
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues 
   * Quoted fields, quoted single char delimiter.
   */
  public void testQuotedFields_QuotedSingleCharDelimiter(){
    /* Comma delimiter */
    convertor.setDelimiter(",");
    convertor.setProtectQuotedFields(true);
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(false);
    quotedCommaDelimiter2();
    
    /* Pipe delimiter */
    convertor.setDelimiter("|");
    quotedPipeDelimiter2();
    convertor.setDelimiterAlwaysLiteralString(true);
    quotedPipeDelimiter2();
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(true);    
    check(convertor.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
        new String[] {"", "\"a|1\"", "|",  "\"b\"", "|",  "\"c|2\"", ""});
  }
  
  private void quotedCommaDelimiter2(){
    check(convertor.extractValues("\"a,1\",\"b\",\"c,2\""),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(convertor.extractValues("\"a,1\",\"b\",\"c,2\""),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(convertor.extractValues("\"a,1\",\"b,\",\"c,2\""),  //"a,1","b,","c,2"
        new String[] {"\"a,1\"", "\"b,\"", "\"c,2\""});
    check(convertor.extractValues("\"a,1\",\"b, ,\",\"c,2\","),  //"a,1","b, ,","c,2",
        new String[] {"\"a,1\"", "\"b, ,\"", "\"c,2\"", ""});
  }
  
  private void quotedPipeDelimiter2(){
    check(convertor.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
        new String[] {"\"a|1\"", "\"b\"", "\"c|2\""});
    check(convertor.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
        new String[] {"\"a|1\"", "\"b\"", "\"c|2\""});
    check(convertor.extractValues("\"a|1\"|\"b|\"|\"c|2\""),  //"a|1"|"b|"|"c|2"
        new String[] {"\"a|1\"", "\"b|\"", "\"c|2\""});
    check(convertor.extractValues("\"a|1\"|\"b| |\"|\"c|2\"|"),  //"a|1"|"b| |"|"c|2"|
        new String[] {"\"a|1\"", "\"b| |\"", "\"c|2\"", ""});
  }
  
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues.
   * Quoted and unquoted fields, quoted and unquoted multi char delimiter.
   */
  public void testQuotedFields_QuotedMultiCharDelimiter(){
    convertor.setDelimiter("::");
    convertor.setProtectQuotedFields(true);
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(false);
    doubleColonDelimiter();
    convertor.setDelimiterAlwaysRegExp(true);
    doubleColonDelimiter();
    convertor.setDelimiterAlwaysRegExp(false);
    convertor.setDelimiterAlwaysLiteralString(true);
    doubleColonDelimiter();
  }
  
  
  private void doubleColonDelimiter(){
    check(convertor.extractValues("a::b::c"),
        new String[] {"a", "b", "c"});
    check(convertor.extractValues("a::b::c::"),
        new String[] {"a", "b", "c", ""});
    check(convertor.extractValues("a::b::::c"),
        new String[] {"a", "b", "", "c"});
    check(convertor.extractValues("a::b:: ::c"),
        new String[] {"a", "b", " ", "c"});
    check(convertor.extractValues("a::b:::c"),
        new String[] {"a", "b", ":c"});
    check(convertor.extractValues(":a::b:::c:"),
        new String[] {":a", "b", ":c:"});
    check(convertor.extractValues("\"a::1\"::\"b\"::\"c::2\""), //"a::1"::"b"::"c::2"
        new String[] {"\"a::1\"", "\"b\"", "\"c::2\""});
    check(convertor.extractValues("\"a:::1\":::\"b\"::\"c::2\""), //"a:::1":::"b"::"c::2"
        new String[] {"\"a:::1\"",":\"b\"", "\"c::2\""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues 
   * Tests unclosed quotes.
   */
  public void testUnclosedQuotes(){
    convertor.setProtectQuotedFields(true);
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(false);
    unclosedQuotes();
    convertor.setDelimiterAlwaysLiteralString(true);
    unclosedQuotes();
    convertor.setDelimiterAlwaysLiteralString(false);
    convertor.setDelimiterAlwaysRegExp(true);
    unclosedQuotes();
  }
  
  private void unclosedQuotes(){
    convertor.setDelimiter(",");
    check(convertor.extractValues("foo\",bar"),  //foo",bar
        new String[] {"foo\"", "bar"});
    check(convertor.extractValues("foo,\" ,bar"),//foo," ,bar
        new String[] {"foo", "\" ", "bar"});
    convertor.setDelimiter("::");
    check(convertor.extractValues("\"a::1\"::\"b\"::\"c::2"), //"a::1"::"b"::"c::2
        new String[] {"\"a::1\"", "\"b\"", "\"c", "2"});
  }
  

  private void check(String[] strings, String[] strings2) {
    assertTrue(strings.length == strings2.length);
    for (int i = 0; i < strings2.length; i++) {
      assertTrue(strings[i].equals(strings2[i]));
    }
  }
  
}
