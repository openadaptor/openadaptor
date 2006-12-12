package org.oa3.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class SystemProperties {

  public void setProperties(Properties props) {
    for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      System.setProperty(entry.getKey().toString(), entry.getValue().toString());
    }
  }
}
