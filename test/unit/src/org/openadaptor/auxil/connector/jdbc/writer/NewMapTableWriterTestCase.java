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

import org.jmock.Mock;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;

import java.sql.*;
import java.util.Map;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 18, 2007 by oa3 Core Team
 */

public class NewMapTableWriterTestCase  extends AbstractMapWriterTests {

  protected static String MockTableName = "MockTableName";
  protected static String InitialiseQuery = "SELECT * FROM " + MockTableName + " WHERE 1=2";
  protected static int ColumnCount = 6;
  protected static String[] ColumnNames = new String[] {"TRADEID", "BUYSELL", "SECID", "PARTYID", "QTY", "PRICE"};
  protected static String PreparedStatementSQL = "INSERT INTO " + MockTableName + "(TRADEID,BUYSELL,SECID,PARTYID,QTY,PRICE) VALUES (?,?,?,?,?,?)";

  protected static Map SampleMapOne = new OrderedHashMap();
  protected static Map SampleMapTwo = new OrderedHashMap();
  protected static Map SampleMapThree = new OrderedHashMap();

  static {
    SampleMapOne.put(ColumnNames[0], new Integer(1));
    SampleMapOne.put(ColumnNames[1], "B");
    SampleMapOne.put(ColumnNames[2], new Integer(1));
    SampleMapOne.put(ColumnNames[3], new Integer(1));
    SampleMapOne.put(ColumnNames[4], new Integer(1000000));
    SampleMapOne.put(ColumnNames[5], new Double(3.251));
  }

  static {
    SampleMapTwo.put(ColumnNames[0], new Integer(2));
    SampleMapTwo.put(ColumnNames[1], "S");
    SampleMapTwo.put(ColumnNames[2], new Integer(2));
    SampleMapTwo.put(ColumnNames[3], new Integer(2));
    SampleMapTwo.put(ColumnNames[4], new Integer(2000000));
    SampleMapTwo.put(ColumnNames[5], new Double(3.252));
  }

  static {
    SampleMapThree.put(ColumnNames[0], new Integer(3));
    SampleMapThree.put(ColumnNames[1], "X");
    SampleMapThree.put(ColumnNames[2], new Integer(3));
    SampleMapThree.put(ColumnNames[3], new Integer(3));
    SampleMapThree.put(ColumnNames[4], new Integer(3000000));
    SampleMapThree.put(ColumnNames[5], new Double(3.253));
  }

  protected Mock metaDataMock;
  protected Mock statementMock;
  protected Mock resultSetMock;
  protected Mock resultSetMetaDataMock;


  protected ISQLWriter instantiateTestWriter() {
    MapTableWriter writer = new MapTableWriter();
    writer.setTableName(MockTableName);
    return writer;
  }

  protected void setMocksFor(ISQLWriter writer) {
  }

  protected void setUp() throws Exception {
    super.setUp();
    metaDataMock = mock(DatabaseMetaData.class);
    statementMock = mock(Statement.class);
    resultSetMock = mock(ResultSet.class);
    resultSetMetaDataMock = mock(ResultSetMetaData.class);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    metaDataMock = null;
    statementMock = null;
    resultSetMock = null;
    resultSetMetaDataMock = null;
  }

  /**
   * Set the expectations of the jdbc mocks reasonably to enable the the writer
   * to be initialised. NB this is a little circular at the moment ie. these
   * expectations have been set with respect to what the writer already does.
   *
   * @param supportsBatch Set batch support on the JDBC Layer
   */
  protected void setupInitialiseExpectations(boolean supportsBatch) {
    connectionMock.expects(once()).method("getMetaData").will(returnValue(metaDataMock.proxy()));
    metaDataMock.expects(once()).method("supportsBatchUpdates").will(returnValue(supportsBatch));
    connectionMock.expects(once()).method("createStatement").will(returnValue(statementMock.proxy()));
    statementMock.expects(once()).method("executeQuery").with(eq(InitialiseQuery)).will(returnValue(resultSetMock.proxy()));
    resultSetMock.expects(once()).method("getMetaData").will(returnValue(resultSetMetaDataMock.proxy()));
    resultSetMetaDataMock.stubs().method("getColumnCount").will(returnValue(ColumnCount));
    for (int i = 0; i < ColumnCount ; i++) {
      resultSetMetaDataMock.expects(once()).method("getColumnName").with(eq(i+1)).will(returnValue(ColumnNames[i]));
    }
    connectionMock.expects(once()).method("prepareStatement").with(eq(PreparedStatementSQL)).will(returnValue(preparedStatementMock.proxy()));
  }

  /**
   * Test writing a batch of one with batch support enabled.
   */
  public void testWriteSingleton() {
    Object[] data = setUpSingletonDataAndDataExpections();

    setupInitialiseExpectations(true);
    testWriter.initialise((Connection)connectionMock.proxy());

    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }


  protected Object[] setUpSingletonDataAndDataExpections() {
    Object[] data = new Object [] {SampleMapOne};
    preparedStatementMock.expects(once()).method("clearParameters");
    for (int i = 0; i < SampleMapOne.size(); i++ ) {
      preparedStatementMock.expects(once()).method("setObject").with(eq ( i+1), eq(SampleMapOne.get(ColumnNames[i]) ));
    }
    preparedStatementMock.expects(once()).method("executeUpdate").will(returnValue(1));
    return data;
  }


  protected Object[] setupWriteBatchDataAndExpectationsBatchingEnabled() {
    Object[] data = new Object [] {SampleMapOne, SampleMapTwo, SampleMapThree};
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      Map dataElement = (Map) data[dataIndex];
      for (int i = 0; i < dataElement.size(); i++) {
        preparedStatementMock.expects(once()).method("setObject").with(eq(i + 1), eq(dataElement.get(ColumnNames[i])));
      }
    }
    preparedStatementMock.expects(atLeastOnce()).method("addBatch");
    preparedStatementMock.expects(once()).method("executeBatch").will(returnValue(new int[] {1, 1, 1}));
    return data;
  }

  protected Object[] setupWriteBatchDataAndExpectationsBatchingDisabled() {
    Object[] data = new Object [] {SampleMapOne, SampleMapTwo, SampleMapThree};
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      Map dataElement = (Map) data[dataIndex];
      for (int i = 0; i < dataElement.size(); i++) {
        preparedStatementMock.expects(once()).method("setObject").with(eq(i + 1), eq(dataElement.get(ColumnNames[i])));
      }
    }
    preparedStatementMock.expects(never()).method("addBatch");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(atLeastOnce()).method("executeUpdate").will(returnValue(1));
    return data;
  }

  protected Object[] setupDataForWriteNotInitialised() {
    Object[] data = new Object [] { SampleMapOne };
    // Test should bail before any of these methods get called (with or without batching).
    preparedStatementMock.expects(never()).method("clearParameters");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(never()).method("close");
    return data;
  }
}
