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

package org.openadaptor.util;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some useful helper methods to centralise common functions
 * 
 * @author Russ Fennell
 */
public class URLUtils {
  private static final Log log = LogFactory.getLog(URLUtils.class);

  /**
   * Checks the supplied url string to make sure that it is valid. It does this by checking that it:
   * 
   * <pre>
   *   a. a valid url format
   *   b. points to a data source that contains data
   * </pre>
   * 
   * Note that in the case of files you do not need to supply the "file:" protocol.
   * 
   * @throws org.openadaptor.control.RuntimeException
   *           if the url is null, or it does not exist, or it does not contain any data
   */
  public static void validateURLAsDataSource(String url) {
    if (url == null || url.equals(""))
      throw new RuntimeException("Null url");

    // check the url is of valid format, exists and contains data
    try {
      URL u;

      try {
        u = new URL(url);
      } catch (MalformedURLException e) {
        // we assume that if it is not a valid URL then we are dealing with
        // a file and need to check that it exists and is not a directory
        File f = new File(url);

        if (!f.exists())
          throw new Exception("File not found and " + e.getMessage());

        if (f.isDirectory())
          throw new Exception("URL points to a directory");

        u = f.toURL();
      }

      InputStream in = u.openStream();
      int numBytes = in.available();
      in.close();

      if (numBytes <= 0) {
        log.warn("Number of bytes available from [" + url + "] = " + in.available());
        throw new Exception("Unable to read from file");
      }
    } catch (Exception e) {
      throw new RuntimeException("Invalid URL [" + url + "]: " + e.toString());
    }

    log.info("URL [" + url + "] is valid");
  }

}
