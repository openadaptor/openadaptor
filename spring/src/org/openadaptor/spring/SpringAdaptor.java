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

package org.openadaptor.spring;

import java.io.PrintStream;

import org.openadaptor.core.adaptor.Adaptor;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * helper class for launch openadaptor adaptor processes based on spring.
 * 
 * @author perryj
 *
 */
public class SpringAdaptor extends SpringApplication {

  public static void main(String[] args) {
    SpringAdaptor springAdaptor = new SpringAdaptor();
    springAdaptor.execute(args);
    System.exit(0);
  }
  
  
  public void execute(String [] args){
    try {
      SpringAdaptor app = new SpringAdaptor();
      app.parseArgs(args);
      app.run();   
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      usage(System.err);
      System.exit(1);
    } 
  }

  
  protected Runnable getRunnableBean(ListableBeanFactory factory) {
    if (getBeanId() == null) {
      String[] ids = factory.getBeanNamesForType(Adaptor.class);
      if (ids.length == 1) {
        setBeanId(ids[0]);
      } else if (ids.length == 0){
        throw new RuntimeException("No Adaptor bean found in config");
      } else if (ids.length > 1) {
        throw new RuntimeException("Mulitple Adaptor beans found in config");
      }
    }
    return (Adaptor) factory.getBean(getBeanId());
  }


  protected static void usage(PrintStream ps) {
    ps.println("usage: java " + SpringApplication.class.getName()
      + "\n  " + CONFIG + " <url> [ " + CONFIG + " <url> ]"
      + "\n  [ " + BEAN + " <id> ]"
      + "\n  [ " + PROPS + " <url> [ " + PROPS + " <url> ] ]"
      + "\n  [ " + NOPROPS + " ]"
      + "\n  [ " + JMXPORT + " <http port>]"
      + "\n\n"
      + " e.g. java " + SpringApplication.class.getName() + " " + CONFIG + " file:test.xml " + BEAN + " Application");
  }

}
