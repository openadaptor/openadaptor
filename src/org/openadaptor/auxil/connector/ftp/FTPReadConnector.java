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

package org.openadaptor.auxil.connector.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.AbstractStreamReadConnector;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * A Read Connector that gets files from an FTP site. Needs to be configured with an instance
 * of {@link IFTPConnection}, file(s) to get and a {@link IDataReader}
 * 
 * Based on the original ideas by Mike Day <p/>
 * 
 * @author Russ Fennell, Eddy Higgins
 * 
 * @see IFTPConnection
 * @see AbstractFTPLibrary
 */
public class FTPReadConnector extends AbstractStreamReadConnector {
  
  private static final Log log = LogFactory.getLog(FTPReadConnector.class);

  private String dir;

  private String file;

  private boolean deleteFile = false;

  protected IFTPConnection connection;

  private List filenames = new ArrayList();
  
  private String currentFilename;

  public FTPReadConnector() {
    super();
  }

  public FTPReadConnector(String id) {
    super(id);
  }
  
  public void setConnection(IFTPConnection ftp) {
    this.connection = ftp;
  }

  public void setDir(String s) {
    this.dir = s;
  }

  /**
   * Sets the name of the file to retrieve. If you specify a wildcard in then
   * all files that match the pattern will be retrieved.
   */
  public void setFile(String s) {
    this.file = s;
  }

  /**
   * Set to true if the remote file(s) is to be be deleted after it has been
   * read
   */
  public void setDeleteFile(boolean b) {
    this.deleteFile = b;
  }

  protected InputStream getInputStream() throws IOException {
    return getNextInputStream();
  }

  public boolean isDry() {
    if (super.isDry() && !filenames.isEmpty()) {
      setInputStream(getNextInputStream());
    }
    return super.isDry();
  }

  public Object getReaderContext() {
    return currentFilename;
  }

  private InputStream getNextInputStream() {
    closeInputStream();
    if (deleteFile && currentFilename != null) {
      connection.delete(currentFilename);
    }
    if (!filenames.isEmpty()) {
      currentFilename = (String) filenames.remove(0);
      log.info(getId() + " getting " + currentFilename + "...");
      return connection.get(currentFilename);
    } else {
      currentFilename = null;
      return null;
    }
  }

  /**
   * Connects to the ftp server, logs in and establishes list of file to get.
   * 
   * @throws ConnectionException
   *           if there was any problems with the FTP client actions
   */
  public void connect() {

    connection.connect();
    connection.logon();
    if (dir != null) {
      connection.cd(dir);
    }

    filenames.clear();
    if (file.indexOf("*") > -1) {
      String[] files = connection.fileList(file);
      if (files == null) {
        throw new ConnectionException("No files match the pattern [" + file + "]", this);
      }
      filenames.addAll(Arrays.asList(files));
    } else {
      filenames.add(file);
    }

    StringBuffer buffer = new StringBuffer();
    for (Iterator iter = filenames.iterator(); iter.hasNext();) {
      String filename = (String) iter.next();
      buffer.append(buffer.length() > 0 ? "," : "").append(filename);
    }
    log.info(getId() + " files={" + buffer.toString() + "}");
    super.connect();
  }

  /**
   * closes the FTP connection
   */
  public void disconnect() {
    connection.close();
    super.disconnect();
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    if (connection == null) {
      exceptions.add(new ValidationException("connection property not configured", this));
    }
  }
}
