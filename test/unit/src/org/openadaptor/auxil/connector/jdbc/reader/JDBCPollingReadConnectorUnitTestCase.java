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

import java.sql.SQLException;
import java.util.Map;

//import org.openadaptor.core.connector.LoopingPollingStrategy;
import org.openadaptor.core.exception.ConnectionException;

/**
 * Unit tests for {@link JDBCReadConnector}. 
 * These tests verify the  {@link JDBCReadConnector} combined with the 
 * {@LoopingPollingStrategy} is fully compatible with {@link OldJDBCReadConnector}, which
 * it is replacing.
 * 
 * @author Kris Lachor
 */
public class JDBCPollingReadConnectorUnitTestCase extends AbstractJDBCConnectorTest{
  
  private JDBCReadConnector jdbcPollingReadConnector = new JDBCReadConnector();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    jdbcPollingReadConnector.setId("Test Read Connector");
    jdbcPollingReadConnector.setSql(sql);
    jdbcPollingReadConnector.setJdbcConnection(mockConnection);    
  }

  
  /**
   * Tests {@link JDBCReadConnector#connect}.
   */
  public void testConnect(){
    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
    assertNull(jdbcPollingReadConnector.statement);
    jdbcPollingReadConnector.connect();
    assertEquals(mockStatement.proxy(), jdbcPollingReadConnector.statement);
  }
  
  /**
   * Tests {@link JDBCReadConnector#connect}.
   * Creating the statement throws exception.
   */
  public void testConnect2(){
    mockSqlConnection.expects(once()).method("createStatement").will(throwException(new SQLException("test", "test")));
    assertNull(jdbcPollingReadConnector.statement);
    try{ 
       jdbcPollingReadConnector.connect();
    }catch(ConnectionException ce){
       assertNull(jdbcPollingReadConnector.statement);
       return;
    }
    assertTrue(false);
  }
  
  /**
   * Tests {@link JDBCReadConnector#disconnect}.
   */
  public void testDisonnect(){
    testConnect();
    mockStatement.expects(once()).method("close");
    mockSqlConnection.expects(once()).method("close");
    jdbcPollingReadConnector.disconnect();
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.jdbc.reader.JDBCReadConnector#next(long)}.
   * Initialises mock interfaces to result a result set with one column and one row.
   * One call to the {@link JDBCReadConnector#next(long)} method.
   */
  public void testNext() {
    //no need to set the looping strategy - it's a default
//    jdbcPollingReadConnector.setPollingStrategy(new LoopingPollingStrategy());
    mockStatement.expects(once()).method("executeQuery").with(eq(sql)).will(returnValue(mockResultSet.proxy()));
    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
    jdbcPollingReadConnector.connect();
    assertFalse("Read connector dry to soon.", jdbcPollingReadConnector.isDry());
    Object [] arr = (Object []) jdbcPollingReadConnector.next(10);
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
   */
  public void testNext2() {
//  no need to set the looping strategy - it's a default
//    jdbcPollingReadConnector.setPollingStrategy(new LoopingPollingStrategy());
    mockStatement.expects(once()).method("executeQuery").with(eq(sql)).will(returnValue(mockResultSet.proxy()));
    mockSqlConnection.expects(once()).method("createStatement").will(returnValue(mockStatement.proxy()));
    mockResultSet.expects(atLeastOnce()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
    mockResultSet.expects(once()).method("close"); 
    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
    jdbcPollingReadConnector.connect();
    assertFalse("Read connector dry to soon.", jdbcPollingReadConnector.isDry());
    Object [] arr = (Object []) jdbcPollingReadConnector.next(10);
    assertTrue("Unexpected result type", arr[0] instanceof Map);
    assertTrue("Unexpected result count", arr.length == 1);
    Map map = (Map) arr[0];
    String s = (String) map.get(COL1);
    assertTrue("Unexpected result", s.equals(TEST_STRING));
    assertFalse("Read connector dry to soon.", jdbcPollingReadConnector.isDry());
    jdbcPollingReadConnector.next(10);
    assertTrue("Read connector not dry. Should be.", jdbcPollingReadConnector.isDry());
  }

}
