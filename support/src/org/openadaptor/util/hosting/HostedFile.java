/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.util.hosting;

import org.dom4j.Element;

public class HostedFile extends HostedNode {
  
  protected static final String DRAFT="Draft";
  protected static final String REVIEWED="Reviewed";
  protected static final String BASELINED="Baselined";
  public static final String STABLE="Stable"; 
  protected static final String ARCHIVAL="Archival"; 
  protected static final String OBSOLETE="Obsolete";
  public static final String[] STATUS_VALUES= {DRAFT,REVIEWED,BASELINED,STABLE,ARCHIVAL,OBSOLETE};
  
  private static final String NAME_XPATH="TD[1]//A" ;// XPath is one-based
  private static final String LINK_XPATH="TD[7]//A";
  private static final String PATH_PREFIX="";
  private static final String HREF_TAG="href";
  public static final int ID_NOT_FOUND=-1;
  
  private static final String TD_TAG="TD";
  private static final int STATUS_INDEX=1;
  private static final int DESCRIPTION_INDEX=5;

  private String status;
   
  protected void setStatus(String status) {this.status=status;}
  protected String getStatus() {return status;}
  
  public HostedFile(HostedNode parent,Element tr) {
    super(parent);
    setFields(tr);
  }
  
  private void setFields(Element tr) {
    //Name field
    Element anchor = (Element)tr.selectSingleNode(NAME_XPATH);  // XPath is 1-origin
    setName(anchor.getTextTrim());
    //path field
    String path=anchor.attributeValue(HREF_TAG);
    if ((path!=null) && (path.startsWith(PATH_PREFIX))) {
      setPath(path);
    }
    else {
      HostedConnection.fail("Failed to derive path for file from:" +path);
    } 
    setStatus(getTableDataValue(tr, STATUS_INDEX));
    setDescription(getTableDataValue(tr,DESCRIPTION_INDEX));
    setId(extractId((Element)tr.selectSingleNode(LINK_XPATH)));
  }
  private static final int extractId(Element linkElement) {
    String link=linkElement.attributeValue(HREF_TAG);
    int id=ID_NOT_FOUND;
    final String docIdParam="?documentID=";
    int idParamOffset=link.indexOf(docIdParam);
    if (idParamOffset != -1) {
      link=link.substring(idParamOffset+docIdParam.length());
      int idEnd=link.indexOf('&');
      if (idEnd !=-1) { //There are other args, strip them off
        link=link.substring(0,idEnd);
      }
      try {
      id=Integer.parseInt(link);
      }
      catch (NumberFormatException nfe) {
        HostedConnection.fail("Failed to extract id from "+link,nfe);
      }
    }
    else {
      HostedConnection.fail("Failed to locate query parameter "+docIdParam+" from "+link);
    }
    return id;
  }
  
  private static final String getTableDataValue(Element tableRow,int columnIndex){
    return ((Element)tableRow.elements(TD_TAG).get(columnIndex)).getTextTrim();
  }
  protected String generateToStringFields() {
    StringBuffer sb=new StringBuffer(super.generateToStringFields());
    sb.append(";status=");
    if (status!=null) {
      sb.append(status);      
    }
    return sb.toString();
  }

}
