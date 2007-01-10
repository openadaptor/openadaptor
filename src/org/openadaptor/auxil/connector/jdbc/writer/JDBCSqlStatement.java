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
package org.oa3.auxil.connector.jdbc.writer;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCSqlStatement.java,v 1.3 2006/11/04 23:32:44 ottalk Exp $
 * Rev:  $Revision: 1.3 $
 * Created Oct 22, 2006 by Kuldip Ottal
 */

import org.oa3.core.exception.ComponentException;
import org.oa3.core.Component;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class implements the <code>IJDBCStatement</code> interface.
 * The class writes data to the database using a supplied sql statement.
 * It uses the supplied ordered map values to replace the placeholder variables
 * and then executes the sql against the database.
 */
public class JDBCSqlStatement extends Component implements IJDBCStatement {

  private static final Log log = LogFactory.getLog(JDBCSqlStatement.class.getName());

  private String sql="";
  private String delimiter="";

  //Constructor
  public  JDBCSqlStatement(String sqlStatement,String delimiter, Connection connection ) {
    initialiseStatement(sqlStatement,delimiter, null,connection);
  }

  /**
   * This method sets the properties in the class
   *
   * @param sqlStatement SQL statement
   * @param delimiter Delimiter character used to indicate placeholder variables
   * @param mapping Not used
   * @param connection Not used
   * @throws org.oa3.core.exception.ComponentException
   */
  public void initialiseStatement(String sqlStatement, String delimiter,Object mapping, Connection connection) throws ComponentException {
    this.sql=sqlStatement;
    this.delimiter=delimiter;
  }

  /**
   * This method calls the <code>parseSqlStatement</code> method to replace placeholder variables
   * with values from the supplied ordered map in the configured sql statement.
   * It then executes the prepared statement and returns the number of rows updated.
   *
   * @param om Ordered Map
   * @param connection JDBC Connection
   * @return int Number of database rows updated
   * @throws ComponentException
   */
  public int executeStatement(IOrderedMap om, Connection connection) throws ComponentException {
    int updateCount=0;
    Statement stmt;
    String parsedSql="";
    //Todo: Rectify massive inefficiencies of creating statement even when we don't need to.
    try {
      parsedSql = parseSqlStatement(sql,om);
      if (!(parsedSql.equals(""))) {
        stmt = connection.createStatement();
        log.info("Executing statement to insert data");
        log.debug("SQL Statement: " + parsedSql);
        updateCount = stmt.executeUpdate(parsedSql);
      } else {
        throw new SQLException("Empty parsed sql statement");
      }
    } catch (SQLException sqle) {
      throw new RuntimeException(sqle.getMessage(), sqle);
    }
    return updateCount;
  }

  /**
   * Parse supplied sql statement replacing placeholders.
   *
   * @param map - ordered map to be processed.
   * @return parsed sql statement to be executed against database.
   * @throws ComponentException
   */
  private String parseSqlStatement(String sqlStatement,IOrderedMap map) throws ComponentException{

    String parsedSqlStatement=sqlStatement;
    List omKeys = map.keys();
    if (validatePlaceHolders(sqlStatement,omKeys)) {
      Iterator omKeysIterator = omKeys.iterator();

      while (omKeysIterator.hasNext()) {
        String key = (String) omKeysIterator.next();
        parsedSqlStatement = replacePlaceHolder(parsedSqlStatement, key, map.get(key).toString());
      }
      parsedSqlStatement = stringCleanUp(parsedSqlStatement,delimiter,false);
    } else {
      throw new ComponentException ("Invalid SQL statement placeholders", this);
    }

    return parsedSqlStatement;
  }

  /**
   * Ensure all sql statement placeholders exist in ordered map, otherwise throw exception.
   *
   * @param omKeys - list of ordered map keys to be processed.
   * @return boolean indicate whether all placeholders are present in ordered map
   * @throws ComponentException
   */
  private boolean validatePlaceHolders(String sqlStatement, List omKeys) throws ComponentException {
    boolean valid=true;

    //get keys from sql statement
    List sqlStatementKeys = new ArrayList();
    String regex = "\\" + delimiter +"(\\w+)" + "\\" + delimiter;
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(sqlStatement);
    while (matcher.find()) {
      //remove delimiters from key
      String key = stringCleanUp(matcher.group(),delimiter, false);
      sqlStatementKeys.add(key);
    }

    //Iterate through sql statement placeholders
    Iterator sqlStatementKeysIterator = sqlStatementKeys.iterator();
    while (sqlStatementKeysIterator.hasNext()) {
      String key = (String) sqlStatementKeysIterator.next();
      if (!(omKeys.contains(key))) {
        valid=false;
        throw new ComponentException("Sql statement placeholder '" + key + "' not found in OrderedMap", this);
      }
    }

    return valid;
  }

  /**
   * Method to clean up a string either to strip or keep indicated characters
   *
   * @param inputString - String to be processed
   *        matchString - characters to use for processing
   *        isToKeep - true -> keep , false -> strip
   * @return String - Processed String
   */
  protected String stringCleanUp( String inputString, String matchString, boolean isToKeep ) {
    final int size = inputString.length();
    StringBuffer buf = new StringBuffer( size );
    if ( ! isToKeep ) {
      for ( int i = 0; i < size; i++ ){
        if ( matchString.indexOf(inputString.charAt(i) ) == -1 ){
          buf.append( inputString.charAt(i) );
        }
      }
    }
    else {
      for ( int i = 0; i < size; i++ ){
        if ( matchString.indexOf(inputString.charAt(i) ) != -1 ){
          buf.append( inputString.charAt(i) );
        }
      }
    }
    return buf.toString();
  }

  /**
   * Method to replace placeholders
   *
   * @param inputString - String to be processed
   *        matchString - String to search for
   *        replacementString - String to replace matchString
   * @return String - Processed String
   */
  protected String replacePlaceHolder(String inputString, String matchString, String replacementString) {
    String result = "";
    StringTokenizer st = new StringTokenizer(inputString, delimiter, true);
    while (st.hasMoreTokens()) {
      String w = st.nextToken();
      if (w.equals(matchString)) {
        result = result + replacementString;
      } else {
        result = result + w;
      }
    }
    return result;
  }

}
