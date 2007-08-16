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

package org.openadaptor.core.connector;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.core.IPollingStrategy;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.util.JDBCUtil;
import org.openadaptor.util.ThreadUtil;
import org.openadaptor.auxil.connector.jdbc.reader.JDBCReadConnector;

/** 
 * A polling strategy that uses a stored proc to poll for database events, these
 * events must be in a specific format and this component will convert that into
 * a call to underlying connector to query the data that relates to the event. 
 * By default it calls a predefined stored procedure called OA3_GetNextQueuedEvent. 
 * Refer to openadaptor resources for the schema it is associated with.
 * 
 * @author Kuldip Ottal, Kris Lachor
 */
public class DBEventDrivenPollingStrategy extends AbstractPollingStrategy {

  private static final Log log = LogFactory.getLog(DBEventDrivenPollingStrategy.class);

  private static final String DEFAULT_SP_NAME = "OA_GetNextEvent";

  private static final int EVENT_RS_STORED_PROC = 4;
  private static final int EVENT_RS_PARAM1 = 6;

  private String eventPollSP = DEFAULT_SP_NAME;

  private String eventServiceID = null;
  
  private String eventTypeID = null;

  private CallableStatement pollStatement;
  
  private JDBCConnection jdbcConnection;

  /**
   * sets the stored procedure used to poll for data events
   */
  public void setEventPollStoredProcedure(String eventPollSP) {
    this.eventPollSP = eventPollSP;
  }

  /**
   * Set service id for data events we are polling for
   */
  public void setEventServiceID(final String eventServiceID) {
    this.eventServiceID = eventServiceID;
  }

  /**
   * Set event type id for data events we are polling for, if unset
   * then all event types will be polled
   * @param eventTypeID
   */
  public void setEventTypeID(final String eventTypeID) {
    this.eventTypeID = eventTypeID;
  }

 
  /**
   * polls for the data that relates to the next event in the database
   * the format of the data is controlled by the resultSetConverter returns
   * null if there are no outstanding events to process.
   */
  public Object[] next(long timeoutMs) throws ComponentException {
    log.info("Polling..");
    Object[] data = null;
    CallableStatement callableStatement = null;
    try {
      callableStatement = getNextStatement();
      if (callableStatement != null) {      
        //
        // todo: ultimately the event data needs to be set on the underlying connector
        // in a protocol - neutral way, otherwise we're tied to the JDBC connector.
        //
        ((JDBCReadConnector) delegate).setCallableStatement(callableStatement);
        data = delegate.next(timeoutMs);
      } else {
        ThreadUtil.sleepNoThrow(timeoutMs);
      }
    } finally {
      JDBCUtil.closeNoThrow(callableStatement);
    }
    return data;
  }
  
  
  /**
   * ensures that eventServiceId has been set
   */
  public void connect() {
    try {
      jdbcConnection.connect();
      pollStatement = jdbcConnection.getConnection().prepareCall("{ ? = call " + eventPollSP + "(?,?) }");
      pollStatement.registerOutParameter(1, java.sql.Types.INTEGER);
      pollStatement.setInt(2, Integer.parseInt(eventServiceID));
      if (eventTypeID != null) {
        pollStatement.setInt(3, Integer.parseInt(eventTypeID));
      } else {
        pollStatement.setNull(3, java.sql.Types.INTEGER);
      }
    } catch (SQLException e) {
      throw new ConnectionException("failed to create poll callable statement, " + e.getMessage(), e, null);
    }
    delegate.connect();
  }

  /**
   * gets next statement to execute against the database, by calling
   * the eventPollSP and converting its ResultSet to a CallableStatement
   */
  private CallableStatement getNextStatement() {
    CallableStatement cs = null;
    ResultSet rs = null;
    try {
      rs = pollStatement.executeQuery();
      if (rs.next()) {
        JDBCUtil.logCurrentResultSetRow(log, "event ResultSet", rs);
        cs = convertEventToStatement(rs);
      }
      return cs;
    } catch (SQLException e) {
      jdbcConnection.handleException(e, null);
    } finally {
      JDBCUtil.closeNoThrow(rs);
    }
    return cs;
  }

  
  /**
   * convert event ResultSet into a statement to get the actual data
   */
  private CallableStatement convertEventToStatement(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd=rs.getMetaData();
    int cols=rsmd.getColumnCount();
    // create statement string
    StringBuffer buffer = new StringBuffer();
    buffer.append("{ call ").append(rs.getString(EVENT_RS_STORED_PROC)).append(" (");
    
    for (int i = EVENT_RS_PARAM1; i <= cols; i++) {
      buffer.append(i > EVENT_RS_PARAM1 ? ",?" : "?");
    }
    
    String sql = buffer.append(")}").toString();
    
    // create statement
    CallableStatement callableStatement = prepareCall(sql);

    // set in parameters
    String loggedSql = sql;
    for (int i = EVENT_RS_PARAM1; i <= cols; i++) {
      String stringVal=(rs.getObject(i)==null)?null:rs.getString(i);      
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
   * @return the poll statement
   */
  public CallableStatement getPollStatement() {
     return pollStatement;
  }
  
  public void setJdbcConnection(JDBCConnection connection) {
    jdbcConnection = connection;
    super.setJdbcConnection(jdbcConnection);
  }

  public int getConvertMode() {
    return IPollingStrategy.CONVERT_ALL;
  }
  
}
