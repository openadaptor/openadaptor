package org.openadaptor.auxil.connector.jdbc.writer.deprecated.orderedmap;

import java.sql.Connection;

import org.openadaptor.auxil.connector.jdbc.writer.deprecated.orderedmap.SQLStatementConverter;

import junit.framework.TestCase;

public class SQLStatementConvertorTestCase extends TestCase {
	
	SQLStatementConverter convertor = null;
	
	public void setUp() {
		convertor = new SQLStatementConverter();
	}
	
	public void testConvertor() {
		String expected = "INSERT INTO blah (one, two, three, four) VALUES ('$aaa$', '$bbb$', '$ccc$', '$ddd$')";
		String converted = "INSERT INTO blah (one, two, three, four) VALUES ('$1$', '$2$', '$3$', '$4$')";
		converted = convertor.replacePlaceHolder(converted, "1", "aaa");
		converted = convertor.replacePlaceHolder(converted, "2", "bbb");
		converted = convertor.replacePlaceHolder(converted, "3", "ccc");
		converted = convertor.replacePlaceHolder(converted, "4", "ddd");
		assertEquals(expected, converted);
	}

	public void testConvertorDelimiter() {
		convertor.setDelimiter("x");
		String expected = "INSERT INTO blah (one, two, three, four) VALUES ('xaaax', 'xbbbx', 'xcccx', 'xdddx')";
		String converted = "INSERT INTO blah (one, two, three, four) VALUES ('x1x', 'x2x', 'x3x', 'x4x')";
		converted = convertor.replacePlaceHolder(converted, "1", "aaa");
		converted = convertor.replacePlaceHolder(converted, "2", "bbb");
		converted = convertor.replacePlaceHolder(converted, "3", "ccc");
		converted = convertor.replacePlaceHolder(converted, "4", "ddd");
		assertEquals(expected, converted);
	}

	public void testConvertorLongDelimiter() {
		convertor.setDelimiter("xxxx");
		String expected = "INSERT INTO blah (one, two, three, four) VALUES ('xxxxaaaxxxx', 'xxxxbbbxxxx', 'xxxxcccxxxx', 'xxxxdddxxxx')";
		String converted = "INSERT INTO blah (one, two, three, four) VALUES ('xxxx1xxxx', 'xxxx2xxxx', 'xxxx3xxxx', 'xxxx4xxxx')";
		converted = convertor.replacePlaceHolder(converted, "1", "aaa");
		converted = convertor.replacePlaceHolder(converted, "2", "bbb");
		converted = convertor.replacePlaceHolder(converted, "3", "ccc");
		converted = convertor.replacePlaceHolder(converted, "4", "ddd");
		assertEquals(expected, converted);
	}

	public void testEscape() {
		convertor.setEscapeParameters(true);
		String expected = "INSERT INTO blah (one, two, three, four) VALUES ('$''aaa$', '$b''bb$', '$ccc''$', '$''ddd''''$')";
		String converted = "INSERT INTO blah (one, two, three, four) VALUES ('$1$', '$2$', '$3$', '$4$')";
		converted = convertor.replacePlaceHolder(converted, "1", "'aaa");
		converted = convertor.replacePlaceHolder(converted, "2", "b'bb");
		converted = convertor.replacePlaceHolder(converted, "3", "ccc'");
		converted = convertor.replacePlaceHolder(converted, "4", "'ddd''");
		assertEquals(expected, converted);
	}

	public void testTidyString() {
		String expected = "INSERT INTO blah (one, two, three, four) VALUES ('aaa', 'b''bb', 'ccc''', '''ddd''')";
		String preconverted = "INSERT INTO blah (one, two, three, four) VALUES ('$aaa$', '$b''bb$', '$ccc''$', '$''ddd''$')";
		String converted = convertor.stringCleanUp(preconverted, "$", false);
		assertEquals(expected, converted);
	}
	
	public void testConvertAndTidy() {
		convertor.setDelimiter("xxxx");
		String expected = "INSERT INTO blah (one, two, three, four) VALUES ('aaa', 'bbb', 'ccc', 'ddd')";
		String preconverted = "INSERT INTO blah (one, two, three, four) VALUES ('xxxx1xxxx', 'xxxx2xxxx', 'xxxx3xxxx', 'xxxx4xxxx')";
		preconverted = convertor.replacePlaceHolder(preconverted, "1", "aaa");
		preconverted = convertor.replacePlaceHolder(preconverted, "2", "bbb");
		preconverted = convertor.replacePlaceHolder(preconverted, "3", "ccc");
		preconverted = convertor.replacePlaceHolder(preconverted, "4", "ddd");
		
		String converted = convertor.stringCleanUp(preconverted, "xxxx", false);
		assertEquals(expected, converted);
	}
}
