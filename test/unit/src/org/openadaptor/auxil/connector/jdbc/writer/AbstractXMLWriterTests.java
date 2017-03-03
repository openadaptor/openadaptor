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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.jmock.Mock;

/**
 * Abstract superclass for XML writer tests.
 *
 * @author cawthorng
 */
public abstract class AbstractXMLWriterTests extends AbstractSQLWriterTests {

  protected static Document SampleXMLOne;
  protected static Document SampleXMLTwo;
  protected static Document SampleXMLThree;
  
  static {
    try {
      SampleXMLOne = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><row><TRADEID>1</TRADEID><BUYSELL>B</BUYSELL><SECID>1</SECID><PARTYID>1</PARTYID><QTY>1000000</QTY><PRICE>3.251</PRICE></row>");
      SampleXMLTwo = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><row><TRADEID>2</TRADEID><BUYSELL>S</BUYSELL><SECID>2</SECID><PARTYID>2</PARTYID><QTY>2000000</QTY><PRICE>3.252</PRICE></row>");
      SampleXMLThree = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><row><TRADEID>3</TRADEID><SECID>3</SECID><PARTYID>3</PARTYID><QTY>3000000</QTY><PRICE>3.253</PRICE></row>");
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected Mock statementMock;
  protected Mock resultSetMock;
  protected Mock resultSetMetaDataMock;
  
  protected void setUp() throws Exception {
    super.setUp();
    statementMock = mock(Statement.class);
    resultSetMock = mock(ResultSet.class);
    resultSetMetaDataMock = mock(ResultSetMetaData.class);
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
    statementMock = null;
    resultSetMock = null;
    resultSetMetaDataMock = null;
  }
  
  protected Object[] setupDataForWriteNotInitialised() {
    Object[] data = new Object [] { SampleXMLOne };
    // Test should bail before any of these methods get called (with or without batching).
    preparedStatementMock.expects(never()).method("clearParameters");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(never()).method("close");
    return data;
  }
}
