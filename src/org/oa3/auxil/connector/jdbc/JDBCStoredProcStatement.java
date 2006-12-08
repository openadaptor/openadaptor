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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCStoredProcStatement.java,v 1.6 2006/11/30 15:38:05 ottalk Exp $
 * Rev:  $Revision: 1.6 $
 * Created Oct 30, 2006 by Kuldip Ottal
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.OAException;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;

import java.sql.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements the <code>IJDBCStatement</code> interface.
 * The class writes data to the database using a stored procedure. It uses the supplied JDBC connection to
 * gather information about the stored procedure and then generates and executes the sql required.
 */
public class JDBCStoredProcStatement implements IJDBCStatement,IJDBCConstants {

  private static final Log log = LogFactory.getLog(JDBCStoredProcStatement.class.getName());

  private String sql="";
  private int[] storedProcTypes;
  private List omKeyToStoredProcParameterMapping;

  //Accessors
  public String getSql() {
    return sql;
  }

  //Constructors
  public JDBCStoredProcStatement(String storedProcName, Object mapping, Connection connection ) {
    initialiseStatement(storedProcName,null,mapping,connection);
  }

  //Methods

  /**
   * Initialises instance by setting properties using parameters and database.
   *
   * @param storedProcName
   * @param delimiter Not used
   * @param mapping Mapping object created from configuration file
   * @param connection JDBC Connection
   * @throws OAException
   */
  public void initialiseStatement(String storedProcName,String delimiter, Object mapping, Connection connection) throws OAException {
    log.info("Getting parameter information for stored proc '" + storedProcName + "' ...");

    try {
      //Set mapping property
      omKeyToStoredProcParameterMapping = (List) mapping;
      //Load properties with stored procedure metadata values
      storedProcTypes = getTargetStoredProcTypes(storedProcName, connection);
    } catch (SQLException sqle) {
      throw new OAException("Failed to sql types information for target table, " + sqle.toString(), sqle);
    }
    sql ="{ call "+ storedProcName + "(";
  }

  /**
   *  This method enriches the generated sql statement with parameter values and types. It then executes the statement.
   *
   * @param om IOrderedMap
   * @param connection
   * @return int Number of updated database rows
   * @throws OAException
   */
  public int executeStatement(IOrderedMap om, Connection connection) throws OAException {
    CallableStatement cs;
    int updateCount=0;
    IOrderedMap mappedOM;
    String generatedSql=sql;
    try {
      mappedOM = mapStoredProcParameters(om);
      generatedSql += statementParameters(mappedOM.size());
      cs = connection.prepareCall(generatedSql);
      //Add om values as stored procedure parameters
      for (int i=0;i<mappedOM.size();i++) {
        Object value=mappedOM.get(i);
        log.debug("Value to be written is "+value);
        cs.setObject(i+1,value,storedProcTypes[i]);
      }
      log.info("Executing Callable statement (Stored Proc)  to insert data: " + generatedSql);
      updateCount += cs.executeUpdate();
    } catch (SQLException sqle) {
      throw new OAException(sqle.getMessage(),sqle);
    }
    //Return number of rows updated
    return updateCount;
  }

  /**
   * This method returns parameter type information for the specified stored procedure
   *
   * @param storedProcName
   * @return array of sql types (int values)
   * @throws java.sql.SQLException
   */
  private int[] getTargetStoredProcTypes(String storedProcName, Connection connection) throws SQLException {
    DatabaseMetaData dmd = connection.getMetaData();
    ResultSet rs = dmd.getProcedureColumns(connection.getCatalog(),"%",storedProcName,"%");
    // Some unattractive code to find out how many parameters have been returned....there must be a better way.
    int paramCount = 0;
    // Skip the first result set entry as it is the return value of the stored proc
    rs.next();
    while (rs.next()) {
      paramCount++;
      log.debug("Catalog = " + rs.getString(1) + ", Schema = " + rs.getString(2) + ", Proc Name = " + rs.getString(3) + ", Proc Column = " + rs.getString(4) + ", Sql Type = " + rs.getString(6) + ", Sql Type Name = " + rs.getString(7));
    }
    log.debug("Number of stored procedure parameters found: " + paramCount);
    int[] types=new int[paramCount];

    rs = dmd.getProcedureColumns(connection.getCatalog(),"%",storedProcName,"%");
    int i=0;
    // Skip the first result set entry as it is the return value of the stored proc
    rs.next();
    while (rs.next()) {
      types[i]=rs.getInt(6); // ResultSet - Column types
      //There is a bug with getColumnName in this situation, exception thrown when you look up type 93 (java.sql.Timestamp, datetime)
      if (types[i] != 93 && APPLY_STOREDPROC_METADATA_FIX) {
        log.info("Parameter ["+ (i+DB_COLUMN_OFFSET) +"] has type: "+ types[i] + ". Sql Type Name = " + rs.getString(7));
      } else {
        log.info("Parameter ["+ (i+DB_COLUMN_OFFSET) +"] has type: "+ types[i] + ". Sql Type Name = " + rs.getString(7));
      }
      i++;
    }
    rs.close();

    return types;
  }

  /**
   * Create parameter placeholder string which will be used to write data to database
   *
   * @param argCount Number of stored procedure parameters
   * @return String Parameter placeholder string
   * @throws SQLException
   */
  private String statementParameters(int argCount) throws SQLException {
    StringBuffer sqlString=new StringBuffer();
    for (int i=0;i<argCount;i++) {
      sqlString.append("?,");
    }
    sqlString.deleteCharAt(sqlString.length()-1);
    sqlString.append(")}");
    return sqlString.toString();
  }

  /**
   * Map ordered map keys to stored procedure parameters using <code>omKeyToStoredProcParameterMapping</code> object configured in
   * configuration file. If a key is specified but does not exist in the ordered map, it is added with a null value to preserve parameter ordering.
   *
   * @param om Ordered Map
   * @return IOrderedMap Processed OrderedMap
   */
  public IOrderedMap mapStoredProcParameters(IOrderedMap om) {
    if (omKeyToStoredProcParameterMapping == null) {
      return om;
    } else {
      IOrderedMap mappedOM = new OrderedHashMap() ;
      int paramCount=0;
      Iterator it = omKeyToStoredProcParameterMapping.iterator();
      while (it.hasNext()) {
        String parameter = (String)it.next();
        if (om.containsKey(parameter)) {
          mappedOM.put(parameter,om.get(parameter));
        } else {
          mappedOM.put(parameter,null);
        }
        paramCount++;
        log.debug("Parameter " + paramCount + " is " + parameter);
      }
      return mappedOM;
    }
  }
}
