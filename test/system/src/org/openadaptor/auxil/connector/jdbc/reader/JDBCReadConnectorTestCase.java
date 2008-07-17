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
package org.openadaptor.auxil.connector.jdbc.reader;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.AbstractJDBCConnectionTests;
import org.openadaptor.auxil.connector.jdbc.reader.orderedmap.ResultSetToOrderedMapConverter;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.LocalHSQLJdbcConnection;
import org.openadaptor.util.SystemTestUtil;
import org.openadaptor.util.TestComponent;

/**
 * System tests for {@link JDBCReadConnector}.
 * Uses a test table with two columns and three rows.
 * Extends {@link AbstractJDBCConnectionTests} that starts up HSQL with appropriate schema. 
 * Tests reader with different batch sizes.
 * 
 * @author Kris Lachor
 */
public class JDBCReadConnectorTestCase extends AbstractJDBCConnectionTests{
  private static final Log log =LogFactory.getLog(JDBCReadConnectorTestCase.class);  

  protected static String COL1 = "COL1";

  protected static String COL2 = "COL2";

  private static String SCHEMA = "CREATE MEMORY TABLE OA_TEST(" + COL1 + " CHAR(32)," + COL2 + " CHAR(32)); "
  + "INSERT INTO OA_TEST VALUES ('foo1', 'bar1'); INSERT INTO OA_TEST VALUES ('foo2', 'bar2');"
  + "INSERT INTO OA_TEST VALUES ('foo3', 'bar3')";

  protected static String SELECT_STMT_1 = "SELECT " + COL1 + ", " +  COL2 + " FROM OA_TEST";

  protected Router router = new Router();

  protected Map processMap = new HashMap();

  protected Adaptor adaptor = new Adaptor();

  JDBCReadConnector reader = assembleJDBCReader(SELECT_STMT_1);

  TestComponent.TestWriteConnector writer = new TestComponent.TestWriteConnector();

  protected void setUp() throws Exception {
    log.info("setup() called. Delegating to "+super.getClass().getName());
    super.setUp();
    adaptor.setMessageProcessor(router);
  }

  /**
   * Runs an adaptor that reads from the test table.
   * Doesn't use batches (batch size == 1 elem).
   * Ensures the writer received two records in one call.
   */
  public void testLoopingPollingReadConnectorBatchOne()throws Exception{
    log.debug("--- Beginning testLoopingPollingReadConnectorBatchOne --");
    processMap.put(reader, writer);
    router.setProcessMap(processMap);
    assertTrue(writer.counter==0);
    SystemTestUtil.adaptorRun(adaptor);
    assertTrue(adaptor.getExitCode()==0);
    assertTrue(writer.counter==3);
    assertTrue(writer.dataCollection.size()==3);
    for(int i=0; i<writer.dataCollection.size(); i++){
      Map row = (Map)((Object [])writer.dataCollection.get(i))[0];
      String rowNo = new Integer(i+1).toString();
      assertNotNull(row.get(COL1));
      assertEquals(row.get(COL1), "foo" + rowNo);
      assertEquals(row.get(COL2), "bar" + rowNo);
    }
    log.debug("--- Ending testLoopingPollingReadConnectorBatchOne --");
  }

  /**
   * Runs an adaptor that reads from the test table.
   * Uses looping polling read connector. Reads all elements in one batch.
   */
  public void testLoopingPollingReadConnectorBatchAll()throws Exception{
    log.debug("--- Beginning testLoopingPollingReadConnectorBatchAll --");
    reader.setBatchSize(0);
    processMap.put(reader, writer);
    router.setProcessMap(processMap);
    assertTrue(writer.counter==0);
    SystemTestUtil.adaptorRun(adaptor);
    assertTrue(adaptor.getExitCode()==0);
    assertTrue(writer.counter==1);
    assertTrue(writer.dataCollection.size()==1);
//  //change in the returned type from next() when converting all rows from 
//  //the result set 
//  //Object [] rows = (Object[]) ((Object []) writer.dataCollection.get(0))[0];
    Object [] rows = (Object []) writer.dataCollection.get(0);
    for(int i=0; i<rows.length; i++){
      Map row = (Map)rows[i];
      String rowNo = new Integer(i+1).toString();
      assertNotNull(row.get(COL1));
      assertEquals(row.get(COL1), "foo" + rowNo);
      assertEquals(row.get(COL2), "bar" + rowNo);
    }
    log.debug("--- Ending testLoopingPollingReadConnectorBatchAll --");
  }

  /**
   * Runs an adaptor that reads from the test table.
   * Uses looping polling read connector. Reads in batches of two elements.
   */
  public void testLoopingPollingReadConnectorBatchTwo()throws Exception{
    log.debug("--- Beginning testLoopingPollingReadConnectorBatchTwo --");
    reader.setBatchSize(2);
    processMap.put(reader, writer);
    router.setProcessMap(processMap);
    assertTrue(writer.counter==0);
    SystemTestUtil.adaptorRun(adaptor);
    assertTrue(adaptor.getExitCode()==0);
    assertTrue(writer.counter==2);
    assertTrue(writer.dataCollection.size()==2);
    /* First message should contain two records */
    Object [] firstTwoRows = (Object []) writer.dataCollection.get(0);
    for(int i=0; i<firstTwoRows.length; i++){
      Map row = (Map)firstTwoRows[i];
      String rowNo = new Integer(i+1).toString();
      assertNotNull(row.get(COL1));
      assertEquals(row.get(COL1), "foo" + rowNo);
      assertEquals(row.get(COL2), "bar" + rowNo);
    }
    Object [] lastRow = (Object []) writer.dataCollection.get(1);    
    Map row = (Map)lastRow[0];
    assertNotNull(row.get(COL1));
    assertEquals(row.get(COL1), "foo3");
    assertEquals(row.get(COL2), "bar3");
    log.debug("--- Ending testLoopingPollingReadConnectorBatchTwo --");
  }


  private JDBCReadConnector assembleJDBCReader(String sql){
    JDBCConnection jdbcConnection = new LocalHSQLJdbcConnection();
    JDBCReadConnector jdbcReader = new JDBCReadConnector();
    ResultSetToOrderedMapConverter resultSetConverter = new ResultSetToOrderedMapConverter();
    jdbcReader.setResultSetConverter(resultSetConverter);
    jdbcReader.setJdbcConnection(jdbcConnection);
    jdbcReader.setSql(sql);
    return jdbcReader;
  }


  /**
   * No connection set, should cause a validation exception.
   */
  public void testValidation() {
    log.debug("--- Beginning testValidation --");
    JDBCReadConnector jdbcReader = new JDBCReadConnector();
    ResultSetToOrderedMapConverter resultSetConverter = new ResultSetToOrderedMapConverter();
    jdbcReader.setResultSetConverter(resultSetConverter);
    jdbcReader.setSql("test");
    processMap.put(jdbcReader, writer);
    router.setProcessMap(processMap);
    assertTrue(writer.counter==0);
    SystemTestUtil.adaptorRun(adaptor);
    assertTrue(adaptor.getExitCode()==1);
    log.debug("--- Beginning testValidation --");
  }

  /**
   * @return test table definition.
   */
  public String getSchemaDefinition() {
    return SCHEMA;
  }
}
