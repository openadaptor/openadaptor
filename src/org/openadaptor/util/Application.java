/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * core functionality that is desirable for applications, outputs license, loads build
 * properties and if configured posts all the system properties to a url.
 * 
 * All the work is done in the constructor, so can be subclassed or instantiated by
 * top level application class (such as an adaptor).
 * 
 */
public class Application {

  private static final Log log = LogFactory.getLog(Application.class);

  private static final String PROPERTY_HOSTADDRESS      = "openadaptor.hostaddress";

  private static final String PROPERTY_HOSTNAME         = "openadaptor.hostname";

  private static final String PROPERTY_REGISTRATION_URL = "openadaptor.registration.url";

  public static final String PROPERTY_CONFIG_URL        = "openadaptor.config.url";

  public static final String PROPERTY_PROPS_URL         = "openadaptor.props.url";

  public static final String PROPERTY_COMPONENT_ID      = "openadaptor.component.id";

  public static final String PROPERTY_START_TIMESTAMP   = "openadaptor.application.start";

  public static final String START_TIMESTAMP_FORMAT     = "yyyy/MM/dd HH:mm:ss";

  private static final String BUILD_PROPERTIES_NAME     = ".openadaptor.properties";

  private static final String LICENCE_TEXT = "\n"
      + " [[                                                                                  \n"
      + " Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights reserved. \n"
      + "                                                                                     \n"
      + " Permission is hereby granted, free of charge, to any person obtaining a             \n"
      + " copy of this software and associated documentation files (the                       \n"
      + "\"Software\"), to deal in the Software without restriction, including                \n"
      + " without limitation the rights to use, copy, modify, merge, publish,                 \n"
      + " distribute, sublicense, and/or sell copies of the Software, and to                  \n"
      + " permit persons to whom the Software is furnished to do so, subject to               \n"
      + " the following conditions:                                                           \n"
      + "                                                                                     \n"
      + " The above copyright notice and this permission notice shall be included             \n"
      + " in all copies or substantial portions of the Software.                              \n"
      + "                                                                                     \n"
      + " THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS           \n"
      + " OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          \n"
      + " MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               \n"
      + " NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              \n"
      + " LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              \n"
      + " OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               \n"
      + " WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     \n"
      + "                                                                                     \n"
      + " Nothing in this notice shall be deemed to grant any rights to                       \n"
      + " trademarks, copyrights, patents, trade secrets or any other intellectual            \n"
      + " property of the licensor or any contributor except as expressly stated              \n"
      + " herein. No patent license is granted separate from the Software, for                \n"
      + " code that you delete from the Software, or for combinations of the                  \n"
      + " Software with other software or hardware.                                           \n"
      + " ]]                                                                                  \n"
      + "                                                                                     \n";

  static {
    System.err.println(LICENCE_TEXT);
  }
  
  public Application() {

    // get system props
    Properties props = new Properties();
    props.putAll(System.getProperties());

    // override with build properties
    Properties buildProps = new Properties();
    InputStream in = Application.class.getResourceAsStream(BUILD_PROPERTIES_NAME);
    if (in != null) {
      try {
        buildProps.load(in);
      } catch (IOException e) {
        log.error("failed to load " + BUILD_PROPERTIES_NAME + " from classpath");
      }
    } else {
      log.warn("failed to find " + BUILD_PROPERTIES_NAME + " in classpath");
    }

    // set start time and host properties
    props.put(PROPERTY_START_TIMESTAMP, (new SimpleDateFormat(START_TIMESTAMP_FORMAT)).format(new Date()));
    props.put(PROPERTY_HOSTNAME, ResourceUtils.getLocalHostname());
    props.put(PROPERTY_HOSTADDRESS, ResourceUtils.getLocalHostAddress());

    for (Iterator iter = buildProps.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      if (props.containsKey(entry.getKey())) {
        log.info("build property " + entry.getKey() + " overrides system property");
      }
      props.put(entry.getKey(), entry.getValue());
    }

    System.setProperties(props);
    for (Iterator iter = System.getProperties().entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      log.debug("system property " + entry.getKey() + "=[" + entry.getValue() + "]");
    }

    String url = props.getProperty(PROPERTY_REGISTRATION_URL, null);

    if (url != null && url.length() > 0) {
      try {
        PropertiesPoster.post(url, props);
        log.info("posted system properties to " + url);
      } catch (Exception e) {
        log.warn("failed to post system properties : " + e.getMessage());
      }
    } else {
      log.info("registration url property (" + PROPERTY_REGISTRATION_URL + ") is not configured");
    }

  }

  public static void main(String[] args) {
    new Application();
  }
}
