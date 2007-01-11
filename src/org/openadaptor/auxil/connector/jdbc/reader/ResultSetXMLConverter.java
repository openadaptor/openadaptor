package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ResultSetXMLConverter extends ResultSetConverter {

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
