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

package org.openadaptor.auxil.connector.jdbc.sybase;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ProcessingException;

/**
 * Sybase specific JDBC Connection with Sybase specific handling.
 * Has Sybase specific tests for for deadlock identification and empty
 * result sets, and exception type determination.
 * 
 * @author OA3 Core Team
 *
 */
public class JDBCConnection extends org.openadaptor.auxil.connector.jdbc.JDBCConnection {

  private static final Log log = LogFactory.getLog(JDBCConnection.class);
  //Sybase specific state indicating empty result set
  public static final String SYBASE_EMPTY_RESULT_SET="JZ0R2";
  
  public static final int SYBASE_DEADLOCK_DETECTED=1205;
  public static final int SYBASE_COMMUNICATION_ERROR=1602;

  /**
   * Perform Sybase specific exception handling.
   * Has bespoke handling for deadlock detection, empty result set and determination
   * of Exception type to propagate.
   * @param e - SQLException to check for Sybase specific handling
   * @param message - String which will be included as part of propagated Exception if necessary.
   */
  public void handleException(SQLException e, String message) {
    
    // ignore deadlock, if num retries > 0
    if ( (SYBASE_DEADLOCK_DETECTED== e.getErrorCode()) && incrementDeadlockCount() > 0) {
      log.info("Sybase deadlock detected, " + getDeadlockRetriesRemaining() + " retries remaining");
      return;
    }
    // ignore empty ResultSet
    // Fix for SC104 - e.sqlState() may return null, so put it as the argument to the equals() instead.
    if (SYBASE_EMPTY_RESULT_SET.equals(e.getSQLState())) {
      return;
    }
    
    // decide whether its a ConnectionException or a ProcessingException
    switch (e.getErrorCode()) {
      case SYBASE_COMMUNICATION_ERROR:
        throw new ConnectionException((message != null ? message + ", " : "")
            + ", SQLException, " + e.getMessage() 
            + ", Error Code = " + e.getErrorCode()
            + ", State = " + e.getSQLState(), e, this);
      default:
        throw new ProcessingException((message != null ? message + ", " : "")
            + ", SQLException, " + e.getMessage() 
            + ", Error Code = " + e.getErrorCode()
            + ", State = " + e.getSQLState(), e, this);
    }
  }
}
