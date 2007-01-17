/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.auxil.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ExceptionFileStore implements ExceptionStore {

  private static final Log log = LogFactory.getLog(ExceptionFileStore.class);
  
  private static final String LAST_ID_XML = "lastId.xml";
  
  File dir;
  File idfile;
  long lastId;
  
  public ExceptionFileStore(String dirname) {
    this.dir = new File(dirname);
    if (dir.exists() && ! dir.isDirectory()) {
      throw new RuntimeException(dirname + " is not a directory");
    }
    if (!dir.exists() && !dir.mkdir()) {
      throw new RuntimeException(dirname + " cannont be created");
    }
    idfile = new File(dir, LAST_ID_XML);
    if (idfile.exists()) {
      readLastId();
    } else {
      lastId = 0;
    }
  }
  
  private synchronized void readLastId() {
    SAXReader reader = new SAXReader();
    try {
      Document doc = reader.read(idfile);
      Node node = doc.selectSingleNode("id");
      lastId = Long.parseLong(node.getText());
    } catch (DocumentException e) {
      throw new RuntimeException("failed to read lastId", e);
    }
  }

  private synchronized String getNextId() {
    String id = String.valueOf(++lastId);
    try {
      XMLWriter writer = new XMLWriter(new FileWriter(idfile));
      Document doc = DocumentHelper.createDocument();
      doc.addElement("id").setText(id);
      writer.write(doc);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("failed to write lastId", e);
    }
    return id;
  }

  public List getIds(final ExceptionSummary filter) {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        boolean accept = name.endsWith(".xml") && !name.equals(idfile.getName());
        if (accept && filter.getId() != null) {
          accept &= name.startsWith(filter.getId());
        }
        if (accept && filter.getFrom() != null) {
          accept &= name.indexOf(filter.getFrom()) > 0;
        }
        if (accept && filter.getDate() != null) {
          long lower = filter.getTime();
          long upper = lower + (24 * 60 * 60 * 1000);
          String timeString = name.split("-")[2].replaceFirst("\\.xml", "");
          long time = Long.parseLong(timeString);
          accept &=  time >= lower && time < upper;
        }
        return accept;
      }
    });
    
    List ids = new ArrayList();
    for (int i = 0; i < files.length; i++) {
      ids.add(files[i].split("-")[0]);
    }
    Collections.sort(ids);
    Collections.reverse(ids);
    return ids;
  }

  public String getExceptionForId(final String id) {
    return getExceptionDocument(id).asXML();
  }

  private Document getExceptionDocument(final String id) {
    try {
      SAXReader reader = new SAXReader();
      return reader.read(new File(dir, getFilename(id)));
    } catch (DocumentException e) {
      throw new RuntimeException("failed to exception " + e.getMessage(), e);
    }
  }

  private String getFilename(final String id) {
    if (id == null || id.length() == 0) {
      throw new RuntimeException("id is null or empty");
    }
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith(id) && name.endsWith(".xml");
      }
    });
    if (files.length == 0) {
      throw new RuntimeException("id " + id + " not found");
    }
    return files[0];
  }

  public String store(String exception) {
    String id = getNextId();
    try {
      Document doc = DocumentHelper.parseText(exception);
      ExceptionSummary summary = new ExceptionSummary();
      XMLUtil.populateSummary(doc, summary);
      String filename = id + "-" + summary.getFrom() + "-" + summary.getTime() + ".xml";
      save(doc, filename);
    } catch (Exception e) {
      log.error("failed to store exception " + exception);
      throw new RuntimeException("failed to store exception", e);
    }
    return id;
  }

  private void save(Document doc, String filename) {
    try {
      XMLWriter writer = new XMLWriter(new FileWriter(new File(dir, filename)));
      writer.write(doc);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("failed to save file, " + e.getMessage(), e);
    }
  }

  public void incrementRetryCount(String id) {
    String filename = getFilename(id);
    Document doc = getExceptionDocument(id);
    XMLUtil.incrementRetryCount(doc);
    save(doc, filename);
  }

  public void delete(final String id) {
    File file = new File(dir, getFilename(id));
    log.info("deleted " + file.getName());
    file.delete();
  }

  public ExceptionSummary getExceptionSummary(String id) {
    Document doc = getExceptionDocument(id);
    ExceptionSummary summary = new ExceptionSummary();
    summary.setId(id);
    XMLUtil.populateSummary(doc, summary);
    return summary;
  }

  public String[] getStackTrace(String id) {
    Document doc = getExceptionDocument(id);
    return XMLUtil.getStackTrace(doc);
  }
  
  public String getDataForId(String id) {
    Document doc = getExceptionDocument(id);
    return XMLUtil.getData(doc);
  }

}
