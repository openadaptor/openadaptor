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

import java.util.ArrayList;
import java.util.Properties;

/**
 * Helper methods for use with Java properties files
 * 
 * @author Russ Fennell
 */
public class PropsUtils {

  /**
   * Trys to convert the property defined by the name into an int
   * 
   * @param props
   *          the Java Properties object
   * @param name
   *          the property name
   * @param defaultValue
   *          the default value to use if the property is not defined
   * 
   * @return the integer value of the property or the default value
   * 
   * @throws RuntimeException
   *           if the property value cannot be converted into an int
   */
  public static int getIntFromProps(Properties props, String name, int defaultValue) {
    String s = props.getProperty(name);

    int i = defaultValue;

    if (s != null) {
      try {
        i = Integer.parseInt(s);
      } catch (NumberFormatException e) {
        throw new RuntimeException("Failed to convert " + name + " to int: " + e.getMessage());
      }
    }

    return i;
  }

  /**
   * Trys to convert the property defined by the name into a float
   * 
   * @param props
   *          the Java Properties object
   * @param name
   *          the property name
   * @param defaultValue
   *          the default value to use if the property is not defined
   * 
   * @return the float value of the property or the default value
   * 
   * @throws RuntimeException
   *           if the property value cannot be converted into a float
   */
  public static float getFloatFromProps(Properties props, String name, float defaultValue) {
    String s = props.getProperty(name);

    float f = defaultValue;

    if (s != null) {
      try {
        f = Float.parseFloat(s);
      } catch (NumberFormatException e) {
        throw new RuntimeException("Failed to convert " + name + " to float: " + e.getMessage());
      }
    }

    return f;
  }

  /**
   * Trys to convert the property defined by the name into a boolean
   * 
   * @param props
   *          the Java Properties object
   * @param name
   *          the property name
   * @param defaultValue
   *          the default value to use if the property is not defined
   * 
   * @return true if the property value is not null and is equal, ignoring case, to the string "true" or the default
   *         value if the property doesn't exist
   */
  public static boolean getBooleanFromProps(Properties props, String name, boolean defaultValue) {
    String s = props.getProperty(name);

    if (s != null)
      return Boolean.valueOf(s).booleanValue();
    else
      return defaultValue;
  }

  /**
   * Returns a list of the properties defined by the name and with a zero based index appended to the name. Will return
   * all properties until the next one in the list does not exist. Therefore you cannot retrieve non-contiguous sets of
   * properties.
   * 
   * @param props
   *          the Java Properties object
   * @param name
   *          the property name
   * 
   * @return list of property values according to the rules above
   */
  public static ArrayList getArrayListFromProps(Properties props, String name) {
    ArrayList a = new ArrayList();
    String p;

    for (int i = 0; (p = props.getProperty(name + i)) != null; i++)
      a.add(p);

    return a;
  }

}
