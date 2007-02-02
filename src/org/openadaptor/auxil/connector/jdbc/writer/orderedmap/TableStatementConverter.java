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

package org.openadaptor.auxil.connector.jdbc.writer.orderedmap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;


public class TableStatementConverter extends AbstractStatementConverter {

  private static final Log log = LogFactory.getLog(TableStatementConverter.class);

  private static final int DB_COLUMN_OFFSET = 1;

  private String tableName="";
  private int[] tableColumnTypes;
  private String[] tableColumnNames;
  private Map mapping;

  public TableStatementConverter() {
  }
  
  public TableStatementConverter(final String tableName) {
    this.tableName = tableName;
  }
  
  public TableStatementConverter(final String tableName, final Map mapping) {
    this.tableName = tableName;
    this.mapping = mapping;
  }
  
  public void setMapping(final Map mapping) {
    this.mapping = mapping;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  public PreparedStatement convert(IOrderedMap map, Connection connection) {
    PreparedStatement ps;
    IOrderedMap mappedOM;
    String sql="";
    try {
      mappedOM = mapDBTableColumns(map);
      //Generate SQL
      sql = generateSql(mappedOM);
      //Create Prepared Statement
      ps = connection.prepareStatement(sql);
      //Set Prepared Statement fields
      for (int i=0;i<mappedOM.size();i++) {
        Object value=mappedOM.get(i);
        ps.setObject(i+1,value,tableColumnTypes[i]);
        if (log.isDebugEnabled()) {
          sql = sql.replaceFirst("\\?", getDebugValueString(value, tableColumnTypes[i]));
        }
      }
      log.debug("INSERT SQL = " + sql);
      return ps;
    } catch (SQLException e) {
      throw new RuntimeException("SQLException " + e.getMessage(), e);
    }
  }

  public void initialise(Connection connection) {
    log.info("Getting sql types information for target table...");
    try {
      //Load bean properties with database metadata values
      getDBTableDetails(tableName, connection);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to sql types information for target table, " + e.toString(), e);
    }
  }

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

  public IOrderedMap mapDBTableColumns(IOrderedMap om) {
    if (mapping == null) {
      return om;
    } else {
      IOrderedMap mappedOM = new OrderedHashMap();
      Iterator it = mapping.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        if (om.containsKey(pairs.getKey())) {
          log.debug("mapped " + pairs.getKey() + " to " + mapping.get(pairs.getKey()));
          mappedOM.put(mapping.get(pairs.getKey()),om.get(pairs.getKey()));
        }
      }
      return mappedOM;
    }
  }

}
