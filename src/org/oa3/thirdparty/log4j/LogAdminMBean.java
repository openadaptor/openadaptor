package org.oa3.thirdparty.log4j;

public interface LogAdminMBean {
  String[] getLoggers();
  void enableInfo(String logger);
  void enableDebug(String logger);
}
