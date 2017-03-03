/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Trivial default implementation of ISQLWriter.
 * Expects data records to contain valid SQL statements to perform database writes.
 * <br>
 * It will attempt to get a string value from each record, and construct
 * a Prepared statement from it.
 * <br>
 * Potentially quite inefficient as it cannot support reusable prepared statements (obviously)
 * 
 * @author higginse
 * @since 3.2.2
 */
public class RawSQLWriter extends AbstractSQLWriter {
  private static final Log log = LogFactory.getLog(RawSQLWriter.class);
 
  /**
   * This writer does not support batch writes.
   * @return false
   */
  protected boolean checkBatchSupport()  {
    log.debug("Batch writes not supported.");
    return false;
  } 

  /**
   * Create prepared statement to write a batch of records.
   * @param data Non-null data Object[] of records to be written.
   * @return PreparedStatement ready for execution.
   * @throws SQLException
   */
  protected PreparedStatement createBatchStatement(Object[] data) throws SQLException{    
    throw new SQLException("Batch writes are not supported");
  }
  
  /**
   * Create a prepared statement to write a single data record.
   * It will use toString() to extract the SQL from the data record.
   * @throws SQLException if datum is null, or the String value cannot be
   *         used to generate a prepared statement.
   */
  protected PreparedStatement createStatement(Object datum) throws SQLException {
    if (datum==null) {
      throw new SQLException("Cannot create Statement from null data");
    }
    String datumString=datum.toString();
    if (log.isDebugEnabled()) {
      log.debug("SQL: "+datumString);
    }
    return connection.prepareStatement(datumString);
  }

  /**
   * This implementation does not reuse a prepared statement, and so does
   * nothing.
   * @see org.openadaptor.auxil.connector.jdbc.writer.AbstractSQLWriter#initialiseReusablePreparedStatement()
   */
  protected void initialiseReusablePreparedStatement() {
  }
}
