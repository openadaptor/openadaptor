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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for writing Maps to JDBC databases
 * @author higginse
 * @since 3.2.2
 */
public abstract class AbstractMapWriter extends AbstractSQLWriter {

  protected String[] outputColumns;

  /**
   * Set the names of the columns to be used when writing output rows.
   * <br>
   * Mandatory for Maps, but optional for orderedMaps if unspecified, 
   * the incoming ordered map fields must 
   * correspond exactly to output fields.
   * @param columns
   */
  public void setOutputColumns(final List columns) {
    if (columns==null || columns.isEmpty()) {
      outputColumns=null;
    }
    else {
      this.outputColumns=(String[])columns.toArray(new String[columns.size()]);
    }
  }
  /**
   * Returns the names of the columns which are written on output.
   * @return
   */
  public List getOutputColumns() {
    return Collections.unmodifiableList(Arrays.asList(outputColumns));
  }

  /**
   * Create prepared statement to write a batch of  maps.
   * @param data Object[] which should contain Map instances.
   * @return PreparedStatement ready for execution.
   * @throws RuntimeException if supplied array is null or does not contain Maps
   * @throws SQLException
   */
  protected final PreparedStatement createBatchStatement(Object[] data) throws SQLException {
    if (data==null) {
      throw new RuntimeException("Null data provided");
    }
    Map[] maps=new Map[data.length];
    for (int i=0;i<maps.length;i++) {
      Object datum=data[i];
      if (! (datum instanceof Map)){
        throw new RuntimeException("Batch element ["+i+"]. Expected Map. Got - "+data.getClass());
      }
      maps[i]=(Map)datum;
    }
    return createBatchStatement(maps);
  }

  /**
   * Create prepared statement to write a single ordered map.
   * @param Object which should contain an IOrderedMap instance
   * @return PreparedStatement ready for execution
   * @throws RuntimeException if supplied Object is null or does not contain IOrderedMaps
   * @throws SQLException
   */
  protected final PreparedStatement createStatement(Object datum) throws SQLException {
    if (datum==null) {
      throw new RuntimeException("Null data provided");
    }
    if (! (datum instanceof Map)){
      throw new RuntimeException("Expected Map. Got - "+datum.getClass());
    }
    return createStatement((Map)datum);
  }

  /**
   * Create prepared statement to write a batch of ordered maps.
   * @param Non-null IOrderedMap[]
   * @return PreparedStatement ready for execution.
   * @throws SQLException
   */
  protected abstract PreparedStatement createBatchStatement(Map[] data) throws SQLException;

  /**
   * Create prepared statement to write a single ordered map.
   * @param Non-null IOrderedMap instance
   * @return PreparedStatement ready for execution
   * @throws SQLException
   */
  protected abstract PreparedStatement createStatement(Map datum) throws SQLException;


}
