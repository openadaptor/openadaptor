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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/MultiLineStringRecordReader.java,v 1.5 2006/10/18
 * 17:09:05 higginse Exp $ Rev: $Revision: 1.5 $ Created Jun 23, 2006 by Eddy Higgins
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Split Reader data into multi-line String records.
 * <p>
 * It can be configured with Regular Expressions to match record start or end patterns. Note: It's still a rudimentary
 * implementation.
 * 
 * @author Eddy Higgins
 */
public class MultiLineStringRecordReader extends AbstractRecordReader {

  private static final Log log = LogFactory.getLog(MultiLineStringRecordReader.class);

  /**
   * Regular expression to match start-of-record.
   */
  private String startRegex;

  /**
   * Regular expression to match end-of-record.
   */
  private String endRegex;

  /**
   * used internally whilst 'parsing' records from reader.
   */
  private BufferedReader bufferedReader;

  /**
   * RE matcher for start regex.
   */
  private Matcher startMatcher;

  /**
   * RE matcher for end regex.
   */
  private Matcher endMatcher;

  /**
   * Read ahead buffer size for matching. Currently not configurable!
   * 
   */
  private static final int READ_AHEAD_BUFSIZE = 4096;

  /**
   * Line separator string
   */
  public static final String LINE_SEPARATOR = "\n"; // ToDo: pick up platform dependent version, or make configurable.

  // BEGIN Bean getters/setters

  /**
   * Assign a regular expression which marks the beginning of a record.
   * 
   * @param regex
   *          String containing a regular expression.
   */
  public void setRecordStartRegex(String regex) {
    this.startRegex = regex;
    startMatcher = Pattern.compile(regex).matcher("DUMMY"); // Done so we can always use matcher.reset()
  }

  /**
   * Assign a regular expression which marks the end of a record.
   * 
   * @param regex
   *          String containing a regular expression.
   */
  public void setRecordEndRegex(String regex) {
    this.endRegex = regex;
    endMatcher = Pattern.compile(regex).matcher("DUMMY");// Done so we can always use matcher.reset()
  }

  /**
   * Return the regular expression which marks the beginning of a record.
   * 
   * @return regex String containing a regular expression, or <tt>null</tt>
   */
  public String getRecordStartRegex() {
    return startRegex;
  }

  /**
   * Return the regular expression which marks the end of a record.
   * 
   * @return regex String containing a regular expression, or <tt>null</tt>
   */
  public String getRecordEndRegex() {
    return endRegex;
  }

  // END Bean getters/setters

  /**
   * Prepare this record reader for use.
   * 
   * @throws IOException
   *           if the underlying <code>Reader</code> is <tt>null</tt>
   */
  public void initialise() throws IOException {
    // ToDo: Perhaps make reader protected instead in base class. No time to check now.
    Reader reader = getReader(); // Get the reader from the base class
    if (reader == null) {
      throw new IOException("Reader cannot be null");
    }
    log.debug("Creating a BufferedReader from the Reader.");
    bufferedReader = new BufferedReader(getReader());
    if (startMatcher != null) {
      log.info("Will search for Records starting with matches for  " + startRegex);
    }
    if (endMatcher != null) {
      log.info("Will search for Records ending with matches for  " + endMatcher);
    }
  }

  /**
   * Return a multi-line string record.
   * <p>
   * The record is deemed to start at the first instance of startRegex encountered. If startRegex is not configured,
   * then the record is deemed to start immediately.
   * 
   * The record is deemed to end when the endRegex is encountered, the end of input data, or, if endRegex is not
   * configured, but startRegex is, the next instance of startRegex (the line containing the startRegex is not
   * included).
   * 
   * @return a multi-line <code>StringM/code> record.
   * @throws IOException
   */

  // TODO: this needs to be reviewed - seems very inflexible, can't match specific part of
  // expression to form record, surely we want a single expression, rather than line specific
  public Object next() throws IOException {
    if (bufferedReader == null) {
      initialise();
    }

    StringBuffer sb = null;
    String line = getFirstLine(startMatcher, bufferedReader);
    boolean midRecord = line != null;
    if (midRecord) {
      log.debug("Adding: " + line);
      sb = new StringBuffer(line).append(LINE_SEPARATOR);
    }
    while (midRecord) {
      bufferedReader.mark(READ_AHEAD_BUFSIZE);
      // GetBlock
      line = readLine();
      midRecord = line != null;
      if (midRecord) { // Need to check if we've reached the end.
        if (endMatcher != null) {
          if (endMatcher.reset(line).matches()) {
            log.debug("Found recordEnd Marker: " + line);
            midRecord = false;
          }
        } else { // No endmatcher. Check for another start instead
          if (startMatcher != null) {
            if (startMatcher.reset(line).matches()) { // Overstepped onto next one. Oops
              log.debug("Found next recordStart Marker ... " + line);
              bufferedReader.reset();
              midRecord = false;
            }
          }
        }
      }
      if (midRecord) {
        log.debug("Adding: " + line);
        sb.append(line).append(LINE_SEPARATOR);
      }
    }
    return sb == null ? null : sb.toString();
  }

  protected String readLine() throws IOException {
    String line = bufferedReader.readLine();
    setIsDry(line == null);
    return null;
  }

  /**
   * Find the start of a block.
   * 
   * @param matcher
   * @param br
   * @return String containing matched line.
   * @throws IOException
   */
  private String getFirstLine(Matcher matcher, BufferedReader br) throws IOException {
    String line = br.readLine();
    while ((line != null) && (matcher != null) && (!matcher.reset(line).matches())) {
      line = br.readLine();
    }
    setIsDry(line == null);
    return line;
  }

}
