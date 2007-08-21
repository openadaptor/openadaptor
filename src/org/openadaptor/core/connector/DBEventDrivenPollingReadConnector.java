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
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.reader.AbstractResultSetConverter;
import org.openadaptor.auxil.connector.jdbc.reader.orderedmap.ResultSetToOrderedMapConverter;
//import org.openadaptor.core.IPollingStrategy;
import org.openadaptor.core.IPollingReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.util.JDBCUtil;
import org.openadaptor.util.ThreadUtil;
import org.openadaptor.auxil.orderedmap.IOrderedMap;

/** 
 * A polling strategy that uses a stored proc to poll for database events, these
 * events must be in a specific format and this component will convert that into
 * a call to underlying connector to query the data that relates to the event. 
 * By default it calls a predefined stored procedure called OA3_GetNextQueuedEvent. 
 * Refer to openadaptor resources for the schema it is associated with.
 * 
 * @author Kuldip Ottal, Kris Lachor
 */
public class DBEventDrivenPollingReadConnector extends AbstractPollingReadConnector {

  private static final Log log = LogFactory.getLog(DBEventDrivenPollingReadConnector.class);
  
  private static final String DEFAULT_SP_NAME = "OA_GetNextEvent";

  private String eventPollSP = DEFAULT_SP_NAME;

  private static AbstractResultSetConverter DEFAULT_CONVERTER = new ResultSetToOrderedMapConverter();
  
  private String eventServiceID = null;
  
  private String eventTypeID = null;

  private CallableStatement pollStatement;
  
  private JDBCConnection jdbcConnection;
  
  private AbstractResultSetConverter resultSetConverter = DEFAULT_CONVERTER;

  /**
   * Constructor.
   */
  public DBEventDrivenPollingReadConnector() {
    super();
  }
  
  /**
   * Constructor.
   * @param id the id
   */
  public DBEventDrivenPollingReadConnector(String id) {
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
    IOrderedMap event = getNextEvent();
    if (event != null) {      
      delegate.setReaderConext(event);
      data = delegate.next(timeoutMs);
    } else {
      //
      // @todo - eventually this sleeping should be removed from here. The same effect could be
      // achieved by wrapping this strategy in the LoopingPollingStrategy and defining poll
      // interval there
      //
      ThreadUtil.sleepNoThrow(timeoutMs);
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
  private IOrderedMap getNextEvent() {
    IOrderedMap event = null;
    ResultSet rs = null;
    try {
      rs = pollStatement.executeQuery();
      Object [] data = resultSetConverter.convertAll(rs);
      if(data == null || data.length == 0){
        return null;
      }
      event = (IOrderedMap) data[0];
    } catch (SQLException e) {
      jdbcConnection.handleException(e, null);
    } finally {
      JDBCUtil.closeNoThrow(rs);
    }
    return event;
  }

  /**
   * @return the poll statement
   */
  public CallableStatement getPollStatement() {
     return pollStatement;
  }
  
  public void setJdbcConnection(JDBCConnection connection) {
    jdbcConnection = connection;
  }

  public int getConvertMode() {
    return IPollingReadConnector.CONVERT_ALL;
  }
  
}
