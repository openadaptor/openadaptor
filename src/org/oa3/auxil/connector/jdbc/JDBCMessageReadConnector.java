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
package org.oa3.auxil.connector.jdbc;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCMessageReadConnector.java,v 1.10 2006/11/04 23:32:44 ottalk Exp $
 * Rev:  $Revision: 1.10 $
 * Created May 26, 2006 by Kuldip Ottal
 */
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.core.exception.ComponentException;

/**
 * This class replicates the behaviour of BCG Message functionality in openadaptor
 * It will establish database connections via jdbc, and provide records,
 * using the stored procedure specified, to the node to which it is attached
 * @author Kuldip Ottal
 */
public class JDBCMessageReadConnector extends JDBCReadConnector  {

  private static final Log log = LogFactory.getLog(JDBCMessageReadConnector.class.getName());

  private String messageStoredProcedure="OA3_ProcessNextQueuedMessage";
  private String messageServiceID;
  private String updateMessageStatus;
  private ResultSet messageRetrievalInfoResultSet = null;
  private ResultSet messageDataResultSet = null;
  private boolean messageDataRowAvailable;

  //No-arg constructor for beans
  public JDBCMessageReadConnector() {
  }

  /**
   * Returns stored procedure name used to return the next unprocessed OA3 Message
   *
   * @return stored procedure name
   */
  public String getMessageStoredProcedure() {
    return messageStoredProcedure;
  }

  /**
   * Set stored procedure used to return the next unprocessed OA3 Message
   */
  public void setMessageStoredProcedure(String messageStoredProcedure) {
    this.messageStoredProcedure = messageStoredProcedure;
  }

  /**
   * Returns message service id, this determines which type of OA3 Messages are fetched
   *
   * @return message service id
   */
  public String getMessageServiceID() {
    return messageServiceID;
  }

  /**
   * Set message service id, this determines which type of OA3 Messages are fetched
   */
  public void setMessageServiceID(String messageServiceID) {
    this.messageServiceID = messageServiceID;
  }

  /**
   * Determine if message is updated after processing
   *
   * @return determine if message is updated after processing
   */
  public String getUpdateMessageStatus() {
    return updateMessageStatus;
  }

  /**
   * Determine if message is updated after processing
   */
  public void setUpdateMessageStatus(String updateMessageStatus) {
    this.updateMessageStatus = updateMessageStatus;
  }


  /**
   * Get the next record.
   *
   * @return  an array containing the next record to be processed.
   * @throws org.oa3.core.exception.ComponentException
   */

  public Object[] nextRecord() throws ComponentException  {
    Object[] result=null;
    ArrayList recordArrayList = new ArrayList();
    int columnCount = 0;

    if (messageDataRowAvailable) {
      try {
        ResultSetMetaData rsmd = messageDataResultSet.getMetaData();
        columnCount = rsmd.getColumnCount();

        while( messageDataResultSet.next()) {
          IOrderedMap record =new OrderedHashMap(columnCount);

          for (int i = 1; i <= columnCount; i++) {
            record.put(rsmd.getColumnName(i),messageDataResultSet.getObject(i));
          }
          recordArrayList.add(record);
        }
        log.debug("Number of records = " + recordArrayList.size());
        result=recordArrayList.toArray(new Object[recordArrayList.size()]);
        messageDataRowAvailable = false;

      } catch (SQLException sqle) {
        throw new ComponentException(sqle.getMessage(), sqle, this);
      }
    }
    return result;
  }

  /**
   * Method called by inpoint to re-run query to fetch more data
   *
   */
  public void refreshData() {
    log.debug("Refreshing data - executing query");
    try {
      messageRetrievalInfoResultSet = runMessageRetrievalInformationQuery();
      if (messageRetrievalInfoResultSet.next()) {
        displayResultSetInfo(messageRetrievalInfoResultSet);

        messageDataResultSet = runMessageDataQuery();

        if (messageDataResultSet.next()) {
          //displayResultSetInfo(messageDataResultSet);
          messageDataRowAvailable = true;
        } else {
          log.debug("Empty message data result set...");
          messageDataRowAvailable = false;
        }
      } else {
        log.debug("Empty message retrieval info result set..");
      }

    } catch (SQLException sqle) {
      log.debug("Empty result set..");
    }


  }

  /**
   * This method displays result set information
   *
   * @param rs ResultSet
   * @throws SQLException
   */
  private void displayResultSetInfo(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    log.debug("RESULT SET:");
    for (int i = 1; i <= columnCount; i++) {
      log.debug("Column Name: " + rsmd.getColumnClassName(i)  + ",Object: " + rs.getObject(i));
    }

  }

  /**
   * This method execute stored procedure which fetches next unprocessed OA3 Message
   *
   * @return ResultSet
   * @throws SQLException
   */
  private ResultSet runMessageRetrievalInformationQuery() throws SQLException {
    String callableStatement = null;

    callableStatement = "{ call " + messageStoredProcedure + "(?,?) }";

    CallableStatement cs = jdbcConnection.getConnection().prepareCall(callableStatement);
    cs.setInt(1,Integer.parseInt(messageServiceID));
    cs.setString(2,updateMessageStatus);
    return cs.executeQuery();

  }

  /**
   * This method executes stored procedure which is contained in OA3 Message
   *
   * Execution will return data which is passed to inpoint
   * @return ResultSet
   * @throws SQLException
   */
  private ResultSet runMessageDataQuery() throws SQLException {
    int storedProcNameRSLocation=4;
    int storedProcParamRSStartLocation=6;
    String applicationCallableStatement = null;
    CallableStatement messageDataCS = null;


    //Build up application stored procedure with parameters markers
    String storedProcName = messageRetrievalInfoResultSet.getObject(storedProcNameRSLocation).toString();
    applicationCallableStatement = "{ call " + storedProcName +" (";
    for (int j = storedProcParamRSStartLocation; j <= messageRetrievalInfoResultSet.getMetaData().getColumnCount(); j++) {
      if (messageRetrievalInfoResultSet.getObject(j) != null) {
        log.debug("object=" + messageRetrievalInfoResultSet.getObject(j).toString());
        applicationCallableStatement += "?,";
      }
    }
    // Remove trailing comma
    applicationCallableStatement = applicationCallableStatement.substring(0, applicationCallableStatement.length() -1);
    //Close callable statement string
    applicationCallableStatement += ")}";

    log.debug("Executing '" + applicationCallableStatement + "' to retrieve data");


    messageDataCS = jdbcConnection.getConnection().prepareCall(applicationCallableStatement);

    //Build up parameter list for callable statement
    int parameterCount=1;
    for(int i=storedProcParamRSStartLocation; i <= messageRetrievalInfoResultSet.getMetaData().getColumnCount(); i++) {
      if (messageRetrievalInfoResultSet.getObject(i) != null) {
        log.debug("Adding parameter " + parameterCount + ", value = " + messageRetrievalInfoResultSet.getObject(i).toString());
        messageDataCS.setString(parameterCount,messageRetrievalInfoResultSet.getObject(i).toString());
        parameterCount++;
      }
    }
    log.debug("Number of stored proc parameters = " + parameterCount );

    return messageDataCS.executeQuery();

  }

}