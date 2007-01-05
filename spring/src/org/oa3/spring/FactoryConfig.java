package org.oa3.spring;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FactoryConfig implements FactoryConfigMBean {

  private static final Log log = LogFactory.getLog(FactoryConfig.class);

  String config;

  Properties properties;

  public FactoryConfig(String configUrl, String propsUrl) {
    properties = new Properties();
    if (propsUrl != null) {
      InputStream is;
      try {
        is = (new URL(propsUrl).openStream());
        properties.load(is);
        is.close();
      } catch (Exception e) {
        log.error("failed to load properties", e);
      }
    }
    try {
      URL url = new URL(configUrl);
      InputStream is = url.openStream();
      BufferedReader r = new BufferedReader(new InputStreamReader(is));
      StringBuffer buffer = new StringBuffer();
      String line;
      while ((line = r.readLine()) != null) {
        buffer.append(line).append('\n');
      }
      r.close();
      config = buffer.toString();
    } catch (Exception e) {
      log.error("failed to load config", e);
    }
  }

  public String dumpConfig() {
    return "<pre>" + config.replaceAll("&", "&amp").replaceAll(">", "&gt").replaceAll("<", "&lt") + "]]></pre>";
  }

  public String dumpProperties() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<table>");
    for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      buffer.append("<tr><td>").append(entry.getKey().toString());
      buffer.append("</td><td> = ").append(entry.getValue().toString());
      buffer.append("</td></tr>");
    }
    buffer.append("</table>");
    return buffer.toString();
  }

}
