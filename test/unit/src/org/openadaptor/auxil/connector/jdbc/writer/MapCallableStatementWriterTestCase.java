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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.jmock.Mock;
import org.jmock.core.Stub;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;

public class MapCallableStatementWriterTestCase extends AbstractMapWriterTests {

  protected static String CatalogName = "MockCatalogName";
  protected static String StoredProcName = "MockStoredProcName";
  protected static String MockTableName = "MockTableName";
  protected static int ParameterCount = 4;
  protected static String InitialiseQuery = "SELECT * FROM " + MockTableName + " WHERE 1=2";
  protected static String PreparedStatementSQL = "{ CALL "+ StoredProcName +"(?,?,?,?)}";

  protected static IOrderedMap SampleOrderedMapOne = new OrderedHashMap();

  static {
    SampleOrderedMapOne.add("Map One Param One");
    SampleOrderedMapOne.add("Map One Param Two");
    SampleOrderedMapOne.add("Map One Param Three");
    SampleOrderedMapOne.add("Map One Param Four");
  }

  protected static IOrderedMap SampleOrderedMapTwo = new OrderedHashMap();

  static {
    SampleOrderedMapTwo.add("Map Two Param One");
    SampleOrderedMapTwo.add("Map Two Param Two");
    SampleOrderedMapTwo.add("Map Two Param Three");
    SampleOrderedMapTwo.add("Map Two Param Four");
  }

  protected static IOrderedMap SampleOrderedMapThree = new OrderedHashMap();

  static {
    SampleOrderedMapThree.add("Map Three Param One");
    SampleOrderedMapThree.add("Map Three Param Two");
    SampleOrderedMapThree.add("Map Three Param Three");
    SampleOrderedMapThree.add("Map Three Param Four");
  }

  protected Mock metaDataMock;
  protected Mock statementMock;
  protected Mock resultSetMock;
  protected Mock resultSetMetaDataMock;


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


  protected ISQLWriter instantiateTestWriter() {
    MapCallableStatementWriter writer = new MapCallableStatementWriter();
    writer.setCallableStatement(StoredProcName);
    return writer;
  }

  protected void setMocksFor(ISQLWriter writer) {
  }

  /**
   * Set the expectations of the jdbc mocks reasonably to enable the the writer
   * to be initialised. NB this is a little circular at the moment ie. these
   * expectations have been set with respect to what the writer already does.
   *
   * @param supportsBatch Set batch support on the JDBC Layer
   */
  protected void setupInitialiseExpectations(boolean supportsBatch) {
    connectionMock.expects(atLeastOnce()).method("getMetaData").will(returnValue(metaDataMock.proxy()));
    metaDataMock.expects(once()).method("supportsBatchUpdates").will(returnValue(supportsBatch));
    connectionMock.expects(once()).method("getCatalog").will(returnValue(CatalogName));
    metaDataMock.expects(once()).method("getProcedureColumns").with(eq(CatalogName), eq("%"), eq(StoredProcName), eq("%")).will(returnValue(resultSetMock.proxy()));
    resultSetMock.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(new Stub[] {returnValue(true), returnValue(true), returnValue(true), returnValue(true), returnValue(true), returnValue(false)}));
    resultSetMock.stubs().method("getString").will(returnValue("Dummy ResultSet Info")); // Not being specific here as this only happens when logging set to debug
    resultSetMock.expects(atLeastOnce()).method("close");
    connectionMock.expects(once()).method("prepareStatement").with(eq(PreparedStatementSQL)).will(returnValue(preparedStatementMock.proxy()));
  }

  protected Object[] setUpSingletonDataAndDataExpections() {
    Object[] data = new Object [] {SampleOrderedMapOne};
    preparedStatementMock.expects(once()).method("clearParameters");
    for (int i = 0; i < SampleOrderedMapOne.size(); i++ ) {
      preparedStatementMock.expects(once()).method("setObject").with(eq ( i+1), eq(SampleOrderedMapOne.get(i) ));
    }
    preparedStatementMock.expects(once()).method("executeUpdate").will(returnValue(1));
    return data;
  }

  /**
   * This method must be overrriden to generate test data consisting of a  batch.
   * Associated mock expectations relating to this component must also be set. Note
   * that these expectations are to be set assuming the underlying jdbc layer is confugured
   * to eanble batch uploads.
   *
   * @return Object[]
   */
  protected Object[] setupWriteBatchDataAndExpectationsBatchingEnabled() {
    Object[] data = new Object[] { SampleOrderedMapOne, SampleOrderedMapTwo, SampleOrderedMapThree };
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      IOrderedMap dataElement = (IOrderedMap) data[dataIndex];
      for (int i = 0; i < dataElement.size(); i++) {
        preparedStatementMock.expects(once()).method("setObject").with(eq(i + 1), eq(dataElement.get(i)));
      }
    }
    preparedStatementMock.expects(exactly(3)).method("addBatch");
    preparedStatementMock.expects(once()).method("executeBatch").will(returnValue(new int[] {1, 1, 1}));
    return data;
  }

  /**
   * This method must be overriden to generate test data consisting of a  batch.
   * Associated mock expectations relating to this component must also be set. Note
   * that these expectations are to be set assuming the underlying jdbc layer is configured
   * to disable batch uploads.
   *
   * @return Object[]
   */
  protected Object[] setupWriteBatchDataAndExpectationsBatchingDisabled() {
    Object[] data = new Object[] { SampleOrderedMapOne, SampleOrderedMapTwo, SampleOrderedMapThree };
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      IOrderedMap dataElement = (IOrderedMap) data[dataIndex];
      for (int i = 0; i < dataElement.size(); i++) {
        preparedStatementMock.expects(once()).method("setObject").with(eq(i + 1), eq(dataElement.get(i)));
      }
    }
    preparedStatementMock.expects(never()).method("addBatch");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(exactly(3)).method("executeUpdate").will(returnValue(1));
    return data;
  }

  protected Object[] setupDataForWriteNotInitialised() {
    Object[] data = new Object [] { SampleOrderedMapOne };
    // Test should bail before any of these methods get called (with or without batching).
    preparedStatementMock.expects(never()).method("clearParameters");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(never()).method("close");
    return data;
  }
}
