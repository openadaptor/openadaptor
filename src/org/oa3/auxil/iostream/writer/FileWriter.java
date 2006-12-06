/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.auxil.iostream.writer;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/writer/FileWriter.java,v 1.2 2006/07/25 09:06:03 fennelr Exp $
 * Rev: $Revision: 1.2 $ Created Feb 23, 2006 by Eddy Higgins
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.OAException;
import org.oa3.util.FileUtils;

/**
 * Simple writer that writes data to the specified file
 * 
 * @author OA3 Core Team
 */
public class FileWriter extends AbstractStreamWriter {
  
  private static final Log log = LogFactory.getLog(FileWriter.class);

  /**
   * The output file path
   */
  private String path;

  /**
   * Flag to indicate whether the file should be overwritten (false) or appended to (true). Default is to append.
   */
  private boolean append = true;

  /**
   * If defined then the FileWriter will rename any existing file when it starts up. Defaults to null so the file will
   * not be moved
   */
  private String moveExistingFileTo = null;

  // BEGIN Bean getters/setters
  public boolean isAppend() {
    return append;
  }

  public String getMoveExistingFileTo() {
    return moveExistingFileTo;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) throws OAException {
    this.path = path;
  }

  public void setAppend(boolean append) {
    this.append = append;
    log.info("Will " + (append ? "append" : "overwrite") + " file");
  }

  public void setMoveExistingFileTo(String path) {
    if (path != null) {
      this.moveExistingFileTo = path;
      log.info("Existing file (if it exists) will be moved to " + path);
    }
  }

  // END Bean getters/setters

  /**
   * Creates a connection to the file defined by the "path" field for writing out the data. If the field
   * "moveExistingFileTo" has been set and a file with the same name exists then it is moved prior to writing out any
   * data.
   * 
   * @throws OAException
   */
  public void connect() throws OAException {
    if (moveExistingFileTo != null) {
      File f = new File(path);
      if (f.exists())
        FileUtils.moveFile(path, moveExistingFileTo);
    }

    log.debug("Opening File " + path);
    try {
      if (path != null) {
        outputStream = new FileOutputStream(path, append);
      } else {
        outputStream = System.out;
      }
      super.connect();
    } catch (IOException ioe) {
      // Only catching exceptions that the super class doesn't
      log.error("Failed to open file - " + path + ". Exception - " + ioe.toString());
      throw new OAException("Failed to open path " + path, ioe);
    }
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   * 
   * @throws OAException
   *           if there was a problem with AbstractStreamWriter.disconnect()
   */
  public void disconnect() {
    log.debug("Disconnecting from " + path);
    super.disconnect();
  }
}
