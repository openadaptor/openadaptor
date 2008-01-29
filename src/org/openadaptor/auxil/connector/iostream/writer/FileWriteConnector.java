/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.writer.string.LineWriter;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Write Connector that write data to a file (or stdout if the filename property
 * is not set).
 * 
 * @author OA3 Core Team
 */
public class FileWriteConnector extends AbstractStreamWriteConnector {

  private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSSZ";
  
  private static final Log log = LogFactory.getLog(FileWriteConnector.class);

  private String filename;

  private boolean append = true;

  private String moveExistingFileTo = null;

  private boolean addTimestampToMovedFile = false;

  private String timestampFormat = DEFAULT_TIMESTAMP_FORMAT;
  
  /**
   * Constructor
   */
  public FileWriteConnector() {
    super();
    setDataWriter(new LineWriter());
  }

  /**
   * Constructor.
   * 
   * @param id
   */
  public FileWriteConnector(String id) {
    super(id);
    setDataWriter(new LineWriter());
  }


  /**
   * Creates a file output stream. If <code>filename</code> is not set, the System.out
   * (console stream) is returned. 
   * If the file already exists, it is renamed to <code>moveExistingFileTo</code> (if the property is set).
   * If the file already exists and the <code>addTimestampToMovedFile</code> is
   * set to true, the new filename  (<code>moveExistingFileTo</code>) will be 
   * extended by a timestamp. 
   * 
   * @return an output stream this connector will write to
   */
  protected OutputStream getOutputStream() {
    if (filename != null) {
      if (moveExistingFileTo != null) {
        if (addTimestampToMovedFile) {
          DateFormat dataFormat = new SimpleDateFormat(timestampFormat);
          String formattedDate = dataFormat.format(new Date());
          moveExistingFileTo = moveExistingFileTo + "." + formattedDate;
        }
        File file = new File(filename);
        if (file.exists()){
          FileUtils.moveFile(filename, moveExistingFileTo);
        }
      }
      try {
        return new FileOutputStream(filename, append);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("FileNotFoundException, "
                + e.getMessage(), e);
      }
    } else {
      return System.out;
    }
  }
  
  /**
   * Sets file name.
   * @param path
   * @throws ConnectionException
   */
  public void setFilename(String path) throws ConnectionException {
    this.filename = path;
  }

  /**
   * Sets value of append flag.
   */
  public void setAppend(boolean append) {
    this.append = append;
    log.info(getId() + " will " + (append ? "append" : "overwrite") + " file");
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @return value of append flag
   */
  public boolean isAppend() {
	return append;
  }

  /**
   * @return name of the file the existing file will be renamed to.
   */
  public String getMoveExistingFileTo() {
	return moveExistingFileTo;
  }
  
  /**
   * Sets the path which an existing output file will be moved to.
   */
  public void setMoveExistingFileTo(String path) {
    if (path != null) {
      this.moveExistingFileTo = path;
      log.info("Existing file (if it exists) will be moved to " + path);
    }
  }
  
  /**
   * A flag that determines if a timestamp is to be appneded to 
   * <code>moveExistingFileTo</code>.
   */
  public void setAddTimestampToMovedFile(boolean addTimeStamp) {
  	this.addTimestampToMovedFile = addTimeStamp;
  	if (addTimestampToMovedFile){
  		log.info("Existing file (if it exists) will have timestamp added when moved.");
  	}
  }
  
  /**
   * Allows to overwrite the default <code>timestampFormat</code>.
   * 
   * @param timestampFormat new timestamp format
   */
  public void setTimestampFormat(String timestampFormat){
  	if (timestampFormat != null){
  		this.timestampFormat = timestampFormat;
  		log.info("Timestamp will be formatted as " + timestampFormat);
  	}
  }

}
