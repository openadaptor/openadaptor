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

package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * Interface for convertion of an SQL ResultSet into Object record(s).
 * The resultant record types is ultimately determined by the implementation
 * classes.
 * @author higginse
 * @since Post 3.2.1
 */

public interface IResultSetConverter {
  /**
   * Flag to indicate converter shoudl convert a single row at a time.
   */
  public static final int CONVERT_ONE=1;

  /**
   * This is the default batch size for converting a ResultSet.
   * 
   */
  public static final int DEFAULT_BATCH_SIZE=CONVERT_ONE;

  /**
   * This indicates that all rows from a ResultSet should be converted.
   */
  public static final int CONVERT_ALL=0;
  /**
   * Convert rows from ResultSet into corresponding record Objects.
   * @param rs
   * @param maxBatchSize sets upper limit on number of records which may be converted from the ResultSet.
   * @return Object[] with at most maxBatchSize records, unless maxBatchSize is CONVERT_ALL where all records
   *                  will be converted.
   * @throws SQLException
   */
  public Object[] convert(ResultSet rs,int maxBatchSize) throws SQLException;
}
