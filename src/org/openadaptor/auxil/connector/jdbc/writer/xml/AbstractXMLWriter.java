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

package org.openadaptor.auxil.connector.jdbc.writer.xml;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openadaptor.auxil.connector.jdbc.writer.AbstractSQLWriter;

/**
 * Abstract superclass for writing XML {@link Document}s to JDBC databases.
 *
 * @author cawthorng
 */
public abstract class AbstractXMLWriter extends AbstractSQLWriter {

  /**
   * Populates the reusable prepared statement with batched values from the 
   * supplied data, which is assumed to be one or more {@link Document}s.
   * @see org.openadaptor.auxil.connector.jdbc.writer.AbstractSQLWriter#createBatchStatement(java.lang.Object[])
   */
  protected PreparedStatement createBatchStatement(Object[] data) throws SQLException {
    if (data == null) {
      throw new RuntimeException("Null data provided");
    }
    
    for (int i = 0; i < data.length; i++) {
      createStatement(data[i]);
      reusablePreparedStatement.addBatch();
    }
    
    return reusablePreparedStatement;
  }

  /**
   * Populates the reusable prepared statement with values from the supplied
   * datum, which is assumed to be a {@link Document}.
   * @see org.openadaptor.auxil.connector.jdbc.writer.AbstractSQLWriter#createStatement(java.lang.Object)
   */
  protected PreparedStatement createStatement(Object datum) throws SQLException {
    if (datum == null) {
      throw new RuntimeException("Null data provided");
    }
    
    if (!(datum instanceof Document)){
      throw new RuntimeException("Expected Document. Got - " + datum.getClass());
    }
    
    reusablePreparedStatement.clearParameters();
    Document doc = (Document) datum;
    Element root = doc.getRootElement();
    
    if (outputColumns != null) {
      populateStatementFromOutputColumns(root);
    } else {
      populateStatementFromElements(root);
    }
    
    return reusablePreparedStatement;
  }

  /*
   * Populates the prepared statement, using the outputColumns
   * property to determine the parameters and their order.
   */
  private void populateStatementFromOutputColumns(Element root) throws SQLException {
    for (int i = 0; i < outputColumns.length; i++) {
      Node node = root.selectSingleNode(outputColumns[i]);
      populateParameter(i+1, node != null ? node.getStringValue() : null);
    }
  }
  
  /*
   * Populates the prepared statement, using the ordering of the
   * XML elements to determine the parameters and their order.
   */
  private void populateStatementFromElements(Element root) throws SQLException {
    for (int i = 0; i < root.nodeCount(); i++) {
      Node node = root.node(i);
      populateParameter(i+1, node.getStringValue());
    }
  }
  
  /*
   * Populates the prepared statement parameter at the given index
   * with the value. 
   */
  private void populateParameter(int index, String value) throws SQLException {
    if (StringUtils.isNotBlank(value)) {
      if (argSqlTypes != null) {
	reusablePreparedStatement.setObject(index, value, argSqlTypes[index - 1]);
      } else {
	reusablePreparedStatement.setObject(index, value);
      }
    } else {
      if (argSqlTypes != null) {
	reusablePreparedStatement.setNull(index, argSqlTypes[index-1]);
      } else {
	reusablePreparedStatement.setObject(index, value);
      }
    }
  }
}
