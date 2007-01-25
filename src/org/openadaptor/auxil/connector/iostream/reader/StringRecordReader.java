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

package org.openadaptor.auxil.connector.iostream.reader;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Very basic String Record Reader.
 * <p>
 * It treats <tt>cr</tt> and <tt>lf</tt> as record delimiters.
 * 
 * @author Eddy Higgins
 */
public class StringRecordReader extends AbstractRecordReader {

  //private static final Log log = LogFactory.getLog(StringRecordReader.class);

  private StreamTokenizer st;

  private List includes = new ArrayList();
  private List excludes = new ArrayList();
  
  private boolean eof = false;

  // BEGIN Bean getters/setters
  // END Bean getters/setters

  /**
   * Default Constructor.
   */
  public StringRecordReader() {
  }

  public void setIncludePatterns(String[] regexPatterns) {
    includes.clear();
    for (int i = 0; i < regexPatterns.length; i++) {
      includes.add(Pattern.compile(regexPatterns[i]));
    }
  }
  
  public void setIncludePattern(String regexPattern) {
    includes.clear();
    includes.add(Pattern.compile(regexPattern));
  }
  
  public void setExcludePatterns(String[] regexPatterns) {
    excludes.clear();
    for (int i = 0; i < regexPatterns.length; i++) {
      excludes.add(Pattern.compile(regexPatterns[i]));
    }
  }
  
  public void setExcludePattern(String regexPattern) {
    excludes.clear();
    excludes.add(Pattern.compile(regexPattern));
  }
  
  /**
   * Prepares this reader for use.
   * 
   * @throws IOException
   *           if the underlying <code>Reader</code> is <tt>null</tt>
   */
  public void initialise() throws IOException {
    Reader reader = getReader();
    if (reader == null) {
      throw new IOException("Reader cannot be null");
    }
    st = new StreamTokenizer(reader);
    st.resetSyntax();
    st.wordChars(0, 9);
    st.whitespaceChars(10, 10);
    st.wordChars(11, 12);
    st.whitespaceChars(13, 13);
    st.wordChars(14, 0xffff);
  }

  /**
   * Read the next String record.
   * 
   * @return Object which is a <code>String</code>
   * @throws IOException
   *           if there is a problem
   */
  public Object next() throws IOException {
    if (st == null) {
      initialise();
    }
    boolean reading = true;
    String result = null;
    while (reading) {
      int token = st.nextToken();
      switch (token) {
      case StreamTokenizer.TT_EOF:
        reading = false;
        eof = true;
        break;
      case StreamTokenizer.TT_NUMBER:
        result = st.toString();
        reading = !match(result);
        break;
      case StreamTokenizer.TT_WORD:
        result = st.sval;
        reading = !match(result);
        break;
      case StreamTokenizer.TT_EOL: // ignore for now.
        break;
      default:
        break;
      }
    }
    return result;
  }

  public boolean isDry() {
    return eof;
  }
  
  public boolean match(String s) {
    // if include patterns have been specified then string must match one
    boolean match = includes.size() == 0;
    for (Iterator iter = includes.iterator(); !match && iter.hasNext();) {
      Pattern pattern = (Pattern) iter.next();
      match = pattern.matcher(s).matches();
    }
    
    // if excludes patterns have been specified then string must not match all
    for (Iterator iter = excludes.iterator(); iter.hasNext();) {
      Pattern pattern = (Pattern) iter.next();
      match &= !(pattern.matcher(s).matches());
    }
    return match;
  }
}
