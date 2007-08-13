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
import java.util.Map;

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

  
  /**
   * Tests {@link JDBCEventReadConnector#connect}.
   */
  public void testConnect(){
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
  
//  /**
//   * Test method for {@link JDBCEventReadConnector#next(long)}.
//   */
//  public void testNext() {
//	//connecting  
//	mockSqlConnection.expects(once()).method("prepareCall").will(returnValue(mockStatement.proxy()));
//	mockStatement.expects(once()).method("registerOutParameter").with(eq(1), eq(java.sql.Types.INTEGER));
//	mockStatement.expects(once()).method("setInt").with(eq(2), eq(10));
//	mockStatement.expects(once()).method("setInt").with(eq(3), eq(20));
//	  
//	  
//	//actual call to next
////    mockStatement.expects(once()).method("executeQuery").with(eq(sql)).will(returnValue(mockResultSet.proxy()));
//    mockStatement.expects(once()).method("executeQuery").will(returnValue(mockResultSet.proxy()));
//
//    mockResultSet.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(returnValue(true), returnValue(false)));
//    mockResultSet.expects(once()).method("getMetaData").will(returnValue(mockResultSetMetaData.proxy()));
////    mockResultSet.expects(once()).method("getObject").with(eq(1)).will(returnValue(TEST_STRING)); 
//    mockResultSetMetaData.expects(once()).method("getColumnCount").will(returnValue(1));
//    mockResultSetMetaData.expects(once()).method("getColumnName").will(returnValue(COL1)); 
//    mockResultSet.expects(once()).method("close");
//    jdbcEventReadConnector.connect();
//    assertFalse("Read connector dry to soon.", jdbcEventReadConnector.isDry());
//    Object [] arr = (Object []) jdbcEventReadConnector.next(10);
//    assertTrue("Unexpected result type", arr[0] instanceof Map);
//    assertTrue("Unexpected result count", arr.length == 1);
//    Map map = (Map) arr[0];
//    String s = (String) map.get(COL1);
//    assertTrue("Unexpected result", s.equals(TEST_STRING));
//  }
  
}
