/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openadaptor.util.JDBCUtil;
import org.openadaptor.util.ThreadUtil;

public class JDBCPollConnector extends AbstractJDBCReadConnector {

  private String sqlStatement;

  private PreparedStatement pollStatement;

  public JDBCPollConnector() {
    super();
  }
  
  public JDBCPollConnector(String id) {
    super(id);
  }

  public void setSqlStatement(String sqlStatement) {
    this.sqlStatement = sqlStatement;
  }

  public void connect() {
    super.connect();
    try {
      pollStatement = prepareCall(sqlStatement);
    } catch (SQLException e) {
      throw new RuntimeException("failed to create poll callable statement, " + e.getMessage(), e);
    }
    resetDeadlockCount();
  }

  public Object[] next(long timeoutMs) {
    Object[] data = null;
    CallableStatement s = null;
    try {
      ResultSet rs = pollStatement.executeQuery();
      data = convertAll(rs);
      if (data.length == 0) {
        ThreadUtil.sleepNoThrow(timeoutMs);
      }
      resetDeadlockCount();
    } catch (SQLException e) {
      handleSQLException(e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
    return data;
  }

  public void disconnect() {
    JDBCUtil.closeNoThrow(pollStatement);
    pollStatement = null;
    super.disconnect();
  }

}
