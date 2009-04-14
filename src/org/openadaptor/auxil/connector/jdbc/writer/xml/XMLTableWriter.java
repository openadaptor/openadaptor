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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.exception.ValidationException;

/**
 * Defines a concrete SQL writer for writing XML {@link Document}s
 * to a database table using a SQL insert.
 * <p>
 * It will match elements in the {@link Document} to columns in the
 * configured database table, and write the values as rows in the
 * output database table.
 * 
 * @author cawthorng
 */
public class XMLTableWriter extends AbstractXMLWriter {

  private static final Log log = LogFactory.getLog(XMLTableWriter.class);

  private String tableName;

  /**
   * This creates a reusable Prepared Statement for inserts into the configured table.
   */
  protected void initialiseReusablePreparedStatement() {
    initialiseReusableInsertStatement(tableName);
  }
  
  /**
   * Check that tableName has been assigned.
   */
  public void validate(List exceptions, IComponent comp) {
    super.validate(exceptions, comp);
    
    if (StringUtils.isBlank(tableName)) {
      log.debug("Mandatory property tableName has not been supplied");
      exceptions.add(new ValidationException("Property [tableName] is mandatory", comp));
    }
    
    if (outputColumns == null) {
      log.info("outputColumns undefined - columns will be derived from db table metadata");
    }
  }

  /**
   * Set the name of the database table to which data rows are to be inserted.
   * <br>
   * This is mandatory.
   * @param tableName
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
  /**
   * Returns the name of the database table where data is to be written
   * @return String containing the name of the table
   */
  public String getTableName() {
    return tableName;
  }
}
