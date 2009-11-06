/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.exception.ConnectionException;

/**
 * 
 * Abstract base implementation of ISQLWriter.
 * <br> 
 * This handles common behaviour such as checking for batch handling support
 * in the underlying connection.
 * It is also responsible for managing a reusable PreparedStatement, if reuse
 * is possible.
 * 
 * @author higginse
 */
public abstract class AbstractSQLWriter implements ISQLWriter{
  private static final Log log = LogFactory.getLog(AbstractSQLWriter.class);
  //Mask to indicate that a db column type is an input (or inout) to stored proc.
  protected static final int SP_IN_ARG_TYPE_MASK= DatabaseMetaData.procedureColumnIn | DatabaseMetaData.procedureColumnInOut;

  public static final char DEFAULT_ID_QUOTE='\"';
  private static final char DEFAULT_MYSQL_ID_QUOTE='`';
  private static final char QUOTE_UNSET='\u0000'; //Flag for unset quote char

  protected Connection connection;
  protected PreparedStatement reusablePreparedStatement=null;
  protected int[] argSqlTypes; //Might need the sql types for null columns.  
  protected String[] outputColumns;

  private boolean batchSupport;

  //Potentially log database version info, but only once
  private boolean debug_db_version_not_logged=true; //flag indicating db version info has not yet been logged

  //Flag to indicate whether table names and column identifiers should be quoted.
  protected boolean quoteIdentifiers=true;

  protected char identifierQuoteOpen =QUOTE_UNSET;
  protected char identifierQuoteClose=QUOTE_UNSET;
  
  //If set, this package will be assumed (only for stored procs on  Oracle databases)
  protected String oraclePackage=null;
  /**
   * Flag to indicate that table and column name identifiers should be quoted in 
   * calls to the database.
   * It is enabled by default (note that this changes default behaviour from 
   * 3.4.4 where no quoting was possible)
   * @since 3.4.5
   * @param enabled - if true (the default), quoting will happen
   */
  public void setQuoteIdentifiers(boolean enabled) {
    this.quoteIdentifiers=enabled;
  }

  /**
   * If set, and identifierQuoting is enabled, this character will be used 
   * to quote table and column identifiers. 
   * If identifierQuoteCloseChar is not specified, this will also be 
   * used as the closing quote character for such identifiers.
   * It defaults to DEFAULT_ID_QUOTE (or DEFAULT_MYSQL_ID_QUOTE when using
   * mysql databases).
   * @param c quote character.
   */
  public void setIdentifierQuoteChar(char c) {
    identifierQuoteOpen=c;
  }

  /**
   * If set, and identifierQuoting is enabled, it
   * assigned the character to use for closing quotes
   * on table and column identifiers.
   * @param c character to sue for quotes.
   * It defaults to value assigned to identifierQuoteChar
   * 
   */
  public void setIdentifierQuoteCloseChar(char c) {
    identifierQuoteClose=c;
  }
  
  /**
   * Initialise the writer.
   * Determines if batch handling is supported by the databse.
   * 
   */
  public void initialise(Connection connection) throws ConnectionException {
    log.info("Initialising writer");
    try {
      this.connection=connection;
      DatabaseMetaData dmd = connection.getMetaData();
      logDBInfo(dmd);

      //Initialise table and column name quoting mechanism.
      if (quoteIdentifiers) {
        initialiseQuoting(dmd.getDatabaseProductName().toLowerCase());
      }
      batchSupport=checkBatchSupport();
      log.info("Writer does "+(batchSupport?"":"NOT ")+"have batch support");
      initialiseReusablePreparedStatement();
      if ((reusablePreparedStatement!=null) && (argSqlTypes==null)){ //Subclass didn't setup the argument types!
        String msg="Argument types not set for PreparedStatement calls. This may not work with null values!";
        log.warn(msg);
      }

    } catch (SQLException e) {
      throw new ConnectionException("Failed to initialise" + e.toString(), e);
    }
  }
  /**
   * Assigns appropriate values to opening and closing quote characters for
   * table name and identifier quoting purposes.
   * 
   * Is uses db metadata to get the db name - mysql databases use a different
   * default quoting mechanism to most others (backtick instead of double quote).
   * @param connectionClassName String containing the database name
   */
  protected void initialiseQuoting(String dbName) {
    //Cludge to assign mysql a different identifier default quot
    char defaultQuote=(dbName.indexOf("mysql")>=0)?DEFAULT_MYSQL_ID_QUOTE:DEFAULT_ID_QUOTE; 
    if (identifierQuoteOpen==QUOTE_UNSET) {
      identifierQuoteOpen=defaultQuote;
    }
    if (identifierQuoteClose==QUOTE_UNSET) {
      identifierQuoteClose=identifierQuoteOpen;
    }   
  }

  /**
   * Check if this Writer's underlying connection has batch support.
   * <br>
   * This will query the underlying connection's metadata to see
   * if it supports batch updates.
   * @return <code>true</code> if the connection supports batch updates, <code>false</code> otherwise
   * @throws SQLException
   */
  protected boolean checkBatchSupport() throws SQLException {
    return connection.getMetaData().supportsBatchUpdates();
  }


  /**
   * Flag indicating if this Writer can support batching.
   * <br>
   * @return <code>true</code> if it can support batching, or <code>false</code> otherwise.
   */
  public boolean hasBatchSupport() {
    return batchSupport;
  }

  /**
   * Initialise a reusable Prepared statement.
   * <br>
   * Subclasses must implement this to allow reusable prepared statements.
   */
  protected abstract void initialiseReusablePreparedStatement();

  /**
   * This creates a reusable Prepared Statement for insert statements 
   * into a table.
   */
  protected void initialiseReusableInsertStatement(String tableName) {
    log.info("Initialising prepared statement for insertion into "+tableName+"...");
    try {
      //Load bean properties with database metadata values
      if (outputColumns==null) {
        setOutputColumns(getTableColumnNames(tableName, connection));
      }
      argSqlTypes=getPreparedStatementTypes(tableName, connection, outputColumns);
      reusablePreparedStatement=generatePreparedStatement(connection, tableName, outputColumns);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialise information for target table, " + e.toString(), e);
    }
  }

  /**
   * This creates a reusable Prepared Statement for calls to a
   * stored procedure.
   */
  protected void initialiseReusableStoredProcStatement(String procName) {
    log.info("Initialising prepared statement for " + procName);
    try {
      argSqlTypes = getStoredProcArgumentTypes(procName, connection);
      if (outputColumns != null) {
        if (argSqlTypes.length != outputColumns.length) {
          throw new SQLException("Proc expects " + argSqlTypes.length + " arguments, but outputColumns contains " + outputColumns.length);
        }
      }
      String sql = generateStoredProcSQL(procName,argSqlTypes);
      reusablePreparedStatement = connection.prepareStatement(sql);
      log.debug("Reusable prepared statement is: " + sql);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create prepared statement for proc " + procName + " - " + e.toString(), e);
    }
  }

  /**
   * Create prepared statement to write a batch of records.
   * @param data Non-null data Object[] of records to be written.
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
   * @param data Object[] containing records to be written.
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
    StringBuffer sql=new StringBuffer("INSERT INTO "+quoteIdentifier(tableName)+"(");
    StringBuffer params=new StringBuffer();
    for (int i=0;i<columnNames.length;i++) {
      sql.append(quoteIdentifier(columnNames[i])+",");
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

  protected String quoteIdentifier(String identifier) {
    if ((identifier!=null) && (quoteIdentifiers)) {
      return identifierQuoteOpen+identifier+identifierQuoteClose;
    }
    else {
      return identifier;
    }
  }

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

  /**
   * Get the names of the columns of a given table.
   * @param tableName
   * @param connection
   * @return List containing the column names for a given table
   * @throws SQLException
   */
  protected List getTableColumnNames(String tableName, Connection connection) throws SQLException {
    //Execute a dummy sql statement against database to collect table metadata
    String sql= "SELECT * FROM "+quoteIdentifier(tableName)+" WHERE 1=2";
    Statement s = connection.createStatement();
    log.debug("Executing SQL: " + sql);
    ResultSet rs=s.executeQuery(sql);
    ResultSetMetaData rsmd=rs.getMetaData();
    List names=new ArrayList(rsmd.getColumnCount());

    for (int i=0;i<rsmd.getColumnCount();i++) {
      names.add(rsmd.getColumnName(i+1));
    }
    return names;
  }

  /**
   * Generate the SQL for a stored procedure call.
   * It will add placeholders for the required number of arguments also.
   * @param procName The name of the stored procedure to be used
   * @return String containing an SQL call ready for compilation as a PreparedStatement
   */
  protected String generateStoredProcSQL(String procName,int[] sqlTypes) {
    StringBuffer sqlString = new StringBuffer("{ CALL "+ procName + "(");
    int args=sqlTypes.length;// Only need the number of args.
    for (int i=0;i<args;i++) {
      sqlString.append("?,");
    }
    if (args>0) { //Drop the last comma.
      sqlString.deleteCharAt(sqlString.length()-1);
    }
    sqlString.append(")}");
    return sqlString.toString();
  }

  /**
   * Get the types of the args of a stored proc.
   * <br>
   * From javadoc on DatabaseMetaData.getProcedureColumns()
   * <pre>
   * 1. PROCEDURE_CAT String => procedure catalog (may be null)
   * 2. PROCEDURE_SCHEM String => procedure schema (may be null)
   * 3. PROCEDURE_NAME String => procedure name
   * 4. COLUMN_NAME String => column/parameter name
   * 5. COLUMN_TYPE Short => kind of column/parameter:
   *        * procedureColumnUnknown - nobody knows
   *        * procedureColumnIn - IN parameter
   *        * procedureColumnInOut - INOUT parameter
   *        * procedureColumnOut - OUT parameter
   *        * procedureColumnReturn - procedure return value
   *        * procedureColumnResult - result column in ResultSet 
   * 6. DATA_TYPE int => SQL type from java.sql.Types
   * 7. TYPE_NAME String => SQL type name, for a UDT type the type name is fully qualified
   * 8. PRECISION int => precision
   * 9. LENGTH int => length in bytes of data
   *10. SCALE short => scale
   *11. RADIX short => radix
   *12. NULLABLE short => can it contain NULL.
   *        * procedureNoNulls - does not allow NULL values
   *        * procedureNullable - allows NULL values
   *        * procedureNullableUnknown - nullability unknown 
   *13. REMARKS String => comment describing parameter/column 
   *
   *</pre>
   *
   */
  protected int[] getStoredProcArgumentTypes(String storedProcName,Connection connection) throws SQLException {
    //Fix for #SC36: MapCallableStatementWriter misses first argument for Oracle databases
    // Now it checks each columnType, and only includes IN or INOUT types.
    // ToDo: Further validation of this approach. Perhaps OUT should also be included?
    DatabaseMetaData dmd = connection.getMetaData();
    List sqlTypeList=new ArrayList();
    String catalog=connection.getCatalog();
    String schema="%";
    String proc=storedProcName;
    String column="%";
    
    log.debug("Catalog for stored proc "+storedProcName+" is "+catalog);
    ResultSet rs;
    if ((catalog==null) &&
        (dmd.getDatabaseProductName().toLowerCase().indexOf("oracle")>=0)) {
      //Oracle doesn't bother with catalogs at all :-(
      //Thus if it's an oracle db, we may need to substitute package name instead
      //of catalog.
      if (oraclePackage!=null) {
        log.debug("Setting catalog to oracle package of"+oraclePackage);
        catalog=oraclePackage;
        schema=null;//Oracle 'ignore' setting. Probably the same as "%" anyway.
      }
    }
    //Check if there's a schema reference in the proc name...
    String[] components=storedProcName.split("\\.");
    int len=components.length;
    if (len>1) {
      schema=components[len-2];
      proc=components[len-1];
    }
    log.debug("Resolving proc - catalog="+catalog+";schema="+schema+";proc="+proc+";column="+column);
    rs = dmd.getProcedureColumns(catalog,schema,proc,column);
    //If RS is empty, then we have failed in our mission.
    if (!rs.next()) { //First rs is return value.
      rs.close();
      String msg="Failed to lookup stored procedure " +storedProcName;
      log.warn(msg);
      throw new SQLException(msg);
    }
    do { //Verify that each argument is an IN or INOUT arg type.
      int type=rs.getInt(5); //Need to check if it is a result, or an input arg.
      int dataType=rs.getInt(6); // DATA_TYPE is column six!
      if (log.isDebugEnabled()) {
        log.debug("Catalog="+rs.getString(1)+
            "; Schema="+rs.getString(2)+
            "; Proc="+rs.getString(3)+ 
            "; Column=" + rs.getString(4) + 
            "; ParamType="+spTypeToString(type)+"("+type+")"+
            "; DataType=" + dataType + 
            "; TypeName=" + rs.getString(7));
      }     
      if (type==DatabaseMetaData.procedureColumnIn || type==DatabaseMetaData.procedureColumnInOut) {
        log.debug("Argument of type "+type+" is IN or INOUT");
        sqlTypeList.add(Integer.valueOf(dataType)); // DATA_TYPE is column six!
      }
      else {
        log.debug("Ignoring column of type " +type+" as it is neither IN nor INOUT");
      }

    }
    while (rs.next());

    log.debug("Number of stored procedure parameters found: " + sqlTypeList.size());
    int[] sqlTypes=new int[sqlTypeList.size()];
    for (int i=0;i<sqlTypes.length;i++) {
      sqlTypes[i]=((Integer)sqlTypeList.get(i)).intValue();
    }
    rs.close(); 
    return sqlTypes;
  }
 
  protected void logDBInfo(DatabaseMetaData dmd) throws SQLException {
    if (debug_db_version_not_logged && log.isDebugEnabled()) {
      String productName=dmd.getDatabaseProductName();
      try {
        log.debug("DB Name (version major/minor): "+productName+" ("+dmd.getDatabaseMajorVersion()+"/"+dmd.getDatabaseMinorVersion()+")");
      }
      catch (AbstractMethodError ame) { //Sybase jconn2 driver doesn't implement the maj/min methods.
        log.debug("DB Name: "+productName);
      }
      log.debug("DB Version: "+dmd.getDatabaseProductVersion());
      debug_db_version_not_logged=false; //Don't report it any more.
    }
  }

  private static final String spTypeToString(int type) {
    String result;
    switch(type) {
    case DatabaseMetaData.procedureColumnUnknown: // 0
      result="unknown";
      break;
    case DatabaseMetaData.procedureColumnIn: // 1
      result="IN";
      break;
    case DatabaseMetaData.procedureColumnInOut: // 2
      result="INOUT";
      break;
    case DatabaseMetaData.procedureColumnOut: //3
      result="OUT";
      break;
    case DatabaseMetaData.procedureColumnReturn: // 4
      result="RETURN";
      break;
    case DatabaseMetaData.procedureColumnResult: // 5
      result="RESULT";
      break;
    default: //This *should* never arise
      result="Illegal value for procedureColumn type";
    break;
    }
    return result;
  }

  /**
   * Set the names of the columns to be used when writing output rows.
   * <br>
   * For concrete subclasses that write via {@link CallableStatement}s
   * this property is optional.
   * <br>
   * For concrete subclasses that write to tables, this property is mandatory 
   * for Maps, but optional for orderedMaps. If it is unspecified, the 
   * incoming ordered map fields must correspond exactly to output fields.
   * @param columns
   */
  public void setOutputColumns(final List columns) {
    if (columns==null || columns.isEmpty()) {
      outputColumns=null;
    } else {
      this.outputColumns=(String[])columns.toArray(new String[columns.size()]);
    }
  }

  /**
   * Returns the names of the columns which are written on output.
   * @return Unmodifiable List with names of the output columns.
   */
  public List getOutputColumns() {
    return Collections.unmodifiableList(Arrays.asList(outputColumns));
  }
}

