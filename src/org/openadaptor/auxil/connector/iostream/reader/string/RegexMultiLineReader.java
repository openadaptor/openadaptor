/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
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

public class RegexMultiLineReader extends EncodingAwareObject implements IDataReader {

  private static final Log log = LogFactory.getLog(RegexMultiLineReader.class);

  private BufferedReader reader;

  private Matcher startLineMatcher;

  private Matcher endLineMatcher;

  private boolean includeRecordDelimiters;

  public void setStartLineRegex(String regex) {
    startLineMatcher = Pattern.compile(regex).matcher("");
  }

  public void setEndLineRegex(String regex) {
    endLineMatcher = Pattern.compile(regex).matcher("");
  }

  public Object read() throws IOException {
    StringBuffer buffer = new StringBuffer();
    String line;
    boolean inRecord = false;
    while ((line = reader.readLine()) != null) {
      if (inRecord) {
        if (endLineMatcher.reset(line).matches()) {
          if (includeRecordDelimiters) {
            buffer.append(buffer.length() > 0 ? "\n" + line : line);
          }
          return buffer.toString();
        } else {
          buffer.append(buffer.length() > 0 ? "\n" + line : line);
        }
      } else {
        if (startLineMatcher.reset(line).matches()) {
          if (includeRecordDelimiters) {
            buffer.append(buffer.length() > 0 ? "\n" + line : line);
          }
          inRecord = true;
        } else {
          log.debug("discarding line " + line);
        }
      }
    }
    if (buffer.length() > 0) {
      throw new RuntimeException("partial record, " + buffer.toString());
    }
    return null;
  }

  public void setInputStream(InputStream inputStream) {
    reader = new BufferedReader(createInputStreamReader(inputStream));
  }

  public void setIncludeRecordDelimiters(boolean include) {
    this.includeRecordDelimiters = include;
  }

}
