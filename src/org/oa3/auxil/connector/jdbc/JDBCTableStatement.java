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
package org.oa3.auxil.connector.jdbc;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCTableStatement.java,v 1.3 2006/11/04 23:32:44 ottalk Exp $
 * Rev:  $Revision: 1.3 $
 * Created Oct 30, 2006 by Kuldip Ottal
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.core.exception.OAException;

import java.sql.*;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements the <code>IJDBCStatement</code> interface.
 * The class writes directly to a database table. It uses the supplied JDBC connection to
 * gather information about the database table and then generates and executes the sql required.
 *
 */
public class JDBCTableStatement implements IJDBCStatement, IJDBCConstants {

  private static final Log log = LogFactory.getLog(JDBCTableStatement.class.getName());

  private String tableName="";
  private int[] tableColumnTypes;
  private String[] tableColumnNames;
  private Map omKeyToDBTableColumnMapping;

  //Constructors
  public JDBCTableStatement(String tableName, Object mapping, Connection connection ) {
    initialiseStatement(tableName,null,mapping,connection);
  }

  //Methods

  /**
   * This method loads the properties with table metadata from
   * the target database using the supplied JDBC connection
   *
   * @param tableName
   * @param delimiter Not currently used
   * @param connection JDBC connection
   * @throws org.oa3.core.exception.OAException
   */
  public void initialiseStatement(String tableName, String delimiter,Object mapping,Connection connection) throws OAException {
    this.tableName = tableName;
    log.info("Getting sql types information for target table...");
    try {
      //Set mapping property
      omKeyToDBTableColumnMapping = (Map) mapping;
      //Load bean properties with database metadata values
      getDBTableDetails(tableName, connection);
    } catch (SQLException sqle) {
      throw new OAException("Failed to sql types information for target table, " + sqle.toString(), sqle);
    }
  }

  /**
   * This method calls the <code>generateSql</code> method to generate the sql required to insert data
   * from the supplied ordered map. It then creates a prepared statement, sets the fields using metadata
   * contained in the property <code>tableColumnTypes</code> . It then executes the prepared statement and
   * returns the number of rows updated.
   *
   * @param om Ordered Map
   * @param connection JDBC connection
   * @return int Number of rows updated
   * @throws OAException
   */
  public int executeStatement(IOrderedMap om,Connection connection) throws OAException {

    //TODO: Add in jdbc batch functionality
    int updateCount=0;
    PreparedStatement ps;
    IOrderedMap mappedOM;
    String sql="";
    try {
      mappedOM = mapDBTableColumns(om);
      //Generate SQL
      sql = generateSql(mappedOM);
      log.debug("executeStatement: sql = " + sql);
      //Create Prepared Statement
      ps = connection.prepareStatement(sql);
      //Set Prepared Statement fields
      for (int i=0;i<mappedOM.size();i++) {
        Object value=mappedOM.get(i);
        log.debug("Value to be written is "+value);
        ps.setObject(i+1,value,tableColumnTypes[i]);
      }
      log.info("Executing prepared statement to insert data: " + sql);
      //Execute Prepared statement
      updateCount += ps.executeUpdate();
    } catch (SQLException sqle) {
      throw new OAException(sqle.getMessage(),sqle);
    }
    return updateCount;
  }

  /**
   * This method generates a sql statement using the keys in the ordered map supplied
   *
   * @param om
   * @return String sql statement
   */
  private String generateSql(IOrderedMap om) {
    //Define the base sql
    String sql="insert into " + tableName ;
    //Define iterators
    List keys = om.keys();
    Iterator it = keys.iterator();
    //Generate sql for table columns and fields into which data will be inserted
    StringBuffer columnNames=new StringBuffer(" (");
    StringBuffer  fields=new StringBuffer(" (");
    while (it.hasNext()) {
      columnNames.append(((String) it.next()) + ",");
      fields.append("?,");
    }
    //Remove trailing comma
    columnNames.deleteCharAt(columnNames.length()-1);
    fields.deleteCharAt(fields.length()-1);
    columnNames.append(") ");
    fields.append(") ");
    //Combine sql to produce the final statement
    sql += columnNames.toString() + " values " + fields;
    return sql;
  }

  /**
   * This method returns column information for the specified table
   *
   * @param tableName
   * @throws SQLException
   */
  private void getDBTableDetails(String tableName, Connection connection) throws SQLException {
    //Execute a "null" sql statement against database to collect table metadata
    String sql= "select * from "+tableName+" where 1=2";
    Statement s = connection.createStatement();
    log.debug("Executing SQL: " + sql);
    ResultSet rs=s.executeQuery(sql);
    ResultSetMetaData rsmd=rs.getMetaData();
    //Retrieve table column names and table column types from metadata
    tableColumnTypes=new int[rsmd.getColumnCount()];
    tableColumnNames=new String[rsmd.getColumnCount()];
    for (int i=0;i<rsmd.getColumnCount();i++){
      tableColumnNames[i]=rsmd.getColumnName(i+1);
      tableColumnTypes[i]=rsmd.getColumnType(i+1);
      log.debug("Column["+(i+DB_COLUMN_OFFSET)+"] name '" + tableColumnNames[i] + "' has type: "+tableColumnTypes[i]+". Java class is "+rsmd.getColumnClassName(i+DB_COLUMN_OFFSET));
    }
  }

  /**
   * Map ordered map keys to database table parameters using <code>omKeyToDBTableColumnMapping</code> object configured in
   * configuration file
   *
   * @param om  Ordered Map
   * @return IOrderedMap Mapped Ordered Map
   */
  public IOrderedMap mapDBTableColumns(IOrderedMap om) {
    if (omKeyToDBTableColumnMapping == null) {
      return om;
    } else {
      IOrderedMap mappedOM = new OrderedHashMap();
      Iterator it = omKeyToDBTableColumnMapping.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        if (om.containsKey(pairs.getKey())) {
          log.debug("mapped " + pairs.getKey() + " to " + omKeyToDBTableColumnMapping.get(pairs.getKey()));
          mappedOM.put(omKeyToDBTableColumnMapping.get(pairs.getKey()),om.get(pairs.getKey()));
        }
      }
      return mappedOM;
    }
  }
}
