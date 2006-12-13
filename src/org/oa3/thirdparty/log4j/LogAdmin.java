package org.oa3.thirdparty.log4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogAdmin implements LogAdminMBean {

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
