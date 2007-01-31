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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;

/**
 * StreamReader which will read files from a directory.
 * <p>
 * It finds all files which match an optional <code>FileNameFilter</code> and supplies them all as a continuous
 * Stream.
 * 
 * @author Eddy Higgins
 */
public class DirectoryReadConnector extends AbstractStreamReadConnector {
  private static final Log log = LogFactory.getLog(DirectoryReadConnector.class);

  /**
   * Path to the directory being read.
   */
  private File dir;

  private List files = new ArrayList();
  
  private File currentFile;
  
  private FilenameFilter filter;

  public DirectoryReadConnector() {
    super();
    setDataReader(new LineReader());
  }

  public DirectoryReadConnector(String id) {
    super(id);
    setDataReader(new LineReader());
  }

  public void setDirname(String path) {
    dir = new File(path);
  }

  public void setFilenameFilter(FilenameFilter filter) {
    this.filter = filter;
  }

  public void setFilenameRegex(String regex) {
    final Matcher matcher = Pattern.compile(regex).matcher("");
    setFilenameFilter(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return matcher.reset(name).matches();
      }
    });
  }

  public void connect() {
    if (!dir.exists() || !dir.isDirectory()) {
      throw new RuntimeException("dir " + dir.toString() + " does not exist or is not a directory");
    }
    files.addAll(Arrays.asList(dir.listFiles(filter)));
    Collections.sort(files);
    super.connect();
  }
  
  protected InputStream getInputStream() throws IOException {
    return getNextInputStream();
  }

  public boolean isDry() {
    if (super.isDry() && !files.isEmpty()) {
      setInputStream(getNextInputStream());
    }
    return super.isDry();
  }

  public Object getReaderContext() {
    return currentFile.getAbsolutePath();
  }

  private InputStream getNextInputStream() {
    closeInputStream();
    File f = (File) files.remove(0);
    if (f != null) {
      try {
        currentFile = f;
        log.info(getId() + " opening " + f.getAbsolutePath() + "...");
        return new FileInputStream(f);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("FileNotFoundException, " + e.getMessage(), e);
      }
    } else {
      currentFile = null;
      return null;
    }
  }
}
