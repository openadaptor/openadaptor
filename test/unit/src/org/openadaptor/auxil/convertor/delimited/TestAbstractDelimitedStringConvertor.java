package org.openadaptor.auxil.convertor.delimited;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.AbstractTestIDataProcessor;
/**
 * Common unit tests for {@link AbstractDelimitedStringConvertor}.
 */
public abstract class TestAbstractDelimitedStringConvertor extends AbstractTestIDataProcessor {
  
  private AbstractDelimitedStringConvertor adsc;
  
  
  protected static final String[] NAMES = { "F-1", "F-2", "F-3", "F-4" };
  protected static final String[] VALUES = { "Apples", "Oranges", "Bananas", "Pears" };
  protected static final String[] NAMES_FOR_TRAILING_EMPTY_ELEMENTS = { "F-1", "F-2", "F-3", "F-4", "F-5", "F-6", "F-7" };
  protected static final String[] VALUES_TRAILING_EMPTY_ELEMENTS = { "Apples", "Oranges", "Bananas", "Pears", "", "", "" };
  protected static final String DELIMITER = ",";
  protected static final Object[] NON_STRING_NAMES = { new StringBuffer("One"), new Object(), new Integer(3), new BigDecimal(2.3) };
  protected static final Object[] NON_STRING_VALUES = { new Integer(1), null, new HashMap(), new Float(23.4)};
 
  protected String ds,ds2;

  protected IOrderedMap om,om2;

  public void setup() throws Exception {
    super.setUp();
    //Cast testProcessor for our purposes.
    adsc=(AbstractDelimitedStringConvertor)testProcessor;
    
    ds = generateDelimitedString(DELIMITER, VALUES);
    om = generateOrderedMap(NAMES, VALUES);

    om2 = generateOrderedMap(NON_STRING_NAMES,NON_STRING_VALUES);
    ds2= generateDelimitedString(DELIMITER,NON_STRING_VALUES);

  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    ds=null;
    om=null;
    ds2=null;
    om2=null;
    adsc=null;
  }
  
//  /* Need an instance of the tested class */
//  AbstractDelimitedStringConvertor convertor = new AbstractDelimitedStringConvertor(){
//      protected Object convert(Object record) {
//        throw new UnsupportedOperationException("Method not implemetned");
//      }};

  /**
   * Test AbstractDelimitedStringConvertor#extractValues. 
   * Not quoted fields, single char delimiter. 
   */	
  public void testNotQuotedFields_SingleCharDelimiter(){
    /* Comma delimiter */
    adsc.setDelimiter(",");
    adsc.setProtectQuotedFields(false);
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(false);
    notQuotedCommaDelimiter();
    adsc.setProtectQuotedFields(true);
    notQuotedCommaDelimiter();
    adsc.setDelimiterAlwaysLiteralString(true);
    notQuotedCommaDelimiter();
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(true);
    notQuotedCommaDelimiter();
    
    /* Pipe delimiter */
    adsc.setDelimiter("|");
    adsc.setProtectQuotedFields(false);
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(false);
    notQuotedPipeDelimiter();
    adsc.setProtectQuotedFields(true);
    notQuotedPipeDelimiter();
    adsc.setDelimiterAlwaysLiteralString(true);
    notQuotedPipeDelimiter();
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(true);
    check(adsc.extractValues("a|b|c"),
        new String[] {"", "a", "|", "b", "|", "c", ""});
  }
  
  private void notQuotedCommaDelimiter(){
    check(adsc.extractValues("a,b,c"),
        new String[] {"a", "b", "c"});
    check(adsc.extractValues("a,b,c,"),
        new String[] {"a", "b", "c", ""});
    check(adsc.extractValues("a,b,c"),
        new String[] {"a", "b", "c"});
    check(adsc.extractValues("a,b,c,"),
        new String[] {"a", "b", "c", ""});
    check(adsc.extractValues(",,a,b,c,"),
        new String[] {"", "", "a", "b", "c", ""});
    check(adsc.extractValues(",,aa,,b,c,"),
        new String[] {"", "", "aa", "", "b", "c", ""});
  }
  
  private void notQuotedPipeDelimiter(){
    check(adsc.extractValues("a|b|c"),
        new String[] {"a", "b", "c"});
    check(adsc.extractValues("a|b|c|"),
        new String[] {"a", "b", "c", ""});
    check(adsc.extractValues("a|b|c"),
        new String[] {"a", "b", "c"});
    check(adsc.extractValues("a|b|c|"),
        new String[] {"a", "b", "c", ""});
    check(adsc.extractValues("||a|b|c|"),
        new String[] {"", "", "a", "b", "c", ""});
    check(adsc.extractValues("||aa||b|c|"),
        new String[] {"", "", "aa", "", "b", "c", ""});
  }

  /**
   * Test AbstractDelimitedStringConvertor#extractValues. 
   * Quoted fields, single char delimiter. 
   */
  public void testQuotedFields_SingleCharDelimiter(){
    /* Comma delimiter */
    adsc.setDelimiter(",");
    adsc.setProtectQuotedFields(true);
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(false);
    quotedCommaDelimiter();
    adsc.setDelimiterAlwaysRegExp(true);
    quotedCommaDelimiter(); 
  }
  
  private void quotedCommaDelimiter(){
    check(adsc.extractValues("\"a\",\"b\",\"c\""),    //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(adsc.extractValues("\"a\",b,\"c\""),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(adsc.extractValues("\"a\",\"b\",\"c\","),   //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(adsc.extractValues("\"a\",b,\"c\""),        //"a",b,"c"
        new String[] {"\"a\"", "b", "\"c\""});
    check(adsc.extractValues("\"a\",\"b\",\"c\""),     //"a","b","c"
        new String[] {"\"a\"", "\"b\"", "\"c\""});
    check(adsc.extractValues("\"a\",\"b\",\"c\","),    //"a","b","c",
        new String[] {"\"a\"", "\"b\"", "\"c\"", ""});
    check(adsc.extractValues("\"a\",,b,\"c\""),       //"a",,b,"c"
        new String[] {"\"a\"", "", "b", "\"c\""});
    check(adsc.extractValues("\"a\",\"\",\"c\""),      //"a","","c"
        new String[] {"\"a\"", "\"\"", "\"c\""});
    check(adsc.extractValues("\" a \",\" b\",\"c\""),  //" a "," b","c"
        new String[] {"\" a \"", "\" b\"", "\"c\""});  
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues 
   * Quoted fields, quoted single char delimiter.
   */
  public void testQuotedFields_QuotedSingleCharDelimiter(){
    /* Comma delimiter */
    adsc.setDelimiter(",");
    adsc.setProtectQuotedFields(true);
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(false);
    quotedCommaDelimiter2();
    
    /* Pipe delimiter */
    adsc.setDelimiter("|");
    quotedPipeDelimiter2();
    adsc.setDelimiterAlwaysLiteralString(true);
    quotedPipeDelimiter2();
//    convertor.setDelimiterAlwaysLiteralString(false);
//    convertor.setDelimiterAlwaysRegExp(true);    
//    check(convertor.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
//        new String[] {"", "\"a|1\"", "|",  "\"b\"", "|",  "\"c|2\"", ""});
  }
  
  private void quotedCommaDelimiter2(){
    check(adsc.extractValues("\"a,1\",\"b\",\"c,2\""),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(adsc.extractValues("\"a,1\",\"b\",\"c,2\""),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(adsc.extractValues("\"a,1\",\"b,\",\"c,2\""),  //"a,1","b,","c,2"
        new String[] {"\"a,1\"", "\"b,\"", "\"c,2\""});
    check(adsc.extractValues("\"a,1\",\"b, ,\",\"c,2\","),  //"a,1","b, ,","c,2",
        new String[] {"\"a,1\"", "\"b, ,\"", "\"c,2\"", ""});
  }
  
  private void quotedPipeDelimiter2(){
    check(adsc.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
        new String[] {"\"a|1\"", "\"b\"", "\"c|2\""});
    check(adsc.extractValues("\"a|1\"|\"b\"|\"c|2\""),  //"a|1"|"b"|"c|2"
        new String[] {"\"a|1\"", "\"b\"", "\"c|2\""});
    check(adsc.extractValues("\"a|1\"|\"b|\"|\"c|2\""),  //"a|1"|"b|"|"c|2"
        new String[] {"\"a|1\"", "\"b|\"", "\"c|2\""});
    check(adsc.extractValues("\"a|1\"|\"b| |\"|\"c|2\"|"),  //"a|1"|"b| |"|"c|2"|
        new String[] {"\"a|1\"", "\"b| |\"", "\"c|2\"", ""});
  }
  
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues 
   * Quoted blocks, quoted single char delimiter. Tests escaping the quote character 
   * inside a quoted block.
   */
  public void testQuotedFields_QuotedSingleCharDelimiterWithEscapedQuotes(){
    /* Comma delimiter, default quote escaping character  */
    adsc.setDelimiter(",");
    adsc.setQuoteChar('\'');
    adsc.setProtectQuotedFields(true);
    adsc.setDelimiterAlwaysLiteralString(true);
    adsc.setDelimiterAlwaysRegExp(false);
    adsc.setStripEnclosingQuotes(true);
    adsc.setEscapeQuoteCharacters(true);
    quotedCommaDelimiterWithEscapeCharacter();
    
    /* Similar tests but with overriden quote escaping character */
    adsc.setQuoteEscapeChar('@');
    quotedCommaDelimiterWithEscapeCharacter2();
  }
  
  private void quotedCommaDelimiterWithEscapeCharacter(){
    /* unescaped quote character inside quoted block - a problem reported by the user */
    check(adsc.extractValues("\'a,1\',\'b\'1\',\'c,2\'"),  //'a,1','b'1','c,2'
        new String[] {"a,1", "\'b\'1\',\'c", "2\'"});
    
    /* 
     * Escaping the quote character inside quoted block generates the correct output.
     * The escape character is removed and will not occur in the output.
     */
    check(adsc.extractValues("\'a,1\',\'b\\'1\',\'c,2\'"),  //'a,1','b\'1','c,2'
        new String[] {"a,1", "b\'1", "c,2"});
  }
  
  private void quotedCommaDelimiterWithEscapeCharacter2(){
    /* unescaped quote character inside quoted block - a problem reported by the user */
    check(adsc.extractValues("\'a,1\',\'b\'1\',\'c,2\'"),  //'a,1','b'1','c,2'
        new String[] {"a,1", "\'b\'1\',\'c", "2\'"});
    
    /* 
     * Escaping the quote character inside quoted block generates the correct output.
     * The escape character is removed and will not occur in the output.
     */
    check(adsc.extractValues("\'a,1\',\'b@\'1\',\'c,2\'"),  //'a,1','b@'1','c,2'
        new String[] {"a,1", "b\'1", "c,2"});
  }
  
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues 
   * Quoted blocks, quoted single char delimiter. 
   * 
   * @see comments in {@link #quotedCommaDelimiterWithSmartEscapeCharacter()}
   */
  public void testQuotedFields_QuotedSingleCharDelimiterWithSmartEscapedQuotes(){
    adsc.setDelimiter(",");
    adsc.setQuoteChar('\'');
    adsc.setProtectQuotedFields(true);
    adsc.setDelimiterAlwaysLiteralString(true);
    adsc.setDelimiterAlwaysRegExp(false);
    adsc.setStripEnclosingQuotes(true);
    adsc.setSmartEscapeQuoteCharacters(true);
    quotedCommaDelimiterWithSmartQuoteEscaping();
  }
  
  private void quotedCommaDelimiterWithSmartQuoteEscaping(){
    /* 
     * Smart escaping the quote character inside quoted block generates the correct output.
     * No escape character per se is used, the converter is guessing which quotes need
     * to be escaped.
     */
    check(adsc.extractValues("\'a,1\',\'b\'1\',\'c,2\'"),  //'a,1','b'1','c,2'
        //                                                                ^-this quote needs to be 'smart-escaped'
        new String[] {"a,1", "b\'1", "c,2"});
   
    check(adsc.extractValues("\'a,1\',\'b\'\'\'\'\'1\',\'c,2\'"),  //'a,1','b'''''1','c,2'
        //                                                                        ^^^^^
        new String[] {"a,1", "b\'\'\'\'\'1", "c,2"});
    check(adsc.extractValues("\'a,1\',\'b\'1\'\'\',\'c,2\'"),  //'a,1','b'1''','c,2'
        //                                                                ^ ^^ 
        new String[] {"a,1", "b\'1\'\'", "c,2"});
  }
  
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues.
   * Quoted and unquoted fields, quoted and unquoted multi char delimiter.
   */
  public void testQuotedFields_QuotedMultiCharDelimiter(){
    adsc.setDelimiter("::");
    adsc.setProtectQuotedFields(true);
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(false);
    doubleColonDelimiter();
    adsc.setDelimiterAlwaysRegExp(true);
    doubleColonDelimiter();
    adsc.setDelimiterAlwaysRegExp(false);
    adsc.setDelimiterAlwaysLiteralString(true);
    doubleColonDelimiter();
  }
  
  
  private void doubleColonDelimiter(){
    check(adsc.extractValues("a::b::c"),
        new String[] {"a", "b", "c"});
    check(adsc.extractValues("a::b::c::"),
        new String[] {"a", "b", "c", ""});
    check(adsc.extractValues("a::b::::c"),
        new String[] {"a", "b", "", "c"});
    check(adsc.extractValues("a::b:: ::c"),
        new String[] {"a", "b", " ", "c"});
    check(adsc.extractValues("a::b:::c"),
        new String[] {"a", "b", ":c"});
    check(adsc.extractValues(":a::b:::c:"),
        new String[] {":a", "b", ":c:"});
    check(adsc.extractValues("\"a::1\"::\"b\"::\"c::2\""), //"a::1"::"b"::"c::2"
        new String[] {"\"a::1\"", "\"b\"", "\"c::2\""});
    check(adsc.extractValues("\"a:::1\":::\"b\"::\"c::2\""), //"a:::1":::"b"::"c::2"
        new String[] {"\"a:::1\"",":\"b\"", "\"c::2\""});
  }
  
  /**
   * Test AbstractDelimitedStringConvertor#extractValues 
   * Tests unclosed quotes.
   */
  public void testUnclosedQuotes(){
    adsc.setProtectQuotedFields(true);
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(false);
    unclosedQuotes();
    adsc.setDelimiterAlwaysLiteralString(true);
    unclosedQuotes();
    adsc.setDelimiterAlwaysLiteralString(false);
    adsc.setDelimiterAlwaysRegExp(true);
    unclosedQuotes();
  }

  private void unclosedQuotes(){
	    adsc.setDelimiter(",");
		ArrayList exceptions = new ArrayList();
		adsc.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
	    check(adsc.extractValues("foo\",bar"),  //foo",bar
	        new String[] {"foo\"", "bar"});
	    check(adsc.extractValues("foo,\" ,bar"),//foo," ,bar
	        new String[] {"foo", "\" ", "bar"});
	    check(adsc.extractValues("foo,\" ,bar,\" ,flub"),//foo," ,bar," ,flub
		        new String[] {"foo", "\" ,bar,\" ","flub"});
	    adsc.setDelimiter("::");
		exceptions = new ArrayList();
		adsc.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
	    check(adsc.extractValues("\"a::1\"::\"b\"::\"c::2"), //"a::1"::"b"::"c::2
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
  		String[] result = adsc.extractValuesRegExp(record, "e+");
  		assertArraysEqual(expected, result);
  	}
  
  	public void testExtractValuesNumberRegExp() {
  		String record = "one1t..t2two0three9";
  		String[] expected = {"one","t..t", "two", "three", ""};
  		String[] result = adsc.extractValuesRegExp(record, "[0-9]");
  		assertArraysEqual(expected, result);
  	}
  
  	public void testQuotedExtractValuesRegExp() {
  		String record = "'oe'e'en'e'1'ee'tee'eee'.'e'.'eeeee'eet'";
  		String[] expected = {"'oe'","'en'","'1'","'tee'","'.'","'.'","'eet'"};
  		String[] result = adsc.extractQuotedValuesRegExp(record, "e+", '\'');
  		assertArraysEqual(expected, result);
  	}
  
  	public void testQuotedExtractValuesNumberRegExp() {
  		String record = "/one/1/t.00.t/2/t2wo/0/three33/9//";
  		String[] expected = {"/one/","/t.00.t/", "/t2wo/", "/three33/", "//"};
  		String[] result = adsc.extractQuotedValuesRegExp(record, "[0-9]", '/');
  		assertArraysEqual(expected, result);
  	}
  
    //Utility methods
    protected static String generateDelimitedString(String delimiter, Object[] data) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < data.length; i++) {
        sb.append(data[i]);
        if (i < data.length - 1) {
          sb.append(delimiter);
        }
      }
      return sb.toString();
    }

    protected static IOrderedMap generateOrderedMap(Object[] names, Object[] values) {
      // Create using Map add(key, value)
      IOrderedMap map = new OrderedHashMap(values.length);
      for (int i = 0; i < values.length; i++) {
        map.put(names[i], values[i]);
      }
      return map;
    }

  	protected static void check(String[] actual, String[] expected) {
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
