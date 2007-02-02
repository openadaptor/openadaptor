/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.iostream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


public class EncodingAwareObject {

  public static final String US_ASCII = "US-ASCII"; // 7-bit ASCII,aka Basic Latin block of Unicode char set.

  public static final String ASCII = US_ASCII; // Alias - ASCII

  public static final String ISO646_US = US_ASCII; // Alias - ISO646-US

  public static final String ISO_8859_1 = "ISO-8859-1"; // ISO Latin Alphabet #1

  public static final String ISO_LATIN_1 = ISO_8859_1; // Alias - ISO-LATIN-1

  public static final String UTF_8 = "UTF-8"; // Eight-bit UCS Transformation Format

  public static final String UTF_16BE = "UTF-16BE"; // Sixteen-bit UCS Trans. Format, big-endian byte order

  public static final String UTF_16LE = "UTF-16LE"; // Sixteen-bit UCS Trans. Format, little-endian byte order

  public static final String UTF_16 = "UTF-16";// // Sixteen-bit UCS Trans. Format, byte order by optionial byte-order

  private String encoding = null;
  
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }

  public boolean isEncodingSet() {
    return encoding != null;
  }
  
  protected InputStreamReader createInputStreamReader(InputStream is) {
    if (isEncodingSet()) {
      try {
        return new InputStreamReader(is, getEncoding());
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("UnsupportedEncodingException, " + e.getMessage(), e);
      }
    } else {
      return new InputStreamReader(is);
      
    }
  }
  
  protected OutputStreamWriter createOutputStreamReader(OutputStream os) {
    if (isEncodingSet()) {
      try {
        return new OutputStreamWriter(os, getEncoding());
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("UnsupportedEncodingException, " + e.getMessage(), e);
      }
  } else {
    return new OutputStreamWriter(os);
  }
  }
}
