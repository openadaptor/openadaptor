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

package org.openadaptor.auxil.connector.jdbc.writer.deprecated.orderedmap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Use a prepared statement to convert ordered maps into database insertions.
 * 
 * 
 * @author higginse
 * @deprecated Use MapTableWriter instead
 */
public class PreparedStatementTableConverter extends AbstractStatementConverter {

  private static final Log log = LogFactory.getLog(PreparedStatementTableConverter.class);

  private String tableName="";
  private int[] outputTypes;
  private List outputColumns;

  private PreparedStatement reusablePreparedStatement;
  
  public PreparedStatementTableConverter() {
  }

  public PreparedStatementTableConverter(final String tableName) {
    this.tableName = tableName;
  }

  public PreparedStatementTableConverter(final String tableName, final List outputColumns) {
    this.tableName = tableName;
    this.outputColumns = outputColumns;
  }

  public void setOutputColumns(final List columns) {
    this.outputColumns=columns;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  public boolean preparedStatementIsReusable() {
    return true;
  }


  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, Component comp) {
    if ( "".equals(tableName) )
      exceptions.add(new ValidationException("The [tableName] property must be supplied", comp));

    if ( outputColumns == null || outputColumns.size() == 0 )
      log.info("No output columns defined. The attribute names in the ordered map must match the column names in the database");
  }


  public PreparedStatement convert(IOrderedMap map, Connection connection) {
    try {
      reusablePreparedStatement.clearParameters();
      for (int i=0;i<outputColumns.size();i++) {
        Object value=map.get(i);
       reusablePreparedStatement.setObject(i+1, value,outputTypes[i]);
      }
      return reusablePreparedStatement;
    } catch (SQLException e) {
      throw new RuntimeException("SQLException " + e.getMessage(), e);
    }
  }

  public void initialise(Connection connection) {
    log.info("Initialising prepared statement for insertion into "+tableName+"...");
    try {
      //Load bean properties with database metadata values
      if ((outputColumns==null) || outputColumns.isEmpty()) {
        setOutputColumns(getTableColumnNames(tableName, connection));
      }
      outputTypes=getPreparedStatementTypes(tableName, connection, outputColumns);
      reusablePreparedStatement=generatePreparedStatement(connection, tableName, outputColumns);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get sql types information for target table, " + e.toString(), e);
    }
  }
 
  private PreparedStatement generatePreparedStatement(Connection connection,String tableName,List columnNames) throws SQLException {
    StringBuffer sql=new StringBuffer("INSERT INTO "+tableName+"(");
    StringBuffer params=new StringBuffer(); 
    for (int i=0;i<columnNames.size();i++) {
      sql.append(columnNames.get(i)+",");
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

  private int[] getPreparedStatementTypes(String tableName, Connection connection,List columnNames) throws SQLException {
    //Execute a "null" sql statement against database to collect table metadata
    String sql= "select * from "+tableName+" where 1=2";
    Statement s = connection.createStatement();
    log.debug("Executing SQL: " + sql);
    ResultSet rs=s.executeQuery(sql);
    int[] types;
    ResultSetMetaData rsmd=rs.getMetaData();
    int cols=rsmd.getColumnCount();
    types=new int[columnNames.size()];
    int mapped=0;
    for (int i=0;i<cols;i++) {
      int type=rsmd.getColumnType(i+1);
      String name=rsmd.getColumnName(i+1);
      int location=columnNames.indexOf(name);
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
  private List getTableColumnNames(String tableName, Connection connection) throws SQLException {
    //Execute a "null" sql statement against database to collect table metadata
    String sql= "select * from "+tableName+" where 1=2";
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

}
