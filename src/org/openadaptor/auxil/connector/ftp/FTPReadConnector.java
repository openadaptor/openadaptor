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

package org.openadaptor.auxil.connector.ftp;

import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.thirdparty.apache.AbstractFTPLibrary;

/**
 * This component will provide basic File Transfer Protocol connunication to allow the adaptor to GET a file from a
 * remote machine. <p/>
 * 
 * If no file matches the SourceFile pattern then a null is returned otherwise the matching file is transferred and its
 * contents returned. <p/>
 * 
 * The actual transfer is performed by the FTP library defined in the properties file. There are several reference
 * implementations. The default is to use SunFTP which in turn uses the sun.net.ftp libraries. You can specify your own
 * class but it must implement the FTP interface. <p/>
 * 
 * Based on the original ideas by Mike Day <p/>
 * 
 * </blockquote>
 * 
 * @author Russ Fennell, Eddy Higgins
 * 
 * @see IFTPLibrary
 * @see AbstractFTPLibrary
 */
public class FTPReadConnector extends Component implements IReadConnector {
  
  private static final Log log = LogFactory.getLog(FTPReadConnector.class);

  // list of file names to be retrieved from the remote server
  private ArrayList _fileNames = new ArrayList();

  // list of the records held in the current file. A single record
  // is returned on every poll of the listener
  private ArrayList records = new ArrayList();

  private String sourceDir = "./";

  private String sourceFile;

  private boolean deleteSourceFile = false;

  private String recordSeparator = "\n";

  protected IFTPLibrary ftp = new SunFTPLibrary();

  private String currentfileName;

  public FTPReadConnector() {
  }

  public FTPReadConnector(String id) {
    super(id);
  }
  
  /**
   * Set the FTP library that will perform the actual file transfer. Defaults to use the SunFTPLibrary
   */
  public void setFtpLibrary(IFTPLibrary ftp) {
    this.ftp = ftp;
  }

  /**
   * Set the location of the files on the remote server. Defaults to the user's home directory
   */
  public void setSourceDir(String s) {
    this.sourceDir = s;
  }

  /**
   * Sets the name of the file to retrieve. If you specify a wildcard in then all files that match the pattern will be
   * retrieved. On every poll the next file in the list is returned until there are no more. The listener will exit at
   * this point.
   */
  public void setSourceFile(String s) {
    this.sourceFile = s;
  }

  /**
   * Set to true if the remote file is to be be deleted after it has been transferred
   */
  public void setDeleteSourceFile(boolean b) {
    this.deleteSourceFile = b;
  }

  /**
   * Sets the string that separates each record in the file. Defaults to a newline character ("\n")
   */
  public void setRecordSeparator(String s) {
    this.recordSeparator = s;
  }

  /**
   * @return the FTP library that will perform the actual file transfer
   */
  public IFTPLibrary getFtpLibrary() {
    return ftp;
  }

  /**
   * @return the location of the files on the remote server
   */
  public String getSourceDir() {
    return sourceDir;
  }

  /**
   * @return the name of the file to retrieve. May contain wildcards
   */
  public String getSourceFile() {
    return sourceFile;
  }

  /**
   * @return true if the remote file is to be be deleted after it has been transferred
   */
  public boolean isDeleteSourceFile() {
    return deleteSourceFile;
  }

  /**
   * @return the string that separates each record in the file
   */
  public String getRecordSeparator() {
    return recordSeparator;
  }

  /**
   * Retrieves the remote file(s) from the server. The contents of each file are split according to the record seperator
   * and placed in a list. On each poll of the listener an IMessage containing a record from this list is returned. Once
   * the list is exhausted, the next file is retrieved. <p/>
   * 
   * Deletes the source file if requested after it has been transferred. <p/>
   * 
   * If wildcards are specified in the SourceFile property then each file that matches the pattern will be returned. On
   * every poll the next file in the list is returned until there are no more. The listener will exit at this point.
   * 
   * @return an array containing a single element (the next record) or null
   * 
   * @throws ComponentException -
   *           if there are any problems transferring the file
   */
  public Object[] next(long timeoutMs) {
    Object[] result = null;

    // if there are no records to process, we retrieve the next file from the
    // remote server and fill the records list with its contents
    while (records.size() == 0) {
      String contents = getNextFile();

      if (contents == null)
        return null;

      if (contents.equals("")) {
        log.warn("Empty file retrieved. Ignoring and getting next file in the list");
        continue;
      }

      // split out the records from the contents
      String[] recs = contents.split(recordSeparator);
      for (int i = 0; i < recs.length; i++)
        records.add(recs[i]);
    }

    // get next record and remove it from the list
    String record = (String) records.get(0);
    records.remove(0);

    // create the message
    if (record != null) {
      result = new Object[] { record };
    }

    return result;
  }

  /**
   * Retrieves the next file in the list and returns its contents or null if there are no files left. Will delete the
   * source file after it has been retrieved (if required)
   * 
   * @return the file contents or null
   */
  private String getNextFile() {
    // set the listener to exit if there are no more files to retrieve
    if (_fileNames.size() == 0) {
      log.info("No more files to retrieve");
      return null;
    }

    currentfileName = (String) _fileNames.get(0);
    _fileNames.remove(0);
    log.info("Transferring " + currentfileName);

    String contents;
    try {
      // set up a Reader to read the file from the FTP stream
      InputStreamReader in = ftp.get(currentfileName);
      if (in == null)
        return null;
      log.debug("File transfer stream created for " + currentfileName);

      // transfer the file and close the reader
      StringWriter writer = new StringWriter();
      char[] buf = new char[1024];
      int count;

      while ((count = in.read(buf)) != -1)
        writer.write(buf, 0, count);

      contents = writer.toString();

      if (!ftp.verifyFileTransfer())
        throw new ComponentException("Failed to transfer file", this);

      log.info("File transferred: " + contents.length() + " btye(s)");

      // delete the source file is required
      if (deleteSourceFile) {
        log.info("Deleting " + currentfileName);
        ftp.delete(currentfileName);
      }
    } catch (Exception e) {
      throw new ComponentException("Failed to retrieve source file(s) [" + currentfileName + "]: " + e.getMessage(), this);
    }

    return contents;
  }

  /**
   * Conencts to the remote server and logs in. Sets up an array of the one or many file names to be retrieved.
   * 
   * @throws ComponentException
   *           if there was any problems with the FTP client actions
   */
  public void connect() {
    // connect to the remote server and log in using the reqeusted FTP library
    ftp.connect();
    ftp.logon();
    ftp.cd(sourceDir);

    // if we have a wildcard in the file name then we assume that we are
    // performing a mget operation
    if (sourceFile.indexOf("*") > -1) {
      String[] files = ftp.fileList(sourceFile);
      if (files == null)
        throw new ComponentException("No files match the pattern [" + sourceFile + "]", this);

      for (int i = 0; i < files.length; i++)
        _fileNames.add(files[i]);
    } else {
      // otherwise we simply add the single file name to the array
      _fileNames.add(sourceFile);
    }

    // display list of the file(s) to be retrieved
    log.debug("Retrieving:");
    for (int i = 0; i < _fileNames.size(); i++)
      log.debug("\t " + sourceDir + File.separator + _fileNames.get(i));

  }

  /**
   * Calls disconnect() on the super class and closes the FTP connection
   */
  public void disconnect() {
    ftp.close();
    log.info(getId() + " Connection closed");
  }

  public Object getReaderContext() {
    return currentfileName;
  }

  public boolean isDry() {
    return _fileNames.isEmpty();
  }

  public void validate(List exceptions) {
  }
}
