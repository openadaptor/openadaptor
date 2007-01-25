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

package org.openadaptor.spring;

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
    return "<textarea cols=\"120\" rows=\"40\" readonly=\"true\">" + config + "</textarea>";
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
