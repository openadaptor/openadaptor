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

import org.jmock.Mock;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 13, 2007 by oa3 Core Team
 */

public class RawSQLWriterTestCase extends AbstractSQLWriterTests {
  protected Mock preparedStatementMock;

  protected ISQLWriter instantiateTestWriter() {
    RawSQLWriter writer = new RawSQLWriter();
    return writer;
  }

  protected void setMocksFor(ISQLWriter writer) {
  }

  protected void setUp() throws Exception {
    super.setUp();
    preparedStatementMock = mock(PreparedStatement.class);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    preparedStatementMock = null;
  }

  public void testHasBatchSupport() {
    assertFalse("Does not support batching", testWriter.hasBatchSupport());
  }

  public void testWriteEmptyBatch() {
    Object[] data = new Object [] {};
    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
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

}
