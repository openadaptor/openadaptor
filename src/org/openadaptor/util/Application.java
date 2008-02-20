/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;

/**
 * core functionality that is desirable for applications, outputs license, loads build
 * properties and if configured posts all the system properties to a url.
 * 
 * All the work is done in the constructor, so can be subclassed or instantiated by
 * top level application class (such as an adaptor).
 * 
 */
public class Application implements IComponent,IRegistrationCallbackListener {

  private static final Log log = LogFactory.getLog(Application.class);

  private static final String[] REGISTRATION_PROPERTIES_LOCATIONS={"registration.properties","org/openadaptor/util/registration.properties"};

  private static final String PROPERTY_HOSTADDRESS      = "openadaptor.hostaddress";

  private static final String PROPERTY_HOSTNAME         = "openadaptor.hostname";

  private static final String PROPERTY_REGISTRATION_URL = "openadaptor.registration.url";

  private static final String PROPERTY_CONFIG_URL        = "openadaptor.config.url";

  private static final String PROPERTY_COMPONENT_ID      = "openadaptor.component.id";

  public static final String PROPERTY_START_TIMESTAMP   = "openadaptor.application.start";

  public static final String START_TIMESTAMP_FORMAT     = "yyyy/MM/dd HH:mm:ss";

  private static final String[] BUILD_PROPERTIES_LOCATIONS={".openadaptor.properties"};

  private static final String[] LICENCE_LOCATIONS= {"licence.txt","org/openadaptor/util/licence.txt"};

  public static final String LICENCE_TEXT=loadLicence(LICENCE_LOCATIONS);

  private String registrationUrl;

  private Properties props;

  private boolean registerOnlyOnce = true;

  private boolean registered;

  private static String textFromClasspath(String[] locations) {
    return ClasspathUtils.loadStringFromClasspath(locations);
  }

  private static Properties propertiesFromClasspath(String[] locations) {
    return ClasspathUtils.loadPropertiesFromClasspath(locations);
  }

  private static String loadLicence(String[] locations) {
    String text=textFromClasspath(locations);
    if (text==null) {
      log.warn("Failed to get licence text from licence.txt or org/openadaptor/util/licence.txt");
    }
    else {
      log.debug("Issuing licence information to stderr");
      System.err.println("\n" + text + "\n");
    }
    return text;
  }

  protected Application() {

    log.info("classpath = " + System.getProperty("java.class.path", "not set"));
    log.info("java.version = "+System.getProperty("java.version","<unable to determine>"));
    // get system props
    props = new Properties();
    props.putAll(System.getProperties());

    // set start time and host properties
    String timestamp = (new SimpleDateFormat(START_TIMESTAMP_FORMAT)).format(new Date());
    // TODO: remove this and make property name private
    System.setProperty(PROPERTY_START_TIMESTAMP, timestamp);
    props.put(PROPERTY_START_TIMESTAMP, timestamp);
    props.put(PROPERTY_HOSTNAME, NetUtil.getLocalHostname());
    props.put(PROPERTY_HOSTADDRESS, NetUtil.getLocalHostAddress());

    // override with build properties
    Properties buildProps=propertiesFromClasspath(BUILD_PROPERTIES_LOCATIONS);
    
    if (buildProps!=null) {
      for (Iterator iter = buildProps.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        if (props.containsKey(entry.getKey()) && !props.get(entry.getKey()).equals(entry.getValue())) {
          log.info("build property " + entry.getKey() + " overrides system property");
        }
        log.info(entry.getKey() + " = " + entry.getValue());
        props.put(entry.getKey(), entry.getValue());
      }
    }
    else {
      log.warn("Failed to load build properties from classpath");
    }

  }

  public String getId() {
    return props.getProperty(PROPERTY_COMPONENT_ID, null);
  }

  public void setId(String id) {
    props.setProperty(PROPERTY_COMPONENT_ID, id);
  }

  public void setConfigData(String data) {
    props.setProperty(PROPERTY_CONFIG_URL, data);
  }

  /**
   * called by subclasses that want to register with a url
   */
  protected void register() {

    if (registered && registerOnlyOnce) {
      return;
    }

    // filter and transform properties
    Properties propsToRegister = filterProperties(props);

    // sort and log
    List toLog = new ArrayList();
    for (Iterator iter = propsToRegister.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      toLog.add(entry.getKey() + "=" + entry.getValue());
    }
    Collections.sort(toLog);
    for (Iterator iter = toLog.iterator(); iter.hasNext();) {
      log.info("property, " + iter.next());

    }

    // if registration url is set then post
    String url = getRegistrationUrl();
    if (url != null && url.length() > 0) {
      try {
        //Uncomment this when we move to Async Registration.
        //PropertiesPoster.post(url, propsToRegister,this);
        PropertiesPoster.post(url,propsToRegister);

        registered = true;
        log.info("posted registration properties to " + url);
      } catch (Exception e) {
        log.warn("failed to post registration properties : " + e.getMessage());
      }
    }
    registered = true;
  }

  private String getRegistrationUrl() {
    if (registrationUrl != null) {
      return registrationUrl;
    } else {
      return System.getProperty(PROPERTY_REGISTRATION_URL, null);
    }
  }

  private static Properties filterProperties(Properties props) {
    Properties newProps = new Properties();
    props=props==null?new Properties():props; //Make sure it's not null.
    Properties registrationProps = propertiesFromClasspath(REGISTRATION_PROPERTIES_LOCATIONS);
    if (registrationProps!=null) {
      for (Iterator iter = registrationProps.keySet().iterator(); iter.hasNext();) {
        String key = (String) iter.next();
        String propertyName = registrationProps.getProperty(key);
        if (propertyName != null && propertyName.length() > 0) {
          String value = props.getProperty(propertyName, "<unknown>");
          log.debug("registration property : " + key + "," + value);
          newProps.setProperty(key, value);
        }
      }
    }
    else {
      log.warn("Failed to load registration properties from classpath");
    }
    return newProps;
  }

  /*
  private static Properties filterProperties(Properties props) {
    Properties newProps = new Properties();
    InputStream is = Application.class.getResourceAsStream(REGISTRATION_PROPERTIES);
    Properties registrationProps = new Properties();
    try {
      registrationProps.load(is);
      for (Iterator iter = registrationProps.keySet().iterator(); iter.hasNext();) {
        String key = (String) iter.next();
        String propertyName = registrationProps.getProperty(key);
        if (propertyName != null && propertyName.length() > 0) {
          String value = props.getProperty(propertyName, "<unknown>");
          log.debug("registration property : " + key + "," + value);
          newProps.setProperty(key, value);
        }
      }
    } catch (IOException e) {
      log.warn("failed to load " + REGISTRATION_PROPERTIES);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
      }
    }
    return newProps;
  }
   */
  /**
   * Adds props to application properties
   */
  public void setAdditionalRegistrationProps(final Properties props) {
    for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      this.props.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * 
   * @param registrationUrl a url to post application properties to
   */
  public void setRegistrationUrl(final String registrationUrl) {
    this.registrationUrl = registrationUrl;
  }

  /**
   * 
   * @param registerOnlyOnce if true then only the first call to register will
   * post the properties to the registration url
   */
  public void setRegisterOnlyOnce(final boolean registerOnlyOnce) {
    this.registerOnlyOnce = registerOnlyOnce;
  }

  /**
   * Process a callback from Registration.
   * 
   * Not used yet.
   */
  public void registrationCallbackEvent(Object src, Object data) {
    log.debug("registrationCallbackEvent occured from "+src);
    String info=data==null?"No information":data.toString();
    log.info("Registration status: "+info);
  }
}
