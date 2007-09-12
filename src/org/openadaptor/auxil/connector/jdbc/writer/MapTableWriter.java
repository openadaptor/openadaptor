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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ValidationException;

/**
 * This writes IOrderedMap records to JDBC database tables.
 * <br>
 * It will match fields in incoming IOrderedMaps to columns in the
 * configured database table, and write the values as rows in the
 * output database table.
 *
 */
public class MapTableWriter extends AbstractMapWriter {
  private static final Log log = LogFactory.getLog(MapTableWriter.class);

  private String tableName;
  //private int[] outputTypes; //This will cache the output types for each column.

  /**
   * Set the name of the database table to which data rows are to be inserted.
   * <br>
   * This is mandatory.
   * @param tableName
   */
  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }
  /**
   * Returns the name of the database table where data is to be written
   * @return
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * This creates a reusable Prepared Statement for inserts into the configured table.
   */
  protected PreparedStatement initialiseReusablePreparedStatement() {
    PreparedStatement reusablePreparedStatement=null;
    log.info("Initialising prepared statement for insertion into "+tableName+"...");
    try {
      //Load bean properties with database metadata values
      if (outputColumns==null) {
        setOutputColumns(getTableColumnNames(tableName, connection));
      }
      //outputTypes=getPreparedStatementTypes(tableName, connection, outputColumns);
      reusablePreparedStatement=generatePreparedStatement(connection, tableName, outputColumns);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialise information for target table, " + e.toString(), e);
    }
    return reusablePreparedStatement;
  }

  /**
   * Configures reusable prepared statement with values from supplied map.
   */
  protected PreparedStatement createStatement(Map map) throws SQLException {
    reusablePreparedStatement.clearParameters();
    for (int i=0;i<outputColumns.length;i++) {
      Object value=map.get(outputColumns[i]);
      reusablePreparedStatement.setObject(i+1, value);//,outputTypes[i]);
    }
    return reusablePreparedStatement;
  }

  /**
   * Configures reusable prepared statement as batch using values from supplied map[]
   *
   */
  public PreparedStatement createBatchStatement(Map[] maps) throws SQLException {
    log.debug("Creating batch prepared statment for "+maps.length+" records");
    int outputColumnsLength=outputColumns.length;
    for (int m=0;m<maps.length;m++){
      reusablePreparedStatement.clearParameters();
      Map map=maps[m];
      for (int i=0;i<outputColumnsLength;i++) {
        Object value=map.get(outputColumns[i]);//ToDo: Convert to Array instead of list.
        reusablePreparedStatement.setObject(i+1, value);//,outputTypes[i]);
      }
      reusablePreparedStatement.addBatch();
    }
    return reusablePreparedStatement;
  }

  /**
   * Check that tableName has been assigned.
   */
  public void validate(List exceptions, Component comp) {
    super.validate(exceptions,comp);
    if ( (tableName==null) || ("".equals(tableName.trim())) ) {
      log.debug("Mandatory property tableName has not been supplied");
      exceptions.add(new ValidationException("Property [tableName] property is mandatory", comp));
    }
    if ( outputColumns == null){
      log.info("outputColumns undefined - columns will be derived from db table metadata");
    }
  }
}
