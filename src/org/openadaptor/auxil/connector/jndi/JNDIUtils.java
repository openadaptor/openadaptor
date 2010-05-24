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

package org.openadaptor.auxil.connector.jndi;

import java.util.ArrayList;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;

/**
 * Miscellaneous JNDI Utilities.
 * 
 * @author Eddy Higgins
 */
public class JNDIUtils {
  
  //private static final Log log = LogFactory.getLog(JNDIUtils.class);

  public static Object getAttributeValue(Attribute attribute, boolean multiValuedAsArray, 
      String joinArraysWithSeparator) throws NamingException {
    Object result = null;
    if (attribute != null) {
      if (multiValuedAsArray) {
        result = namingEnumerationToStringArray(attribute.getAll());
        if (joinArraysWithSeparator != null) {
          String[] resultAsArray = (String[]) result;
          StringBuffer aggregate = new StringBuffer();
          for (int i = 0; i < resultAsArray.length; i++) {
            if (i > 0) {
              aggregate.append(joinArraysWithSeparator);
            }
            aggregate.append(resultAsArray[i]);
          }
          result = aggregate.toString();
        }
      } else {
        result = attribute.get();
      }
    }
    return result;
  }

  /**
   * Utility method to convert a SearchResult into an orderedMap of results.
   * 
   * @param searchResult
   * @param treatMultiValuedAttributesAsArray
   * @param joinArraysWithSeparator
   *          (typically this is null meaning don't join arrays of values into single value)
   * @return OrderedMap representation of the SearchResult
   * @throws NamingException
   */
  public static IOrderedMap getOrderedMap(SearchResult searchResult, boolean treatMultiValuedAttributesAsArray,
      String joinArraysWithSeparator) throws NamingException {

    IOrderedMap result = new OrderedHashMap();

    Attributes attrs = searchResult.getAttributes();

    NamingEnumeration ids = attrs.getIDs();
    while (ids.hasMore()) {
      String name = (String) ids.next();
      Object value = JNDIUtils.getAttributeValue(attrs.get(name), treatMultiValuedAttributesAsArray,
          joinArraysWithSeparator);
      result.put(name, value);

    }
    return result;
  }

  public static String[] namingEnumerationToStringArray(NamingEnumeration ne) {
    String[] result = null;
    if (ne != null) {
      ArrayList values = new ArrayList();
      while (ne.hasMoreElements()) {
        values.add((String) ne.nextElement());
      }
      result = (String[]) values.toArray(new String[values.size()]);
    }
    return result;
  }

  public static String[] getAttributeNames(Attributes attrs) {
    return namingEnumerationToStringArray(attrs.getIDs());
  }

}
