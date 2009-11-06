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

package org.openadaptor.auxil.connector.jdbc.writer.map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.IComponent;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Writer which will call a stored procedure to write records to a 
 * database.
 * <p>
 * Note that outputColumns is mandatory unless all records will be 
 * IOrderedMap instances. This is because the order of fields for
 * other Maps is not defined, meaning that it would not be possible
 * to appropriately match fields to stored procedure parameters.
 * 
 * @author higginse
 * @since 3.2.2
 */
public class MapCallableStatementWriter extends AbstractMapWriter {

  private static final Log log = LogFactory.getLog(MapCallableStatementWriter.class);

  private String procName;

  /**
   * This should contain the name of the stored procedure to be executed.
   * Generally may also be prefixed with a schema name (e.g. myschema.myproc) but
   * this behaviour is db vendor dependent.
   * Note also that Oracle's package.proc syntax will not work - use packageName property
   * instead to specify a package name.
   * @param procName Name of a stored procedure to execute.
   */
  public void setCallableStatement(String procName) {
    this.procName = procName;
  }
  
  /**
   * Optional argument to specify a package name for a CallableStatement.
   * Optional, and only makes sense for Oracle databases.
   * 
   * @param oraclePackageName - String containing the name of package to which
   *                            the stored procedure belongs.
   */
  public void setPackageName(String oraclePackageName) {
    this.oraclePackage=oraclePackageName;
  }

  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, IComponent comp) {
    if ( procName == null || procName.equals("") ){
      exceptions.add(new ValidationException("The [procName] property must be supplied", comp));
    }
    if ( outputColumns == null){
      log.warn("outputColumns undefined - records *MUST* be IOrderedMap instances");
    }

  }

  /**
   * This creates a reusable Prepared Statement for calls to the configured stored proc.
   */
  protected void initialiseReusablePreparedStatement() {
    initialiseReusableStoredProcStatement(procName);
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
    String[] colNames=outputColumns;
    if (colNames==null) { //Expect Map to contain correct args.
      if (!(map instanceof IOrderedMap)) { //Only OrderedMaps will work
        throw new SQLException("Map is not an IOrderedMap instance - outputColumns must be specified");
      }
      int mapSize=map.size();
      if (argSqlTypes.length !=mapSize) {
        throw new SQLException("Expected "+argSqlTypes.length+" arguments, but map contains "+mapSize);
      }

      IOrderedMap om=(IOrderedMap)map;
      colNames=(String[])om.keys().toArray(new String[om.size()]); 
    }
    setArguments(ps,map,colNames,argSqlTypes);
  }
}
