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

package org.openadaptor.auxil.connector.jdbc.writer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;

/**
 * 
 * Base implementation of ISQLWriter. 
 * This handles common behaviour such as checking for batch handling support
 * in the undelying connection.
 * It is also responsible for managing a reusable PreparedStatement, if reuse
 * is possible.
 * 
 * @author higginse
 *
 */
public abstract class AbstractSQLWriter implements ISQLWriter{
  private static final Log log = LogFactory.getLog(AbstractSQLWriter.class);

  protected Connection connection;
  protected PreparedStatement reusablePreparedStatement=null;
  private boolean batchSupport;

  /**
   * Initialise the writer.
   * Determines if batch handling is supported by the databse.
   * 
   */
  public void initialise(Connection connection) {
    log.info("Initialising writer");
    try {
      this.connection=connection;
      batchSupport=checkBatchSupport();
      log.info("Writer does "+(batchSupport?"":"NOT ")+"have batch support");
      reusablePreparedStatement=initialiseReusablePreparedStatement();

    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialise" + e.toString(), e);
    }
  }

  /**
   * Check if connection has batch support.
   * @return
   * @throws SQLException
   */
  protected boolean checkBatchSupport() throws SQLException {
    return connection.getMetaData().supportsBatchUpdates();
  }


  public boolean hasBatchSupport() {
    return batchSupport;
  }

  /**
   * Initialise a reusable Prepared statement.
   * If null, statement reuse is not possible, and
   * one will have to be generated for each write.
   * <br>
   * Subclasses must override this to allow reusable prepared statements.
   * @return PreparedStatement - reusable prepared statement, or null if reuse is not possible.
   */
  protected PreparedStatement initialiseReusablePreparedStatement() {
    return null; //By default PreparedStatement cannot be reused.
  }

  /**
   * Create prepared statement to write a batch of records.
   * @param Non-null data Object[] of records to be written.
   * @return PreparedStatement ready for execution.
   * @throws SQLException
   */
  protected abstract PreparedStatement createBatchStatement(Object[] data) throws SQLException;

  /**
   * Create an appropriate PreparedStatement for writing this datum.
   * @param datum - Non-null Object
   * @return PreparedStatement ready for execution
   * @throws SQLException
   */
  protected abstract PreparedStatement createStatement(Object datum) throws SQLException;

  /**
   * Write a batch of records to a database.
   * <br>
   * This will execute a batch PreparedStatement if the incoming batch
   * has multiple record, and the connection has batch support.
   * Otherwise it will repeatedly execute a PreparedStatement for
   * each record in the batch. It delegates to createBatchStatement() or
   * createStatement accordingly.
   * 
   * @param dataBatch Object[] containing records to be written.
   * @throws SQLException if the batch cannot be written.
   */
  public void writeBatch(Object[] data) throws SQLException {
    try {
      int len=data.length;
      if ((len>1) && (batchSupport)) {
        log.debug("Constructing a batch, size="+len);
        PreparedStatement ps=createBatchStatement(data);
        log.debug("Writing batch");
        int[] updateCounts=ps.executeBatch();
        if (log.isDebugEnabled()) {
          int updates=0;
          for (int i=0;i<updateCounts.length;i++) {
            updates +=updateCounts[i];
          }
          log.debug("Summed update count: "+updates);
        }
        log.info("Batch written");
        releaseStatement(ps);
      }
      else {
        if (log.isDebugEnabled()) {
          if (len>1) {
            log.debug("No batch support - executing individual statements for "+len+" records");
          }
          else {
            log.debug("Executing statement for single record");
          }
        }
        for (int i = 0; i < len; i++) {
          Object datum=data[i];
          if (datum==null) {
            throw new SQLException("Cannot create Statement from null data");
          }
          PreparedStatement ps=createStatement(data[i]);
          ps.executeUpdate();
          releaseStatement(ps);
        }
      }
    }
    catch (SQLException sqle) { //Just log to debug and rethrow
      log.debug("Exception in writeBatch(): "+sqle.getMessage());
      throw sqle;
    }
  }
  /**
   * Close a prepared statement, unless it is a reusable one.
   * @param ps
   * @throws SQLException
   */
  private void releaseStatement(PreparedStatement ps) throws SQLException{
    if (ps!=reusablePreparedStatement){
      ps.close();
    }
  }

  /**
   * Validate configuration.
   */
  public void validate(List exceptions, IComponent comp) {}

  //DB Utility methods

  /**
   * Generate a prepared statement to insert named values into a database table.
   * <br>
   * Note that it does not check that the table name is valid, or that the
   * columnNames exist in that table.
   * 
   * @param connection the database connection to use
   * @param tableName the name of the table to insert into
   * @param columnNames String[] of the table column names
   */

  protected PreparedStatement generatePreparedStatement(Connection connection,String tableName,String[] columnNames) throws SQLException {
    StringBuffer sql=new StringBuffer("INSERT INTO "+tableName+"(");
    StringBuffer params=new StringBuffer();
    for (int i=0;i<columnNames.length;i++) {
      sql.append(columnNames[i]+",");
      params.append("?,");
    }
    sql.setCharAt(sql.length()-1, ')'); //Swap last comma for a bracket.
    params.setCharAt(params.length()-1, ')');//Ditto
    sql.append(" VALUES (").append(params);
    if (log.isDebugEnabled()) {
      log.debug("Generated Prepared stmt: "+sql.toString());
    }
    return connection.prepareStatement(sql.toString());
  }
//  protected PreparedStatement generatePreparedStatement(Connection connection,String tableName,List columnNames) throws SQLException {
//    StringBuffer sql=new StringBuffer("INSERT INTO "+tableName+"(");
//    StringBuffer params=new StringBuffer(); 
//    for (int i=0;i<columnNames.size();i++) {
//      sql.append(columnNames.get(i)+",");
//      params.append("?,");
//    }
//    sql.setCharAt(sql.length()-1, ')'); //Swap last comma for a bracket.
//    params.setCharAt(params.length()-1, ')');//Ditto
//    sql.append(" VALUES (").append(params);
//    if (log.isDebugEnabled()) {
//      log.debug("Generated Prepared stmt: "+sql.toString());
//    }
//    return connection.prepareStatement(sql.toString());
//  }


  /**
   * Determine the types of the columns in a table.
   * <br>
   * It does not check that the table exists, or that the columns actually
   * exist in the table.
   * @param tableName
   * @param connection
   * @param columnNames
   * @return int[] of database types.
   * @throws SQLException
   */
  protected int[] getPreparedStatementTypes(String tableName, Connection connection,String[] columnNames) throws SQLException {
    //Execute a dummy sql statement against database purely to collect table metadata
    String sql= "SELECT * FROM "+tableName+" WHERE 1=2";
    Statement s = connection.createStatement();
    log.debug("Executing SQL: " + sql);
    ResultSet rs=s.executeQuery(sql);
    int[] types;
    ResultSetMetaData rsmd=rs.getMetaData();
    int cols=rsmd.getColumnCount();
    types=new int[columnNames.length];
    List nameList=Arrays.asList(columnNames);
    int mapped=0;
    for (int i=0;i<cols;i++) {
      int type=rsmd.getColumnType(i+1);
      String name=rsmd.getColumnName(i+1);
      int location=nameList.indexOf(name);
      if (location >=0) {
        types[location]=type;
        mapped++;
      }
      else {
        if (log.isDebugEnabled()) {
          log.debug("Ignoring column "+i+"["+name+" ("+rsmd.getColumnTypeName(i+1)+")]");
        }
      }
    }
    if (mapped<types.length) {
      log.warn("Not all column names were mapped. This is probably a configuration error");
    }
    return types;
  }
//  protected int[] getPreparedStatementTypes(String tableName, Connection connection,List columnNames) throws SQLException {
//    //Execute a dummy sql statement against database purely to collect table metadata
//    String sql= "SELECT * FROM "+tableName+" WHERE 1=2";
//    Statement s = connection.createStatement();
//    log.debug("Executing SQL: " + sql);
//    ResultSet rs=s.executeQuery(sql);
//    int[] types;
//    ResultSetMetaData rsmd=rs.getMetaData();
//    int cols=rsmd.getColumnCount();
//    types=new int[columnNames.size()];
//    int mapped=0;
//    for (int i=0;i<cols;i++) {
//      int type=rsmd.getColumnType(i+1);
//      String name=rsmd.getColumnName(i+1);
//      int location=columnNames.indexOf(name);
//      if (location >=0) {
//        types[location]=type;
//        mapped++;
//      }
//      else {
//        if (log.isDebugEnabled()) {
//          log.debug("Ignoring column "+i+"["+name+" ("+rsmd.getColumnTypeName(i+1)+")]");
//        }
//      }
//    }
//    if (mapped<types.length) {
//      log.warn("Not all column names were mapped. This is probably a configuration error");
//    }
//    return types;
//  }

  /**
   * Get the names of the columns of a given table.
   * @param tableName
   * @param connection
   * @return
   * @throws SQLException
   */
  protected List getTableColumnNames(String tableName, Connection connection) throws SQLException {
    //Execute a dummy sql statement against database to collect table metadata
    String sql= "SELECT * FROM "+tableName+" WHERE 1=2";
    Statement s = connection.createStatement();
    log.debug("Executing SQL: " + sql);
    ResultSet rs=s.executeQuery(sql);
    List names=new ArrayList();
    ResultSetMetaData rsmd=rs.getMetaData();

    for (int i=0;i<rsmd.getColumnCount();i++) {
      names.add(rsmd.getColumnName(i+1));
    }
    return names;
  }

  protected int getStoredProcArgumentCount(String storedProcName,Connection connection) throws SQLException {
    DatabaseMetaData dmd = connection.getMetaData();
    int argCount=-1;
    ResultSet rs = dmd.getProcedureColumns(connection.getCatalog(),"%",storedProcName,"%");
    if (!rs.next()) { //First rs is return value.
      rs.close();
      log.warn("Failed to lookup stored procedure " +storedProcName);
      throw new SQLException("failed to lookup stored procedure "+storedProcName);
    }
    argCount = 0;
    while (rs.next()) {
      argCount++;
      if (log.isDebugEnabled()) {
        log.debug("Catalog=" + rs.getString(1) + ", Schema=" + rs.getString(2) + ", Proc=" + rs.getString(3) + ", Column=" + rs.getString(4) + ",Type=" + rs.getString(6) + "TypeName=" + rs.getString(7));
      }
    }
    log.debug("Number of stored procedure parameters found: " + argCount);

    rs.close();
    return argCount;
  }

}

