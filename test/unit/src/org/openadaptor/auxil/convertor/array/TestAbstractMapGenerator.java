/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
 */

package org.openadaptor.auxil.convertor.array;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractMapGenerator;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.AbstractTestIDataProcessor;
/**
 * Common unit tests for {@link AbstractDelimitedStringConvertor}.
 */
public abstract class TestAbstractMapGenerator extends AbstractTestIDataProcessor {
  private static final Log log =LogFactory.getLog(AbstractTestIDataProcessor.class);
  
  private AbstractMapGenerator amg;
  private static final String[] DEFAULT_FIELD_NAMES={"F1","F2","F3","F4"};
  private static final Object[] NON_STRING_FIELD_NAMES={new Integer(6),new StringBuffer("SB")};
   
  protected Object[] testInput;
  public void setup() throws Exception {
    super.setUp();
    //Cast testProcessor for our purposes.
    amg=(AbstractMapGenerator)testProcessor;
    testInput=generateTestRecords(5, 6);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    amg=null;
    testInput=null;
  }
  
  public void testFieldNamesAccessors() {
  	//Test setFieldNames
  	amg.setFieldNames(DEFAULT_FIELD_NAMES);
  	String[] stored=amg.getFieldNames();
  	assertEquals("fieldName array length does not match expected", stored.length,DEFAULT_FIELD_NAMES.length);
  	amg.setFieldNames((String[])null);
  	List fieldNamesAsList=Arrays.asList(DEFAULT_FIELD_NAMES);
  	amg.setFieldNames(fieldNamesAsList);
  	stored=amg.getFieldNames();
  	check(amg.getFieldNames(),DEFAULT_FIELD_NAMES);
  	assertEquals("fieldName array length does not match expected", stored.length,DEFAULT_FIELD_NAMES.length);  	
    //Non string field names
    amg.setFieldNames(NON_STRING_FIELD_NAMES);
    stored=amg.getFieldNames();
    assertEquals("mismatched fiend name count",stored.length,NON_STRING_FIELD_NAMES.length);
  }
  
  public void testPadMissingFields() {
  	log.debug("Need test case for padMissingField here");
  }
  
    //Utility methods

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
  	
    protected static Object[] generateTestRecords(int count,int recordSize) {
    	Object[] data=new Object[count];
    	for (int i=0;i<count; i++) {
    		Object[] record = new Object[recordSize];
    		for (int j=0;j<recordSize;j++) {
    			record[j]="R"+String.valueOf(i)+",C"+String.valueOf(j);
    		}
    	}
    	return data;
    }


}
