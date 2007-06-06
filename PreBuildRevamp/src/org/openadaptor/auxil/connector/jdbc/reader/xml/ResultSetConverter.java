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

package org.openadaptor.auxil.connector.jdbc.reader.xml;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openadaptor.auxil.connector.jdbc.reader.AbstractResultSetConverter;

public class ResultSetConverter extends AbstractResultSetConverter {

  private boolean convertToString = true;
  private String rootElement = "row";
  private boolean setTypeAttributes = false;
  
  public void setConvertToString(final boolean convertToString) {
    this.convertToString = convertToString;
  }

  public void setSetTypeAttributes(boolean setTypeAttributes) {
    this.setTypeAttributes = setTypeAttributes;
  }

  public void setRootElement(final String rootElement) {
    this.rootElement = rootElement;
  }

  protected Object convertNext(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
    int columnCount = rsmd.getColumnCount();
    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement(rootElement);
    for (int i = 1; i <= columnCount; i++) {
      Element e = root.addElement(rsmd.getColumnName(i));
      String text = rs.getString(i);
      if (text != null) {
        e.setText(text);
      }
      if (setTypeAttributes) {
        e.addAttribute("type", rsmd.getColumnClassName(i));
      }
    }
    return convertToString ? (Object)doc.asXML() : doc;
  }

}
