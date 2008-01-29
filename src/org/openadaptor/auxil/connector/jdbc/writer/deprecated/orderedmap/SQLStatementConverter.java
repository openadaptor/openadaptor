/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.Component;

/**
 * The SQLStatementConvertor is a class to transform an input
 * SQL template into an executable SQL statement.
 * It takes a <code>sql</code> and for each <code>deliver</code>
 * method call on its <code>JDBCWriteConnector</code> it runs
 * the <code>convert</code> method.
 * 
 * This class effectively performs the same function as the
 * <code>javax.sql.PreparedStatement</code>
 * @deprecated Use stored procedure calls and MapCallableStatementWriter instead
 *       
 */
public class SQLStatementConverter extends AbstractStatementConverter {

  private static final Log log = LogFactory.getLog(SQLStatementConverter.class);

  private String sql="";
  private String delimiter="$";
  private boolean escapeParameters = false;

  public void setDelimiter(final String delimiter) {
    this.delimiter = delimiter;
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }

  public void setEscapeParameters(boolean escapeParameters) {
	  this.escapeParameters = escapeParameters;
  }
  public void initialise(Connection connection) {
  }


  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, Component comp) {
    if ( "".equals(sql) )
      exceptions.add(new ValidationException("The [sql] property must be supplied", comp));
  }


  /**
   * Converts the <code>sql</code> to a valid statement and
   * returns as a <code>PreparedStatement</code> on the given
   * <code>connection</code>.
   * The values in the given {@link IOrderedMap} <code>om</code>
   * are substituted where the keys match in <code>sql</code>.
   * @param om the map containing the keys representing the 
   * placeholders and the values to be substituted in the sql.
   * @param connection the <code>Connection</code> to prepare the
   * statement against.
   * @return the <code>PreparedStatement</code>
   */
  public PreparedStatement convert(IOrderedMap om, Connection connection) {
    try {
      String resolvedSql = parseSqlStatement(sql,om);
      if (!(resolvedSql.equals(""))) {
        log.debug("resolved SQL = " + resolvedSql);
        return connection.prepareStatement(resolvedSql);
      } else {
        throw new SQLException("Empty parsed sql statement");
      }
    } catch (SQLException sqle) {
      throw new RuntimeException(sqle.getMessage(), sqle);
    }
  }

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
      throw new RuntimeException ("Invalid SQL statement placeholders");
    }

    return parsedSqlStatement;
  }

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
      if (!(omKeys.contains(key)))
        throw new RuntimeException("Sql statement placeholder '" + key + "' not found in OrderedMap");
    }

    return valid;
  }

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
   * Replaces all occurrences of the given <code>matchString</code> with
   * the given <code>replacementString</code> in the given <code>inputString</code>.
   * Note the <code>matchString</code> must be surrounded by the <code>delimiter</code>
   * character.
   * This method makes no effort to perform SQL escaping, nor does it treat
   * <code>matchString</code> as a regular expression. 
   * @param inputString
   * @param matchString
   * @param replacementString
   * @return the <code>inputString</code> with the replacements made.
   */
  protected String replacePlaceHolder(String inputString, String matchString, String replacementString) {
    String result = "";
    StringTokenizer st = new StringTokenizer(inputString, delimiter, true);
    while (st.hasMoreTokens()) {
      String w = st.nextToken();
      if (w.equals(matchString)) {
    	  if (escapeParameters) replacementString = sqlEscape(replacementString);
        result = result + replacementString;
      } else {
        result = result + w;
      }
    }
    return result;
  }
  
  protected static String sqlEscape(String str) {
	  return str.replaceAll("'", "''");
  }
}
