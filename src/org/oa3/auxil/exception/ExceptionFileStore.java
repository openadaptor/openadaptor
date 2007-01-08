package org.oa3.auxil.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

  public String[] getAllIds(int max) {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".xml") && !name.equals(idfile.getName());
      }
    });
    List ids = new ArrayList();
    for (int i = 0; i < files.length; i++) {
      ids.add(files[i].split("-")[0]);
    }
    Collections.sort(ids);
    Collections.reverse(ids);
    String[] id_array = new String[max == 0 ? ids.size() : Math.min(max, ids.size())];
    for (int i = 0; i < id_array.length; i++) {
      id_array[i] = (String) ids.get(i);
    }
    return id_array;
  }

  public String getExceptionForId(final String id) {
    return getExceptionDocument(id).asXML();
  }

  private Document getExceptionDocument(final String id) {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith(id) && name.endsWith(".xml");
      }
    });
    if (files.length == 0) {
      throw new RuntimeException("id " + id + " not found");
    }
    try {
      SAXReader reader = new SAXReader();
      return reader.read(new File(dir, files[0]));
    } catch (DocumentException e) {
      throw new RuntimeException("failed to exception " + e.getMessage(), e);
    }
  }

  public String store(String exception) {
    String id = getNextId();
    try {
      Document doc = DocumentHelper.parseText(exception);
      String from  = doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
          + "/" + MessageExceptionXmlConverter.FROM).getText();
      String time  = doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
          + "/" + MessageExceptionXmlConverter.TIME).getText();
      String filename = id + "-" + from + "-" + time + ".xml";
      XMLWriter writer = new XMLWriter(new FileWriter(new File(dir, filename)));
      writer.write(doc);
      writer.close();
    } catch (Exception e) {
      log.error("failed to store exception " + exception);
      throw new RuntimeException("failed to store exception", e);
    }
    return id;
  }

  public void delete(final String id) {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(id + ".xml");
      }
    });
    if (files.length == 0) {
      throw new RuntimeException("id " + id + " not found");
    }
    File file = new File(dir, files[0]);
    file.delete();
  }

  public void deleteAll() {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".xml") && !name.equals(idfile.getName());
      }
    });
    for (int i = 0; i < files.length; i++) {
      File file = new File(dir, files[i]);
      file.delete();
    }
  }

  public ExceptionSummary getExceptionSummary(String id) {
    Document doc = getExceptionDocument(id);
    ExceptionSummary summary = new ExceptionSummary();
    summary.setMessage(doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
          + "/" + MessageExceptionXmlConverter.EXCEPTION + 
          "/" + MessageExceptionXmlConverter.MESSAGE).getText());
    summary.setId(id);
    summary.setFrom(doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
          + "/" + MessageExceptionXmlConverter.FROM).getText());
    long time = Long.parseLong(doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
          + "/" + MessageExceptionXmlConverter.TIME).getText());
    summary.setDate(new Date(time));
    return summary;
  }

}
