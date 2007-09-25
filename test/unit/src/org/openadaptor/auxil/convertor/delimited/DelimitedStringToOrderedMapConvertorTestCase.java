package org.openadaptor.auxil.convertor.delimited;

import java.util.ArrayList;
import java.util.Iterator;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

public class DelimitedStringToOrderedMapConvertorTestCase extends TestAbstractDelimitedStringConvertor {

	private DelimitedStringToOrderedMapConvertor convertor = null;
		
	public void setUp() throws Exception {
    super.setup();
		convertor =(DelimitedStringToOrderedMapConvertor)testProcessor;
	}
  
  protected IDataProcessor createProcessor() {
    return new DelimitedStringToOrderedMapConvertor();
  }
  
  // Test conversion from Delimited String to Ordered Map
  public void testDelimitedStringToOrderedMapConversion() {
    convertor.setFieldNames(NAMES);
    convertor.validate(new ArrayList());
    try {
      Object[] maps = convertor.process(ds);
      assertEquals(maps.length, 1);
      assertEquals(om, maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }
  
  public void testDelimitedStringToOrderedMapWithHeadersInFirstRow() {
    convertor.validate(new ArrayList());
    try {
      convertor.setFirstRecordContainsFieldNames(true);

      String headerDS = generateDelimitedString(DELIMITER, NAMES);
      Object[] maps = convertor.process(headerDS);
      assertEquals(maps.length, 0);
      assertEquals(headerDS, generateDelimitedString(DELIMITER, convertor.getFieldNames()));

      maps = convertor.process(ds);
      assertEquals(maps.length, 1);
      assertEquals(om, maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testDelimitedStringToOrderedMapWithTrailingEmptyStrings() {
    // Set Test Data and Expectations
    IOrderedMap expectedOm = generateOrderedMap(NAMES_FOR_TRAILING_EMPTY_ELEMENTS, VALUES_TRAILING_EMPTY_ELEMENTS);
    String testDS = generateDelimitedString(DELIMITER, VALUES_TRAILING_EMPTY_ELEMENTS);
    convertor.setFieldNames(NAMES_FOR_TRAILING_EMPTY_ELEMENTS);
    // Test
    convertor.validate(new ArrayList());
    try {
      Object[] maps = convertor.process(testDS);
      assertEquals(maps.length, 1);
      assertEquals(expectedOm, maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }
  
  public void testInvalidInput() {
    convertor.validate(new ArrayList());
    try {
      convertor.process(om);
      fail("Convertor expects a String value " + om.getClass().getName());
    } catch (RecordException pe) {
    }
  }

	
  public void testProcessRecord() {
		String record = "abc,b,xyz,dddddddddddddddddddddddddddddddddddddddddddd";
		Object result = convertor.convert(record);
		assertTrue(result instanceof IOrderedMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("abc");
		map.add("b");
		map.add("xyz");
		map.add("dddddddddddddddddddddddddddddddddddddddddddd");
		assertEquals(map, result);
	}
	
	public void testInvalidConvert() {
		Object o = new Object();
		try {
			convertor.convert(o);
			fail("Expected a RecordFormatException");
		} catch (RecordFormatException e) {
		}
	}

	public void testQuotedConvert() {
		String record = "\"abc\",\"b\",\"xyz\",dddddddddddddddddddddddddddddddddddddddddddd";
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof IOrderedMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("abc");
		map.add("b");
		map.add("xyz");
		map.add("dddddddddddddddddddddddddddddddddddddddddddd");
		assertEquals(map, result);
	}
	
	public void testQuotedConvertNotStripped() {
		String record = "\"abc\",\"b\",\"xyz\",dddddddddddddddddddddddddddddddddddddddddddd";
		convertor.setStripEnclosingQuotes(false);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof IOrderedMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("\"abc\"");
		map.add("\"b\"");
		map.add("\"xyz\"");
		map.add("dddddddddddddddddddddddddddddddddddddddddddd");
		assertEquals(map, result);
	}
	
	public void testSingleQuotedConvert() {
		String record = "'abc','b','xyz',dddddddddddddddddddddddddddddddddddddddddddd";
		convertor.setStripEnclosingQuotes(true);
		convertor.setQuoteChar('\'');
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof IOrderedMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("abc");
		map.add("b");
		map.add("xyz");
		map.add("dddddddddddddddddddddddddddddddddddddddddddd");
		assertEquals(map, result);
	}
	
	public void testSlashDelimtedConvert() {
		String record = "abc/b/xyz/dddddddddddddddddddddddddddddddddddddddddddd";
		convertor.setDelimiter("/");
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof IOrderedMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("abc");
		map.add("b");
		map.add("xyz");
		map.add("dddddddddddddddddddddddddddddddddddddddddddd");
		assertEquals(map, result);
	}
	
	public void testMultiCharDelimtedConvert() {
		String record = "abcxxxbxxxxyzxxxdddddddddddddddddddddddddddddddddddddddddddd";
		convertor.setDelimiter("xxx");
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof IOrderedMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("abc");
		map.add("b");
		map.add("xyz");
		map.add("dddddddddddddddddddddddddddddddddddddddddddd");
		assertEquals(map, result);
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
  	
	public void testExtractFieldNames() {
		String record = "one,two,three,four";
		convertor.setFirstRecordContainsFieldNames(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		convertor.convert(record);
		String[] fieldNames = convertor.getFieldNames();
		String[] headers = {"one","two","three","four"};
		assertArraysEqual(headers, fieldNames);
	}

	public void testExtractQuotedFieldNames() {
		String record = "'one','two','three','four','five'";
		convertor.setFirstRecordContainsFieldNames(true);
		convertor.setQuoteChar('\'');
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		convertor.convert(record);
		String[] fieldNames = convertor.getFieldNames();
		String[] headers = {"one","two","three","four","five"};
		assertArraysEqual(headers, fieldNames);
	}

	public void testMultiCharExtractQuotedFieldNames() {
		String record = "'||one|||'|||'||||two||||'|||three||||f,o,u,r|||'|||five'";
		convertor.setFirstRecordContainsFieldNames(true);
		convertor.setQuoteChar('\'');
		convertor.setDelimiter("|||");
		convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		convertor.setDelimiterAlwaysLiteralString(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		convertor.convert(record);
		String[] fieldNames = convertor.getFieldNames();
		String[] headers = {"||one|||","||||two||||","three","|f,o,u,r","|||five"};
		assertArraysEqual(headers, fieldNames);
	}

	public void testMultiCharExtractFieldNames() {
		String record = "||one|||||two|||three|||f,o,u,r|||'five'";
		convertor.setFirstRecordContainsFieldNames(true);
		convertor.setDelimiter("|||");
		convertor.setProtectQuotedFields(true);
		convertor.setDelimiterAlwaysLiteralString(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		convertor.convert(record);
		String[] fieldNames = convertor.getFieldNames();
		String[] headers = {"||one","||two","three","f,o,u,r","'five'"};
		assertArraysEqual(headers, fieldNames);
	}
	
	public void testRegEx() {
		String record = "one1two2three3f,o,u,r4'five'";
		convertor.setDelimiter("[12345]");
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("one");
		map.add("two");
		map.add("three");
		map.add("f,o,u,r");
		map.add("'five'");
		assertEquals(map, (OrderedHashMap)result);
	}
	
	public void testForcedRegEx() {
		String record = "opienpeeepoe1pyetpee..t";
		convertor.setDelimiter("p.e");
		convertor.setDelimiterAlwaysRegExp(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("o");
		map.add("n");
		map.add("e");
		map.add("1");
		map.add("t");
		map.add("..t");
		assertEquals(map, (OrderedHashMap)result);
	}
	
	public void testForcedNotRegEx1() {
		String record = "one1t..t";
		convertor.setDelimiter(".");
		convertor.setDelimiterAlwaysRegExp(false);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("one1t");
		map.add("");
		map.add("t");
		assertEquals(map, (OrderedHashMap)result);
	}
	
	public void testForcedNotRegEx2() {
		String record = "one1t..t";
		convertor.setDelimiter(".");
		convertor.setDelimiterAlwaysLiteralString(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("one1t");
		map.add("");
		map.add("t");
		assertEquals(map, (OrderedHashMap)result);
	}
	
	public void testConflictingDelimeter() {
		convertor.setDelimiter(".");
		convertor.setDelimiterAlwaysLiteralString(true);
		convertor.setDelimiterAlwaysRegExp(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertTrue("Exceptected a validation exception", exceptions.size() > 0);
		for (Iterator i = exceptions.iterator(); i.hasNext();) {
			Exception e = (Exception)i.next();
			assertTrue("Expected a validation exception not a " + e.getClass().getName(), e instanceof ValidationException);
		}
	}
	
	public void testRegExWithQuotes() {
		String record = "one1two2three3f,o,u,r4\"five\"";
		convertor.setDelimiter("[0-9]");
		//convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("one");
		map.add("two");
		map.add("three");
		map.add("f,o,u,r");
		map.add("five");
		assertEquals(map, (OrderedHashMap)result);
	}
	
	public void testRegExWithSlashQuotes() {
		String record = "one1two2/three/3f-o-u-r4'five'";
		convertor.setDelimiter("[12345]");
		//convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		convertor.setQuoteChar('/');
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap map = new OrderedHashMap();
		map.add("one");
		map.add("two");
		map.add("three");
		map.add("f-o-u-r");
		map.add("'five'");
		assertEquals(map, (OrderedHashMap)result);
	}

	public void testInvalidRegEx() {
		convertor.setDelimiter("[12345");
		convertor.setProtectQuotedFields(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertTrue("Expected exception during validation", exceptions.size() > 0);
		for (Iterator i = exceptions.iterator(); i.hasNext();) {
			Exception e = (Exception)i.next();
			assertTrue("Expected a validation exception not a " + e.getClass().getName(), e instanceof ValidationException);
		}
	}

	public void testRegExWithConflictingQuotes() {
		convertor.setDelimiter("[0-9]");
		convertor.setProtectQuotedFields(true);
		convertor.setQuoteChar('0');
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertTrue("Expected exception during validation", exceptions.size() > 0);
		for (Iterator i = exceptions.iterator(); i.hasNext();) {
			Exception e = (Exception)i.next();
			assertTrue("Expected a validation exception not a " + e.getClass().getName(), e instanceof ValidationException);
		}
	}

	public void testUnbalancedEndQuotes() {
		String record = "'one','two','three";
		convertor.setDelimiter(",");
		convertor.setQuoteChar('\'');
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		try {
			Object result = convertor.convert(record);
			assertTrue("Result is not an OrderedHashMap", result instanceof OrderedHashMap);
			OrderedHashMap expected = new OrderedHashMap();
			expected.add("one");
			expected.add("two");
			expected.add("'three");
			assertEquals(expected, (OrderedHashMap)result);
		} catch (RecordFormatException e) {
			fail("Unexpected exception: " + e);
		}
	}

	public void testUnbalancedQuotes() {
		String record = "'one,'two','three'";
		convertor.setDelimiter(",");
		convertor.setQuoteChar('\'');
		convertor.setStripEnclosingQuotes(true);
		convertor.setProtectQuotedFields(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		OrderedHashMap expected = new OrderedHashMap();
		assertTrue(expected instanceof OrderedHashMap);
		expected.add("one,'two','three");
		
		assertEquals(expected, (OrderedHashMap)result);
	}

	public void testFunkyQuotes1() {
		String record = "'one'one,'two','three'";
		convertor.setDelimiter(",");
		convertor.setQuoteChar('\'');
		convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		OrderedHashMap expected = new OrderedHashMap();
		assertTrue(expected instanceof OrderedHashMap);
		expected.add("'one'one");
		expected.add("two");
		expected.add("three");
		
		assertEquals(expected, (OrderedHashMap)result);
	}

	public void testFunkyQuotes2() {
		String record = "'one',two'two','three'";
		convertor.setDelimiter(",");
		convertor.setQuoteChar('\'');
		convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		OrderedHashMap expected = new OrderedHashMap();
		assertTrue(expected instanceof OrderedHashMap);
		expected.add("one");
		expected.add("two'two'");
		expected.add("three");
		
		assertEquals(expected, (OrderedHashMap)result);
	}
	
	public void testFunkyQuotes3() {
		String record = "'one','two','three'3";
		convertor.setDelimiter(",");
		convertor.setQuoteChar('\'');
		convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap expected = new OrderedHashMap();
		expected.add("one");
		expected.add("two");
		expected.add("'three'3");
		
		assertEquals(expected, (OrderedHashMap)result);
	}

	public void testFunkyQuotes4() {
		String record = "'one,'two'three','four'";
		convertor.setDelimiter(",");
		convertor.setQuoteChar('\'');
		convertor.setProtectQuotedFields(true);
		convertor.setStripEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(record);
		assertTrue(result instanceof OrderedHashMap);
		OrderedHashMap expected = new OrderedHashMap();
		expected.add("one,'two'three");
		expected.add("four");
		
		assertEquals(expected, (OrderedHashMap)result);
	}
 
}