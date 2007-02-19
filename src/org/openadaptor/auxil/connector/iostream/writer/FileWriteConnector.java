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

package org.openadaptor.auxil.connector.iostream.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.writer.string.LineWriter;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.util.FileUtils;

/**
 * A Write Connector that write data to a file (or stdout if the filename property
 * is not set).
 * 
 * @author OA3 Core Team
 */
public class FileWriteConnector extends AbstractStreamWriteConnector {

  private static final Log log = LogFactory.getLog(FileWriteConnector.class);

  private String filename;

  private boolean append = true;

  private String moveExistingFileTo = null;

  public FileWriteConnector() {
    super();
    setDataWriter(new LineWriter());
  }

  public FileWriteConnector(String id) {
    super(id);
    setDataWriter(new LineWriter());
  }

  public void setFilename(String path) throws ComponentException {
    this.filename = path;
  }

  public void setAppend(boolean append) {
    this.append = append;
    log.info(getId() + " will " + (append ? "append" : "overwrite") + " file");
  }

  /**
   * sets the path which an existing output file will be moved to
   */
  public void setMoveExistingFileTo(String path) {
    if (path != null) {
      this.moveExistingFileTo = path;
      log.info("Existing file (if it exists) will be moved to " + path);
    }
  }

  protected OutputStream getOutputStream() {
    if (filename != null) {
      if (moveExistingFileTo != null) {
        File f = new File(filename);
        if (f.exists())
          FileUtils.moveFile(filename, moveExistingFileTo);
      }
      try {
        return new FileOutputStream(filename, append);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("FileNotFoundException, " + e.getMessage(), e);
      }
    } else {
      return System.out;
    }
  }

}
