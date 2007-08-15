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

import org.jmock.Mock;
import org.openadaptor.core.exception.ConnectionException;


/**
 * Unit tests for {@link JDBCEventReadConnector}.
 * 
 * @author Kris Lachor
 */
public class JDBCEventReadConnectorUnitTestCase extends AbstractJDBCConnectorTest {
	
  private JDBCEventReadConnector jdbcEventReadConnector = new JDBCEventReadConnector();

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    mockStatement =  new Mock(CallableStatement.class);
    jdbcEventReadConnector.setId("Test Read Connector");
    jdbcEventReadConnector.setJdbcConnection(mockConnection);    
    jdbcEventReadConnector.setEventServiceID("10");
    jdbcEventReadConnector.setEventTypeID("20");
  }

  private void connect(Mock mockStatement){
    mockSqlConnection.expects(once()).method("prepareCall").will(returnValue(mockStatement.proxy()));
    mockStatement.expects(once()).method("registerOutParameter").with(eq(1), eq(java.sql.Types.INTEGER));
    mockStatement.expects(once()).method("setInt").with(eq(2), eq(10));
    mockStatement.expects(once()).method("setInt").with(eq(3), eq(20));
    assertNull(jdbcEventReadConnector.getPollStatement());
    jdbcEventReadConnector.connect();
    assertEquals(mockStatement.proxy(), jdbcEventReadConnector.getPollStatement());
  }
  
  /**
   * Tests {@link JDBCEventReadConnector#connect}.
   */
  public void testConnect(){
    connect(mockStatement);
  }
  
  /**
   * Tests {@link JDBCEventReadConnector#connect}.
   */
  public void testConnect2(){
    mockSqlConnection.expects(once()).method("prepareCall").will(returnValue(mockStatement.proxy()));
    mockStatement.expects(once()).method("registerOutParameter").with(eq(1), eq(java.sql.Types.INTEGER));
    mockStatement.expects(once()).method("setInt").with(eq(2), eq(10));
    mockStatement.expects(once()).method("setInt").with(eq(3), eq(20)).will(throwException(new java.sql.SQLException("test","test")));
    assertNull(jdbcEventReadConnector.getPollStatement());
    try{
    	jdbcEventReadConnector.connect();
    }
    catch(ConnectionException ce){
    	return;
    }
    assertTrue(false);
  }
  
  /**   
   * Tests {@link JDBCEventReadConnector#disconnect}.
   */
  public void testDisonnect(){
    testConnect();
    mockStatement.expects(once()).method("close");
    mockSqlConnection.expects(once()).method("close");
    jdbcEventReadConnector.disconnect();
  }
  

  /**
   * Test method for {@link JDBCEventReadConnector#next(long)}.
   * Stored procedure that polls returns nothing (no new events).
   */
  public void testNext1() {
    
    Mock mockPollStatement =  new Mock(CallableStatement.class);
    connect(mockPollStatement);
 
    /* 
     * actual call to next. executeQuery returns a result set that is immediately closed
     * (because of an SQLException thrown when no results are found..).
     */
    mockPollStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    mockPollStatement.expects(once()).method("close"); 
    mockResultSet.expects(once()).method("next").will(returnValue(false));
    mockResultSet.expects(once()).method("close");
    
    assertFalse("Read connector dry to soon.", jdbcEventReadConnector.isDry());
    assertNull(jdbcEventReadConnector.next(10));  
  }
  
  
  /**
   * Test method for {@link JDBCEventReadConnector#next(long)}.
   * Stored procesure that polls returns one new event. Query constructed based on this
   * event returns an empty result set.
   */
  public void testNext2() {
    Mock mockPollStatement =  new Mock(CallableStatement.class);
    connect(mockPollStatement);
 
    /* actual call to next */
    mockPollStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
    mockResultSet.expects(atLeastOnce()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
    mockResultSet.expects(once()).method("getString");
    mockSqlConnection.expects(once()).method("prepareCall").will(returnValue(mockStatement.proxy()));

    mockStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    mockResultSet.expects(once()).method("close");
    mockStatement.expects(once()).method("close"); 
    
    assertFalse("Read connector dry to soon.", jdbcEventReadConnector.isDry());
    Object [] arr = (Object []) jdbcEventReadConnector.next(10);
    assertTrue("Unexpected result count", arr.length == 0);
  }
  
  
  /**
   * Test method for {@link JDBCEventReadConnector#next(long)}.
   * Stored procesure that polls returns one new event. Query constructed based on this
   * event returns a result set with one row.
   */
  public void testNext3() {
    Mock mockPollStatement =  new Mock(CallableStatement.class);
    connect(mockPollStatement);
 
    /* actual call to next */
    mockPollStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    
    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(false)));
    mockResultSet.expects(atLeastOnce()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
    mockResultSetMetaData.expects(atLeastOnce()).method("getColumnCount").will(returnValue(1));
    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue("COL1"));
    mockResultSet.expects(once()).method("getString");
    mockResultSet.expects(once()).method("getObject");
    mockSqlConnection.expects(once()).method("prepareCall").will(returnValue(mockStatement.proxy()));

    mockResultSet.expects(once()).method("close");
    mockStatement.expects(once()).method("close"); 

    mockStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
    
    assertFalse("Read connector dry to soon.", jdbcEventReadConnector.isDry());
    Object [] arr = (Object []) jdbcEventReadConnector.next(10);
    assertTrue("Unexpected result count", arr.length == 1);
  }
  
}
