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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.Component;

import java.sql.*;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @deprecated Use MapCallableStatementWriter instead
 */
public class CallableStatementConverter extends AbstractStatementConverter {

  private static final Log log = LogFactory.getLog(CallableStatementConverter.class);

  private String sql = "";
  private int[] paramTypes;
  private List mapping;
  private String procName;

  public void setProcName(String procName) {
    this.procName = procName;
  }

  public void setMapping(List mapping) {
    this.mapping = mapping;
  }

  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, Component comp) {
    if ( procName == null || procName.equals("") )
      exceptions.add(new ValidationException("The [procName] property must be supplied", comp));

    if ( mapping == null || mapping.size() == 0 )
      log.info("No field mapping defined. The attribute names in the ordered map must match the column names in the database");
  }


  public PreparedStatement convert(IOrderedMap om, Connection connection) {
    CallableStatement cs;
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
        //EH 2007_08_24 This doesn't work properly, but more importantly isn't necessary.
        //openadaptor3 should be delegating type conversion completely to the database driver,
        //i.e. pure call to setObject without specifying the sql type.
        //cs.setObject(i+1,value,paramTypes[i]);
        cs.setObject(i+1,value);
        if (log.isDebugEnabled()) {
          generatedSql = generatedSql.replaceFirst("\\?", getDebugValueString(value, paramTypes[i]));
        }
      }
      log.debug("generated SQL = " + generatedSql);
      return cs;
    } catch (SQLException e) {
      throw new RuntimeException("SQLException, " + e.getMessage(), e);
    }
  }

  public void initialise(Connection connection) {
    log.info("Getting parameter information for stored proc '" + procName + "' ...");

    try {
      //Load properties with stored procedure metadata values
      paramTypes = getTargetStoredProcTypes(procName, connection);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to sql types information for target table, " + e.getMessage(), e);
    }
    sql ="{ call "+ procName + "(";
  }

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

  private String statementParameters(int argCount) {
    StringBuffer sqlString=new StringBuffer();
    for (int i=0;i<argCount;i++) {
      sqlString.append("?,");
    }
    sqlString.deleteCharAt(sqlString.length()-1);
    sqlString.append(")}");
    return sqlString.toString();
  }

  public IOrderedMap mapStoredProcParameters(IOrderedMap om) {
    if (mapping == null) {
      return om;
    } else {
      IOrderedMap mappedOM = new OrderedHashMap() ;
      int paramCount=0;
      Iterator it = mapping.iterator();
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
