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
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Write Connector that write data to a file (or stdout if the filename property
 * is not set).
 * 
 * The FileWriteConnector component now supports the ability to move the output files out of the
 * way once they reach a predefined size or age. This is particularly useful if you
 * are using the FileWriteConnector as a log of what the adaptor has processed. For example, you
 * could arrange for the output file to be rolled over every day. The rollover process
 * uses the same mechanics as the <i>moveExistingFileTo</i> property and rolled over files
 * have a timestamp applied the end of their names.
 * 
 * <table border="0">
 * <tr>
 * <td valign="top">RolloverSize</td> <td>[null] When set, causes the output file to be rolled over when it exceeds
 * the give size. Uses our own notation:
 * <br> xG  - x Gigabytes (that is 1,000,000,000 bytes, not 2^30 bytes)
 * <br> xGb - x Gigabytes (that is 1,000,000,000 bytes, not 2^30 bytes)
 * <br> xM  - x megabytes (that is 1,000,000 bytes, not 2^20 bytes)
 * <br> xMb - x megabytes (that is 1,000,000 bytes, not 2^20 bytes)
 * <br> xK  - x kilobytes (that is 1,000,000 bytes, not 2^10 or 1024 bytes)
 * <br> xKb - x kilobytes (that is 1,000,000 bytes, not 2^10 or 1024 bytes)
 * <br> xB  - x bytes</td>
 * </tr>
 * <tr><td>&nbsp;</td></tr>
 * <tr>
 * <td valign="top">RolloverPeriod</td > <td>[null] When set, causes the output file to be rolled over when the
 * difference between its creation date and now exceeds the
 * supplied period. Uses our own notation:
 * <br> xW  - x weeks
 * <br> xD  - x days
 * <br> xH  - x hours
 * <br> xM  - x minutes
 * <br> xS  - x seconds</td>
 * </tr>
 * </table>
 * 
 * @author OA3 Core Team
 */
public class FileWriteConnector extends AbstractStreamWriteConnector {

  private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSSZ";
  
  private static final Log log = LogFactory.getLog(FileWriteConnector.class);

  private String filename;

  private boolean append = true;

  private String moveExistingFileTo = null;
  
  private String lastFileMovedTo = null;

  private boolean addTimestampToMovedFile = false;

  private String timestampFormat = DEFAULT_TIMESTAMP_FORMAT;
  
  /**
   * file size (in bytes) at which to perform a rollover. -1 indicates that
   * there is no rollover
   */
  private long rolloverSize = -1;

  /**
   * rollover period (in milliseconds) to perform a rollover since the epoch
   * (00:00:00 GMT, January 1, 1970). -1 indicates that there is no rollover
   */
  private long rolloverPeriod = -1;
  
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

  private void moveOutputFile(){
    String newFileName = moveExistingFileTo;
    if (addTimestampToMovedFile) {
      DateFormat dataFormat = new SimpleDateFormat(timestampFormat);
      String formattedDate = dataFormat.format(new Date());
      newFileName = moveExistingFileTo + "." + formattedDate;
    }
    File file = new File(filename);
    if (file.exists()){
      try {
        FileUtils.moveFile(filename, newFileName);
        lastFileMovedTo = newFileName;
      } catch (RuntimeException e) {
        log.error("File move unsuccessfull: " + filename + " to " + newFileName, e);
      }
    }
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
        moveOutputFile();
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
   * Delivers the data but first checks if the file needs to be rolled over,
   * either because of being too large or too old.
   */
  public Object deliver(Object[] data) {
    
    /*  check to see if there are any file size rollover options set */
    if (rolloverSize > -1) {
      File f = new File(filename);
      if (f.exists() && f.length() > rolloverSize) {
          rolloverFile();
      }
    }
   
    /* 
     * check for any date based rollover options set
     * Important note: we can use the lastmodified time on the file
     * because the file's timestamp is initially set when it is
     * created and is not updated until the Writer is closed. This
     * means that lastModified() effectively returns the files
     * creation date each time it is called.
     */
    if (rolloverPeriod > -1) {
      File f = new File(filename);
      Date now = new Date();
      if (f.exists() &&
          (now.getTime() - f.lastModified()) > rolloverPeriod) {
          rolloverFile();
      }
    }
    
    return super.deliver(data);
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

  /**
   * validates that the supplied string representing the size at which
   * the output file is rolled over to a new version and sets the
   * rolloverSize property accordingly. The acceptable file sizes are:
   * <p/>
   * xG - x Gigabtyes
   * xGb    - x Gigabytes
   * xM - x megabytes
   * xMb    - x megabytes
   * xK - x kilobytes
   * xKb    - x kilobytes
   * xb - x bytes
   *
   * @param prop - the string to be parsesd
   *
   * @throws ComponentException - if an invalid file size is encountered
   */
  private void parseRolloverSize(String prop) throws ComponentException
  {
      if (prop == null || prop.equals("-1")) {
          return;
      }

      // we attempt a conversion for a size indicater. If the rolloverSize
      // variable has not been modified then we move on to the next size
      // indicater. If the rolloverSize has still not been set at the end
      // then we have an unparsible string and throw an error
      parseSize(prop, "g", 1000000000);

      if (rolloverSize == -1) {
          parseSize(prop, "gb", 1000000000);
      }

      if (rolloverSize == -1) {
          parseSize(prop, "m", 1000000);
      }

      if (rolloverSize == -1) {
          parseSize(prop, "mb", 1000000);
      }

      if (rolloverSize == -1) {
          parseSize(prop, "k", 1000);
      }

      if (rolloverSize == -1) {
          parseSize(prop, "kb", 1000);
      }

      if (rolloverSize == -1) {
          parseSize(prop, "b", 1);
      }

      if (rolloverSize == -1) {
          throw new ComponentException("Unrecognised rollover size: " + prop, this);
      }

      log.info(filename + " Rollover size set to " + prop + " (" +
          rolloverSize +
          " bytes)");
  }

  
  /**
   * validates that the supplied string representing the time period at
   * which the output file is rolled over to a new version and sets the
   * _rollover_date property accordingly. The acceptable periods are:
   * <p/>
   * xW - x weeks
   * xD - x days
   * xH - x hours
   * xM - x minutes
   * xS - x seconds
   *
   * @param prop - the string to be parsesd
   *
   * @throws IbafException - if an invalid period is encountered
   */
  private void parseRolloverDate(String prop) throws ComponentException{
      if (prop == null) {
          return;
      }

      // we attempt a conversion for a period indicater. If the _rollover_date
      // variable has not been modified then we move on to the next period
      // indicater. If the _rollover_date has still not been set at the end
      // then we have an unparsible string and throw an error
      parseDate(prop, "w", 7 * 24 * 60 * 60 * 1000);

      if (rolloverPeriod == -1) {
          parseDate(prop, "d", 24 * 60 * 60 * 1000);
      }

      if (rolloverPeriod == -1) {
          parseDate(prop, "h", 60 * 60 * 1000);
      }

      if (rolloverPeriod == -1) {
          parseDate(prop, "m", 60 * 1000);
      }

      if (rolloverPeriod == -1) {
          parseDate(prop, "s", 1000);
      }

      if (rolloverPeriod == -1) {
          throw new ComponentException("Unrecognised rollover period: " + prop, this);
      }

      log.info(filename + " Rollover date set to " + prop + " (" +
          rolloverPeriod +
          "ms)");
  }
  
  
  /**
   * attempts to retrieve a valid number from the supplied string. This
   * number is then multiplied by the mupltiplier and used for the
   * rollover size. If the string does not fit the format then we simply
   * leave the rollover size as it was and return.
   *
   * @param s - the string to be converted
   * @param indicater - the file size indicater (eg. Gb, Mb, Kb, ...)
   * @param multiplier - the value to multiply the rollover size by to
   * convert to bytes (eg. 2Kb becomes 2000)
   */
  private void parseSize(String s, String indicater, int multiplier){
      try {
          DecimalFormat nf = new DecimalFormat("#,##0" + indicater);
          Number n = nf.parse(s.toLowerCase());

          rolloverSize = n.longValue() * multiplier;
      }
      catch (ParseException e) {
      }
  }
  
  /**
   * attempts to retrieve a valid number from the supplied string. This
   * number is then multiplied by the mupltiplier to convert it into
   * milliseconds and used for the rollover period. If the string does
   * not fit the format then we simply leave the rollover period as it
   * was and return.
   *
   * @param s - the string to be converted
   * @param indicater - the rollover period indicater (eg. M, W, D, ...)
   * @param multiplier - the value to multiply the rollover period by to
   * convert to milliseconds (eg. 2D becomes 2*24*60*60*1000)
   */
  private void parseDate(String s, String indicater, long multiplier){
      try {
          DecimalFormat nf = new DecimalFormat("#" + indicater);
          Number n = nf.parse(s.toLowerCase());

          rolloverPeriod = n.longValue() * multiplier;
      }
      catch (ParseException e) {
      }
  }

  /**
   * performs the file rollover
   *
   * @throws ComponentException
   */
  private void rolloverFile() throws ComponentException{
    /*
     * we use the moveFile() method to do the rollovers so we must
     */
    if (moveExistingFileTo == null) {
      moveExistingFileTo = filename;
      addTimestampToMovedFile = true;
    }
    
    /*
     * close the file. The file will be moved and a new one created as part
     * of #connect()
     */
    try {
      disconnect();
      connect();
    }
    catch (Exception e) {
      throw new ComponentException("", e, this);
    }
  }

  
  /**
   * Sets the file rollover size. 
   * 
   * validates that the supplied string representing the size at which
   * the output file is rolled over to a new version and sets the
   * rolloverSize property accordingly. The acceptable file sizes are:
   * <p/>
   * xG - x Gigabtyes
   * xGb    - x Gigabytes
   * xM - x megabytes
   * xMb    - x megabytes
   * xK - x kilobytes
   * xKb    - x kilobytes
   * xb - x bytes
   *
   * @param rolloverSizeStr - the string to be parsesd
   * @throws ComponentException - if an invalid file size is encountered
   */
  public void setRolloverSize(String rolloverSizeStr) {
    parseRolloverSize(rolloverSizeStr);
  }

  /**
   * Sets the rollover date.
   * 
   * validates that the supplied string representing the time period at
   * which the output file is rolled over to a new version and sets the
   * rolloverDate property accordingly. The acceptable periods are:
   * <p/>
   * xW - x weeks
   * xD - x days
   * xH - x hours
   * xM - x minutes
   * xS - x seconds
   *
   * @param rolloverDateStr - the string to be parsesd
   * @throws ComponentException - if an invalid period is encountered
   */
  public void setRolloverDate(String rolloverDateStr){
    parseRolloverDate(rolloverDateStr);
  }
  
  /**
   * @return name of the last moved file.
   */
  public String getLastFileMovedTo() {
    return lastFileMovedTo;
  }

}
