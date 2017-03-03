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

package org.openadaptor.util.beans;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Trivial utility bean to generate round-robin values
 * @author higginse
 *
 */
public class RoundRobinGeneratorBean {
  public static final Log log = LogFactory.getLog(RoundRobinGeneratorBean.class);

  private Object[] values;
  private int currentIndex=0;

  /**
   * Provide a list of values to be repeated in a round-robin fashion
   * @param valueList List of values.
   */
  public void setValues(List valueList) {
    if (valueList!=null) {
      values=valueList.toArray(new Object[valueList.size()]);
    }
  }
  /**
   * Get the list of (Object) values to be included
   * @return List containing the values.
   */
  public List getValues() {
    return Collections.unmodifiableList(Arrays.asList(values));
  }

  /**
   * Default constructor.
   * setValues() will need to be used to populate the values.
   */
  public RoundRobinGeneratorBean() {}

  /**
   * Constructor which implicitly calls setValues to 
   * popluate the list.
   * 
   * @param valueList list of Object values.
   */
  public RoundRobinGeneratorBean(List valueList) {
    this();
    setValues(valueList);
  }

  /**
   * Returns the currentValue in the round-robin sequence.
   * @return current value from the list.
   */
  public Object getCurrentValue() {
    return values[currentIndex];
  }

  /**
   * Advances to the next item in the round-robin sequence,
   * cyling back to the beginning if necessary.
   *
   * @return the next value in the sequence. getCurrentValue() will
   *         also return this value, until next is again called.
   */
  public Object next() {
    currentIndex=(currentIndex+1) % values.length;
    return getCurrentValue();
  }

  /* Purely for sanit test.
  public static void main(String[] argv) {
    Object[][] args={{0,1,2},{0,1,2,3},{1,2,3,4},{10,11,12,13,14,15}};
    RoundRobinGeneratorBean[] beans =new RoundRobinGeneratorBean[args.length];
    for (int i=0;i<beans.length;i++) {
      Object[] vals=args[i];
      beans[i]=new RoundRobinGeneratorBean(Arrays.asList(vals));
      RoundRobinGeneratorBean bean=beans[i];
      for (int j=0;j<20;j++) {
        System.out.print(bean.getCurrentValue().toString()+" ");
        bean.next();
      }
      System.out.println();
    }
  }
  */
}

