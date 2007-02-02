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

import org.openadaptor.core.jmx.Administrable;

/**
 * jmx component for accessing system info
 * 
 * @author perryj
 *
 */
public class SystemUtil implements Administrable {

  public Object getAdmin() {
    return new Admin();
  }

  public interface AdminMBean {
    public void exit();

    public String getMemory();

    public String dumpThreads();
  }

  public class Admin implements AdminMBean {

    public void exit() {
      System.exit(0);
    }

    public String getMemory() {
      long mb = 1024 * 1024;
      int max = (int) (Runtime.getRuntime().maxMemory() / mb);
      int free = (int) (Runtime.getRuntime().freeMemory() / mb);
      int total = (int) (Runtime.getRuntime().totalMemory() / mb);
      int used = total - free;

      StringBuffer buffer = new StringBuffer();
      buffer.append("used=").append(used).append(",");
      buffer.append("free=").append(free).append(",");
      buffer.append("total=").append(total).append(",");
      buffer.append("max=").append(max).append(" (Mb)");
      return buffer.toString();
    }

    public String dumpThreads() {
      try {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<pre>");
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) {
          threadGroup = threadGroup.getParent();
        }
        dumpThreadGroup(buffer, "", threadGroup);
        buffer.append("</pre>");
        return buffer.toString();
      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      }
    }

    private void dumpThreadGroup(StringBuffer buffer, String indent, ThreadGroup threadGroup) {
      buffer.append(indent).append("<b>").append(threadGroup.getName()).append("</b>\n");
      indent += "  ";
      Thread[] threads = new Thread[threadGroup.activeCount()];
      threadGroup.enumerate(threads);
      for (int i = 0; i < threads.length; i++) {
        if (threads[i] != null && threads[i].getThreadGroup() == threadGroup) {
          buffer.append(indent).append(threads[i].getName());
          buffer.append("(").append(threads[i].getPriority()).append(")");
          buffer.append('\n');
        }
      }
      ThreadGroup[] threadGroups = new ThreadGroup[threadGroup.activeGroupCount()];
      threadGroup.enumerate(threadGroups);
      for (int i = 0; i < threadGroups.length; i++) {
        if (threadGroups[i] != null) {
          dumpThreadGroup(buffer, indent, threadGroups[i]);
        }
      }
    }
  }

}
