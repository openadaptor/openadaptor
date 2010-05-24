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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

public class HostedFolder extends HostedNode {
  private static final Log log = LogFactory.getLog(HostedFolder.class);

  private static final int ROOT_ID=0;
  
  protected void setId(int id) {
    super.setId(id);
    setPath(FOLDER_PATH+id);
  }
  
  public HostedFolder(HostedConnection connection) {
    super(connection);
    setId(ROOT_ID);
  }
  
  public HostedFolder(HostedFolder parent,String name,String description,int id) {
    super(parent);
    setName(name);
    setDescription(description);
    setId(id);
  }
  
  public HostedFolder getSubFolder(String name) {
    HostedFolder match=null;
    Iterator it=getSubFolders().iterator();
    while((match==null) && it.hasNext()) {
      HostedFolder folder=(HostedFolder)it.next();
      if (name.equals(folder.getName())) {
        match=folder;
      }
    }
    return match;
  }

  public List getSubFolders() {
    Document document=fetchDocument();  //Refresh the document.
    log.debug("Fetching subfolders for "+getName());
    Element current = (Element)document.selectSingleNode(FOLDER_NODE_SELECT_XPATH);
    List subFolders=new ArrayList();
    List currentList = (List)current.selectNodes(SUBFOLDER_LIST_NODES_XPATH);
    Iterator iter = currentList.iterator();
    while (iter.hasNext()){
      Element anchor = (Element)iter.next();
      String name = anchor.getTextTrim();
      // The current CEE has '(<number of files>)' at the end of each folder
      // so we need to go backwards and find the last '(' and remove everything
      // from there.
      int index = name.lastIndexOf("(");
      if (index != -1)
        name = name.substring(0, index - 1);
      String href = anchor.attributeValue("href");

      int sidx = href.indexOf("?folderID=");
      int eidx = href.indexOf("&expandFolder=");
      if (sidx == -1 || eidx == -1) {
        log.warn("Failed to parse the link " + href);
      }

      int id = Integer.parseInt(href.substring(sidx + 10, eidx)); // 10 = "?folderID=".length()
      HostedFolder subFolder=new HostedFolder(this,name,null,id);
      subFolders.add(subFolder);
    }
    log.debug("Fetched "+subFolders.size()+" subfolders.");
    return subFolders;
  }
  
  public HostedFile getFile(String name) {
    HostedFile match=null;
    Iterator it=getFiles().iterator();
    while((match==null) && it.hasNext()) {
      HostedFile file=(HostedFile)it.next();
      if (name.equals(file.getName())) {
        match=file;
      }
    }
    return match;   
  }

  public List getFiles() {
    Document document=fetchDocument();
    log.debug("Fetching files in folder "+getName());
    List files=new ArrayList();
    List trs = document.selectNodes(FILE_LIST_NODES_XPATH);
    // row 0 == header
    for(int i = 1; i < trs.size(); i++){
      Element tr = (Element)trs.get(i);
      HostedFile file=new HostedFile(this,tr);
      files.add(file);
    }
    log.debug("Fetched "+files.size()+" files.");
    return files;
  }
  public void addFile(String filePath,String name,String status,String description){
    getConnection().addFile(this.getId(),name,status,description,filePath);   
  }
  
  public void addFile(String filePath,String description) {
    addFile(filePath,null,HostedFile.STABLE,description);
  }
  
  public void delete(String filename) {
    HostedFile file=getFile(filename);
    if (file!=null) {
      getConnection().deleteFile(file.getId());
    }
  }
  
}
