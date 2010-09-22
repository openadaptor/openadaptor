/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;

/**
 * Line based reader which uses regular expressions to define record boundaries.
 * A typical use case might be where the reader is presented with multiple XML documents in a single 
 * stream/file which need to be split into records for processing.<br/>
 * This may be achieved by
 *  <ul>
 *  <li>defining a startLineRegex similar to "^\s*&lt;\?xml.*$"</li>
 *  <li>omitting an endLineRegex</li>
 *  <li>setting includeRecordDelimiters to <code>true</code></li>
 * </ul>
 * Notes:
 * <ul>
 * <li>Users should configure a starting regular expression ({@link setStartLineRegex() startLineRegex}) to identify the
 * beginning of a record.</li>
 * <li>Optionally they may configure a second expression ({@link setEndLineRegex() endLineRegex}) to identify the end of a record</li>
 * <li>If the end expression is not defined, a record is assumed to end before the next
 * matching starting expression, or end of file.</li>
 * <li>lines between an ending expression and the next start expression are discarded.</li>
 * <li>The delimiting expressions themselves may be included or excluded from the record by setting 
 *     {@link #setIncludeRecordDelimiters() includeRecordDelimiters} as desired.
 * <li>Note that if an end expression is omitted, the reader must read-ahead to find the next starting line. The size
 *     of the read-ahead buffer may be specified using {@link setReadAheadLimit() readAheadLimit}. By default
 *     this is set to {@link DEFAULT_READ_AHEAD_LIMIT}. It has no effect if the end expression is set.
 * </li>
 * </ul>
 *  SC106 - Modified to make ending expression optional, allowing records to be delimited by starting regex alone
 * @author Eddy Higgins
 * @see IDataReader
 */
public class RegexMultiLineReader extends EncodingAwareObject implements IDataReader {

	private static final Log log = LogFactory.getLog(RegexMultiLineReader.class);

	/**
	 * Default maximum size for read-ahead buffer.
	 */
	public static final int DEFAULT_READ_AHEAD_LIMIT=8192;

	private int readAheadLimit=DEFAULT_READ_AHEAD_LIMIT;

	private BufferedReader reader;

	private Matcher startLineMatcher;

	private Matcher endLineMatcher;

	private boolean includeRecordDelimiters;

	/**
	 * Regular expression which is used to identify the beginning of a record.
	 * This expression is mandatory.
	 * @param regex Regular expression describing the beginning of a record.
	 */
	public void setStartLineRegex(String regex) {
		startLineMatcher = Pattern.compile(regex).matcher("");
	}

	/**
	 * Regular expression which is used to identify the end of a record.
	 * Optional (as of 3.4.6) - if omitted the end of a record is assumed the next time
	 * a matching starting expression is encountered, or end of file.
	 * @param regex - Regular expression describing the end of a record
	 */
	public void setEndLineRegex(String regex) {
		endLineMatcher = Pattern.compile(regex).matcher("");
	}

	/**
	 * 
	 * @param readAheadLimit - size in characters of the read-ahead buffer to 
	 * use. 
	 * The read-ahead buffer will be <i>at least</i> this size.
	 * Defaults to {@link DEFAULT_READ_AHEAD_LIMIT}.
	 * 
	 * @since 3.4.6 - Introduced when ending regex became optional
	 */
	public void setReadAheadLimit(int readAheadLimit) {
		if (readAheadLimit>0) {
			this.readAheadLimit=readAheadLimit;
		}
		else {
			log.warn("Ignoring invalid readAheadLimit of "+readAheadLimit);
		}
	}
	
	/**
	 * Read the next record from input.
	 */
	public Object read() throws IOException {
		StringBuffer buffer = new StringBuffer();
		String line;
		boolean inRecord = false;
		boolean endRegexSet=endLineMatcher!=null; //Convenience flag
		while ((line = reader.readLine()) != null) { //Read a line
			if (inRecord) { //If currently in the middle of a record
				if (endRegexSet) { //Do we have a closing regex set?
					if (endLineMatcher.reset(line).matches()) { //Does this line match closing regex?
						if (includeRecordDelimiters) { //Append this line
							buffer.append(buffer.length() > 0 ? "\n" + line : line);
						}
						return buffer.toString(); //Record has been read; finished.
					} else {
						buffer.append(buffer.length() > 0 ? "\n" + line : line);
					}
				}
				else { //No end regex defined, try start regex instead.
					if (startLineMatcher.reset(line).matches()) {	//We've overshot to next record.					
						reader.reset(); //Go back to previous line		
						return buffer.toString();
					}
					else { // Just part of the current record; append it
						buffer.append(buffer.length() > 0 ? "\n" + line : line);
					}				
				}
			} 
			else { //Not curently in a record 
				if (startLineMatcher.reset(line).matches()) { //Start of a new record
					if (includeRecordDelimiters) { //Add this line if delimiters included
						buffer.append(buffer.length() > 0 ? "\n" + line : line);
					}
					inRecord = true;
				} 
				else { //Ignore this line -doesn't match starting regex.
					log.debug("discarding line " + line);
				}
			}
			if (!endRegexSet) { //Only required if no closing regex is defined.
			  reader.mark(readAheadLimit); //Mark where we are so we can go back.
			}
		}
		if (buffer.length() > 0 && endRegexSet) { //Incomplete record
			throw new RuntimeException("partial record, " + buffer.toString());
		}
		return buffer.length()>0?buffer.toString():null; //Return what we've got
	}

	/**
	 * Assign an input stream for this reader.
	 * @param inputStream - the InputStream which will provide input records.
	 */
	public void setInputStream(InputStream inputStream) {
		reader = new BufferedReader(createInputStreamReader(inputStream));
	}

	/**
	 * If set, include matched record delimiters as part of the output record.
	 * Defaults to <code>false</code>
	 * @param include flag indicating whether or not to include the delimiters
	 * themselves.
	 */
	public void setIncludeRecordDelimiters(boolean include) {
		this.includeRecordDelimiters = include;
	}

	/**
	 * Closes this reader
	 */
	public void close() throws IOException {
		reader.close();
	}

}
