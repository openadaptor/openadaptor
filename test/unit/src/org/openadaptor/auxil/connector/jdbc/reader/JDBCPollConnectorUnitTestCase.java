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
import java.sql.SQLException;
import java.util.Map;

import org.jmock.Mock;

/**
 * Unit tests for {@link JDBCPollConnector}. The {@link JDBCPollConnector} is going 
 * to be replaced by a generic JDBC reader {@link JDBCPollingReadConnector}, these 
 * tests were written to ensure compatibility of both classes.
 * 
 * @author Kris Lachor
 */
public class JDBCPollConnectorUnitTestCase  extends AbstractJDBCConnectorTest{
  
  private JDBCPollConnector jdbcPollConnector = new JDBCPollConnector();
    
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    //the difference between this and JDBCReadConnector is a generic Statement is used there
    mockStatement =  new Mock(CallableStatement.class);
    jdbcPollConnector.setId("Test Read Connector");
    jdbcPollConnector.setSqlStatement(sql);
    jdbcPollConnector.setJdbcConnection(mockConnection);    
  }

  
  /**
   * Tests {@link JDBCPollConnector#connect}.
   */
  public void testConnect(){
    mockSqlConnection.expects(once()).method("prepareCall").with(eq(sql)).will(returnValue(mockStatement.proxy()));
    assertNull(jdbcPollConnector.pollStatement);
    jdbcPollConnector.connect();
    assertEquals(mockStatement.proxy(), jdbcPollConnector.pollStatement);
  }
  
  /**
   * Tests {@link JDBCPollConnector#connect}.
   * Creating the statement throws exception.
   */
  public void testConnect2(){
    mockSqlConnection.expects(once()).method("prepareCall").with(eq(sql)).will(throwException(new SQLException("test", "test")));
    assertNull(jdbcPollConnector.pollStatement);
    try{ 
       jdbcPollConnector.connect();
     //JDBCReadConnector throws a ConnectionException here
    }catch(RuntimeException re){
       assertNull(jdbcPollConnector.pollStatement);
       return;
    }
    assertTrue(false);
  }
  
  /**
   * Tests {@link JDBCPollConnector#disconnect}.
   */
  public void testDisonnect(){
    testConnect();
    mockStatement.expects(once()).method("close");
    mockSqlConnection.expects(once()).method("close");
    jdbcPollConnector.disconnect();
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.jdbc.reader.JDBCPollConnector#next(long)}.
   * Initialises mock interfaces to result a result set with one column and one row.
   * One call to the {@link JDBCPollConnector#next(long)} method.
   * This connector will never become dry.
   */
  public void testNext() {
    mockSqlConnection.expects(once()).method("prepareCall").with(eq(sql)).will(returnValue(mockStatement.proxy()));
    //JDBCReadConnector would execute the query passing SQL. This has already passed the sql when creating 
    //the statement
    mockStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
    jdbcPollConnector.connect();
    assertFalse("Read connector dry to soon.", jdbcPollConnector.isDry());
    Object [] arr = (Object []) jdbcPollConnector.next(10);
    assertTrue("Unexpected result type", arr[0] instanceof Map);
    assertTrue("Unexpected result count", arr.length == 1);
    Map map = (Map) arr[0];
    String s = (String) map.get(COL1);
    assertTrue("Unexpected result", s.equals(TEST_STRING));
  }


}
