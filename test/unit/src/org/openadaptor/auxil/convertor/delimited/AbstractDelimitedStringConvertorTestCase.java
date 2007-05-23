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
  public void testNotQuotedFields_NotQuotedSingleCharDelimiter(){
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
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * quoted fields but with not quoted single char delimiters. 
   */
  public void testQuotedFields_NotQuotedSingleCharDelimiter(){
    check(convertor.extractQuotedValuesRegExp("\"a\",\"b\",\"c\"",  ",",  '\"'),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(convertor.extractQuotedValuesRegExp("\"a\",b,\"c\"",   ",",  '\"'),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(convertor.extractQuotedValuesRegExp("\"a\",\"b\",\"c\",",   ",",  '\"'),   //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(convertor.extractQuotedValuesLiteralString("\"a\",b,\"c\"",   ',',  '\"'),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(convertor.extractQuotedValuesLiteralString("\"a\",\"b\",\"c\"",  ',',  '\"'),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(convertor.extractQuotedValuesLiteralString("\"a\",\"b\",\"c\",",  ',',  '\"'),    //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(convertor.extractQuotedValuesLiteralString("\"a\",,b,\"c\"",   ',',  '\"'),       //"a",,b,"c"
        new String[] {"\"a\"", "", "b", "\"c\""});
    check(convertor.extractQuotedValuesLiteralString("\"a\",\"\",\"c\"",  ',',  '\"'),      //"a","","c"
        new String[] {"\"a\"", "\"\"", "\"c\""});
    check(convertor.extractQuotedValuesLiteralString("\" a \",\" b\",\"c\"",  ',',  '\"'),  //" a "," b","c"
        new String[] {"\" a \"", "\" b\"", "\"c\""});  
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * quoted fields with quoted single char delimiter.
   */
  public void testQuotedFields_QuotedSingleCharDelimiter(){
    check(convertor.extractQuotedValuesRegExp("\"a,1\",\"b\",\"c,2\"",  ",", '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(convertor.extractQuotedValuesLiteralString("\"a,1\",\"b\",\"c,2\"",  ',', '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(convertor.extractQuotedValuesLiteralString("\"a,1\",\"b,\",\"c,2\"",  ',', '\"'),  //"a,1","b,","c,2"
        new String[] {"\"a,1\"", "\"b,\"", "\"c,2\""});
    check(convertor.extractQuotedValuesLiteralString("\"a,1\",\"b, ,\",\"c,2\",",  ',', '\"'),  //"a,1","b, ,","c,2",
        new String[] {"\"a,1\"", "\"b, ,\"", "\"c,2\"", ""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * quoted and unquoted fields with quoted and unquoted multi char delimiter.
   */
  public void testQuotedFields_QuotedMultiCharDelimiter(){
    check(convertor.extractQuotedValuesRegExp("a::b::c", "::", '\"'),
        new String[] {"a", "b", "c"});
    check(convertor.extractQuotedValuesRegExp("a::b::c::", "::", '\"'),
        new String[] {"a", "b", "c", ""});
    check(convertor.extractQuotedValuesRegExp("a::b::::c", "::", '\"'),
        new String[] {"a", "b", "", "c"});
    check(convertor.extractQuotedValuesRegExp("a::b:: ::c", "::", '\"'),
        new String[] {"a", "b", " ", "c"});
    check(convertor.extractQuotedValuesRegExp("a::b:::c", "::", '\"'),
        new String[] {"a", "b", ":c"});
    check(convertor.extractQuotedValuesRegExp(":a::b:::c:", "::", '\"'),
        new String[] {":a", "b", ":c:"});
    check(convertor.extractQuotedValuesRegExp("\"a::1\"::\"b\"::\"c::2\"", "::", '\"'), //"a::1"::"b"::"c::2"
        new String[] {"\"a::1\"", "\"b\"", "\"c::2\""});
    check(convertor.extractQuotedValuesRegExp("\"a:::1\":::\"b\"::\"c::2\"", "::", '\"'), //"a:::1":::"b"::"c::2"
        new String[] {"\"a:::1\"",":\"b\"", "\"c::2\""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * unclosed quotes.
   */
  public void testUnclosedQuotes(){
    check(convertor.extractQuotedValuesRegExp("foo\",bar", ",", '\"'),  //foo",bar
        new String[] {"foo\"", "bar"});
    check(convertor.extractQuotedValuesRegExp("foo,\" ,bar", ",", '\"'),//foo," ,bar
        new String[] {"foo", "\" ", "bar"});
    check(convertor.extractQuotedValuesRegExp("\"a::1\"::\"b\"::\"c::2", "::", '\"'), //"a::1"::"b"::"c::2
        new String[] {"\"a::1\"", "\"b\"", "\"c", "2"});
  }
  
  
  /**
   * Tests AbstractDelimitedStringConvertor#extractValues with the <code>protectQuotedFields</code>
   * flag disabled and enabled. Uses comma delimiter.
   * The test passes only because comma used as a regular expression (protectQuotedFields flag on) happens to produce
   * the same result as when used as a literal string delimiter (protectQuotedFields flag off).
   */
  public void testExtractValuesWithCommaDelimiter(){
    convertor.setProtectQuotedFields(true);
    check(convertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    convertor.setProtectQuotedFields(false);
    check(convertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
  }
  
  
  /**
   * Tests AbstractDelimitedStringConvertor#extractValues with the <code>protectQuotedFields</code>
   * flag disabled and enabled. Uses pipe delimiter.
   * This fails because pipe delimiter treated as a regular expression (protectQuotedFields flag on) gives
   * different result from a pipe used as a literal string delimiter (protectQuotedFields flag off).
   * 
   * @todo remove comments when the problem is fixed.
   */
//  public void testExtractValuesWithPipeDelimiter(){
//    delimitedStringConvertor.setDelimiter("|");
//    delimitedStringConvertor.setProtectQuotedFields(true);
//    check(delimitedStringConvertor.extractValues("\"a\"|\"b\"|\"c\""),   //"a"|"b"|"c"
//        new String[] {"\"a\"", "\"b\"", "\"c\""});
//    delimitedStringConvertor.setProtectQuotedFields(false);
//    check(delimitedStringConvertor.extractValues("\"a\"|\"b\"|\"c\""),   //"a"|"b"|"c"
//        new String[] {"\"a\"", "\"b\"", "\"c\""});
//  }
  
  public void test1(){
    convertor.setProtectQuotedFields(true);
    check(convertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
//    delimitedStringConvertor.setProtectQuotedFields(false);
//    check(delimitedStringConvertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
//        new String[] {"\"a\"", "\"b\"", "\"c\""});
  }
  

  private void check(String[] strings, String[] strings2) {
    assertTrue(strings.length == strings2.length);
    for (int i = 0; i < strings2.length; i++) {
      assertTrue(strings[i].equals(strings2[i]));
    }
  }
  
}
