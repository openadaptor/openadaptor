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

package org.openadaptor.auxil.connector.jdbc;

import junit.framework.TestCase;
import java.sql.SQLException;

import org.openadaptor.auxil.connector.jdbc.JDBCConnection;

/**
 * This test case uses the Hypersonic database. The database is created in memory when a jdbc connection is established.
 *
 */
public class JDBCConnectionTestCase extends TestCase {
    private static final String DB_DRIVER="org.hsqldb.jdbcDriver";
    private static final String DB_URL="jdbc:hsqldb:mem:test";
    private static final String DB_USER="sa";
    private static final String DB_PASSWORD="";

    private JDBCConnection jdbcConnection;

    protected void setUp() throws Exception {
      super.setUp();

      jdbcConnection = new JDBCConnection();
      jdbcConnection.setDriver(DB_DRIVER);
      jdbcConnection.setUrl(DB_URL);
      jdbcConnection.setUsername(DB_USER);
      jdbcConnection.setPassword(DB_PASSWORD);

    }

    protected void tearDown() throws Exception {
      super.tearDown();
      jdbcConnection.disconnect();
      jdbcConnection = null;
    }

    public void testConnection() throws SQLException {
      jdbcConnection.connect();
      assertTrue("JDBCConnection not connected", jdbcConnection.isConnected());
    }

    public void testDisconnection() throws SQLException {
      jdbcConnection.disconnect();
      assertFalse("JDBCConnection not disconnected", jdbcConnection.isConnected());
    }

}
