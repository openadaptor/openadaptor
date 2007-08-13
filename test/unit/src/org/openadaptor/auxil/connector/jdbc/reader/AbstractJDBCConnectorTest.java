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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;

/**
 * Abstract class with useful constacts/methods/inner classes for JDBC connectors
 * tests.
 * 
 * @author Kris Lachor
 */
public abstract class AbstractJDBCConnectorTest extends MockObjectTestCase {
  
  protected static final String TEST_STRING = "TEST";
  
  protected static final String COL1 = "COL1";
  
  protected static final String COL2 = "COL2";
  
  protected String sql = "Some SQL";
  
  protected Mock mockStatement;
  
  protected Mock mockResultSet;
  
  protected Mock mockResultSetMetaData;
  
  protected Mock mockSqlConnection = new Mock(java.sql.Connection.class);
  
  protected JDBCConnection mockConnection = new MockJDBCConnection();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    mockStatement =  new Mock(Statement.class);
    mockResultSet =  new Mock(ResultSet.class);
    mockResultSetMetaData = new Mock(ResultSetMetaData.class);
  }
  
  
  /* Inner mock of {@link JDBCConnection} */
  class MockJDBCConnection extends JDBCConnection {
    
    public MockJDBCConnection() {
      super();
      setConnection((java.sql.Connection) mockSqlConnection.proxy());
    }

    public void connect() throws SQLException {}
  }
}
