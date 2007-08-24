package org.openadaptor.auxil.convertor.delimited;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Unit tests for {@link OrderedMapToDelimitedStringConvertorTestCase}.
 */
public class OrderedMapToDelimitedStringConvertorTestCase extends TestCase {

	private OrderedMapToDelimitedStringConvertor convertor;
	
	public void setUp() {
		convertor = new OrderedMapToDelimitedStringConvertor();
	}
	
	public void testConvertNull() {
		OrderedHashMap map = null;
		try {
			Object result = convertor.convert(map);
			fail("Convertor should have thrown a RecordFormatException on null");
		} catch (Exception e) {
			assertTrue("Expected a RecordFormatException not a " + e, e instanceof RecordFormatException);
		}
	}
	
	public void testConvertEmptyMap() {
		OrderedHashMap map = new OrderedHashMap();
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("",(String)result);
	}
	
	public void testConvertOneEmptyEntry() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("");
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("",(String)result);
	}
	
	public void testConvertOneSpaceEntry() {
		OrderedHashMap map = new OrderedHashMap();
		map.add(" ");
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals(" ",(String)result);
	}
	
	public void testConvertOneQuotedEmptyEntry() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("");
		convertor.setQuoteChar('\'');
		convertor.setAddNeededEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("",(String)result);
	}
	
	public void testConvertTwoEmptyEntries() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("");
		map.add("");
		convertor.setAddNeededEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals(",",(String)result);
	}
	
	public void testConvertTwoSpaceEntries() {
		OrderedHashMap map = new OrderedHashMap();
		map.add(" ");
		map.add(" ");
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals(" , ",(String)result);
	}
	
	public void testConvertTwoQuotedEmptyEntries() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("");
		map.add("");
		convertor.setProtectQuotedFields(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals(",",(String)result);
	}
	
	public void testConvertTwoQuotedDelimiterEntries() {
		OrderedHashMap map = new OrderedHashMap();
		map.add(",");
		map.add("");
		convertor.setAddNeededEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("\",\",",(String)result);
	}
	
	public void testConvertOrderedHashMap() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("ONE");
		map.add("TWO");
		map.add("THREE");
		map.add("FOUR");
		convertor.setFirstRecordContainsFieldNames(false);
		convertor.setAddNeededEnclosingQuotes(false);
		convertor.setDelimiter(",");
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("ONE,TWO,THREE,FOUR",(String)result);
	}

	public void testConvertOrderedHashMapWithSlashDelimiter() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("ONE");
		map.add("TWO");
		map.add("THREE");
		map.add("FOUR");
		map.add("FIRST");
		map.add("SECOND");
		map.add("THIRD");
		map.add("FOURTH");
		convertor.setFirstRecordContainsFieldNames(false);
		convertor.setAddNeededEnclosingQuotes(false);
		convertor.setDelimiter("/");
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("ONE/TWO/THREE/FOUR/FIRST/SECOND/THIRD/FOURTH", (String)result);
	}

	public void testConvertOrderedHashMapWithDoubleQuotes() {
		OrderedHashMap map = new OrderedHashMap();
		map.add(",ONE");
		map.add("T,WO");
		map.add("TH,REE");
		map.add("FOU,R");
		map.add("a,");
		map.add("b");
		map.add("c");
		map.add("d");
		convertor.setFirstRecordContainsFieldNames(false);
		convertor.setAddNeededEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("\",ONE\",\"T,WO\",\"TH,REE\",\"FOU,R\",\"a,\",b,c,d", (String)result);
	}
	
	public void testConvertOrderedHashMapContainingSingleQuotes() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("'ONE");
		map.add("TWO'");
		map.add("TH''REE");
		map.add("FOUR");
		map.add("a''");
		map.add("b");
		map.add("''c");
		map.add("d");
		convertor.setFirstRecordContainsFieldNames(false);
		convertor.setAddNeededEnclosingQuotes(true);
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("'ONE,TWO',TH''REE,FOUR,a'',b,''c,d", (String)result);
	}

	public void testConvertOrderedHashMapSlashDelimiter() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("\"ONE");
		map.add("TWO\"");
		map.add("TH\"\"REE");
		map.add("FOUR");
		map.add("a\"\"");
		map.add("b");
		map.add("\"\"c");
		map.add("d");
		convertor.setFirstRecordContainsFieldNames(false);
		convertor.setAddNeededEnclosingQuotes(true);
		convertor.setDelimiter("/");
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("\"ONE/TWO\"/TH\"\"REE/FOUR/a\"\"/b/\"\"c/d", (String)result);
	}

	public void testConvertOrderedHashMapSlashDelimiterSingleQuote() {
		OrderedHashMap map = new OrderedHashMap();
		map.add("/ONE");
		map.add("T/WO");
		map.add("TH//REE");
		map.add("FOU/R");
		map.add("a/");
		map.add("////b");
		map.add("c");
		map.add("d/////");
		convertor.setFirstRecordContainsFieldNames(false);
		convertor.setAddNeededEnclosingQuotes(true);
		convertor.setDelimiter("/");
		convertor.setQuoteChar('\'');
		ArrayList exceptions = new ArrayList();
		convertor.validate(exceptions);
		assertEquals("Unexpected validation error", exceptions.size(), 0);
		Object result = convertor.convert(map);
		assertTrue(result instanceof String);
		assertEquals("'/ONE'/'T/WO'/'TH//REE'/'FOU/R'/'a/'/'////b'/c/'d/////'", (String)result);
	}
}