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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.jmock.Mock;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.connector.DBEventDrivenPollingReadConnector;
import org.openadaptor.core.exception.ConnectionException;

/**
 * Unit tests for {@link JDBCReadConnector}. 
 * These tests verify the  {@link JDBCReadConnector} combined with the 
 * {@LoopingPollingStrategy} is fully compatible with {@link OldJDBCReadConnector},
 * {@link JDBCPollConnector} and {@link JDBCEventReadConnector} which
 * it is replacing.
 * 
 * @author Kris Lachor
 */
public class JDBCReadConnectorUnitTestCase extends AbstractJDBCConnectorTest{
  
  private JDBCReadConnector jdbcReadConnector = new JDBCReadConnector();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    jdbcReadConnector.setId("Test Read Connector");
    jdbcReadConnector.setSql(sql);
    jdbcReadConnector.setJdbcConnection(mockConnection);    
  }

  
  /**
   * Tests {@link JDBCReadConnector#connect}.
   */
  public void testConnect(){
    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
    assertNull(jdbcReadConnector.statement);
    jdbcReadConnector.connect();
    assertEquals(mockStatement.proxy(), jdbcReadConnector.statement);
  }
  
  /**
   * Tests {@link JDBCReadConnector#connect}.
   * Creating the statement throws exception.
   */
  public void testConnect2(){
    mockSqlConnection.expects(once()).method("createStatement").will(throwException(new SQLException("test", "test")));
    assertNull(jdbcReadConnector.statement);
    try{ 
       jdbcReadConnector.connect();
    }catch(ConnectionException ce){
       assertNull(jdbcReadConnector.statement);
       return;
    }
    assertTrue(false);
  }
  
  /**
   * ported from {@link JDBCEventReadConnectorUnitTestCase#testConnect()}
   */
  public void testConnect3(){
    DBEventDrivenPollingReadConnector pollingStrategy = new DBEventDrivenPollingReadConnector();
    pollingStrategy.setEventServiceID("10");
    pollingStrategy.setEventTypeID("20");
    pollingStrategy.setJdbcConnection(mockConnection);
    jdbcReadConnector.setPollingStrategy(pollingStrategy);
    Mock mockCallableStatement =  new Mock(CallableStatement.class);   
    connectDBEventDrivenConnector(mockCallableStatement, pollingStrategy);
  }
  
  /**   
   * Tests {@link JDBCReadConnector#disconnect}.
   */
  public void testDisonnect(){
    testConnect();
    mockStatement.expects(once()).method("close");
    mockSqlConnection.expects(once()).method("close");
    jdbcReadConnector.disconnect();
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.jdbc.reader.JDBCReadConnector#next(long)}.
   * Initialises mock interfaces to result a result set with one column and one row.
   * One call to the {@link JDBCReadConnector#next(long)} method.
   * Test ported from OldJDBCReadConnectorUnitTestCase.
   */
  public void testNext() {
    mockStatement.expects(once()).method("executeQuery").with(eq(sql)).will(returnValue(mockResultSet.proxy()));
    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
    jdbcReadConnector.connect();
    assertFalse("Read connector dry to soon.", jdbcReadConnector.isDry());
    Object [] arr = (Object []) jdbcReadConnector.next(10);
    assertTrue("Unexpected result type", arr[0] instanceof Map);
    assertTrue("Unexpected result count", arr.length == 1);
    Map map = (Map) arr[0];
    String s = (String) map.get(COL1);
    assertTrue("Unexpected result", s.equals(TEST_STRING));
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.jdbc.reader.JDBCReadConnector#next(long)}.
   * Initialises mock interfaces to result a result set with one column and one row.
   * Two calls to the {@link JDBCReadConnector#next(long)} method.
   * Checks value {@link JDBCReadConnector#isDry()}.
   * Test ported from OldJDBCReadConnectorUnitTestCase.
   */
  public void testNext2() {
    mockStatement.expects(once()).method("executeQuery").with(eq(sql)).will(returnValue(mockResultSet.proxy()));
    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
    mockResultSet.expects(atLeastOnce()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
    mockResultSet.expects(once()).method("close"); 
    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
    jdbcReadConnector.connect();
    assertFalse("Read connector dry to soon.", jdbcReadConnector.isDry());
    Object [] arr = (Object []) jdbcReadConnector.next(10);
    assertTrue("Unexpected result type", arr[0] instanceof Map);
    assertTrue("Unexpected result count", arr.length == 1);
    Map map = (Map) arr[0];
    String s = (String) map.get(COL1);
    assertTrue("Unexpected result", s.equals(TEST_STRING));
    assertFalse("Read connector dry to soon.", jdbcReadConnector.isDry());
    jdbcReadConnector.next(10);
    assertTrue("Read connector not dry. Should be.", jdbcReadConnector.isDry());
  }
  
//  /**
//   * Test method for {@link org.openadaptor.auxil.connector.jdbc.reader.JDBCReadConnector#next(long)}.
//   * Initialises mock interfaces to a result set with one column and one row.
//   * One call to the {@link JDBCReadConnector#next(long)} method.
//   * This connector will never become dry.
//   * Test ported from JDBCPollConnectorUnitTestCase.
//   */
//  public void testNext3() {
////    mockSqlConnection.expects(once()).method("prepareCall").with(eq(sql)).will(returnValue(mockStatement.proxy()));
//    //JDBCReadConnector would execute the query passing SQL. This has already passed the sql when creating 
//    //the statement
//    mockStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
//    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
//    
//    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
//    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
//    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
//    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
//    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
//    jdbcReadConnector.connect();
//    assertFalse("Read connector dry to soon.", jdbcReadConnector.isDry());
//    Object [] arr = (Object []) jdbcReadConnector.next(10);
//    assertTrue("Unexpected result type", arr[0] instanceof Map);
//    assertTrue("Unexpected result count", arr.length == 1);
//    Map map = (Map) arr[0];
//    String s = (String) map.get(COL1);
//    assertTrue("Unexpected result", s.equals(TEST_STRING));
//    assertFalse("Read connector dry to soon.", jdbcReadConnector.isDry());
//  
//  }
  
  
  /**
   * ported from {@link JDBCEventReadConnectorUnitTestCase#testNext1()}
   */
  public void testNext4() {
    DBEventDrivenPollingReadConnector pollingStrategy = new DBEventDrivenPollingReadConnector();
    pollingStrategy.setEventServiceID("10");
    pollingStrategy.setEventTypeID("20");
    pollingStrategy.setJdbcConnection(mockConnection);
    jdbcReadConnector.setPollingStrategy(pollingStrategy);
    
    Mock mockPollStatement =  new Mock(CallableStatement.class);
    connectDBEventDrivenConnector(mockPollStatement, pollingStrategy);
 
    /* 
     * actual call to next. executeQuery returns a result set that is immediately closed
     * (because of an SQLException thrown when no results are found..).
     */
    mockPollStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    mockPollStatement.expects(once()).method("close");  
    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSet.expects(once()).method("next").will(returnValue(false));
    mockResultSet.expects(once()).method("close");
    
    assertFalse("Read connector dry to soon.", pollingStrategy.isDry());
    assertNull(pollingStrategy.next(10));  
  }
  
  /**
   * ported from {@link JDBCEventReadConnectorUnitTestCase#testNext2()}
   */
  public void testNext5(){
    DBEventDrivenPollingReadConnector pollingStrategy = new DBEventDrivenPollingReadConnector();
    pollingStrategy.setEventServiceID("10");
    pollingStrategy.setEventTypeID("20");
    pollingStrategy.setJdbcConnection(mockConnection);
    jdbcReadConnector.setPollingStrategy(pollingStrategy);
    
    Mock mockPollStatement =  new Mock(CallableStatement.class);
    connectDBEventDrivenConnector(mockPollStatement, pollingStrategy);
    
    /* actual call to next */
    mockPollStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));    
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false), returnValue(false)));
    mockResultSet.expects(atLeastOnce()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSetMetaData.expects(atLeastOnce()).method("getColumnCount").will(returnValue(15));
    for(int i=1; i<=15; i++){
      mockResultSetMetaData.expects(once()).method("getColumnName").with(eq(i)).will(returnValue("COL" + new Integer(i)));
    }
    mockResultSet.expects(atLeastOnce()).method("getObject").will(returnValue("TEST"));
    
    Mock mockActualStatement =  new Mock(CallableStatement.class);
    mockSqlConnection.expects(once()).method("prepareCall").with(eq("{ call TEST (?,?,?,?,?,?,?,?,?,?)}")).will(returnValue(mockActualStatement.proxy()));
    mockActualStatement.expects(atLeastOnce()).method("setString");
    mockActualStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    mockResultSet.expects(once()).method("close");
    mockActualStatement.expects(once()).method("close");   
    
    assertFalse("Read connector dry to soon.", pollingStrategy.isDry());
    Object [] arr = (Object []) pollingStrategy.next(10);
    assertTrue("Unexpected result count", arr.length == 0);
  }
  
  /**
   * ported from {@link JDBCEventReadConnectorUnitTestCase#testNext3()}
   */
  public void testNext6() {
    DBEventDrivenPollingReadConnector pollingStrategy = new DBEventDrivenPollingReadConnector();
    pollingStrategy.setEventServiceID("10");
    pollingStrategy.setEventTypeID("20");
    pollingStrategy.setJdbcConnection(mockConnection);
    jdbcReadConnector.setPollingStrategy(pollingStrategy);
    
    Mock mockPollStatement =  new Mock(CallableStatement.class);
    Mock mockResultSet2 = new Mock(ResultSet.class);
    Mock mockResultSetMetaData2 = new Mock(ResultSetMetaData.class);
    connectDBEventDrivenConnector(mockPollStatement, pollingStrategy);
 
    /* actual call to next */
    mockPollStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSetMetaData.expects(atLeastOnce()).method("getColumnCount").will(returnValue(15));
    for(int i=1; i<=15; i++){
      mockResultSetMetaData.expects(once()).method("getColumnName").with(eq(i)).will(returnValue("COL" + new Integer(i)));
    }
    mockResultSet.expects(atLeastOnce()).method("getObject").will(returnValue("TEST"));

    Mock mockActualStatement =  new Mock(CallableStatement.class);
    mockSqlConnection.expects(once()).method("prepareCall").with(eq("{ call TEST (?,?,?,?,?,?,?,?,?,?)}")).will(returnValue(mockActualStatement.proxy()));
    mockActualStatement.expects(atLeastOnce()).method("setString");
  
    mockActualStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet2.proxy()));
    mockResultSet2.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet2.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData2.proxy()));
    mockResultSet2.expects(atLeastOnce()).method("getObject").will(returnValue("TEST2"));

    mockResultSetMetaData2.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData2.expects(once()).method("getColumnName").with(eq(1)).will(returnValue("COL1"));

    mockResultSet.expects(once()).method("close");
    mockActualStatement.expects(once()).method("close"); 
    
    assertFalse("Read connector dry to soon.", pollingStrategy.isDry());
    Object [] arr = (Object []) pollingStrategy.next(10);
    assertTrue("Unexpected result count", arr.length == 1);
  }
  
  private void connectDBEventDrivenConnector(Mock mockStatement, IReadConnector readConnector){
    /* Callable statement for the pollingStrategy */
    mockSqlConnection.expects(once()).method("prepareCall").will(returnValue(mockStatement.proxy()));
    
    /* 'Plain' statement for the underlying connector */
    mockSqlConnection.expects(once()).method("createStatement");
    
    mockStatement.expects(once()).method("registerOutParameter").with(eq(1), eq(java.sql.Types.INTEGER));
    mockStatement.expects(once()).method("setInt").with(eq(2), eq(10));
    mockStatement.expects(once()).method("setInt").with(eq(3), eq(20));    
    readConnector.connect();
  }

}
