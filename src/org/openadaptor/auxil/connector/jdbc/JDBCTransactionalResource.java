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

package org.openadaptor.auxil.connector.jdbc;

import org.openadaptor.core.transaction.ITransactionalResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author perryj
 *
 */
public class JDBCTransactionalResource implements ITransactionalResource {

  private static final Log log = LogFactory.getLog(JDBCTransactionalResource.class);

  private JDBCConnection connection;

  public JDBCTransactionalResource(final JDBCConnection connection) {
    this.connection = connection;
  }

  /**
   * Determine if connection is transactional, if it is,start transaction
   */
  public void begin() {
    try {
      connection.beginTransaction();
    } catch (Exception e) {
      throw new RuntimeException("JDBC Exception on attempt to start transaction using a JDBC connection", e);
    }
    log.debug("JDBC Transaction begun");
  }

  /**
   * Commit transaction if connection is transactional
   */
  public void commit() {
    try {
      connection.commitTransaction();
      log.debug("JDBC Transaction committed");
    } catch (Exception e) {
      throw new RuntimeException("JDBC Exception on attempt to commit a transaction using a JDBC connection", e);
    }
  }

  /**
   * Rollback transaction if connection is transactional.
   * Checks if connection is active first (see SC62).
   */
  public void rollback(Throwable t) {
    try {
      if(connection.isConnected()){
        log.debug("JDBC Transaction rolled back");
        connection.rollbackTransaction();
      }
      else{
        log.error("Connection is disconnected, can't rollback.");
        log.debug("JDBC Transaction roll back FAILED.");
      }
    } catch (Exception e) {
      throw new RuntimeException("JDBC Exception on attempt to rollback a transaction using a JDBC connection", e);
    }
  }

}
