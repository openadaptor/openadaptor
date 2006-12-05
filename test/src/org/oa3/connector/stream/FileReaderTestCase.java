package org.oa3.connector.stream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.oa3.iostream.reader.FileReader;
import org.oa3.iostream.reader.StreamReadConnector;
import org.oa3.iostream.reader.StringRecordReader;

public class FileReaderTestCase extends TestCase {

  private static final File TEMP_DIR = new File("test/output");

  public void testSingleRecord() {
    StreamReadConnector connector = new StreamReadConnector();

    FileReader streamReader = new FileReader();
    List input = new ArrayList();
    input.add("larry");
    input.add("curly");
    input.add("mo");
    String filename = createTempFile(input, null, "\n");
    streamReader.setPath(filename);
    connector.setStreamReader(streamReader);
    connector.connect();
    List output = new ArrayList();
    while (!connector.isDry()) {
      Object[] data = connector.next(0);
      for (int i = 0; i < data.length; i++) {
        output.add(data[i]);
      }
    }
    connector.disconnect();
    String s = "";
    for (Iterator iter = input.iterator(); iter.hasNext();) {
      s += (String) iter.next() + "\n";
    }
    assertTrue(output.size() == 1);
    if (!s.equals(output.get(0))) {
      System.err.println("expected output...");
      System.err.println(s);
      System.err.println("actual output...");
      System.err.println(output.get(0));
      fail("output does not match expected output");
    }
  }
  
  public void testLineRecords() {
    StreamReadConnector connector = new StreamReadConnector();

    FileReader streamReader = new FileReader();
    List input = new ArrayList();
    input.add("larry");
    input.add("curly");
    input.add("mo");
    String filename = createTempFile(input, null, "\n");
    streamReader.setPath(filename);
    connector.setStreamReader(streamReader);
    
    StringRecordReader recordReader = new StringRecordReader();
    connector.setRecordReader(recordReader);
    
    connector.connect();
    List output = new ArrayList();
    while (!connector.isDry()) {
      Object[] data = connector.next(0);
      for (int i = 0; data != null && i < data.length; i++) {
        output.add(data[i]);
      }
    }
    connector.disconnect();
    if (!input.equals(output)) {
      System.err.println("expected output...");
      for (Iterator iter = input.iterator(); iter.hasNext();) {
        System.err.println((String) iter.next());
      }
      System.err.println("actual output...");
      for (Iterator iter = output.iterator(); iter.hasNext();) {
        System.err.println((String) iter.next());
      }
      fail("output does not match expected output");
    }
  }
  
  /*
  public void testMultiLineRecords() {
    StreamReadConnector connector = new StreamReadConnector();

    FileReader streamReader = new FileReader();
    List input = new ArrayList();
    input.add("larry");
    input.add("curly");
    input.add("mo");
    String filename = createTempFile(input, "{", "}\n");
    streamReader.setPath(filename);
    connector.setStreamReader(streamReader);
    
    MultiLineStringRecordReader recordReader = new MultiLineStringRecordReader();
    recordReader.setRecordStartRegex("\\{.*");
    recordReader.setRecordEndRegex(".*\\}\n");
    connector.setRecordReader(recordReader);
    
    connector.connect();
    List output = new ArrayList();
    while (!connector.isDry()) {
      Object[] data = connector.next(0);
      for (int i = 0; data != null && i < data.length; i++) {
        output.add(data[i]);
      }
    }
    connector.disconnect();
    List expectedOutput = new ArrayList();
    for (Iterator iter = input.iterator(); iter.hasNext();) {
      expectedOutput.add(new String("{" + iter.next() + "}"));
      
    }
    if (!expectedOutput.equals(output)) {
      System.err.println("expected output...");
      for (Iterator iter = expectedOutput.iterator(); iter.hasNext();) {
        System.err.println((String) iter.next());
      }
      System.err.println("actual output...");
      for (Iterator iter = output.iterator(); iter.hasNext();) {
        System.err.println((String) iter.next());
      }
      fail("output does not match expected output");
    }
  }
 */
  
  public static String createTempFile(List records, String startDelim, String endDelim) {
    try {
      File f = File.createTempFile("temp", "", TEMP_DIR);
      BufferedWriter writer = new BufferedWriter(new FileWriter(f));
      for (Iterator iter = records.iterator(); iter.hasNext();) {
        String record = (String) iter.next();
        writer.write(startDelim != null ? startDelim : "");
        writer.write(record);
        writer.write(endDelim != null ? endDelim : "");
      }
      writer.close();
      return f.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
