package org.openadaptor.auxil.convertor.exception;

import java.text.SimpleDateFormat;

import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.MessageException;

/**
 * Unit tests for {@link ExceptionToOrderedMapConvertor}.
 */
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
		MessageException exception = new MessageException("EEEE", null, new RuntimeException("RUNTIME"), "SOURCE");
		Object result = convertor.convert(exception);
		assertTrue("Result is not a hash map", result instanceof OrderedHashMap);
		OrderedHashMap map = (OrderedHashMap)result;
    testBasicAssertions(map);
		
		assertEquals("java.lang.RuntimeException", map.get(ExceptionToOrderedMapConvertor.EXCEPTION_CLASS));
		assertEquals("SOURCE", map.get(ExceptionToOrderedMapConvertor.COMPONENT));
		assertEquals("EEEE", map.get(ExceptionToOrderedMapConvertor.DATA));
	}

	public void testWithDateFormatConvert() {
		MessageException exception = new MessageException("''", null, new java.sql.SQLException("SQL Exception"), "...");
		convertor.setTimestampFormat("yyyy-MM-dd HH:mm");
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
		Object result = convertor.convert(exception);
		assertTrue("Result is not a hash map", result instanceof OrderedHashMap);
		OrderedHashMap map = (OrderedHashMap)result;
    testBasicAssertions(map);
		
		if (!now.equals(map.get(ExceptionToOrderedMapConvertor.TIMESTAMP))) {
			// possible but very unlikely we clicked over to the next minute
			// between getting the current date and making the conversion
			// but we can handle that
			now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
			assertEquals(now, map.get(ExceptionToOrderedMapConvertor.TIMESTAMP));
		}
		
		assertEquals("java.sql.SQLException", map.get(ExceptionToOrderedMapConvertor.EXCEPTION_CLASS));
		assertEquals("...", map.get(ExceptionToOrderedMapConvertor.COMPONENT));
		assertEquals("''", map.get(ExceptionToOrderedMapConvertor.DATA));
	}

  /**
   * Tests  {@link ExceptionToOrderedMapConvertor#setConvertPayloadToString(boolean)}.
   */
  public void testConvertPayloadToString(){
    MessageException exception = new MessageException(new Object(), null, new RuntimeException("RUNTIME"), "SOURCE");
    Object result = convertor.convert(exception);
    OrderedHashMap map = (OrderedHashMap)result;
    testBasicAssertions(map);
    assertTrue(map.get(ExceptionToOrderedMapConvertor.DATA) instanceof String);
    
    convertor.setConvertPayloadToString(false);
    result = convertor.convert(exception);
    map = (OrderedHashMap)result;
    assertFalse(map.get(ExceptionToOrderedMapConvertor.DATA) instanceof String); 
  }
    
  private void testBasicAssertions(OrderedHashMap map) {
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.TIMESTAMP));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.EXCEPTION_CLASS));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.EXCEPTION_MESSAGE));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.CAUSE_EXCEPTION_CLASS));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.CAUSE_EXCEPTION_MESSAGE));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.STACK_TRACE));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.ADAPTOR_NAME));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.COMPONENT));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.THREAD_NAME));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.DATA_TYPE));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.DATA));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.FIXED));
    assertTrue(map.containsKey(ExceptionToOrderedMapConvertor.REPROCESSED)); 
  }

}