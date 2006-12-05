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
package org.oa3.iostream.reader;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/FileReader.java,v 1.3 2006/10/18 17:09:05 higginse Exp $
 * Rev: $Revision: 1.3 $ Created Nov 4, 2005 by Eddy Higgins
 */
import org.oa3.core.exception.OAException;
import org.oa3.util.FileUtils;

/**
 * Convenience sub-class of <code>URLReader</code> for files. This converts a specified file path into a
 * <code>URL</code>, and lets the base class to do the work.
 * 
 * Note: Future versions may well allow additional file-specific behaviours, such as renaming or deleting a file when it
 * has been 'consumed' by the reader.
 * 
 * @author Eddy Higgins
 */
public class FileReader extends URLReader {
  // private static final Log log = LogFactory.getLog(FileReader.class);

  private String path;

  /*
   * Not yet completed private boolean deleteOriginal=false; private String movePath;
   */
  // BEGIN Bean getters/setters
  public void setPath(String path) throws OAException {
    this.path = path;
    setUrl(FileUtils.toURL(this.path));
  }

  public String getPath() {
    return path;
  }

  /*
   * Not yet completed. public void setMovePath(String path) throws OAException { if
   * (!FileUtils.checkDirectoryPath(path,true)){ throw new OAException("Unable to create movePath of "+path); }
   * movePath=path; validateProperties(); } public String getMovePath() {return movePath;}
   * 
   * public void setDeleteOriginal(boolean deleteOriginal){ this.deleteOriginal=deleteOriginal; validateProperties(); }
   * public boolean getDeleteOriginal() { return deleteOriginal; }
   */
  // END Bean getters/setters
  /*
   * Not yet completed... Override default disconnect - this allows us to move the source file if necessary
   * 
   * 
   * public void disconnect() { log.debug("Disconnecting from "+url); boolean moveRequired=connected &&
   * (movePath!=null); super.disconnect(); if (moveRequired) { log.info("Moving source file"+path+" -> "+movePath);
   * move(path,movePath); } }
   * 
   * private static void move(String old,String newPath) { File oldFile=new File(old); oldFile.renameTo(new
   * File(newPath,oldFile.getName())); }
   * 
   * private void validateProperties() { if (deleteOriginal && (movePath!=null)) { log.warn("Misconfiguration - both
   * 'deleteOriginal' and 'MovePath' have been set - movePath takes precedence."); } }
   */
}
