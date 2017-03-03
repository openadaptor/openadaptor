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
package org.openadaptor.auxil.connector.jdbc.writer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import org.dom4j.Document;
import org.dom4j.Node;
import org.jmock.Mock;
import org.openadaptor.auxil.connector.jdbc.writer.xml.XMLTableWriter;

/**
 * Defines a unit test for the {@link XMLTableWriter}.
 *
 * @author cawthorng
 */
public class XMLTableWriterTestCase extends AbstractXMLWriterTests {

  protected static String MockTableName = "MockTableName";
  protected static String InitialiseQuery = "SELECT * FROM " + MockTableName + " WHERE 1=2";
  protected static int ColumnCount = 6;
  protected static String[] ColumnNames = new String[] {"TRADEID", "BUYSELL", "SECID", "PARTYID", "QTY", "PRICE"};
  protected static String PreparedStatementSQL = "INSERT INTO " + MockTableName + "(TRADEID,BUYSELL,SECID,PARTYID,QTY,PRICE) VALUES (?,?,?,?,?,?)";
  
  protected Mock typeGetStatementMock;
  protected Mock typeResultSetMock;
  protected Mock typeResultSetMetaDataMock;

  protected ISQLWriter instantiateTestWriter() {
    XMLTableWriter writer = new XMLTableWriter();
    writer.setTableName(MockTableName);
    writer.setOutputColumns(Arrays.asList(ColumnNames));
    writer.setQuoteIdentifiers(false); //Disable to match older tests.
    return writer;
  }

  protected void setMocksFor(ISQLWriter writer) {
  }

  protected void setUp() throws Exception {
    super.setUp();
    typeGetStatementMock = mock(Statement.class); //This is the call to get the arg types.
    typeResultSetMock = mock(ResultSet.class);
    typeResultSetMetaDataMock = mock(ResultSetMetaData.class);
  }

  /**
   * Set the expectations of the jdbc mocks reasonably to enable the the writer
   * to be initialised. NB this is a little circular at the moment ie. these
   * expectations have been set with respect to what the writer already does.
   *
   * @param supportsBatch Set batch support on the JDBC Layer
   */
  protected void setupInitialiseExpectations(boolean supportsBatch) {
    setupTypeGetMock();
    connectionMock.expects(atLeastOnce()).method("getMetaData").will(returnValue(dbMetaDataMock.proxy()));
    configureDbMetaDataMock();
    dbMetaDataMock.expects(once()).method("supportsBatchUpdates").will(returnValue(supportsBatch));
    connectionMock.expects(atLeastOnce()).method("createStatement").will(
        onConsecutiveCalls(returnValue(typeGetStatementMock.proxy()), returnValue(typeGetStatementMock.proxy()),
            returnValue(statementMock.proxy())));
    connectionMock.expects(once()).method("prepareStatement").with(eq(PreparedStatementSQL)).will(returnValue(preparedStatementMock.proxy()));
  }
  
  private void setupTypeGetMock() {
    typeGetStatementMock.expects(atLeastOnce()).method("executeQuery").will(returnValue(typeResultSetMock.proxy()));
    typeResultSetMock.expects(atLeastOnce()).method("getMetaData").will(returnValue(typeResultSetMetaDataMock.proxy()));
    typeResultSetMetaDataMock.expects(atLeastOnce()).method("getColumnCount").will(returnValue(ColumnCount));

    for (int i = 0; i < ColumnCount; i++) {
      typeResultSetMetaDataMock.expects(atLeastOnce()).method("getColumnType").with(eq(i+1)).will(returnValue(Types.NUMERIC));
      typeResultSetMetaDataMock.expects(atLeastOnce()).method("getColumnName").with(eq(i+1)).will(returnValue(ColumnNames[i]));
    }
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
    Object[] data = new Object [] {SampleXMLOne};
    preparedStatementMock.expects(once()).method("clearParameters");
    for (int i = 0; i < ColumnNames.length; i++) {
      Node node = SampleXMLOne.getRootElement().selectSingleNode(ColumnNames[i]);
      preparedStatementMock.expects(once()).method("setObject").with(eq(i+1), eq(node.getStringValue()), eq(Types.NUMERIC));
    }
    preparedStatementMock.expects(once()).method("executeUpdate").will(returnValue(1));
    return data;
  }

  protected Object[] setupWriteBatchDataAndExpectationsBatchingEnabled() {
    Object[] data = new Object[] { SampleXMLOne, SampleXMLTwo, SampleXMLThree };
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      Document dataElement = (Document) data[dataIndex];
      for (int i = 0; i < ColumnNames.length; i++) {
	Node node = dataElement.getRootElement().selectSingleNode(ColumnNames[i]);
	if (node != null) {
	  preparedStatementMock.expects(once()).method("setObject").with(eq(i+1), eq(node.getStringValue()), eq(Types.NUMERIC));
	} else {
	  preparedStatementMock.expects(once()).method("setNull").with(eq(i+1), eq(Types.NUMERIC));
	}
      }
    }
    preparedStatementMock.expects(atLeastOnce()).method("addBatch");
    preparedStatementMock.expects(once()).method("executeBatch").will(returnValue(new int[] {1, 1, 1}));
    return data;
  }

  protected Object[] setupWriteBatchDataAndExpectationsBatchingDisabled() {
    Object[] data = new Object[] { SampleXMLOne, SampleXMLTwo, SampleXMLThree };
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      Document dataElement = (Document) data[dataIndex];
      for (int i = 0; i < ColumnNames.length; i++) {
	Node node = dataElement.getRootElement().selectSingleNode(ColumnNames[i]);
	if (node != null) {
	  preparedStatementMock.expects(once()).method("setObject").with(eq(i+1), eq(node.getStringValue()), eq(Types.NUMERIC));
	} else {
	  preparedStatementMock.expects(once()).method("setNull").with(eq(i+1), eq(Types.NUMERIC));
	}
      }
    }
    preparedStatementMock.expects(never()).method("addBatch");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(atLeastOnce()).method("executeUpdate").will(returnValue(1));
    return data;
  }
}
