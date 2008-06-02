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

import java.sql.Connection;
import java.sql.SQLException;

public class RawSQLWriterTestCase extends AbstractSQLWriterTests {

  protected ISQLWriter instantiateTestWriter() {
    RawSQLWriter writer = new RawSQLWriter();
    return writer;
  }
  
  protected void setupInitialiseExpectations(boolean supportsBatch) {
  }
  
  protected Object[] setupWriteBatchDataAndExpectationsBatchingDisabled() {
    return new Object[0];
  }
  
  protected Object[] setUpSingletonDataAndDataExpections() {
    String[] data = new String[] {"SELECT * FROM TABLE"};
    connectionMock.expects(once()).method("prepareStatement").with(eq(data[0])).will(returnValue(preparedStatementMock.proxy()));
    preparedStatementMock.expects(atLeastOnce()).method("executeUpdate").will(returnValue(1));
    preparedStatementMock.expects(atLeastOnce()).method("close");
    
    return data;
  }

  protected void setMocksFor(ISQLWriter writer) {
  }

  /**
   * Since RawSQLWriter does not support batching this shaould always be false.
   */
  public void testHasBatchSupport() {
    // To check batch support the meta data must be first retrieved.
    // RawSQLWriter doesn't look up meta data during initialisation so setting
    // this expectation to "never" is a crude check. If this changes then
    // more serious testing is needed.
    connectionMock.expects(never()).method("getMetaData");
    testWriter.initialise((Connection)connectionMock.proxy());
    assertFalse("Does not support batching", testWriter.hasBatchSupport());
  }

  public void testWriteSingleton() {
    testWriter.initialise((Connection)connectionMock.proxy());
    Object singleton = "this should really be sql";
    Object[] data = new Object [] {singleton};
    connectionMock.expects(once()).method("prepareStatement").with(eq(singleton)).will(returnValue(preparedStatementMock.proxy()));
    preparedStatementMock.expects(once()).method("executeUpdate").will(returnValue(1));
    preparedStatementMock.expects(once()).method("close");

    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testWriteBatch() {
    testWriter.initialise((Connection) connectionMock.proxy());
    Object[] data = new Object []{"this should really be sql", "so should this", "and this"};
    if (testWriter.hasBatchSupport()) {

    } else {
      connectionMock.expects(once()).method("prepareStatement").with(eq(data[0])).will(returnValue(preparedStatementMock.proxy()));
      connectionMock.expects(once()).method("prepareStatement").with(eq(data[1])).will(returnValue(preparedStatementMock.proxy()));
      connectionMock.expects(once()).method("prepareStatement").with(eq(data[2])).will(returnValue(preparedStatementMock.proxy()));
      preparedStatementMock.expects(atLeastOnce()).method("executeUpdate").will(returnValue(1));
      preparedStatementMock.expects(atLeastOnce()).method("close");
    }

    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testWriteNullData() {
    testWriter.initialise((Connection)connectionMock.proxy());
    Object[] data = new Object [] { null };
    connectionMock.expects(never()).method("prepareStatement");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("close");
    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      return;
    }
    fail("Expected an SQLException");
  }

  public void testWriteNotInitialised() {
    Object[] data = new Object [] { "test" };
    connectionMock.expects(never()).method("prepareStatement");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("close");
    try {
      testWriter.writeBatch(data);
    } catch (NullPointerException e) {
      return;
    } catch (SQLException e) {
      fail("Expected a NullPointerException");
    }
    fail("Expected a NullPointerException");
  }
}
