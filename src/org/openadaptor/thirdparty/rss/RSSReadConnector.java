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

package org.openadaptor.thirdparty.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements a simple RSS aggregator. Originally contibuted with the name FeedAggregator. Renamed
 * to adhere to naming conventions for Read Connectors.
 *
 * @author <a href="mailto:sugath.mudali@gmail.com">Sugath Mudali</a>
 */
public class RSSReadConnector extends Component implements IReadConnector {

  /**
   * Logger
   */
  private static final Log log = LogFactory.getLog(RSSReadConnector.class);

  /**
   * Holds a collection of URLs as strings
   */
  private List urlStrings = new ArrayList();

  /**
   * Holds a collection of URL objcts derived from URL strings
   */
  private List urls = new ArrayList();

  /**
   * The output type for RSS. Set it to 2.0 as the default.
   */
  private String outputType = "rss_2.0";

  private Date lastTime = new Date();

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
   * Establish a connection to external message transport. If already
   * connected then do nothing.
   */
  public void connect() {
    log.debug(getId() + " connecting ....");
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
    log.info(getId() + " connected");
  }

  public void disconnect() {
    log.info(getId() + " disconnected");
  }

  public void validate(List exceptions) {
  }
  
  /**
   * Poll for updates
   */
  public Object[] next(long timeoutMs) throws OAException {
    Object[] result = null;
    List list = getLatestEntries();
    if (!list.isEmpty()) {
      // Have entries to process
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType(outputType);
      feed.setTitle("OA3 RSS Listener");
      feed.setDescription("OA3 RSS Aggregated Feed");
      feed.setAuthor("openadaptor");
      feed.setLink("http://www.openadaptor.org");
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
        throw new ConnectionException("Failed to write feed to output string", ex, this);
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
  
  public void setReaderContext(Object context) {
  }

  public boolean isDry() {
    return urls.isEmpty();
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
          if (entry.getPublishedDate().after(lastTime)) {
            log.info("Adding entry: " + entry);
            result.add(entry);
          }
        }
      } catch (Exception ex) {
        log.warn("Failed to process feed from: " + inputUrl, ex);
      }
    }
    urls.clear();
    lastTime = new Date();
    return result;
  }
}
