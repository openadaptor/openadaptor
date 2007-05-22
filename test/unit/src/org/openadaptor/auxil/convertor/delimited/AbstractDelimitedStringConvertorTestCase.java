package org.openadaptor.auxil.convertor.delimited;

import junit.framework.TestCase;

public class AbstractDelimitedStringConvertorTestCase extends TestCase {

  /**
   * Test no quoted fields, single char delimiter. 
   */	
  public void testNotQuotedFields_NotQuotedSingleCharDelimiter(){
	check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c", ",", '\"'),
	    new String[] {"a", "b", "c"});
	check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c,", ",", '\"'),
	    new String[] {"a", "b", "c", ""});
	check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c", ',', '\"'),
	    new String[] {"a", "b", "c"});
	check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c,", ',', '\"'),
    	new String[] {"a", "b", "c", ""});
  }
  
  /**
   * Test quoted fields but no quoted delimiters. 
   */
  public void testQuotedFields_NotQuotedSingleCharDelimiter(){
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",\"b\",\"c\"",  ",",  '\"'),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",b,\"c\"",   ",",  '\"'),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",\"b\",\"c\",",   ",",  '\"'),   //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",b,\"c\"",   ',',  '\"'),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",\"b\",\"c\"",  ',',  '\"'),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",\"b\",\"c\",",  ',',  '\"'),    //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",,b,\"c\"",   ',',  '\"'),       //"a",,b,"c"
        new String[] {"\"a\"", "", "b", "\"c\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a\",\"\",\"c\"",  ',',  '\"'),      //"a","","c"
        new String[] {"\"a\"", "\"\"", "\"c\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\" a \",\" b\",\"c\"",  ',',  '\"'),  //" a "," b","c"
        new String[] {"\" a \"", "\" b\"", "\"c\""});  
  }
  
  /**
   * Test quoted single char delimiter.
   */
  public void testQuotedFields_QuotedSingleCharDelimiter(){
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a,1\",\"b\",\"c,2\"",  ",", '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a,1\",\"b\",\"c,2\"",  ',', '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a,1\",\"b,\",\"c,2\"",  ',', '\"'),  //"a,1","b,","c,2"
        new String[] {"\"a,1\"", "\"b,\"", "\"c,2\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a,1\",\"b, ,\",\"c,2\",",  ',', '\"'),  //"a,1","b, ,","c,2",
        new String[] {"\"a,1\"", "\"b, ,\"", "\"c,2\"", ""});
  }
  
  /**
   * Test quoted multi char delimiter.
   */
  public void testQuotedFields_QuotedMultiCharDelimiter(){
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a::b::c", "::", '\"'),
        new String[] {"a", "b", "c"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a::b::c::", "::", '\"'),
        new String[] {"a", "b", "c", ""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a::b::::c", "::", '\"'),
        new String[] {"a", "b", "", "c"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a::b:: ::c", "::", '\"'),
        new String[] {"a", "b", " ", "c"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a::b:::c", "::", '\"'),
        new String[] {"a", "b", ":c"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues(":a::b:::c:", "::", '\"'),
        new String[] {":a", "b", ":c:"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a::1\"::\"b\"::\"c::2\"", "::", '\"'), //"a::1"::"b"::"c::2"
        new String[] {"\"a::1\"", "\"b\"", "\"c::2\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a:::1\":::\"b\"::\"c::2\"", "::", '\"'), //"a:::1":::"b"::"c::2"
        new String[] {"\"a:::1\"",":\"b\"", "\"c::2\""});
  }
  
  /**
   * Test unclosed quotes.
   */
  public void testUnclosedQuotes(){
    check(AbstractDelimitedStringConvertor.extractQuotedValues("foo\",bar", ",", '\"'),  //foo",bar
        new String[] {"foo\"", "bar"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("foo,\" ,bar", ",", '\"'),//foo," ,bar
        new String[] {"foo", "\" ", "bar"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a::1\"::\"b\"::\"c::2", "::", '\"'), //"a::1"::"b"::"c::2
        new String[] {"\"a::1\"", "\"b\"", "\"c", "2"});
  }
  
  
  private void check(String[] strings, String[] strings2) {
    assertTrue(strings.length == strings2.length);
    for (int i = 0; i < strings2.length; i++) {
      assertTrue(strings[i].equals(strings2[i]));
    }
  }
}
