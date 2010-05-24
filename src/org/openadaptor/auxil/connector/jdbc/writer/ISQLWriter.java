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

import org.openadaptor.core.IComponent;
import org.openadaptor.core.exception.ConnectionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface for classes which write records to JDBC databases.
 * @author higginse
 * @see JDBCWriteConnector
 * @since 3.2.2
 */
public interface ISQLWriter {

  /**
   * Initialise writer with given JDBC Connection object.
   *
   * @param connection Connection which the writer should use.
   * @throws ConnectionException If there is any problem using the Connection
   */
  public void initialise(Connection connection) throws ConnectionException;

  /**
   * Returns true if the connection has batchSupport, and
   * the writer can also support batch writes.
   * @return true batch updates are supported
   */
  public boolean hasBatchSupport();
  /**
   * Write a batch of records to a database.
   * If the writer can allows batch writes, it will attempt to write
   * all records in a single batch.
   * Otherwise, it will perform successive writes, one for each record in
   * the batch.
   * @param dataBatch Object[] containing records to be written.
   * @throws SQLException
   */
  public void writeBatch(Object[] dataBatch) throws SQLException;

  /**
   * validate the state of the writer.
   * @param exceptions list of validation exceptions to append to.
   * @param comp
   */
  public void validate(List exceptions, IComponent comp);
}
