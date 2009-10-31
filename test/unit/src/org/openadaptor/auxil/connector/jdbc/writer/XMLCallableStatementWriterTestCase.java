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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;

import org.dom4j.Document;
import org.dom4j.Node;
import org.jmock.core.Stub;
import org.openadaptor.auxil.connector.jdbc.writer.xml.XMLCallableStatementWriter;

/**
 * Defines a unit test for the {@link XMLCallableStatementWriter}.
 *
 * @author cawthorng
 */
public class XMLCallableStatementWriterTestCase extends AbstractXMLWriterTests {

  protected static String CatalogName = "MockCatalogName";
  protected static String StoredProcName = "MockStoredProcName";
  protected static String PreparedStatementSQL = "{ CALL "+ StoredProcName +"(?,?,?,?,?,?)}";

  protected void setUp() throws Exception {
    super.setUp();
    statementMock = mock(Statement.class);
    resultSetMock = mock(ResultSet.class);
    resultSetMetaDataMock = mock(ResultSetMetaData.class);
  }

  protected ISQLWriter instantiateTestWriter() {
    XMLCallableStatementWriter writer = new XMLCallableStatementWriter();
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
    connectionMock.expects(atLeastOnce()).method("getMetaData").will(returnValue(dbMetaDataMock.proxy()));
    
    dbMetaDataMock.stubs().method("getDatabaseProductName").will(returnValue("Mock Stub DB Product Name"));
    dbMetaDataMock.stubs().method("getDatabaseMajorVersion").will(returnValue(1));
    dbMetaDataMock.stubs().method("getDatabaseMinorVersion").will(returnValue(1));
    dbMetaDataMock.stubs().method("getDatabaseProductVersion").will(returnValue("Mock Stub DB Product version"));
    
    dbMetaDataMock.expects(once()).method("supportsBatchUpdates").will(returnValue(supportsBatch));
    connectionMock.expects(once()).method("getCatalog").will(returnValue(CatalogName));
    dbMetaDataMock.expects(once()).method("getProcedureColumns").with(eq(CatalogName), eq("%"), eq(StoredProcName), eq("%")).will(returnValue(resultSetMock.proxy()));
    resultSetMock.expects(atLeastOnce()).method("next").will(onConsecutiveCalls(new Stub[] {returnValue(true), returnValue(true), returnValue(true), returnValue(true), returnValue(true), returnValue(true), returnValue(false)}));
    resultSetMock.stubs().method("getInt").will(returnValue(2));
    resultSetMock.stubs().method("getString").will(returnValue("Dummy ResultSet Info")); // Not being specific here as this only happens when logging set to debug
    resultSetMock.expects(atLeastOnce()).method("close");
    connectionMock.expects(once()).method("prepareStatement").with(eq(PreparedStatementSQL)).will(returnValue(preparedStatementMock.proxy()));
  }

  protected Object[] setUpSingletonDataAndDataExpections() {
    Object[] data = new Object [] {SampleXMLOne};
    preparedStatementMock.expects(once()).method("clearParameters");
    for (int i = 0; i < SampleXMLOne.getRootElement().nodeCount(); i++) {
      Node node = SampleXMLOne.getRootElement().node(i);
      preparedStatementMock.expects(once()).method("setObject").with(eq(i+1), eq(node.getStringValue()), eq(Types.NUMERIC));
    }
    preparedStatementMock.expects(once()).method("executeUpdate").will(returnValue(1));
    return data;
  }

  /**
   * This method must be overrriden to generate test data consisting of a batch.
   * Associated mock expectations relating to this component must also be set. Note
   * that these expectations are to be set assuming the underlying jdbc layer is configured
   * to enable batch uploads.
   *
   * @return Object[]
   */
  protected Object[] setupWriteBatchDataAndExpectationsBatchingEnabled() {
    Object[] data = new Object[] { SampleXMLOne, SampleXMLTwo, SampleXMLThree };
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      Document dataElement = (Document) data[dataIndex];
      for (int i = 0; i < dataElement.getRootElement().nodeCount(); i++) {
	Node node = dataElement.getRootElement().node(i);
	preparedStatementMock.expects(once()).method("setObject").with(eq(i+1), eq(node.getStringValue()), eq(Types.NUMERIC));
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
    Object[] data = new Object[] { SampleXMLOne, SampleXMLTwo, SampleXMLThree };
    preparedStatementMock.expects(atLeastOnce()).method("clearParameters");
    for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
      Document dataElement = (Document) data[dataIndex];
      for (int i = 0; i < dataElement.getRootElement().nodeCount(); i++) {
	Node node = dataElement.getRootElement().node(i);
	preparedStatementMock.expects(once()).method("setObject").with(eq(i+1), eq(node.getStringValue()), eq(Types.NUMERIC));
      }
    }
    preparedStatementMock.expects(never()).method("addBatch");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(exactly(3)).method("executeUpdate").will(returnValue(1));
    return data;
  }
}
