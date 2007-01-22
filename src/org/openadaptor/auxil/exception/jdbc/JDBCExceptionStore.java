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

package org.openadaptor.auxil.exception.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.openadaptor.auxil.exception.ExceptionStore;
import org.openadaptor.auxil.exception.ExceptionSummary;
import org.openadaptor.auxil.exception.XMLUtil;
import org.openadaptor.util.JDBCUtil;

public class JDBCExceptionStore implements ExceptionStore {

  private static final Log log = LogFactory.getLog(JDBCExceptionStore.class);
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
  
  private Connection connection;
  
  public JDBCExceptionStore(final Connection connection) {
    this.connection = connection;
  }
  
  public void delete(String id) {
    PreparedStatement s = null;
    String sql = "UPDATE ExceptionSummary SET Status = 'D' WHERE ExceptionId = " + id;
    try {
      s = connection.prepareStatement(sql);
      s.executeUpdate();
      connection.commit();
    } catch (Exception e) {
      log.error("unexpected exception, sql = " + sql, e);
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
  }

  public String getDataForId(String id) {
    Document doc = getDetail(id);
    return XMLUtil.getData(doc);
  }

  public ExceptionSummary getExceptionSummary(String id) {
    ExceptionSummary summary = new ExceptionSummary();
    summary.setId(id);
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    sql.append("ComponentId");
    sql.append(",Application");
    sql.append(",Date");
    sql.append(",Message");
    sql.append(",RetryAddress");
    sql.append(",Retries");
    sql.append(",isnull(ParentId, 0)");
    sql.append(",Host");
    sql.append(",Class");
    sql.append(" FROM ExceptionSummary where ExceptionId = " + id);
    
    PreparedStatement s = null;
    try {
      s = connection.prepareStatement(sql.toString());
      ResultSet rs = s.executeQuery();
      rs.next();
      summary.setComponentId(rs.getString(1));
      summary.setFrom(rs.getString(2));
      summary.setDate(new Date(rs.getTimestamp(3).getTime()));
      summary.setMessage(rs.getString(4));
      summary.setRetryAddress(rs.getString(5));
      summary.setRetries(rs.getInt(6));
      summary.setParentId(rs.getInt(7) > 0 ? rs.getString(7) : "");
      summary.setHost(rs.getString(8));
      summary.setException(rs.getString(9));
      connection.commit();
    } catch (Exception e) {
      log.error("unexpected exception, sql = " + sql.toString(), e);
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
    return summary;
  }

  public List getIds(ExceptionSummary filter) {
    ArrayList ids = new ArrayList();
    StringBuffer whereClause = new StringBuffer();
    
    if (filter.getId() != null) {
      if (filter.getId().indexOf('-') > 0) {
        String[] idRange = filter.getId().split("-");
        if (idRange.length > 0) {
          whereClause.append(" AND ExceptionId >= " + idRange[0]);
        }
        if (idRange.length > 1) {
          whereClause.append(" AND ExceptionId <= " + idRange[1]);
        }
      } else {
        whereClause.append(" AND ExceptionId = " + filter.getId());
      }
    }
    
    if (filter.getFrom() != null) {
      whereClause.append(" AND Application like '" + filter.getFrom() + "%'");
    }
    
    if (filter.getDate() != null) {
      long lower = filter.getTime();
      long upper = lower + (24 * 60 * 60 * 1000);
      Date upperDate = new Date(upper);
      whereClause.append(" AND Date >= '" + convertToDateString(filter.getDate()) + "'");
      whereClause.append(" AND Date < '" + convertToDateString(upperDate) + "'");
    }
    
    PreparedStatement s = null;
    String sql = "SELECT ExceptionId  FROM ExceptionSummary WHERE Status = 'C' " + whereClause + " ORDER BY ExceptionId DESC";
    try {
      s = connection.prepareStatement(sql);
      ResultSet rs = s.executeQuery();
      while (rs.next()) {
        ids.add(rs.getString(1));
      }
    } catch (Exception e) {
      log.error("unexpected exception, sql = " + sql.toString(), e);
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
    return ids;
  }

  public String[] getStackTrace(String id) {
    Document doc = getDetail(id);
    return XMLUtil.getStackTrace(doc);
  }

  public Document getDetail(String id) {
    PreparedStatement s = null;
    try {
      s = connection.prepareStatement("SELECT Detail FROM ExceptionDetail WHERE ExceptionId = " + id);
      ResultSet rs = s.executeQuery();
      rs.next();
      String detail = rs.getString(1);
      rs.close();
      s.close();
      return DocumentHelper.parseText(decodeDetail(detail));
    } catch (Exception e) {
      throw new RuntimeException("unexpected exception, " + e.getMessage(), e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
  }

  public void incrementRetryCount(String id) {
    PreparedStatement s = null;
    String sql = "UPDATE ExceptionSummary SET Retries = Retries + 1 WHERE ExceptionId = " + id;
    try {
      s = connection.prepareStatement(sql);
      s.executeUpdate();
      connection.commit();
    } catch (Exception e) {
      log.error("unexpected exception, sql = " + sql.toString(), e);
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
  }

  public String store(String xml) {
    long id = 0;
    Document doc;
    try {
      doc = DocumentHelper.parseText(xml);
    } catch (DocumentException e) {
      throw new RuntimeException("failed to parse exception", e);
    }
    
    ExceptionSummary summary = new ExceptionSummary();
    XMLUtil.populateSummary(doc, summary);
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT ExceptionSummary (");
    sql.append("ComponentId");
    sql.append(",Application");
    sql.append(",Date");
    sql.append(",Message");
    sql.append(",RetryAddress");
    sql.append(",Retries");
    sql.append(",ParentId");
    sql.append(",Host");
    sql.append(",Class");
    sql.append(",Status");
    sql.append(") VALUES (");
    sql.append("'" + summary.getComponentId() + "'");
    sql.append(",'" + summary.getFrom() + "'");
    sql.append(",'" + convertToDateString(summary.getDate()) + "'");
    sql.append(",'" + summary.getMessage() + "'");
    sql.append(",'" + summary.getRetryAddress() + "'");
    sql.append("," + summary.getRetries());
    sql.append("," + convertToInteger(summary.getParentId(), 0));
    sql.append(",'" + summary.getHost() + "'");
    sql.append(",'" + summary.getException() + "'");
    sql.append(",'C'");
    sql.append(")");

    PreparedStatement s = null;
    try {
      
      // store summary
      s = connection.prepareStatement(sql.toString());
      s.executeUpdate();
      s.close();
      
      // get id of new summary
      s = connection.prepareStatement("SELECT MAX(ExceptionId) from ExceptionSummary");
      ResultSet rs = s.executeQuery();
      rs.next();
      id = rs.getLong(1);
      rs.close();
      s. close();
      
      String detail = encodeDetail(doc.asXML());
      s = connection.prepareStatement("INSERT ExceptionDetail (ExceptionId, Detail) VALUES (" + id + ", '" + detail + "')");
      s.executeUpdate();
      s.close();
      
      connection.commit();
    } catch (Exception e) {
      try {
        connection.rollback();
      } catch (SQLException sqle) {
        log.error("rollbakck failed", sqle);
      }
      log.error("unexpected exception, sql = " + sql.toString(), e);
      throw new RuntimeException("failed to store exception", e);
    } finally {
      JDBCUtil.closeNoThrow(s);
    }
    return String.valueOf(id);
  }

  private String encodeDetail(String string) {
    byte[] base64bytes = Base64.encodeBase64(string.getBytes());
    return new String(base64bytes);
  }

  private String decodeDetail(String string) {
    byte[] bytes = Base64.decodeBase64(string.getBytes());
    return new String(bytes);
  }

  private String convertToDateString(Date date) {
    return DATE_FORMATTER.format(date);
  }

  private int convertToInteger(String s, int i) {
    if (s != null && s.length() > 0) {
      try {
        return Integer.parseInt(s);
      } catch (NumberFormatException e) {
      }
    }
    return i;
  }

}
