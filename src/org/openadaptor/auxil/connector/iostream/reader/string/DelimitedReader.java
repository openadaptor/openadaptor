package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;

/**
 * This class reads string records based on a fixed sequence of delimiter
 * characters. It's delimiter / delimiter char codes property must be
 * configured. The includeDelimiter property controls whether the delimiter
 * characters are included in the record.
 * 
 * @author perryj
 * 
 */
public class DelimitedReader extends EncodingAwareObject implements IDataReader {

  private BufferedReader reader;
  private char[] delimiter;
  private boolean includeDelimiter = true;
  
  public void setDelimiter(final String s) {
    delimiter = s.toCharArray();
  }
  
  public void setDelimiterCharCodes(final int[] chars) {
    delimiter = new char[chars.length];
    for (int i = 0; i < chars.length; i++) {
      delimiter[i] = (char)chars[i];
    }
  }
  
  /**
   * Optional
   * 
   * @param include
   *          if true then delimiter characters are returned as part of the
   *          record when {@link #read()} is invoked
   */
  public void setIncludeDelimiter(final boolean include) {
    includeDelimiter = include;
  }
  
  public void close() throws IOException {
    reader.close();
  }

  public Object read() throws IOException {
    StringBuffer buffer = null;
    int i = 0;
    for (int c; (c = reader.read()) != -1; ) {
      buffer = buffer == null ? new StringBuffer() : buffer;
      buffer.append((char)c);
      if (c == delimiter[i]) {
        if (++i == delimiter.length) {
          return includeDelimiter ? buffer.toString() : buffer.substring(0, buffer.length() - i);
        }
      } else {
        i = 0;
      }
    }
    return buffer != null ? buffer.toString() : null;
  }

  public void setInputStream(InputStream inputStream) {
    reader = new BufferedReader(createInputStreamReader(inputStream));
  }

}
