package org.openadaptor.auxil.convertor.delimited;

import java.util.ArrayList;

import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Unit tests for {@link OrderedMapToDelimitedStringConvertorTestCase}.
 */
public class OrderedMapToDelimitedStringConvertorTestCase extends TestAbstractDelimitedStringConvertor {

	private OrderedMapToDelimitedStringConvertor convertor;
	  
	public void setUp() throws Exception {
    super.setup();
		convertor = (OrderedMapToDelimitedStringConvertor)testProcessor;
	}
  protected IDataProcessor createProcessor() {
    return new OrderedMapToDelimitedStringConvertor();
  }
  
  public void testInvalidInput() {
    convertor.validate(new ArrayList());
    try {
      convertor.process(ds);
      fail("Convertor requires IOrderedMap" + om.getClass().getName());
    } catch (RecordException pe) {
      ;
    }

  }
  
  // Test Conversion from OrderedMap to Delimited String
  public void testOrderedMapToDelimitedStringConversion() {
    convertor.validate(new ArrayList());
    try {
      convertor.setDelimiter(DELIMITER);
      Object[] dsList = convertor.process(om);
      assertEquals(dsList.length, 1);
      assertEquals(ds, (String) dsList[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }
  
/**
 * Test that ordered map to delimited string conversion works with
 * data values that are not strings.
 * Tests issue corresponding to collab issue #SC11
 * <p>
 * Basically any data values supplies should be having their 
 * toString() method called.
 */
  public void testNonStringOrderedMapToDelimitedStringConversion() {
    convertor.validate(new ArrayList());
   try {
      convertor.setDelimiter(DELIMITER);
      Object[] dsList = convertor.process(om2);
      assertEquals(dsList.length, 1);
      assertEquals(ds2, (String) dsList[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testOrderedMapToDelimitedStringWithExplicitHeader() {
    try {
      convertor.setDelimiter(DELIMITER);
      convertor.setOutputHeader(true);
      convertor.setFieldNames(TEST_NAMES);
      convertor.validate(new ArrayList());


      Object[] dsList = convertor.process(om);
      assertEquals(dsList.length, 2);
      //Check the header
      assertEquals(generateDelimitedString(DELIMITER,TEST_NAMES), (String) dsList[0]);

      assertEquals(ds, (String) dsList[1]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testOrderedMapToDelimitedStringWithImplicitHeader() {
    try {
      convertor.setDelimiter(DELIMITER);
      convertor.setOutputHeader(true);
      convertor.validate(new ArrayList());


      Object[] dsList = convertor.process(om);
      assertEquals(dsList.length, 2);

      //Check the header
      assertEquals(generateDelimitedString(DELIMITER,TEST_NAMES), (String) dsList[0]);

      assertEquals(ds, (String) dsList[1]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }


  public void testProcessRecord() {
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
    
    public void testConvertOrderedHashMapForceEnclosingQuotes() {
        OrderedHashMap map = new OrderedHashMap();
        map.add("ONE");
        map.add("TWO");
        map.add("THREE");
        convertor.setDelimiter(",");
        convertor.setForceEnclosingQuotes(true);
        convertor.setQuoteChar('\"');
        ArrayList exceptions = new ArrayList();
        convertor.validate(exceptions);
        assertEquals("Unexpected validation error", exceptions.size(), 0);
        Object result = convertor.convert(map);
        assertTrue(result instanceof String);
        assertEquals("\"ONE\",\"TWO\",\"THREE\"", (String)result);
    }
    
    public void testConvertOrderedHashMapForceEnclosingQuotes2() {
      OrderedHashMap map = new OrderedHashMap();
      map.add("ON,E");
      map.add("TWO");
      map.add("THRE,E");
      convertor.setDelimiter(",");
      convertor.setForceEnclosingQuotes(true);
      convertor.setQuoteChar('\"');
      ArrayList exceptions = new ArrayList();
      convertor.validate(exceptions);
      assertEquals("Unexpected validation error", exceptions.size(), 0);
      Object result = convertor.convert(map);
      assertTrue(result instanceof String);
      assertEquals("\"ON,E\",\"TWO\",\"THRE,E\"", (String)result);
    }
}