package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;

/**
 * Reads fixed width blocks from a stream. Must have it's bufferLength
 * property set.
 * 
 * @author perryj
 *
 */
public class FixedWidthReader extends EncodingAwareObject implements IDataReader {

  private InputStreamReader reader;

  private char[] buffer;

  public void setBufferLength(int bufferLength) {
    buffer = new char[bufferLength];
  }

  public Object read() throws IOException {
    int len = reader.read(buffer, 0, buffer.length);

    if (len == buffer.length) {
      return new String(buffer);
    } else if (len >= 0) {
      throw new RuntimeException("not enough chars to read");
    } else {
      return null;
    }
  }

  public void setInputStream(final InputStream inputStream) {
    reader = createInputStreamReader(inputStream);
  }

  public void close() throws IOException {
    reader.close();
  }
}
