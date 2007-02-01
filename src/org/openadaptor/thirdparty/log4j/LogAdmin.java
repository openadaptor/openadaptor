/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.thirdparty.log4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openadaptor.core.jmx.Administrable;

/**
 * 
 * @author perryj
 *
 */
public class LogAdmin implements Administrable {

  public Object getAdmin() {
    return new Admin();
  }
  
  public interface AdminMBean {
    String[] getLoggers();
    void enableInfo(String logger);
    void enableDebug(String logger);
  }

  public class Admin implements AdminMBean {

    public void enableInfo(String loggerName) {
      setLevel(loggerName, Level.INFO);
    }

    public void enableDebug(String loggerName) {
       setLevel(loggerName, Level.DEBUG);
    }

    public String[] getLoggers() {
      ArrayList loggers = new ArrayList();
      Enumeration e = LogManager.getCurrentLoggers();
      while (e.hasMoreElements()) {
        Logger l = (Logger) e.nextElement();
        loggers.add("[" + l.getEffectiveLevel() + "] " + l.getName());
      }
      Collections.sort(loggers);
      return (String[]) loggers.toArray(new String[loggers.size()]);
    }
    
    private void setLevel(String loggerName, Level level) {
      Logger logger;
      if (loggerName != null && loggerName.length() > 0) {
        logger = Logger.getLogger(loggerName);
      } else {
        logger = Logger.getRootLogger();
      }
      if (logger != null) {
        logger.setLevel(level);
        Logger.getRootLogger().info("set level to " + level + " for " + (loggerName != null ? loggerName : "root"));
      } else {
        Logger.getRootLogger().info(loggerName + " not recognised");
      }
    }

  }
}
