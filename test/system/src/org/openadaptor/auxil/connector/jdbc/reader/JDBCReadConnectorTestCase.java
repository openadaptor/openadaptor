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

import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.JDBCConnectionTestCase;
import org.openadaptor.auxil.connector.jdbc.reader.orderedmap.ResultSetToOrderedMapConverter;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.LoopingPollingStrategy;
import org.openadaptor.core.connector.SinglePollPollingStrategy;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.LocalHSQLJdbcConnection;
import org.openadaptor.util.TestComponent;


/**
 * System tests for {@link JDBCReadConnector} that uses different polling
 * strategies:
 * {@link SinglePollPollingStrategy}
 * {@link LoopingPollingStrategy}
 * 
 * Extends {@link JDBCConnectionTestCase} that starts up HSQL with appropriate schema. 
 * 
 * @author Kris Lachor
 */
public class JDBCReadConnectorTestCase extends JDBCConnectionTestCase{
  
  protected static String COL1 = "COL1";
  
  protected static String COL2 = "COL2";
  
  private static String SCHEMA = "CREATE MEMORY TABLE OA_TEST(" + COL1 + " CHAR(32)," + COL2 + " CHAR(32)); "
    + "INSERT INTO OA_TEST VALUES ('foo', 'bar'); INSERT INTO OA_TEST VALUES ('foo2', 'bar2')";
  
  protected static String SELECT_STMT_1 = "SELECT " + COL1 + ", " +  COL2 + " FROM OA_TEST";
  
  protected Router router = new Router();
  
  protected Map processMap = new HashMap();
  
  protected Adaptor adaptor = new Adaptor();
  
  JDBCReadConnector reader = assembleJDBCReader(SELECT_STMT_1);
  
  TestComponent.TestWriteConnector writer = new TestComponent.TestWriteConnector();
  
  protected void setUp() throws Exception {
    super.setUp();
    adaptor.setMessageProcessor(router);
  }
  
  /**
   * Runs an adaptor that reads from the test table.
   * Uses one shot polling strategy. 
   * Ensures the writer received two records in one call.
   */
  public void testOneShotPollingStrategy() throws Exception{
    reader.setPollingStrategy(new SinglePollPollingStrategy());
    processMap.put(reader, writer);
    router.setProcessMap(processMap);
    assertTrue(writer.counter==0);
    adaptor.run();
    assertTrue(adaptor.getExitCode()==0);
    assertTrue(writer.counter==1);
    assertTrue(writer.dataCollection.size()==1);
    Object [] data = (Object[]) ((Object []) writer.dataCollection.get(0))[0];
    assertTrue(data.length == 2);
    HashMap row1 = (HashMap) data[0];
    assertNotNull(row1.get(COL1));
    assertEquals(row1.get(COL1), "foo");
    assertEquals(row1.get(COL2), "bar");
    HashMap row2 = (HashMap) data[1];
    assertNotNull(row2.get(COL1));
    assertEquals(row2.get(COL1), "foo2");
    assertEquals(row2.get(COL2), "bar2");
  }

  /**
   * Runs an adaptor that reads from the test table.
   * Uses looping polling strategy.
   * Ensures the writer received two records in one call.
   */
  public void testLoopingPollingStrategy()throws Exception{
    reader.setPollingStrategy(new LoopingPollingStrategy());
    processMap.put(reader, writer);
    router.setProcessMap(processMap);
    assertTrue(writer.counter==0);
    adaptor.run();
    assertTrue(adaptor.getExitCode()==0);
    assertTrue(writer.counter==2);
    assertTrue(writer.dataCollection.size()==2);
    Map row1 = (Map) ((Object []) writer.dataCollection.get(0))[0];
    assertNotNull(row1.get(COL1));
    assertEquals(row1.get(COL1), "foo");
    assertEquals(row1.get(COL2), "bar");
    Map row2 = (Map) ((Object []) writer.dataCollection.get(1))[0];
    assertNotNull(row2.get(COL1));
    assertEquals(row2.get(COL1), "foo2");
    assertEquals(row2.get(COL2), "bar2");
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
   * @return test table definition.
   */
  public String getSchemaDefinition() {
    return SCHEMA;
  }
}
