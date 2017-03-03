/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

/**
 * Mock SQL Driver used to test JDBCConnection.
 * 
 * @author oa Core Team
 */
package org.openadaptor.auxil.connector.jdbc;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class MockDriver implements java.sql.Driver {
  
  public static Connection MockConnection;
  
  static {
    try {
      java.sql.DriverManager.registerDriver(new MockDriver());
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException("Failed to register mock sql driver.");
    }
  }

  public boolean acceptsURL(String url) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public Connection connect(String url, Properties info)
      throws SQLException {
    // TODO Auto-generated method stub
    return getMockConnection();
  }

  public int getMajorVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getMinorVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean jdbcCompliant() {
    // TODO Auto-generated method stub
    return false;
  }

  public static Connection getMockConnection() {
    return MockConnection;
  }

  public static void setMockConnection(Connection mockConnection) {
    MockConnection = mockConnection;
  }
}
