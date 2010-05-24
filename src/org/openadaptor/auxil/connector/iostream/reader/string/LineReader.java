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

package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;

public class LineReader extends EncodingAwareObject implements IDataReader {

  private static final Log log = LogFactory.getLog(LineReader.class);

  // State:
  private BufferedReader reader;
  private boolean inBlockOfRecords;
  
  // Properties:
  private Matcher[] includeMatchers = new Matcher[0];
  private Matcher[] excludeMatchers = new Matcher[0];

  private Matcher startBlockOfRecordsMatcher;
  private Matcher endBlockOfRecordsMatcher;
  private boolean includeBlockOfRecordsDelimiters = false;

  public void setIncludeRegex(String regex) {
    includeMatchers = new Matcher[1];
    includeMatchers[0] = Pattern.compile(regex).matcher("");
  }

  public void setExcludeRegex(String regex) {
    excludeMatchers = new Matcher[1];
    excludeMatchers[0] = Pattern.compile(regex).matcher("");
  }

  public void setIncludeRegexs(String[] regexs) {
    includeMatchers = new Matcher[regexs.length];
    for (int i = 0; i < regexs.length; i++) {
      includeMatchers[i] = Pattern.compile(regexs[i]).matcher("");
    }
  }

  public void setExcludeRegexs(String[] regexs) {
    excludeMatchers = new Matcher[regexs.length];
    for (int i = 0; i < regexs.length; i++) {
      excludeMatchers[i] = Pattern.compile(regexs[i]).matcher("");
    }
  }

  public void setStartBlockOfRecordsRegex(String regex) {
    startBlockOfRecordsMatcher = Pattern.compile(regex).matcher("");
  }

  public void setEndBlockOfRecordsRegex(String regex) {
    endBlockOfRecordsMatcher = Pattern.compile(regex).matcher("");
  }

  public void setIncludeBlockOfRecordsDelimiters(boolean include) {
    this.includeBlockOfRecordsDelimiters = include;
  }

  public Object read() throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      if (inBlockOfRecords) {
        if (endBlockOfRecordsMatcher != null && endBlockOfRecordsMatcher.reset(line).matches()) {
          inBlockOfRecords = false;  // look for another block of records later in inputstream.
          if (includeBlockOfRecordsDelimiters && match(line)) {
            return line;
          }
        } else if (match(line)) {
          return line;
        }
      } else {
        if (startBlockOfRecordsMatcher != null && startBlockOfRecordsMatcher.reset(line).matches()) {
          inBlockOfRecords = true;  // look for a record in this block of records
          if (includeBlockOfRecordsDelimiters && match(line)) {
            return line;
          }
        } else {
          log.debug("discarding line " + line);
        }
      }
    }
    return null;
  }

  public void setInputStream(final InputStream inputStream) {
    reader = new BufferedReader(createInputStreamReader(inputStream));
    // we are implicitly inside a block of records if they have not set a pattern: 
    inBlockOfRecords = (startBlockOfRecordsMatcher == null);
  }

  public boolean match(String string) {
    boolean match = includeMatchers.length == 0;
    for (int i = 0; !match && i < includeMatchers.length; i++) {
      match = includeMatchers[i].reset(string).matches();
    }
    for (int i = 0; i < excludeMatchers.length; i++) {
      match &= !excludeMatchers[i].reset(string).matches();
    }
    return match;
  }

  public void close() throws IOException {
    reader.close();
  }


}
