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
package org.oa3.util;

/**
 * A set of helper methods relating to the system and environment
 * 
 * @author Russ Fennell
 */
public class SystemUtils {

  /**
   * Returns true if the code is running on a Mac by checking for the mrj.version system property
   */
  public static boolean isMac() {
    return (System.getProperty("mrj.version") != null);
  }

  /**
   * Sets the com.apple.macos.useScreenMenuBar=true so that menus will be displayed in the finder menu at the top of the
   * screen
   */
  public static void setMacMenusOn(boolean on) {
    System.setProperty("apple.laf.useScreenMenuBar", (on ? "true" : "false"));
  }

  /**
   * Sets the application name on the the finder menu
   */
  public static void setMacAppName(String name) {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", name);
  }

  /**
   * Returns the total amount of memory installed in the system as a string. We use "G" to denote Gibabytes and "M" to
   * denote Megabytes
   */
  public static String getTotalMemory() {
    return memAsString(Runtime.getRuntime().totalMemory());
  }

  /**
   * Returns the total amount of free memory in the system as a string. We use "G" to denote Gibabytes and "M" to denote
   * Megabytes
   */
  public static String getFreeMemory() {
    System.gc();
    return memAsString(Runtime.getRuntime().freeMemory());
  }

  /**
   * Just converts a memory size into its nearest mnemonic ("G", M" or "K")
   * 
   * //todo: check that the RunTime does return the number of bytes as we seem to be out by a factor of 100
   */
  private static String memAsString(long m) {
    if (m > 1000000000)
      return (m / 1000000000) + "G";

    if (m > 1000000)
      return (m / 1000000) + "M";

    if (m > 1000)
      return (m / 1000) + "K";

    return "" + m + "B";
  }
}
