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

package org.openadaptor.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceUtil {

  public static String readFileContents(String filename) {
    try {
      return readInputStreamContents(new FileInputStream(filename));
    } catch (FileNotFoundException e) {
      throw new RuntimeException("IOException, " + e.getMessage(), e);
    }
  }

  public static String readInputStreamContents(InputStream is) {
    StringBuffer sb = new StringBuffer();
    char[] cbuf = new char[1024];
    int len = 0;
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(is);
      while ((len = reader.read(cbuf, 0, cbuf.length)) != -1) {
        sb.append(cbuf, 0, len);
      }
    } catch (IOException e) {
      throw new RuntimeException("IOException, " + e.getMessage(), e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
        }
      }
    }
    return sb.toString();
  }

  public static String readFileContents(Class caller, String filename) {
    return readInputStreamContents(caller.getResourceAsStream(filename));
  }

  public static String removeCarriageReturns(String s) {
    return s.replaceAll("\\r", "");
  }

  public static String readFileContents(Object caller, String filename) {
    return readFileContents(caller.getClass(), filename);
  }

  public static void writeFileContents(Object caller, String prefix, String filename, String contents) {
    String s = getResourcePath(caller, prefix, filename);
    try {
      FileWriter writer = new FileWriter(s);
      writer.write(contents);
      writer.flush();
      writer.close();
    } catch (IOException ioe) {
      throw new RuntimeException("write file:" + filename + " : " + ioe.getMessage());
    }
  }

  public static String getResourcePath(Object caller, String prefix, String filename) {
    String s = caller.getClass().getPackage().getName();
    s = s.replaceAll("\\.", "/");
    s = prefix + s + "/" + filename;
    return s;
  }

}
