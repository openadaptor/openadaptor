package org.openadaptor.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Util {

  public static String createTempFile(File dir, List records, String startDelim, String endDelim) {
    try {
      File f = File.createTempFile("temp", "", dir);
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
