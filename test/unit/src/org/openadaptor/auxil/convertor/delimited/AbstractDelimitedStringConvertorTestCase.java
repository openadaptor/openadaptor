package org.openadaptor.auxil.convertor.delimited;

import java.util.ArrayList;

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
//    convertor.setDelimiterAlwaysLiteralString(false);
//    convertor.setDelimiterAlwaysRegExp(true);    
//    check(convertor.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
//        new String[] {"", "\"a|1\"", "|",  "\"b\"", "|",  "\"c|2\"", ""});
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
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
	    check(convertor.extractValues("foo\",bar"),  //foo",bar
	        new String[] {"foo\"", "bar"});
	    check(convertor.extractValues("foo,\" ,bar"),//foo," ,bar
	        new String[] {"foo", "\" ", "bar"});
	    check(convertor.extractValues("foo,\" ,bar,\" ,flub"),//foo," ,bar," ,flub
		        new String[] {"foo", "\" ,bar,\" ","flub"});
	    convertor.setDelimiter("::");
		exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
	    check(convertor.extractValues("\"a::1\"::\"b\"::\"c::2"), //"a::1"::"b"::"c::2
	        new String[] {"\"a::1\"", "\"b\"", "\"c", "2"});
	  }
	  
  	private static void assertArraysEqual(String[] expected, String[] actual) {
  		if (expected.length != actual.length) {
  			String got = "Array length mismatch. Here's what we got:";
  			for (int i = 0; i < actual.length; i++) {
  				got += " {" + actual[i] + "}";
  			}
  			fail(got);
  		}
  		for (int i = 0; i < expected.length; i++) {
  			assertEquals("Array parameter mismatch at " + i, expected[i], actual[i]);
  		}
  	}
  	
  	public void testExtractValuesRegExp() {
  		String record = "one1..ttee..";
  		String[] expected = {"on", "1..tt", ".."};
  		String[] result = convertor.extractValuesRegExp(record, "e+");
  		assertArraysEqual(expected, result);
  	}
  
  	public void testExtractValuesNumberRegExp() {
  		String record = "one1t..t2two0three9";
  		String[] expected = {"one","t..t", "two", "three", ""};
  		String[] result = convertor.extractValuesRegExp(record, "[0-9]");
  		assertArraysEqual(expected, result);
  	}
  
  	public void testQuotedExtractValuesRegExp() {
  		String record = "'oe'e'en'e'1'ee'tee'eee'.'e'.'eeeee'eet'";
  		String[] expected = {"'oe'","'en'","'1'","'tee'","'.'","'.'","'eet'"};
  		String[] result = convertor.extractQuotedValuesRegExp(record, "e+", '\'');
  		assertArraysEqual(expected, result);
  	}
  
  	public void testQuotedExtractValuesNumberRegExp() {
  		String record = "/one/1/t.00.t/2/t2wo/0/three33/9//";
  		String[] expected = {"/one/","/t.00.t/", "/t2wo/", "/three33/", "//"};
  		String[] result = convertor.extractQuotedValuesRegExp(record, "[0-9]", '/');
  		assertArraysEqual(expected, result);
  	}
  
  	private static void check(String[] actual, String[] expected) {
  		assertNotNull("String array should not be null", actual);
  		if (expected.length != actual.length) {
  			String got = "Array length mismatch (expected " + expected.length + " but got " + actual.length + ". Here's the data we got:";
  			for (int i = 0; i < actual.length; i++) {
  				got += " {" + actual[i] + "}";
  			}
  			fail(got);
  		}
  		for (int i = 0; i < expected.length; i++) {
  			assertEquals("Array parameter mismatch at " + i, expected[i], actual[i]);
  		}
  	}
}
