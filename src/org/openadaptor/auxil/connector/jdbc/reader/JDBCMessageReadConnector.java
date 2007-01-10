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
package org.oa3.auxil.connector.jdbc.reader;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCMessageReadConnector.java,v 1.10 2006/11/04 23:32:44 ottalk
 * Exp $ Rev: $Revision: 1.10 $ Created May 26, 2006 by Kuldip Ottal
 */
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.ComponentException;
import org.oa3.util.JDBCUtil;
import org.oa3.util.ThreadUtil;

/**
 * A JDBCReadConnector that uses a stored proc to poll for database events, these
 * events must be in a specific format and this component will convert that into
 * a stored procedure call that it then calls to query some data. By default it calls
 * a predefined stored procedure called OA3_ProcessNextQueuedMessage. Refer to openadaptor
 * resources for the schema it is associated with.
 * 
 * @author Kuldip Ottal
 * @author perryj
 */
public class JDBCMessageReadConnector extends AbstractJDBCReadConnector {

  private static final Log log = LogFactory.getLog(JDBCMessageReadConnector.class);

  private static final String DEFAULT_SP_NAME = "OA3_ProcessNextQueuedMessage";

  private static final int EVENT_RS_STORED_PROC = 4;
  private static final int EVENT_RS_PARAM1 = 6;

  private String eventPollSP = DEFAULT_SP_NAME;

  private int messageServiceID = -1;

  public JDBCMessageReadConnector() {
    super();
  }

  public JDBCMessageReadConnector(String id) {
    super(id);
  }

  /**
   * sets the stored procedure used to poll for data events
   */
  public void setEventPollStoredProcedure(String eventPollSP) {
    this.eventPollSP = eventPollSP;
  }

  /**
   * Set service id for data events we are polling for
   */
  public void setMessageServiceID(int messageServiceID) {
    this.messageServiceID = messageServiceID;
  }

  /**
   * polls for the data that relates to the next event in the database
   * the format of the data is controlled by the resultSetConverter returns
   * null if there are no outstanding events to process.
   */
  public Object[] next(long timeoutMs) throws ComponentException {
    CallableStatement s = null;
    try {
      s = getNextStatement();
      if (s != null) {
        ResultSet rs = s.executeQuery();
        return convertAll(rs);
      } else {
        ThreadUtil.sleepNoThrow(timeoutMs);
        return null;
      }
    } catch (SQLException sqle) {
      throw new ComponentException(sqle.getMessage(), sqle, this);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
  }

  /**
   * ensures that messageServerId has been set
   */
  public void connect() {
    if (messageServiceID == -1) {
      throw new ComponentException("messageServiceID has not been set", this);
    }
    super.connect();
  }
  
  /**
   * gets next statement to execute against the database, by calling
   * the eventPollSP and converting it's ResultSet to a CallableStatement
   */
  private CallableStatement getNextStatement() {
    CallableStatement s = null;
    try {
      s = prepareCall("{ call " + eventPollSP + "(?,?) }");
      s.setInt(1, messageServiceID);
      s.setString(2, "Y");
      ResultSet rs = s.executeQuery();
      if (rs.next()) {
        JDBCUtil.logResultSet(log, "event ResultSet", rs);
        return convertEventToStatement(rs);
      } else {
        return null;
      }
    } catch (SQLException e) {
      return null;
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
  }

  /**
   * convert event ResultSet into a statement to actually get the data
   */
  private CallableStatement convertEventToStatement(ResultSet rs) throws SQLException {

    // create statement string
    StringBuffer buffer = new StringBuffer();
    buffer.append("{ call ");
    buffer.append(rs.getString(EVENT_RS_STORED_PROC));
    buffer.append(" (");
    for (int i = EVENT_RS_PARAM1; i <= rs.getMetaData().getColumnCount() && rs.getObject(i) != null; i++) {
      buffer.append(i > EVENT_RS_PARAM1 ? ",?" : "?");
    }
    buffer.append(")}");
    String sql = buffer.toString();
    
    // create statement
    CallableStatement s = prepareCall(sql);

    // set in parameters
    for (int i = EVENT_RS_PARAM1; i <= rs.getMetaData().getColumnCount() && rs.getObject(i) != null; i++) {
      s.setString((i+1)-EVENT_RS_PARAM1, rs.getString(i));
      if (log.isDebugEnabled()) {
        sql = sql.replaceFirst("\\?", rs.getString(i));
      }
    }
    log.debug("message statement = " + sql);

    return s;
  }
}