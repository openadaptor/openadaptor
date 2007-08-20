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

package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.IPollingReadConnector;
import org.openadaptor.core.connector.DBEventDrivenPollingReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.util.JDBCUtil;

/**
 * Generic JDBC polling read connector created to replace:
 * {@link JDBCPollConnector}, {@link JDBCReadConnector}, {@link JDBCEventReadConnector}.
 * The legacy JDBCReadConnector is equivalent to this connector with the LoopingPollingStrategy 
 * with no parameters.
 * The legacy JDBCPollConnector is equivalent to this connector with the LoopingPollingStrategy 
 * with pollLimit and pollInterval parameters set.
 * The legacy JDBCEventReadConnector is equivalent to this connector with the DBEventDrivenPollingStrategy. 
 * 
 * @author Eddy Higgins, Kris Lachor
 */
public class JDBCReadConnector extends AbstractJDBCReadConnector {

  private static final int EVENT_RS_STORED_PROC = 3;
  private static final int EVENT_RS_PARAM1 = 5;
  
  private static final Log log = LogFactory.getLog(JDBCReadConnector.class.getName());

  protected String sql;
  
  protected Statement statement = null;
  
  protected CallableStatement callableStatement = null;
  
  protected ResultSet rs = null;
  
  protected ResultSetMetaData rsmd = null;
  
  protected boolean dry = false;

  /**
   * Default constructor
   */
  public JDBCReadConnector() {
    super();
  }
  
  public JDBCReadConnector(String id) {
    super(id);
  }

  /**
   * Set sql statement to be executed
   *
   * @param sql
   */
  public void setSql(final String sql) { 
    this.sql = sql;
  }
  
  /**
   * Sets a prepared or callable (ready to execute) statement on this connector.
   * 
   * @param statement a ready to execute statement
   */
  public void setCallableStatement(CallableStatement callableStatement) {
    this.callableStatement = callableStatement;
  }

  /**
   * Set up connection to database
   */
  public void connect() {
    super.connect();
    try {
      statement = createStatement();
    } catch (SQLException e) {
      handleException(e, "failed to create JDBC statement");
    }
    dry = false;
  }

  /**
   * Disconnect JDBC connection
   *
   * @throws ComponentException
   */
  public void disconnect() throws ComponentException {
    JDBCUtil.closeNoThrow(statement);
    super.disconnect();
  }

  /**
   * Inpoint has no more data
   *
   * @return boolean true if there is no more input data
   */
  public boolean isDry() {
    return dry;
  }

  /**
   * Returns array of objects extracted from result set. Executes the fixed
   * query the first time it is called.
   *
   * @param timeoutMs Ignored as this implementation is non-blocking.
   * @return Object[] array of objects from resultset
   * @throws ComponentException
   */
  public Object[] next(long timeoutMs) throws ComponentException {
    log.info("Call for next record(s)");
    try {
      if (rs == null) {
        /* If a callable statement's been set, ingore the sql and the statement */
        if(callableStatement != null){
          rs = callableStatement.executeQuery(); 
        }
        else{
          rs = statement.executeQuery(sql);
        }
      }
      Object data = null;
      
      /* Converts all records in the result set or only one, depending on strategy settings */
      if(getDelegate().getConvertMode() == IPollingReadConnector.CONVERT_ALL){
        data = convertAll(rs);
        rs = null;
        dry = true;
      }
      else{
        data = convertNext(rs);
      }
      
      if (data != null) {
        /* wraps data in object array only when necessary */       
        return  data instanceof Object [] ? (Object[]) data : new Object[] {data};
      }
      /* Becomes 'dry' if the result set was empty */
      else {
        JDBCUtil.closeNoThrow(rs);
        rs = null;
        dry = true;
      }
    }
    catch (SQLException e) {
      handleException(e);
    }
    return new Object[0];
  }
  
  
  /**
   * convert event ResultSet into a statement to get the actual data
   */
  private CallableStatement convertEventToStatement(IOrderedMap row) throws SQLException {
    int cols=row.size();
    
    /* create statement string */
    StringBuffer buffer = new StringBuffer();
    buffer.append("{ call ").append(row.get(EVENT_RS_STORED_PROC)).append(" (");   
    for (int i = EVENT_RS_PARAM1; i < cols; i++) {
      buffer.append(i > EVENT_RS_PARAM1 ? ",?" : "?");
    }
    String sql = buffer.append(")}").toString();
    
    /* create a call and set in parameters */
    CallableStatement callableStatement = prepareCall(sql);
    String loggedSql = sql;
    for (int i = EVENT_RS_PARAM1; i < cols; i++) {
      String stringVal= (String)((row.get(i)==null)? null: row.get(i));      
      callableStatement.setString(i+1-EVENT_RS_PARAM1, stringVal);
      if (log.isDebugEnabled()) {
        loggedSql = loggedSql.replaceFirst("\\?", (stringVal==null)?"<null>":stringVal);
      }
    }
    if (log.isDebugEnabled()) {
       log.debug("Event sql statement = " + loggedSql);
    }  
    return callableStatement;    
  }

  /**
   * Attempts to convert the <code>context</code> to a callable statement. This
   * will only be successfull if the context is an IOrderedMap with elements in
   * a predefined format, such as that returned by {@link DBEventDrivenPollingReadConnector}.
   * If successfull, the callable statement will be used for polling the database.
   * Otherwise, the context will be ingored. 
   */
  public void setReaderConext(Object context) {
    if(! (context instanceof IOrderedMap)){
      super.setReaderConext(context);
      return;
    }
    IOrderedMap event = (IOrderedMap) context;
    CallableStatement callableStatement = null;
    try {
      callableStatement = convertEventToStatement(event);
    } catch (SQLException e) {
      log.warn("Failed to convert event to a callable statement");
      return;
    }
    setCallableStatement(callableStatement); 
  }

}
