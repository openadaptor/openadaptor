/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import org.dom4j.Document;

public abstract class HostedNode {
  //Relative URLS 
  protected static final String FOLDER_PATH="/servlets/ProjectDocumentList?folderID=";
  //XPATH selectors
  protected static final String FOLDER_NODE_SELECT_XPATH="//DIV[@id='projectdocumentlist']//LI[@class='selection']";
  protected static final String SUBFOLDER_LIST_NODES_XPATH="UL/LI/A[SPAN]";
  protected static final String FILE_NODE_SELECT_XPATH="//DIV[@id='projectdocumentlist']//TD[@class='filebrowse']/DIV/P";
  protected static final String FILE_LIST_NODES_XPATH="//DIV[@id='projectdocumentlist']//TD[@class='filebrowse']/DIV/TABLE/TR";
  
  private int id;
  private String path;
  private String name;
  private String description;
  private HostedNode parent;
  private HostedConnection connection;

  protected void setId(int id) {this.id=id;}
  public int getId() {return id;}
  
  protected void setName(String name) {this.name=name;}
  public String getName() {return name;}

  private void setConnection(HostedConnection connection) {this.connection=connection;}
  protected HostedConnection getConnection() {return connection;}

  protected void setPath(String path){this.path=path;}
  protected String getPath() {return path;}

  protected void setDescription(String description) {this.description=description;}
  protected String getDescription() {return description;}

  protected void setParent(HostedNode parent) {
    this.parent=parent;
    if(parent!=null) {
      setConnection(parent.getConnection());
    }
  }

  protected HostedNode getParent(){return parent;}

  public HostedNode(HostedNode parent) {
    setParent(parent);
  }
  public HostedNode(HostedConnection connection) {
    setConnection(connection);
    setName(connection.getProjectName());
  }
  
  protected Document fetchDocument() {
    return getConnection().fetchDocument(getPath());
  }
  
  protected String generateToStringFields() {
    StringBuffer sb=new StringBuffer();
    sb.append("id=").append(id);
    sb.append(";name=");
    if (name!=null) {
      sb.append(name);
    }
    sb.append(";path=");
    if (path!=null) {
      sb.append(path);
    }
    return sb.toString();
  }

  public String toString() {
    StringBuffer sb=new StringBuffer("{");
    sb.append(generateToStringFields());
    return sb.append("}").toString();
  }
}
