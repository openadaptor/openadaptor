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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.Component;

import java.sql.*;
import java.util.List;
import java.util.Map;


/**
 * Writer which will call a stored procedure to write records to a 
 * database.
 * 
 * Note that outputColumns is mandatory unless all records will be 
 * IOrderedMap instances. This is because the order of fields for
 * other Maps is not defined, meaning that it would not be possible
 * to appropriately match fields to stored procecure parameters.
 * 
 * @author higginse
 * @since 3.2.2
 */
public class MapCallableStatementWriter extends AbstractMapWriter {

  private static final Log log = LogFactory.getLog(MapCallableStatementWriter.class);

  private String procName;
  private int argCount=0;

  public void setCallableStatement(String procName) {
    this.procName = procName;
  }

  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, Component comp) {
    if ( procName == null || procName.equals("") ){
      exceptions.add(new ValidationException("The [procName] property must be supplied", comp));
    }
    if ( outputColumns == null){
      log.warn("outputColumns undefined - records *MUST* be IOrderedMap instances");
    }

  }

  /**
   * This creates a reusable Prepared Statement for inserts into the configured table.
   */
  protected PreparedStatement initialiseReusablePreparedStatement() {
    PreparedStatement reusablePreparedStatement=null;
    log.info("Initialising prepared statement for "+procName);
    try {
      argCount=getStoredProcArgumentCount(procName, connection);
      if (outputColumns!=null) {
        if (argCount != outputColumns.length){
          throw new SQLException("Proc expects "+argCount+" arguments, but outputColumns contains "+outputColumns.length);
        }
      }
      String sql=generateStoredProcSQL(procName,argCount);
      reusablePreparedStatement=connection.prepareStatement(sql);
      log.debug("Reusable prepared statement is: " +sql);
      //Load bean properties with database metadata values
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create prepared statement for proc " +procName+" - " + e.toString(), e);
    }
    return reusablePreparedStatement;
  }

  /**
   * Configures reusable prepared statement with values from supplied map.
   */
  protected PreparedStatement createStatement(Map map) throws SQLException {
    reusablePreparedStatement.clearParameters();
    setArguments(reusablePreparedStatement,map);
    return reusablePreparedStatement;
  }

  /**
   * Configures reusable prepared statement as batch using values from supplied map[]
   */
  public PreparedStatement createBatchStatement(Map[] maps) throws SQLException {
    log.debug("Creating batch prepared statement for "+maps.length+" records");
    for (int m=0;m<maps.length;m++){
      reusablePreparedStatement.clearParameters();
      setArguments(reusablePreparedStatement,maps[m]);
      reusablePreparedStatement.addBatch();
    }
    return reusablePreparedStatement;
  }

  /**
   * Assigns arguments to a PreparedStatement from an incoming Map.
   * <br>
   * If outputColumns has been set, then it will extract those
   * columns from the incoming map; otherwise it assumes that all
   * fields in the incoming map will be used
   * @param ps
   * @param map
   * @throws SQLException
   */
  private void setArguments(PreparedStatement ps,Map map) throws SQLException {
    if (outputColumns==null) { //Expect Map to contain correct args.
      if (!(map instanceof IOrderedMap)) { //Only OrderedMaps will work
        throw new SQLException("Map is not an IOrderedMap instance - outputColumns must be specified");
      }
      int mapSize=map.size();
      if (argCount!=mapSize) {
        throw new SQLException("Expected "+argCount+" arguments, but map contains "+mapSize);
      }
      IOrderedMap om=(IOrderedMap)map;
      for (int i=0;i<mapSize;i++) {
        Object value=om.get(i);
        ps.setObject(i+1, value);
      }
    }
    else {//Use output columns fram map. Expects them to match.
      for (int i=0;i<outputColumns.length;i++) {
        String colName=outputColumns[i];
        ps.setObject(i+1, colName==null?null:map.get(colName));
      }
    }
  }
  /**
   * Generate the SQL for a stored procedure call.
   * It will add placeholders for the required number of arguments also.
   * @param procName The name of the stored procedure to be used
   * @param argCount The number of arguments expected by the proc.
   * @return String containing an SQL call ready for compilation as a PreparedStatement
   */
  private String generateStoredProcSQL(String procName,int argCount) {
    StringBuffer sqlString=new StringBuffer("{ CALL "+ procName + "(");
    for (int i=0;i<argCount;i++) {
      sqlString.append("?,");
    }
    if (argCount>0) {
      
      sqlString.deleteCharAt(sqlString.length()-1);
    }
    sqlString.append(")}");
    return sqlString.toString();
  }
}
