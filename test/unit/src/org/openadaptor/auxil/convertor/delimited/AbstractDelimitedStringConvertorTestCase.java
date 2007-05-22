package org.openadaptor.auxil.convertor.delimited;

import junit.framework.TestCase;

public class AbstractDelimitedStringConvertorTestCase extends TestCase {

  public void test() {
    
    // no quoted fields
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c", ",", '\"'),
        new String[] {"a", "b", "c"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c,", ",", '\"'),
        new String[] {"a", "b", "c", ""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c", ',', '\"'),
        new String[] {"a", "b", "c"});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("a,b,c,", ',', '\"'),
        new String[] {"a", "b", "c", ""});
    
    // quoted fields but no quoted delimiters
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
    
    // quoted single char delimiter
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a,1\",\"b\",\"c,2\"",  ",", '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a,1\",\"b\",\"c,2\"",  ',', '\"'),  //"a,1","b","c,2"
        new String[] {"\"a,1\"", "\"b\"", "\"c,2\""});
    
    // quoted multi char delimiter
    check(AbstractDelimitedStringConvertor.extractQuotedValues("\"a::1\"::\"b\"::\"c::2\"", "::", '\"'), //"a::1"::"b"::"c::2"
        new String[] {"\"a::1\"", "\"b\"", "\"c::2\""});
    
    // unclosed quotes
    check(AbstractDelimitedStringConvertor.extractQuotedValues("foo\",bar", ",", '\"'),  //foo",bar
        new String[] {"foo\"", "bar"});
}

  private void check(String[] strings, String[] strings2) {
    assertTrue(strings.length == strings2.length);
    for (int i = 0; i < strings2.length; i++) {
      assertTrue(strings[i].equals(strings2[i]));
    }
  }
}
