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

package org.openadaptor.auxil.connector.jdbc.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.reader.orderedmap.ResultSetToOrderedMapConverter;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.Component;
import org.openadaptor.core.IEnrichmentReadConnector;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.connector.DBEventDrivenPollingReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.transaction.ITransactional;
import org.openadaptor.util.JDBCUtil;

import java.sql.*;
import java.util.List;

/**
 * Generic JDBC polling read connector that replaced several pre 3.3 JDBC read connectors:
 *
 * The (pre 3.3) JDBCReadConnector is equivalent to this connector with the LoopingPollingReadConnector
 * with no parameters.
 * The (pre 3.3) JDBCPollConnector is equivalent to this connector with the LoopingPollingReadConnector 
 * with pollLimit and pollInterval parameters set.
 * The (pre 3.3) JDBCEventReadConnector is equivalent to this connector with the 
 * DBEventDrivenPollingReadConnector. 
 * 
 * Associates an IResultSetConverter with the connector, by default this 
 * is <code>ResultSetToOrderedMapConverter</code>.
 * 
 * @author Eddy Higgins, Kris Lachor
 */
public class JDBCReadConnector extends Component implements IEnrichmentReadConnector, ITransactional{

  private static final int EVENT_RS_STORED_PROC = 3;
  private static final int EVENT_RS_PARAM1 = 5;

  private static final Log log = LogFactory.getLog(JDBCReadConnector.class.getName());

  private static AbstractResultSetConverter DEFAULT_CONVERTER = new ResultSetToOrderedMapConverter();

  private static final String DEFAULT_PARAMETER_PLACEHOLDER = "?";

  private JDBCConnection jdbcConnection;

  private IResultSetConverter resultSetConverter = DEFAULT_CONVERTER;

  protected String sql;

  /* 
   * SQL that, if set, will get executed right after the connection is established and right before the
   * disconnection, respectively.
   */
  private String afterConnectSql=null;
  private String beforeDisconnectSql=null;

  /* 
   * SQL that, if set, will get executed right before the query is executed and right after the 
   * result set is exhausted, respectively.
   */
  private String preReadSql=null;
  private String postReadSql=null;
  
  /* Internal state. Derived from <code>sql</code> by replacing paramter placeholders with concrete values*/
  protected String postSubstitutionSql;

  /* Internal state */
  private boolean enrichmentMode = false;

  protected Statement statement = null;

  protected CallableStatement callableStatement = null;

  protected ResultSet rs = null;

  protected ResultSetMetaData rsmd = null;

  protected boolean dry = false;

  protected int batchSize = IResultSetConverter.CONVERT_ONE;

  /**
   * Default constructor.
   */
  public JDBCReadConnector() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id component id.
   */
  public JDBCReadConnector(String id) {
    super(id);
  }

  /**
   * Set up connection to database
   */
  public void connect() {
    log.debug("Connector: [" + getId() + "] connecting ....");
    Connection connection=null;
    if (!jdbcConnection.isConnected()) {
      try {
        jdbcConnection.connect();
        connection=jdbcConnection.getConnection();
        statement =  connection.createStatement();
      } catch (SQLException e) {
        handleException(e, "failed to create JDBC statement");
      }
      if (afterConnectSql!=null) {
        log.info("Executing after connect SQL: " + afterConnectSql);
        executePrePostambleSQL(afterConnectSql, connection);
      }    
    }
    else {
      log.info("JDBConnection "+jdbcConnection+" is already connected. Not reconnecting");
    }
    dry = false; 
 }

  //ToDo: This should be refactored to be shared common code with 
  //      JDBCWriteConnector.
  private void executePrePostambleSQL(String sql, Connection connection) {
    try {
      PreparedStatement ps=connection.prepareStatement(sql);
      ps.execute();
      ps.close();
    } catch (SQLException e) {
      jdbcConnection.handleException(e, "Failed to execute sql: "+sql);
    }
  }

  /**
   * Closes the <code>statement</code>.
   * Executes the <code>beforeDisconnectSql</code>, then disconnects the JDBC connection.
   *
   * @throws ConnectionException 
   */
  public void disconnect() throws ConnectionException {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
  
    if ( jdbcConnection == null  || (!jdbcConnection.isConnected())) {
      log.info("Connection already closed/disconnected");
      if (beforeDisconnectSql!=null) {
        log.warn("Unable to execute before disconnect SQL - connection is not available");
      }
      return;
    }  

    JDBCUtil.closeNoThrow(statement);
 
    /* Execute before disconnect sql if it exists... */
    if (beforeDisconnectSql!=null) {
      log.info("Executing before disconnect SQL: " + beforeDisconnectSql);
      executePrePostambleSQL(beforeDisconnectSql, jdbcConnection.getConnection());
    }
  
    /* Disconnect */
    try {
      jdbcConnection.disconnect();
    } catch (SQLException e) {
      handleException(e, "Failed to disconnect JDBC connection");
    }
    log.info("Connector: [" + getId() + "] disconnected");
  }

  /**
   * Inpoint has no more data.
   *
   * @return boolean true if there is no more input data.
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
   * @throws OAException
   */
  public Object[] next(long timeoutMs) throws OAException {
    log.info("Call for next record(s)");
    try {
      if (rs == null || enrichmentMode) {
        
        /* execute preabmle SQL */
        if (preReadSql!=null) {
          log.info("Executing pre read SQL: "+preReadSql);
          executePrePostambleSQL(preReadSql, jdbcConnection.getConnection());
        }    
        
        /* If a callable statement's been set, ignore the sql and the statement */
        if(callableStatement != null){
          rs = callableStatement.executeQuery(); 
        }
        else{
          /* postSubstitutionSql takes precedence over sql */
          String sqlForExecution=postSubstitutionSql==null ? sql : postSubstitutionSql;
          if (log.isDebugEnabled()) {
            log.debug("SQL: "+sqlForExecution);
          }
          rs = statement.executeQuery(sqlForExecution);
        }
      }
      /* set the dry flag in case this is not the first query in this reader's lifetime */
      dry = false;
   
      /* 
       * Converts certain number of records from the result set, depending on batchSize value.
       * If the result set had fewer records than expected, closes the result set and turns 
       * the connector dry. 
       */
      Object [] data = resultSetConverter.convert(rs, batchSize);          
      if( data.length==0 ||  batchSize==IResultSetConverter.CONVERT_ALL
          || (batchSize!=IResultSetConverter.CONVERT_ALL && data.length < batchSize)){
        JDBCUtil.closeNoThrow(rs);
        rs = null;
        dry = true;
        
        /* execute postamble SQL */
        if (postReadSql!=null) {
          log.info("Executing post read SQL: " + postReadSql);
          executePrePostambleSQL(postReadSql, jdbcConnection.getConnection());
        }    
      }

      return data;
    }
    catch (SQLException e) {
      handleException(e);
    }
    return new Object[0];
  }


  /**
   * Sets input parameters on the SQL query with parameter placeholders.
   * Then calls #next(long).
   * 
   * @see #next(long)
   * @see #setParametersForQuery(IOrderedMap)
   * @return Object[] array of objects from resultset
   */  
  public Object[] next(IOrderedMap inputParameters, long timeout) {    
    enrichmentMode = true;
    if(inputParameters != null){
      for(int i=1; i<=inputParameters.size(); i++){
        setParametersForQuery(inputParameters);
      }     
    }
    else{     
      log.info("No input parameters for enrichment call");
    }
    return next(timeout);
  }

  /**
   * Replaces parameters placeholders in <code>sql</code>, with concrete values
   * from <code>inputParameters</code>. 
   * 
   * @see JDBCReadConnector#next(IOrderedMap, long)
   */
  protected void setParametersForQuery(IOrderedMap inputParameters){
    if(inputParameters==null || sql.indexOf(DEFAULT_PARAMETER_PLACEHOLDER)==-1){
      return;
    }
    postSubstitutionSql = sql;
    for(int i=0; i<inputParameters.size(); i++){
      Object value = inputParameters.get(i);
      int index = postSubstitutionSql.indexOf(DEFAULT_PARAMETER_PLACEHOLDER);
      if(index != -1){
        StringBuffer buffer = new StringBuffer();
        buffer.append(postSubstitutionSql.substring(0, index));
        buffer.append(value);
        buffer.append(postSubstitutionSql.substring(index + 1));
        postSubstitutionSql = buffer.toString();
      }
      else{
        log.warn("Insufficient parameter placeholders for input parameters.");
        break;
      }
    }    
  }

  /**
   * Converts the event ResultSet into a statement that will the actual data.
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

    /* create a callable statement and set 'in' parameters */
    CallableStatement callableStatement = jdbcConnection.getConnection().prepareCall(sql);

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
   * 
   * @see IReadConnector#setReaderContext(Object)
   * @param context - context as an IOrderedMap
   */
  public void setReaderContext(Object context) {
    if(! (context instanceof IOrderedMap)){
      setReaderContext(context);
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

  /**
   * @see IReadConnector#getReaderContext()
   * @return null
   */
  public Object getReaderContext() {
    return null;
  }

  /**
   * Allows to overwrite the default IResultSetConverter.
   * 
   * @param resultSetConverter an IResultSetConverter that will overwrite the default
   *        convertor.
   */
  public void setResultSetConverter(IResultSetConverter resultSetConverter) {
    this.resultSetConverter = resultSetConverter;
  }

  protected void handleException(SQLException e, String message) {
    jdbcConnection.handleException(e, message);
  }

  protected void handleException(SQLException e) {
    jdbcConnection.handleException(e, null);
  }

  /**
   * @see ITransactional#getResource()
   * @see JDBCConnection#getTransactionalResource()
   */
  public Object getResource() {
    if (jdbcConnection.isTransacted()) {
      return jdbcConnection.getTransactionalResource();
    } else {
      return null;
    }
  }

  /**
   * Checks that the mandatory properties have been set
   * 
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if (jdbcConnection == null) {
      exceptions.add(new ValidationException("[jdbcConnection] property not set. " 
          + "Please supply an instance of " + JDBCConnection.class.getName(), this));   
    } else {
      jdbcConnection.validate(exceptions);
    }
  }
  
  /**
   * Sets the batch size. A negative number of zero will correspond to {@link IResultSetConverter#CONVERT_ALL}
   * - the batch will contain all rows from the result set. Batch size equal to 1 corresponds to
   * {@link IResultSetConverter#CONVERT_ONE}, which'll fetch the next records from the result set
   * on every call to {@link #next(long)}.
   * 
   * @param batchSize the batch size
   */
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  /**
   * Sets the jdbc connection.
   * 
   * @param connection a JDBCConnection
   */
  public void setJdbcConnection(JDBCConnection connection) {
    jdbcConnection = connection;
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
   * @param callableStatement a ready to execute statement
   */
  private void setCallableStatement(CallableStatement callableStatement) {
    this.callableStatement = callableStatement;
  }

  /**
   * Optional SQL to be executed before connector processes messages.
   * @param sql SQL statement
   */
  public void setAfterConnectSql(String sql) {
    this.afterConnectSql=sql;
  }

  /**
   * Optional SQL to be executed before connector disconnects.
   * @param sql SQL statement
   */
  public void setBeforeDisconnectSql(String sql) {
    this.beforeDisconnectSql=sql;
  }
  
  /**
   * @param sql SQL statement
   */
  public void setPreReadSql(String sql) {
    this.preReadSql=sql;
  }

  /**
   * @param sql SQL statement
   */
  public void setPostReadSql(String sql) {
    this.postReadSql=sql;
  }
  
}
