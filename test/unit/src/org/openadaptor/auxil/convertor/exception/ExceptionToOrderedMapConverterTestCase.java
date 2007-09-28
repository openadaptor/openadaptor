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
		assertTrue(map.containsKey(convertor.TIMESTAMP));
		assertTrue(map.containsKey(convertor.EXCEPTION_CLASS));
		assertTrue(map.containsKey(convertor.COMPONENT));
		assertTrue(map.containsKey(convertor.DATA));
		
		assertEquals("java.lang.RuntimeException", map.get(convertor.EXCEPTION_CLASS));
		assertEquals("SOURCE", map.get(convertor.COMPONENT));
		assertEquals("EEEE", map.get(convertor.DATA));
	}

	public void testWithDateFormatConvert() {
		MessageException exception = new MessageException("''", new java.sql.SQLException("SQL Exception"), "...");
		convertor.setTimestampFormat("yyyy-MM-dd HH:mm");
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
		Object result = convertor.convert(exception);
		assertTrue("Result is not a hash map", result instanceof OrderedHashMap);
		OrderedHashMap map = (OrderedHashMap)result;
		assertTrue(map.containsKey(convertor.TIMESTAMP));
		assertTrue(map.containsKey(convertor.EXCEPTION_CLASS));
		assertTrue(map.containsKey(convertor.COMPONENT));
		assertTrue(map.containsKey(convertor.DATA));
		
		if (!now.equals(map.get(convertor.TIMESTAMP))) {
			// possible but very unlikely we clicked over to the next minute
			// between getting the current date and making the conversion
			// but we can handle that
			now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
			assertEquals(now, map.get(convertor.TIMESTAMP));
		}
		
		assertEquals("java.sql.SQLException", map.get(convertor.EXCEPTION_CLASS));
		assertEquals("...", map.get(convertor.COMPONENT));
		assertEquals("''", map.get(convertor.DATA));
	}

}