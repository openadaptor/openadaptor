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

package org.openadaptor.auxil.connector.jdbc.sybase;

import java.sql.SQLException;

import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ProcessingException;

/**
 * sybase specific jdbc connection for deadlock identification and other exception ignores
 * @author perryj
 *
 */
public class JDBCConnection extends org.openadaptor.auxil.connector.jdbc.JDBCConnection {

  public void handleException(SQLException e, String message) {
    
    // ignore deadlock, if num retries > 0
    if (e.getErrorCode() == 1205 && incrementDeadlockCount() > 0) {
      return;
    }
    
    // ignore empty resultset
    if (e.getSQLState().equals("JZ0R2")) {
      return;
    }
    
    // decide whether its a ConnectionException or a ProcessingException
    switch (e.getErrorCode()) {
      case 1602:
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
