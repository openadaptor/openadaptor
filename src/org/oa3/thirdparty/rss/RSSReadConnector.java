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
package org.oa3.thirdparty.rss;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Dec 13, 2006 by oa3 Core Team
 */

import org.oa3.core.connector.AbstractReadConnector;
import org.oa3.core.exception.ComponentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.StringWriter;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * This class implements a simple RSS aggregator. Originally contibuted with the name FeedAggregator. Renamed
 * to adhere to naming conventions for Read Connectors.
 *
 * @author <a href="mailto:sugath.mudali@gmail.com">Sugath Mudali</a>
 */
public class RSSReadConnector extends AbstractReadConnector {

  /**
   * Logger
   */
  private static final Log log = LogFactory.getLog(RSSReadConnector.class);

  /**
   * The formatter to parse the start poll time
   */
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("H:mm");

  /**
   * Holds a collection of URLs as strings
   */
  private List urlStrings = new ArrayList();

  /**
   * Holds a collection of URL objcts derived from URL strings
   */
  private List urls = new ArrayList();

  /**
   * The last polled date, default to now
   */
  private Calendar lastPolledDate = new GregorianCalendar();

  /**
   * The poll start time in HH:mm format. Eg, 05:30
   */
  private String pollStartTime;

  /**
   * The output type for RSS. Set it to 2.0 as the default.
   */
  private String outputType = "rss_2.0";

  // Bean Getter/Setter methods

  public List getUrlStrings() {
    return urlStrings;
  }

  public void setUrlStrings(List urlStrings) {
    this.urlStrings = urlStrings;
  }

  public String getOutputType() {
    return outputType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  /**
   * Sets the start time for polling. Mainly used for testing as it can be
   * used for polling for past entries.
   *
   * @param time takes the form of HH:mm, where HH is hours in 24-hour clock
   *             format and mm stands for minutes. Invalid value for this entry would
   *             cause the poll time to set to the current time (equivalent to not setting
   *             this property).
   */
  public void setPollStartTime(String time) {
    this.pollStartTime = time;
  }

  // END Bean getters/setters

  /**
   * Establish a connection to external message transport. If already
   * connected then do nothing.
   */
  public void connect() {
    log.debug("Connector: [" + getId() + "] connecting ....");
    urls.clear();
    if (urlStrings != null) {
      for (int i = 0; i < urlStrings.size(); i++) {
        try {
          urls.add(new URL(urlStrings.get(i).toString()));
        } catch (MalformedURLException e) {
          log.warn("URL: " + urlStrings.get(i) + " is badly formed",
              e);
        }
      }
    }
    // Set the polled date.
    setLastPolledDate();
    log.info("The poll scheduled to start at: "
        + this.lastPolledDate.getTime());
    this.connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  /**
   * Poll for updates
   */
  public Object[] nextRecord(long timeoutMs) throws ComponentException {
    Object[] result = null;
    List list = getLatestEntries();
    if (!list.isEmpty()) {
      // Have entries to process
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType(outputType);
      feed.setTitle("OA3 RSS Listener");
      feed.setDescription("OA3 RSS Aggregated Feed");
      feed.setAuthor("oa3");
      feed.setLink("http://www.oa3.org");
      feed.setEntries(list);

      SyndFeedOutput output = new SyndFeedOutput();

      // The string writer to to write the output to.
      StringWriter sw = new StringWriter();
      try {
        output.output(feed, sw);
        result = new Object[]{sw.toString()};
      }
      catch (Exception ex) {
        log.error("Failed to write feed to output string");
        throw new ComponentException("Failed to write feed to output string", ex, this);
      }
      finally {
        // Set the current stamp
        this.lastPolledDate = new GregorianCalendar();
      }
      log.info("Record is: " + result[0]);
    } else {
      log.debug("No data returned.");
    }
    return result;
  }

  public Object getReaderContext() {
    return null;
  }

  public boolean isDry() {
    return !getPollingConfiguration().isActive();
  }

  protected List getLatestEntries() {
    List result = new ArrayList();
    SyndFeedInput input = new SyndFeedInput();
    for (Iterator iter = urls.iterator(); iter.hasNext();) {
      URL inputUrl = (URL) iter.next();
      SyndFeed inFeed;
      try {
        inFeed = input.build(new XmlReader(inputUrl));
        log.info("Processing URL: " + inputUrl);
        List entries = inFeed.getEntries();
        for (Iterator iter1 = entries.iterator(); iter1.hasNext();) {
          SyndEntry entry = (SyndEntry) iter1.next();
          if (entry.getPublishedDate().after(
              this.lastPolledDate.getTime())) {
            log.info("Adding entry: " + entry);
            result.add(entry);
          }
        }
      } catch (Exception ex) {
        log.warn("Failed to process feed from: " + inputUrl, ex);
      }
    }
    return result;
  }

  private void setLastPolledDate() {
    if (this.pollStartTime == null) {
      // Haven't set the start time
      return;
    }
    Date tempdate = null;
    try {
      tempdate = DATE_FORMATTER.parse(this.pollStartTime);
    } catch (ParseException pe) {
      log.error("Parse error with the start poll time, "
          + " defaulting to current time" + this.pollStartTime);
    }
    // tempdate is not null if we have parsed the pollStartTime
    // successfully.
    if (tempdate != null) {
      // Create a temp calendar object to extract hours and minutes
      GregorianCalendar tempCal = new GregorianCalendar();
      tempCal.setTime(tempdate);
      //The current time.
      this.lastPolledDate.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
      this.lastPolledDate.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
    }
  }

}
