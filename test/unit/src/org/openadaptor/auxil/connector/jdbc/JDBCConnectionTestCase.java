/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

/**
 * Unit tests for JDBCConnection. <br> 
 * Currently implements tests for validation and connection.<br>
 * 
 * @author oa Core Team
 * 
 * TODO  tests for connection failure modes and transaction support.
 */
package org.openadaptor.auxil.connector.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;

public class JDBCConnectionTestCase extends MockObjectTestCase {
  
  protected JDBCConnection testConnection;

  protected JDBCConnection instantiateTestObject() {
    JDBCConnection connection = new JDBCConnection();
    return connection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    testConnection = instantiateTestObject();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testConnection = null;
  }

  // Test validation
  
  public void testValidateXaDataSource() {
    List exceptions = new ArrayList();
    XADataSource testXaDS = (XADataSource)(mock(XADataSource.class).proxy());
    testConnection.setXaDataSource(testXaDS);
    testConnection.validate(exceptions);
    assertTrue("There should have been no validation exceptions", exceptions.size() == 0);
  }
  
  public void testValidateDataSource() {
    List exceptions = new ArrayList();
    DataSource testDS = (DataSource)(mock(DataSource.class).proxy());
    testConnection.setDataSource(testDS);
    testConnection.validate(exceptions);
    assertTrue("There should have been no validation exceptions", exceptions.size() == 0);
  }  
  
  public void testValidateDataSourceLookup() {
    List exceptions = new ArrayList();
    testConnection.setJndiConnection(new JNDIConnection());
    testConnection.setDataSourceName("dummy-name");
    testConnection.validate(exceptions);
    assertTrue("There should have been no validation exceptions", exceptions.size() == 0);
  }
  
  public void testValidateRawConfig() {
    List exceptions = new ArrayList();
    testConnection.setDriver("dummy");
    testConnection.setUrl("dummy");
    testConnection.setUsername("dummy");
    testConnection.setPassword("dummy");
    testConnection.validate(exceptions);
    assertTrue("There should have been no validation exceptions", exceptions.size() == 0);
  }
 
  public void testValidateFailNoConfig() {
    List exceptions = new ArrayList();
    testConnection.validate(exceptions);
    assertTrue("Expected just one Validation Exception", exceptions.size() == 1);
  }
    
  public void testValidateFailInvalidDataSourceLookupNoName() {
    List exceptions = new ArrayList();
    testConnection.setJndiConnection(new JNDIConnection());
    testConnection.validate(exceptions);
    assertTrue("Expected just one Validation Exception", exceptions.size() == 1); 
  }
  
  public void testValidateFailInvalidDataSourceLookupNoJndiConnection() {
    List exceptions = new ArrayList();
    testConnection.setDataSourceName("Dummy");
    testConnection.validate(exceptions);
    assertTrue("Expected just one Validation Exception", exceptions.size() == 1); 
  }
  
  public void testValidateFailRawConfig() {
    List exceptions = new ArrayList();
    // Randomly choose two properties to set
    testConnection.setDriver("dummy");
    testConnection.setPassword("dummy");
    testConnection.validate(exceptions);
    assertTrue("Expected two Validation Exceptions", exceptions.size() == 2); 
  }
  
  // test connecting
  
  public void testDataSourceConnection() {
    //Set up the Mocks
    Mock testDSMock = mock(DataSource.class);
    DataSource testDS = (DataSource)(testDSMock.proxy());
    Mock testJdbcConnectionMock = mock(Connection.class);
    Connection testJdbcConnection = (Connection)(testJdbcConnectionMock.proxy());
 
    // Configure the Connection
    testConnection.setDataSource(testDS);    
    
    // Set the mock expectations
    testDSMock.expects(atLeastOnce()).method(eq("getConnection")).will(returnValue(testJdbcConnection));
    testJdbcConnectionMock.expects(atLeastOnce()).method("setAutoCommit").with(eq(false));
    
    try {
      testConnection.connect();
    } catch (SQLException e) {
      fail("Unexpected SqlException during connect(): " + e);
    }
  }
  
  public void testXaDataSourceConnection() {
    //Set up the Mocks
    Mock testXaDSMock = mock(XADataSource.class);
    XADataSource testXaDS = (XADataSource)(testXaDSMock.proxy());
    Mock testXAConnectionMock = mock(XAConnection.class);
    XAConnection testXAConnection = (XAConnection)(testXAConnectionMock.proxy());
    Mock testJdbcConnectionMock = mock(Connection.class);
    Connection testJdbcConnection = (Connection)(testJdbcConnectionMock.proxy());
    Mock testXAResourceMock = mock(XAResource.class);
    XAResource testXAResource = (XAResource)(testXAResourceMock.proxy());
    
    // Configure the Connection
    testConnection.setXaDataSource(testXaDS);
    
    // Set the mock expectations
    testXaDSMock.expects(atLeastOnce()).method("getXAConnection").will(returnValue(testXAConnection));
    testXAConnectionMock.expects(atLeastOnce()).method("getConnection").will(returnValue(testJdbcConnection));
    testXAConnectionMock.expects(atLeastOnce()).method("getXAResource").will(returnValue(testXAResource));
    testJdbcConnectionMock.expects(atLeastOnce()).method("setAutoCommit").with(eq(false));
    
    try {
      testConnection.connect();
    } catch (SQLException e) {
      fail("Unexpected SqlException during connect(): " + e);
    }
  }
  
  public void testDataSourceLookupConnection() {
    // Set up the Mocks
    Mock testContextMock = mock(DirContext.class);
    DirContext testContext = (DirContext)(testContextMock.proxy());
    MockJNDIConnection testJndiConnection = new MockJNDIConnection();
    testJndiConnection.setTestDirContext(testContext);
    Mock testDSMock = mock(DataSource.class);
    DataSource testDS = (DataSource)(testDSMock.proxy());
    Mock testJdbcConnectionMock = mock(Connection.class);
    Connection testJdbcConnection = (Connection)(testJdbcConnectionMock.proxy());
    
    // Set the expectations
    testContextMock.expects(atLeastOnce()).method("lookup").with(eq("Test")).will(returnValue(testDS));
    testDSMock.expects(atLeastOnce()).method(eq("getConnection")).will(returnValue(testJdbcConnection));
    testJdbcConnectionMock.expects(atLeastOnce()).method("setAutoCommit").with(eq(false));
    
    // Configure the test JDBCConnection
    testConnection.setJndiConnection(testJndiConnection);
    testConnection.setDataSourceName("Test");
    
    try {
      testConnection.connect();
    } catch (SQLException e) {
      fail("Unexpected SqlException during connect(): " + e);
    }   
  }
  
  public void testRawConnection() {
    Mock testJdbcConnectionMock = mock(Connection.class);
    Connection testJdbcConnection = (Connection)(testJdbcConnectionMock.proxy());
    
    // Fudge to store the right connection mock statically with the mock driver
    MockDriver.MockConnection = testJdbcConnection;
    
    // Configure the test JDBCConnection
    testConnection.setDriver("org.openadaptor.auxil.connector.jdbc.MockDriver");
    testConnection.setUrl("DummyUrl");
    testConnection.setUsername("DummyUser");
    testConnection.setPassword("DummyPassword");
    
    try {
      testConnection.connect();
    } catch (SQLException e) {
      fail("Unexpected SqlException during connect(): " + e);
    }    
  }

  // Mock subclass of JNDIConnection used only to return a Mock DirContext
  protected class MockJNDIConnection extends JNDIConnection {    
    private DirContext testDirContext;
    protected DirContext getTestDirContext() {
      return testDirContext;
    }
    protected void setTestDirContext(DirContext testDirContext) {
      this.testDirContext = testDirContext;
    }

    public DirContext connect() throws NamingException {
      // TODO Auto-generated method stub
      return testDirContext;
    }
  }
  
}
  

