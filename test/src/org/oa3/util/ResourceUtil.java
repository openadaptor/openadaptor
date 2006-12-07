package org.oa3.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class ResourceUtil {

   public static String readFileContents(Object caller, String filename) {
    StringBuffer sb = new StringBuffer();
    try {
      InputStream in = caller.getClass().getResourceAsStream(filename);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (sb.length() > 0) {
          sb.append("\n");
        }
        sb.append(line);
      }
      br.close();
      in.close();
    } catch (IOException ioe) {
      throw new RuntimeException("read file:" + filename + " : " + ioe.getMessage());
    }
    return sb.toString();
  }

  public static void writeFileContents(Object caller, String filename, String contents) {
    String s = findResource(caller, filename);
    try {
      FileWriter writer = new FileWriter(s);
      writer.write(contents);
      writer.flush();
      writer.close();
    } catch (IOException ioe) {
      throw new RuntimeException("write file:" + filename + " : " + ioe.getMessage());
    }
  }

  public static String findResource(Object caller, String filename) {
    String s = caller.getClass().getPackage().getName();
    s = s.replaceAll("\\.", "/");
    s = "test/src/" + s + "/" + filename;
    return s;
  }

}
