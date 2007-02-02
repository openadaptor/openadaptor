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

package org.openadaptor.auxil.connector.jdbc;

import org.openadaptor.core.transaction.ITransactionalResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
      if (!connection.isTransacted()) {
        throw new RuntimeException("Attempt to start transaction using an untransacted JDBC connection");
      } else {
        connection.beginTransaction();
      }
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
      if (!connection.isTransacted()) {
        throw new RuntimeException("Attempt to commit a transaction using an untransacted JDBC connection");
      } else {
        connection.commitTransaction();
        log.debug("JDBC Transaction committed");
      }
    } catch (Exception e) {
      throw new RuntimeException("JDBC Exception on attempt to commit a transaction using a JDBC connection", e);
    }
  }

  /**
   * Rollback transaction if connection is transactional
   */
  public void rollback(Throwable t) {
    try {
      if (!connection.isTransacted()) {
        throw new RuntimeException("Attempt to rollback a transaction using an untransacted JDBC connection");
      } else {
        log.debug("JDBC Transaction rolled back");
        connection.rollbackTransaction();
      }
    } catch (Exception e) {
      throw new RuntimeException("JDBC Exception on attempt to rollback a transaction using a JDBC connection", e);
    }
  }

}
