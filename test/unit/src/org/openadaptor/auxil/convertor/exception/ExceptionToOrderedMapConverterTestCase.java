package org.openadaptor.auxil.convertor.exception;

import java.text.SimpleDateFormat;

import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.MessageException;

public class ExceptionToOrderedMapConverterTestCase extends AbstractTestIDataProcessor {

	private ExceptionToOrderedMapConvertor convertor;

   public void setUp() throws Exception {
     super.setUp();
     convertor=(ExceptionToOrderedMapConvertor)testProcessor;
   }
   public void tearDown() throws Exception {
     super.tearDown();
   }
   protected IDataProcessor createProcessor() {
      return new ExceptionToOrderedMapConvertor();
    }

	public void testProcessRecord() {
		MessageException exception = new MessageException("EEEE", new RuntimeException("RUNTIME"), "SOURCE");
		Object result = convertor.convert(exception);
		assertTrue("Result is not a hash map", result instanceof OrderedHashMap);
		OrderedHashMap map = (OrderedHashMap)result;
		assertTrue(map.containsKey("timestamp"));
		assertTrue(map.containsKey("exceptionClass"));
		assertTrue(map.containsKey("originatingComponent"));
		assertTrue(map.containsKey("data"));
		
		assertEquals("java.lang.RuntimeException", map.get("exceptionClass"));
		assertEquals("SOURCE", map.get("originatingComponent"));
		assertEquals("EEEE", map.get("data"));
	}

	public void testWithDateFormatConvert() {
		MessageException exception = new MessageException("''", new java.sql.SQLException("SQL Exception"), "...");
		convertor.setTimestampFormat("yyyy-MM-dd HH:mm");
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
		Object result = convertor.convert(exception);
		assertTrue("Result is not a hash map", result instanceof OrderedHashMap);
		OrderedHashMap map = (OrderedHashMap)result;
		assertTrue(map.containsKey("timestamp"));
		assertTrue(map.containsKey("exceptionClass"));
		assertTrue(map.containsKey("originatingComponent"));
		assertTrue(map.containsKey("data"));
		
		if (!now.equals(map.get("timestamp"))) {
			// possible but very unlikely we clicked over to the next minute
			// between getting the current date and making the conversion
			// but we can handle that
			now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
			assertEquals(now, map.get("timestamp"));
		}
		
		assertEquals("java.sql.SQLException", map.get("exceptionClass"));
		assertEquals("...", map.get("originatingComponent"));
		assertEquals("''", map.get("data"));
	}

}