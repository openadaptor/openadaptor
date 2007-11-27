/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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
package org.openadaptor.auxil.processor;

import java.util.List;

import org.openadaptor.auxil.connector.jdbc.JDBCConnectionTestCase;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.spring.SpringAdaptor;
import org.openadaptor.util.SystemTestUtil;
import org.openadaptor.util.TestComponent;

/**
 * System tests for {@link GenericEnrichmentProcessor}.
 * Runs adaptor with a simple test reader, an enricher. Verifies the writer gets 
 * the expected data.
 * 
 * @author Kris Lachor
 */
public class GenericEnrichmentProcessorSystemTestCase extends JDBCConnectionTestCase {
  
  private static String SCHEMA = "CREATE MEMORY TABLE TRADE(TRADEID INTEGER NOT NULL,BUYSELL CHAR(1) NOT NULL,SECID INTEGER NOT NULL,PARTYID INTEGER NOT NULL,QTY INTEGER NOT NULL,PRICE FLOAT NOT NULL); INSERT INTO TRADE VALUES(1,\'B\',1,1,1000000,3.25E0); INSERT INTO TRADE VALUES(2,\'B\',1,1,500000,3.21E0); INSERT INTO TRADE VALUES(3,\'S\',2,1,250000,1.01E0); INSERT INTO TRADE VALUES(4,\'B\',2,1,1000000,0.99E0);"
                                + " INSERT INTO TRADE VALUES(5,\'S\',1,1,1000000,3.26E0)";
  
  private static final String RESOURCE_LOCATION = "test/system/src/";
  
  private static final String DB_ENRICHMENT_PROCESSOR = "db_enrichment_processor.xml";
  
  /**
   * @see JDBCConnectionTestCase#getSchemaDefinition()
   */
  public String getSchemaDefinition() {
    return SCHEMA;
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor
   * #prepareParameters(java.lang.Object)}.
   * 
   * Executes an adaptor with an enrichment processor. Verifies there were no errors.
   * Verifies the writer received one message - an ordered map with two elements, one 
   * of which comes from the enrichment processor.
   */
  public void testPrepareParameters() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, DB_ENRICHMENT_PROCESSOR);
    Adaptor adaptor = springAdaptor.getAdaptor();
    assertEquals(adaptor.getExitCode(),0);
    List writeConnectors = SystemTestUtil.getWriteConnectors(springAdaptor);
    assertNotNull(writeConnectors);
    assertTrue(writeConnectors.size()==1);
    TestComponent.TestWriteConnector testWriter = (TestComponent.TestWriteConnector) 
    	writeConnectors.get(0);
    List data = testWriter.dataCollection;
    assertNotNull(data);
    
    /* It should hold one ordered map with two elements, second element being from the enricher */
    assertTrue(data.size()==1);
    Object [] obj = (Object []) data.get(0);
    assertTrue(obj[0] instanceof IOrderedMap);
    IOrderedMap dataOM = (IOrderedMap) obj[0];
    assertTrue(dataOM.size()==2);
    Object enhanceElement = dataOM.get(1);
    assertTrue(enhanceElement.equals(new Integer(500000))); 
  }

}
