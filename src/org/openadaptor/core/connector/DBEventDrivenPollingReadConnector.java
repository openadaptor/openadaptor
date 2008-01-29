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

package org.openadaptor.core.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.reader.AbstractResultSetConverter;
import org.openadaptor.auxil.connector.jdbc.reader.orderedmap.ResultSetToOrderedMapConverter;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.util.JDBCUtil;
import org.openadaptor.util.ThreadUtil;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** 
 * A polling read connector that uses a stored procedure to poll for database events. 
 * These events must be in a specific format and this component will convert that into
 * a call to underlying connector to query the data that relates to the event. 
 * By default it calls a predefined stored procedure called OA3_GetNextQueuedEvent. 
 * Refer to openadaptor resources for the schema it is associated with.
 * 
 * @author Kuldip Ottal, Kris Lachor
 */
public class DBEventDrivenPollingReadConnector extends AbstractPollingReadConnector {

  private static final Log log = LogFactory.getLog(DBEventDrivenPollingReadConnector.class);
  
  private static final String DEFAULT_SP_NAME = "OA_GetNextEvent";

  private static AbstractResultSetConverter DEFAULT_CONVERTER = new ResultSetToOrderedMapConverter();
  
  private String eventPollSP = DEFAULT_SP_NAME;

  private AbstractResultSetConverter resultSetConverter = DEFAULT_CONVERTER;
  
  private String eventServiceID = null;
  
  private String eventTypeID = null;

  private CallableStatement pollStatement;
  
  private JDBCConnection jdbcConnection;

  /**
   * Constructor.
   */
  public DBEventDrivenPollingReadConnector() {
    super();
  }
  
  /**
   * Constructor.
   * @param id a descriptive identifier for this connector.
   */
  public DBEventDrivenPollingReadConnector(String id) {
    super(id);
  }
 
  /**
   * polls for the data that relates to the next event in the database
   * the format of the data is controlled by the resultSetConverter returns
   * null if there are no outstanding events to process.
   *
   * @throws OAException
   */
  public Object[] next(long timeoutMs) throws OAException {
    log.debug("Polling for events..");
    Object[] data = null;
    IOrderedMap event = getNextEvent();
    if (event != null) {      
      getDelegate().setReaderContext(event);
      try {
      data = getDelegate().next(timeoutMs);
      }
      catch(OAException oae) { //Will be an SQLException.
        log.warn("Delegate failed to process event. Does event refer to an invalid SPROC?");
        throw oae;
      }
    } 
    else {
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
    super.connect();
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
  
}
