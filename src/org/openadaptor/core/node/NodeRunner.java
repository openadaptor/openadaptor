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

package org.openadaptor.core.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 4, 2007 by oa3 Core Team
 */

public class NodeRunner extends AbstractNodeRunner {
  private static final Log log = LogFactory.getLog(NodeRunner.class);

  public void run() {
    if (!isStillRunning()) {
      log.warn(getId() + " has not been started");
      exitCode = 0;
    }
    try {
      log.info(getId() + " running");
      while (isStillRunning()) {
        Response response = messageProcessorDelegate.process(new Message(new Object[]{}, null, null));
        log.debug("Response is: " + response);
      }
    }
    catch (Throwable e) {
      exitCode = 1;
      exitThrowable = e;
      log.error(getId() + " uncaught exception, stopping", e);
      stop();
    }
    finally {
      log.info(getId() + " no longer running");
      stop();
    }
  }

}
