/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.oa3.auxil.connector.jdbc.reader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.util.JDBCUtil;

public class ResultSetOrderedMapConverter implements ResultSetConverter {

  private static final Log log = LogFactory.getLog(ResultSetOrderedMapConverter.class);

  public Object convertNext(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    if (rs.next()) {
      return convertNext(rs, rsmd);
    } else {
      return null;
    }
  }

  public Object[] convertAll(ResultSet rs) throws SQLException {
    ArrayList rows = new ArrayList();
    ResultSetMetaData rsmd = rs.getMetaData();
    while (rs.next()) {
      rows.add(convertNext(rs, rsmd));
    }
    return rows.toArray(new Object[rows.size()]);
  }

  private Object convertNext(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
    JDBCUtil.logResultSet(log, "converting ResultSet", rs);
    int columnCount = rsmd.getColumnCount();
    IOrderedMap map = new OrderedHashMap(columnCount);
    for (int i = 1; i <= columnCount; i++) {
      map.put(rsmd.getColumnName(i), rs.getObject(i));
    }
    return map;
  }


}
