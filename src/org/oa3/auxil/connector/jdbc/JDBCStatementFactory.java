/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.auxil.connector.jdbc;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCStatementFactory.java,v 1.2 2006/11/04 23:32:44 ottalk Exp $
 * Rev:  $Revision: 1.2 $
 * Created Oct 22, 2006 by Kuldip Ottal
 */

import org.oa3.core.exception.ComponentException;
import java.sql.Connection;

/**
 * This static method factory class returns the appropriate object required for
 * write mechanism set in the configuration file.
 */
public class JDBCStatementFactory implements IJDBCConstants {

  /**
   * This method returns the appropriate database write mechanism object
   *
   * @param writeMechanism Constant indicating the database write mechanism configured
   * @param objectName Write mechanism object in database, for example database table, stored procedure or sql statement
   * @param delimiter The character to be used to delimit variables, which are to be replaced with values from oa3 data record
   * @param connection JDBC Connection
   * @return IJDBCStatement
   * @throws ComponentException
   */
  public static IJDBCStatement createStatement(String writeMechanism, String objectName, String delimiter,Object mapping,Connection connection) throws ComponentException {
    if (writeMechanism.equals(DATABASE_TABLE)) {
      return new JDBCTableStatement(objectName,mapping,connection);
    }
    if (writeMechanism.equals(STORED_PROCEDURE)) {
      return new JDBCStoredProcStatement(objectName,mapping,connection);
    }
    if (writeMechanism.equals(SQL_STATEMENT)) {
      return new JDBCSqlStatement(objectName,delimiter,connection);
    }
    throw new RuntimeException ("Unrecognised JDBC write mechanism");
  }
}


