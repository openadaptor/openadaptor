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
package org.oa3.auxillary.iostream.reader;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/DirectoryReader.java,v 1.4 2006/10/19 14:24:26 higginse
 * Exp $ Rev: $Revision: 1.4 $ Created Jul 07, 2006 by Eddy Higgins
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.OAException;

/**
 * StreamReader which will read files from a directory.
 * <p>
 * It finds all files which match an optional <code>FileNameFilter</code> and supplies them all as a continuous
 * Stream.
 * 
 * @author Eddy Higgins
 */
public class DirectoryReader extends AbstractStreamReader {
  private static final Log log = LogFactory.getLog(DirectoryReader.class);

  /**
   * Path to the directory being read.
   */
  private String path;

  private FilenameFilter filter;

  private String fileDelimiter = null;

  // BEGIN Bean getters/setters

  /**
   * This sets the path to the directory which will be used.
   * 
   * @param path
   *          path to the directory which will be used.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Return the path to the directory which will be used.
   * 
   * @return path to the directory which will be used.
   */
  public String getPath() {
    return path;
  }

  /**
   * Assign a <code>FilenameFilter</code> to select which files within a directory should be included.
   * <p>
   * Only files which <i>match</i> the filter are included.
   * 
   * @param filter
   *          Filter to apply to set of files in a directory
   */
  public void setFilenameFilter(FilenameFilter filter) {
    this.filter = filter;
  }

  /**
   * Return a <code>FilenameFilter</code> which selects which files within a directory should be included.
   * 
   * @return (Optional) filter to apply to set of files in a directory
   */
  public FilenameFilter getFilenameFilter() {
    return filter;
  }

  /**
   * Set delimiter which will be inserted between files.
   * 
   * Note: This is a prototype attribute and thus is subject to change/removal. As such it is unsuitable for production
   * use.
   * 
   * @param delimiter
   *          A String which will be insterted in the Stream between each file.
   */
  public void setFileDelimiter(String delimiter) {
    this.fileDelimiter = delimiter;
  }

  /**
   * Return the delimiter, if any which will be inserted between files.
   * 
   * Note: This is a prototype attribute and thus is subject to change/removal. As such it is unsuitable for production
   * use.
   * 
   * @return delimiter A <code>String</code> containing a delimiter,or <tt>null</tt>
   */
  public String getFileDelimiter() {
    return fileDelimiter;
  }

  // END Bean getters/setters

  /**
   * Read all matching files from the configured directory, and create a SequenceInputStream from the matching files.
   * 
   * @throws org.oa3.control.OAException
   *           if there is a problem accessing the directory.
   */
  public void connect() throws OAException {
    log.debug("Reading Directory path " + path);
    try {
      File[] files = getFiles(path, filter);
      _inputStream = getSequenceInputStream(files);
      /**
       * Flag the source of these records.
       */
      // Shouldn't really matter for a Directory - each file will also do it via TaggedInputStream
      super.setReaderContext(path);
      super.connect();
    } catch (IOException ioe) { // Only catching exceptions that the super class doesn't
      log.error("Failed to read directory [" + path + "]  - " + ioe.toString());
      throw new OAException("Failed to open directory " + path, ioe);
    }
  }

  /**
   * Disconnect this StreamReader.
   * 
   * @throws org.oa3.control.OAException
   */
  public void disconnect() {
    log.debug("Disconnecting from directory" + path);
    super.disconnect();
  }

  /**
   * Generate a <code>SequenceInputStream</code> for use by the StreamReader.
   * <p>
   * This takes the list of files, and generates a <code>TaggedInputStream</code> for each one.<br>
   * It then creates a <code>SequenceInputStream</code> from the tagged input streams, optionally inserting a stream
   * containing a delimiter between each of them.
   * 
   * @param files
   *          List of <code>File</code> entries to be included, in order.
   * @return SequenceInputStream of files
   */
  private SequenceInputStream getSequenceInputStream(File[] files) {
    SequenceInputStream sis = null;
    byte[] delimiterBytes = null;
    if (files != null && (files.length > 0)) {
      if (fileDelimiter != null) {
        delimiterBytes = fileDelimiter.getBytes();
      }
      ArrayList streams = new ArrayList();
      for (int i = 0; i < files.length; i++) {
        File file = files[i];
        try {
          FileInputStream fis = new FileInputStream(file);
          String path = file.getCanonicalPath();
          streams.add(new TaggedInputStream(fis, path, this));
          if (delimiterBytes != null) {// Put a the delimiter in as a stream also.
            log.debug("Adding delimiter between files: " + fileDelimiter);
            streams.add(new ByteArrayInputStream(delimiterBytes));
          }
        } catch (IOException ioe) {
          log.warn("Unable to open " + file.getPath() + ". Skipping");
        }
      }
      if (!streams.isEmpty()) {
        sis = new SequenceInputStream(Collections.enumeration(streams));
      }
    } else {
      log.debug("Null or empty File[] supplied - cannot create a SequenceInputStream");
    }
    return sis;
  }

  private File[] getFiles(String directoryPath, FilenameFilter filter) throws IOException {
    File[] files = null;
    File dir = new File(directoryPath);
    if (!dir.isDirectory()) {
      log.error("Supplied path [" + directoryPath + "] is not a directory");
      throw new IOException("Supplied path [" + directoryPath + "] is not a directory");
    } else {// Find files which match the regexp
      files = dir.listFiles(filter);
      log.info("Found " + files.length + " files matching filter " + filter);
    }
    return files;
  }
}
