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

package org.openadaptor.legacy.connector.iostream.reader.string;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.string.RegexMultiLineReader;

/**
 * Utility class for setting up a DOXMLReader.
 * <BR>
 * This is a convenience subclass of RegexMultiLineReader which
 * is solely for reading legacy DOXML.
 * Legacy openadaptor (1.7.2.2) requires a very strict format
 * for incoming doxml.
 * (legacy org.openadaptor.util.sax.XMLUtils provides the mechanism)
 * <pre><code>
 *   public static final String XML_HEADER_START = "&lt;?xml version='1.0' encoding='";
 *   public static final String XML_HEADER_END = "'?&gt";
 * </code></pre>
 * The header must start with exactly XML_HEADER_START, and end with XML_HEADER_END.
 * 
 * @author higginse
 *
 */
public class DOXMLReader extends RegexMultiLineReader {

  private static final Log log = LogFactory.getLog(DOXMLReader.class);

  //Constant from openadaptor 1_7_2_2, converted to regex. 
  private static final String DOXML_HEADER_REGEX="^<\\?xml version='1.0' encoding='.*'\\?>$";

  private static final String DOXML_FOOTER_REGEX="</dataobjectmessage>";

  public void setStartLineRegex(String regex) {
    log.warn("Ignoring startLineRegex value as it is hardcoded for DOXML to "+DOXML_HEADER_REGEX);
  }

  public void setEndLineRegex(String regex) {
    log.warn("Ignoring endLineRegex value as it is hardcoded for DOXML to "+DOXML_FOOTER_REGEX);
  }

  public void setIncludeRecordDelimiters(boolean include) {
    log.warn("Ignoring includeRecordDelimiters value as it is hardcoded for DOXML to true");
  }

  public DOXMLReader() {
    log.debug("Setting startLineRegex to "+DOXML_HEADER_REGEX);
    super.setStartLineRegex(DOXML_HEADER_REGEX);
    log.debug("Setting endLineRegex to "+DOXML_FOOTER_REGEX);
    super.setEndLineRegex(DOXML_FOOTER_REGEX);
    log.debug("Setting includeRecordDelimiters to true");
    super.setIncludeRecordDelimiters(true);
  }

}
