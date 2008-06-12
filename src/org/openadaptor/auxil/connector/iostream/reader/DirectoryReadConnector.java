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

package org.openadaptor.auxil.connector.iostream.reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;
import org.openadaptor.core.transaction.ITransactionalResource;


/**
 * Read Connector that will read all the files in a directory.
 * {@link #setFilenameFilter} and {@link #setFilenameRegex(String)} allow the
 * files to be restricted to a matching subset. The list of files is established
 * when connect is called, the order in which the files are read is the default
 * implementation of {@link java.io.File#compareTo(File)}. This behaviour can
 * be overridden by using the fileComparator property, this class provides class
 * constants for comparing based on name and timestamp. If a valid value for 
 * the property {@link #setProcessedDir(File)} is supplied then successfully
 * processed files will be moved to that directory. If for any reason the move
 * fails then a warning is logged and the processed file is left in the input 
 * area. This feature was added in response to issue number SC49.
 * 
 * Defaults dataReader to {@link LineReader}
 * 
 * @author Eddy Higgins
 */
public class DirectoryReadConnector extends AbstractStreamReadConnector implements ITransactional {

  private static final Log log = LogFactory.getLog(DirectoryReadConnector.class);

  private File dir;
  private File processedDir = null;

  private List files = new ArrayList();
  
  private List processedFiles = new ArrayList();
  
  private File currentFile;
  
  private FilenameFilter filter;
  
  private Comparator fileComparator;

  private Object txnResource;

  /** Used to mark reaching the end of a single file */
  private boolean currentStreamDry = false;

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

  /**
   * The directory to copy processed files to.
   * @return File Must be a Directory
   */
  public File getProcessedDir() {
    return processedDir;
  }

  /**
   * The directory to copy processed files to.
   * @param processedDir Must be a directory.
   */
  public void setProcessedDir(File processedDir) {
    this.processedDir = processedDir;
  }

  /**
   * restricts the files that are read to those that match this filename filter
   */
  public void setFilenameFilter(FilenameFilter filter) {
    this.filter = filter;
  }

  /**
   * sets the filename filter that restricts the files that are read to those
   * whose unqualified name matches this regular expression
   */
  public void setFilenameRegex(String regex) {
    final Matcher matcher = Pattern.compile(regex).matcher("");
    setFilenameFilter(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return matcher.reset(name).matches();
      }
    });
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    if ( dir == null) {
      exceptions.add(new ValidationException("No read directory has been set.", this));
    } else if (!dir.exists() || !dir.isDirectory()) {
      exceptions.add(new ValidationException("dir " + dir.toString() + " does not exist or is not a directory", this));
    }
    if (processedDir != null && (!processedDir.exists() || !processedDir.isDirectory())) {
      exceptions.add(new ValidationException("ProcessedDir has been set and " + processedDir.toString() + " does not exist or is not a directory", this));
    }
  }
  

  /** Overridden to refresh the file list before getting the next stream */ 
  protected void refreshInputStream() {
    if (files == null || files.isEmpty()) {
      refreshFileList();
    }
    super.refreshInputStream();
  }

  /**
   * Add all files in the directory to the file list.
   */
  protected void refreshFileList() {
    files.addAll(Arrays.asList(dir.listFiles(filter)));
    if (fileComparator != null) {
      Collections.sort(files, fileComparator);
    } else {
      Collections.sort(files);
    }
  }
  
  /**
   * open the next input stream to read from
   */
  protected InputStream getInputStream() throws IOException {
    return getNextInputStream();
  }

  /**
   * Returns true when no more messages to be read.
   * <br>
   * If the end of the current input stream (currentStreamDry) is reached,
   * but there are more files to process, it will then open the next file
   * and set the input stream accordingly. 
   * It will return true, only when there are no more files to process and
   * the current (last) file has reached the end if its input stream.
   * NB We are ignoring the superclass implementation of isDry as it doesn't
   * fit well with the multiple stream approach we are using.
   */
  public boolean isDry() {    
    boolean locallyDry = currentStreamDry && files.isEmpty();
    return locallyDry;
  }

  /**
   * @return name of current file we are reading
   */
  public Object getReaderContext() {
    return currentFile.getAbsolutePath();
  }
  
  public Object[] next(long timeoutMs) {
    if ( currentStreamDry ) {refreshInputStream();}    
    try {
      ArrayList batch = new ArrayList();
      for (int i = 0; i < batchSize; i++) {
        Object data = dataReader.read();
        if (data != null) {
          batch.add(data);
        } else {
          currentStreamDry = true;
          closeInputStream(); // TODO Hack. Fix Axel's issue with locked files. Revisit urgently.
          break;
        }
      }
      return batch.toArray();
    } catch (IOException e) {
      throw new ConnectionException("IOException, " + e.getMessage(), e, this);
    }
  }

  /** Called to retrieve next stream. Means last stream is empty */
  private InputStream getNextInputStream() {
    closeInputStream();
    if (!files.isEmpty()) {
      File f = (File) files.remove(0);
      processedFiles.add(f);
      try {
        currentFile = f;
        log.info(getId() + " opening " + f.getAbsolutePath() + "...");
        //isDry = false;
        currentStreamDry = false;
        return new FileInputStream(f);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("FileNotFoundException, " + e.getMessage(), e);
      }
    } else {
      currentFile = null; // Todo check if this belongs here
      return new ByteArrayInputStream(new byte[] {}); // Any empty stream will do.
    }
  } 

  protected void closeInputStream() {
    //Todo Could do postprocess here? If Txn mechanism proves troublesome.
    currentFile = null;
    super.closeInputStream();
    //postProcessFiles();
  }
  
  /**
   * Do any post processing needed to successfully processed files.
   */
  private void postProcessFiles() {
    Iterator iter = processedFiles.iterator();
    while (iter.hasNext()) {
      File nextProcessedFile= (File)iter.next();
      log.debug("Successfully processed: " + nextProcessedFile.getName());
      if (processedDir != null) {        
        File target = new File(processedDir, nextProcessedFile.getName());  
        boolean success = nextProcessedFile.renameTo(target);
        if (!success) {
          log.warn("Failed to move processed file: " + nextProcessedFile);
          }
        else {
          log.info("Successfully postprocessed file: " + nextProcessedFile);
        }
      }
    }
    processedFiles = new ArrayList();
  }

  /**
   * controls the order in which the files are read
   */
  public void setFileComparator(Comparator fileComparator) {
    this.fileComparator = fileComparator;
  }

  /** 
   * Return the resource used by Openadaptor's default Transaction Manager.
   */
  public Object getResource() {    
    if (txnResource == null) { txnResource = new DirectoryReaderTransactionResource(); }
    return txnResource;
  }
  
  /**
   * This Inner Class implements the transactional resource for this connector as used by
   * Openadaptor's default Transaction Manager. The idea is that we use the default transaction
   * mechanism to post-process files. This guarantees that files are not moved until all
   * downstream writers have successfully processed the messages. One proviso is that an 
   * individual file is post processed only if all messages originating in it have been 
   * processed, i.e we've reached the end of the file. This means that if the reader is 
   * rerun after a failure there may be some duplicate messages as the full file will be 
   * reprocessed.
   * 
   * @author scullyk
   */
  protected class DirectoryReaderTransactionResource implements ITransactionalResource {      
    public void begin() {
      // Nothing specific to do when a transaction starts.      
    }
   public void commit() {
      log.debug("Commit called on [" + getId() +"]");
      log.debug("Post-processing: " + processedFiles);
      if ((currentStreamDry) && !processedFiles.isEmpty() ){ // Only post process files if we are not in mid-stream.
        postProcessFiles();    
      }      
    }
    public void rollback(Throwable e) {
      // Just don't post process the files so that they'll still be there for processing the next time around.     
    }
  }  
  
  /**
   * can be used as a value for fileComparator property, compares based on
   * unqualified file name
   */
  public static final Comparator NAME_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      File f1 = (File) o1;
      File f2 = (File) o2;
      return f1.getName().compareTo(f2.getName());
    }
  };
  
  /**
   * can be used as a value for fileComparator property, compares based on
   * file timestamp
   */
  public static final Comparator TIMESTAMP_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      File f1 = (File) o1;
      File f2 = (File) o2;
      return (int) (f1.lastModified() - f2.lastModified());
    }
  }; 
  
}
