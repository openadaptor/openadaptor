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
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 21, 2007 by oa3 Core Team
 */

public abstract class AbstractMapWriterTests extends AbstractSQLWriterTests {

  /**
   * Test writing a batch of one non-Map element. Should throw a RuntimeException.
   */
  public void testWriteNonMapData() {
    setupInitialiseExpectations(true);
    Object[] data = setupDataForWriteNonMapData();
    testWriter.initialise((Connection)connectionMock.proxy());
    try {
      testWriter.writeBatch(data);
    } catch (RuntimeException e) {
      return; // This is the expected outcome.
    } catch (SQLException e) {
      fail("Did not expect SQLException: " + e);
    }
    fail("Expected a RuntimeException");
  }

  protected Object[] setupDataForWriteNonMapData() {
    Object[] data = new Object []{"This is not a Map"};
    // Test should bail before any of these methods get called (with or without batching).
    preparedStatementMock.expects(never()).method("clearParameters");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(never()).method("close");
    return data;
  }
}
