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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * This test case uses the Hypersonic database. The database is created in memory when 
 * a jdbc connection is established.
 * 
 * This class can be used as a superclass for any test cases that require a JDBCConnection.
 */
public abstract class AbstractJDBCConnectionTests extends TestCase {
  private static final Log log =LogFactory.getLog(AbstractJDBCConnectionTests.class); 
  private static final String DB_DRIVER="org.hsqldb.jdbcDriver";
  private static final String DB_URL="jdbc:hsqldb:mem:test";
  private static final String DB_USER="sa";
  private static final String DB_PASSWORD="";

  protected JDBCConnection jdbcConnection;

  protected void setUp() throws Exception {
    log.debug("setUp() beginning");
    super.setUp();

    jdbcConnection = new JDBCConnection();
    jdbcConnection.setDriver(DB_DRIVER);
    jdbcConnection.setUrl(DB_URL);
    jdbcConnection.setUsername(DB_USER);
    jdbcConnection.setPassword(DB_PASSWORD);
    Properties props = new Properties();
    props.setProperty("shutdown", "true");
    jdbcConnection.setProperties(props);
    jdbcConnection.connect();

		/* run in schema if defined */
		List<String> schemas = getSchemaDefinitions();
		if (null != schemas) {
			for (String schema : schemas) {
				PreparedStatement preparedStatement = jdbcConnection.getConnection().prepareStatement(schema);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
		}
		log.debug("setUp() complete");
  }

  protected void tearDown() throws Exception {
    log.debug("tearDown() beginning");
    super.tearDown();
    jdbcConnection.disconnect();
    jdbcConnection = null;
    log.debug("teardown() ending");
  }

  public void testConnection() throws SQLException {
    log.debug("--- Beginning testConnection --");
    assertTrue("JDBCConnection not connected", jdbcConnection.isConnected());
    log.debug("--- Ending testConnection --");
  }

  public void testDisconnection() throws SQLException {
    log.debug("--- Beginning testDisconnection --");
    jdbcConnection.disconnect();
    assertFalse("JDBCConnection not disconnected", jdbcConnection.isConnected());
    log.debug("--- Ending testDisconnection --");
  }

  /**
   * @return DB schemat definition to be set up before the tests are run.
   */
  public abstract List<String> getSchemaDefinitions();
}
