package org.openadaptor.auxil.convertor.delimited;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AbstractDelimitedStringConvertor}.
 */
public class AbstractDelimitedStringConvertorTestCase extends TestCase {
  
  /* Need an instance of the tested class */
  AbstractDelimitedStringConvertor delimitedStringConvertor = new 
    AbstractDelimitedStringConvertor(){
      protected Object convert(Object record) {
        throw new UnsupportedOperationException("Method not implemetned");
      }};

  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * not quoted fields with single char delimiter. 
   */	
  public void testNotQuotedFields_NotQuotedSingleCharDelimiter(){
	check(delimitedStringConvertor.extractQuotedValuesRegExp("a,b,c", ",", '\"'),
	    new String[] {"a", "b", "c"});
	check(delimitedStringConvertor.extractQuotedValuesRegExp("a,b,c,", ",", '\"'),
	    new String[] {"a", "b", "c", ""});
	check(delimitedStringConvertor.extractQuotedValuesLiteralString("a,b,c", ',', '\"'),
	    new String[] {"a", "b", "c"});
	check(delimitedStringConvertor.extractQuotedValuesLiteralString("a,b,c,", ',', '\"'),
    	new String[] {"a", "b", "c", ""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * quoted fields but with not quoted single char delimiters. 
   */
  public void testQuotedFields_NotQuotedSingleCharDelimiter(){
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a\",\"b\",\"c\"",  ",",  '\"'),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a\",b,\"c\"",   ",",  '\"'),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a\",\"b\",\"c\",",   ",",  '\"'),   //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a\",b,\"c\"",   ',',  '\"'),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a\",\"b\",\"c\"",  ',',  '\"'),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a\",\"b\",\"c\",",  ',',  '\"'),    //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a\",,b,\"c\"",   ',',  '\"'),       //"a",,b,"c"
        new String[] {"\"a\"", "", "b", "\"c\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a\",\"\",\"c\"",  ',',  '\"'),      //"a","","c"
        new String[] {"\"a\"", "\"\"", "\"c\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\" a \",\" b\",\"c\"",  ',',  '\"'),  //" a "," b","c"
        new String[] {"\" a \"", "\" b\"", "\"c\""});  
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * quoted fields with quoted single char delimiter.
   */
  public void testQuotedFields_QuotedSingleCharDelimiter(){
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a,1\",\"b\",\"c,2\"",  ",", '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a,1\",\"b\",\"c,2\"",  ',', '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a,1\",\"b,\",\"c,2\"",  ',', '\"'),  //"a,1","b,","c,2"
        new String[] {"\"a,1\"", "\"b,\"", "\"c,2\""});
    check(delimitedStringConvertor.extractQuotedValuesLiteralString("\"a,1\",\"b, ,\",\"c,2\",",  ',', '\"'),  //"a,1","b, ,","c,2",
        new String[] {"\"a,1\"", "\"b, ,\"", "\"c,2\"", ""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * quoted and unquoted fields with quoted and unquoted multi char delimiter.
   */
  public void testQuotedFields_QuotedMultiCharDelimiter(){
    check(delimitedStringConvertor.extractQuotedValuesRegExp("a::b::c", "::", '\"'),
        new String[] {"a", "b", "c"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("a::b::c::", "::", '\"'),
        new String[] {"a", "b", "c", ""});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("a::b::::c", "::", '\"'),
        new String[] {"a", "b", "", "c"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("a::b:: ::c", "::", '\"'),
        new String[] {"a", "b", " ", "c"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("a::b:::c", "::", '\"'),
        new String[] {"a", "b", ":c"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp(":a::b:::c:", "::", '\"'),
        new String[] {":a", "b", ":c:"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a::1\"::\"b\"::\"c::2\"", "::", '\"'), //"a::1"::"b"::"c::2"
        new String[] {"\"a::1\"", "\"b\"", "\"c::2\""});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a:::1\":::\"b\"::\"c::2\"", "::", '\"'), //"a:::1":::"b"::"c::2"
        new String[] {"\"a:::1\"",":\"b\"", "\"c::2\""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractQuotedValues 
   * unclosed quotes.
   */
  public void testUnclosedQuotes(){
    check(delimitedStringConvertor.extractQuotedValuesRegExp("foo\",bar", ",", '\"'),  //foo",bar
        new String[] {"foo\"", "bar"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("foo,\" ,bar", ",", '\"'),//foo," ,bar
        new String[] {"foo", "\" ", "bar"});
    check(delimitedStringConvertor.extractQuotedValuesRegExp("\"a::1\"::\"b\"::\"c::2", "::", '\"'), //"a::1"::"b"::"c::2
        new String[] {"\"a::1\"", "\"b\"", "\"c", "2"});
  }
  
  
  /**
   * Tests AbstractDelimitedStringConvertor#extractValues with the <code>protectQuotedFields</code>
   * flag disabled and enabled. Uses comma delimiter.
   * The test passes only because comma used as a regular expression (protectQuotedFields flag on) happens to produce
   * the same result as when used as a literal string delimiter (protectQuotedFields flag off).
   */
  public void testExtractValuesWithCommaDelimiter(){
    delimitedStringConvertor.setProtectQuotedFields(true);
    check(delimitedStringConvertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    delimitedStringConvertor.setProtectQuotedFields(false);
    check(delimitedStringConvertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
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
    delimitedStringConvertor.setProtectQuotedFields(true);
    check(delimitedStringConvertor.extractValues("\"a\",\"b\",\"c\""),   //"a","b","c"
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
