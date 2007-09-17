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
package org.openadaptor.auxil.connector.jdbc.writer;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 17, 2007 by oa3 Core Team
 */

public class NewJDBCWriteConnectorTestCase extends MockObjectTestCase {

  protected IWriteConnector testWriteConnector;
  protected Mock sqlConnectionMock;
  protected Mock preparedStatementMock;

  protected void setUp() throws Exception {
    testWriteConnector = instantiateTestObject();
    setMocksFor(testWriteConnector);
    preparedStatementMock = mock(PreparedStatement.class);
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testWriteConnector = null;
    sqlConnectionMock = null;
    preparedStatementMock = null;
  }

  protected IWriteConnector instantiateTestObject() {
    NewJDBCWriteConnector writeConnector = new NewJDBCWriteConnector();
    writeConnector.setId("Test Write Connector");
    return writeConnector;
  }

  protected void setMocksFor(IWriteConnector writeConnector) {
    sqlConnectionMock = new Mock(Connection.class);
    ((NewJDBCWriteConnector)writeConnector).setJdbcConnection(new MockJDBCConnection((Connection)sqlConnectionMock.proxy()));
  }

  // Tests


  /**
   * Ensure a default configuration WriteConnector validates correctly.
   */
  public void testValidate() {
    List exceptions = new ArrayList();
    testWriteConnector.validate(exceptions);
    assertTrue("There should be no exceptions", exceptions.size() == 0);
  }

  /**
   * Ensure a default configuration WriteConnector connects correctly.
   */
  public void testConnect() {
    // The default writer used by NewJDBCWriteConnector is RawSQLWriter. This test assumes that.
    try {
      testWriteConnector.connect();
    } catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  /**
   * Test delivering a single payload to a default configuration WriteConnector.
   */
  public void testDeliver() {
    // The default writer used by NewJDBCWriteConnector is RawSQLWriter. This test assumes that.
    Object[] testData = new Object[] { "hello" };

    sqlConnectionMock.expects(once()).method("prepareStatement").with(eq(testData[0])).will(returnValue(preparedStatementMock.proxy()));
    preparedStatementMock.expects(once()).method("executeUpdate").will(returnValue(1));
    preparedStatementMock.expects(once()).method("close");

    testWriteConnector.connect();
    testWriteConnector.deliver(testData);
  }

  /**
   * Test delivering a batch to a default configuration WriteConnector.
   */
  public void testDeliverBatch() {
    // The default writer used by NewJDBCWriteConnector is RawSQLWriter. This test assumes that.
    Object[] testData = new Object[] { "hello", "world", "leaders" };

    sqlConnectionMock.expects(once()).method("prepareStatement").with(eq(testData[0])).will(returnValue(preparedStatementMock.proxy()));
    sqlConnectionMock.expects(once()).method("prepareStatement").with((eq(testData[1]))).will(returnValue(preparedStatementMock.proxy()));
    sqlConnectionMock.expects(once()).method("prepareStatement").with((eq(testData[2]))).will(returnValue(preparedStatementMock.proxy()));
    preparedStatementMock.expects(atLeastOnce()).method("executeUpdate").will(returnValue(1));
    preparedStatementMock.expects(atLeastOnce()).method("close");

    testWriteConnector.connect();
    testWriteConnector.deliver(testData);
  }

  /**
   * Inner mock of {@link JDBCConnection}. We can't mock JDBCConnection
   * directly as it is a class not an interface. We get around this by
   * mocking the java.sql.Connection that it wraps and setting its
   * expectations instead
   *
   */
  class MockJDBCConnection extends JDBCConnection {
    public MockJDBCConnection(Connection connection) {
      super();
      setConnection(connection);
    }
    public void connect() throws SQLException {}
  }

}
