/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.util;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/util/ResourceUtils.java,v 1.3 2006/04/13 09:58:44 higginse Exp $ Rev: $Revision:
 * 1.3 $ Created Feb 22, 2006 by Eddy Higgins
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.ComponentException;

/**
 * Common Resource Utilities for OA3
 * 
 * @author Eddy Higgins
 */
public class ResourceUtils {
  private static final String BUILD_PROPERTIES = "oa3_build";

  private static final String OA3_JAR_NAME = "oa3.jar";

  private static final String OA3_MANIFEST_SECTION = "oa3";

  private static final Log log = LogFactory.getLog(ResourceUtils.class.getName());

  private ResourceUtils() {
  } // No instantiation allowed.

  // This will be lazily loaded.
  private static Properties buildProperties = null;

  /**
   * This method will return a set of properties which should correspond to those used at build time. It attempts to
   * retrieve the resource. It attempts this as follows: Firstly, it attempts to look at the manifest from oa3.jar.
   * Failing this, it tries to load the resource BUILD_PROPERTIES.
   * 
   * @return Properties object containg information on the build enviroment
   */
  public static Properties getBuildProperties() throws ComponentException {
    if (buildProperties == null) {// Attempt to get them.
      log.info("Retrieving build information from " + OA3_JAR_NAME);
      try {
        buildProperties = getPropertiesFromJarManifest(OA3_JAR_NAME, OA3_MANIFEST_SECTION);
      } catch (IOException ioe) {
        log.warn("Failed to get manifest from " + OA3_JAR_NAME + ". Exception: " + ioe);
        try {
          buildProperties = getPropertiesFromClasspathResource(BUILD_PROPERTIES);
        } catch (MissingResourceException mre) {
          throw new RuntimeException("Failed to get build properties via " + BUILD_PROPERTIES);
        }
      }
    }
    Properties result = new Properties();
    result.putAll(buildProperties);
    return result;
  }

  private static Properties getPropertiesFromClasspathResource(String resourceName) throws MissingResourceException {
    Properties props = new Properties();
    log.debug("Retrieving build information from " + resourceName);
    ResourceBundle rb = ResourceBundle.getBundle(resourceName);
    for (Enumeration keys = rb.getKeys(); keys.hasMoreElements();) {
      String key = (String) keys.nextElement();
      String value = rb.getString(key);
      props.put(key, value);
      log.debug(key + " -> " + value);
    }
    return props;
  }

  private static Properties getPropertiesFromJarManifest(String jarName, String section) throws IOException {
    Properties props = new Properties();
    // Firstly get a url for the jar itself.
    URL url = ResourceUtils.class.getClassLoader().getResource(jarName);
    if (url == null) {
      throw new IOException("Failed to locate jar " + jarName);
    }
    log.debug("Jar url is: " + url);
    // Next get a JarInputStream for reading it...
    JarInputStream jis = new JarInputStream(url.openStream());
    // Now extract the Manifest from the jar
    Manifest manifest = jis.getManifest();
    if (manifest != null) {
      log.debug("Jar manifest retrieved from " + jarName);
      Attributes attrs = manifest.getAttributes(section);
      if (attrs != null) {
        log.debug(attrs.size() + " attributes retrieved from section " + section);
        for (Iterator keys = attrs.keySet().iterator(); keys.hasNext();) {
          Object key = keys.next();
          Object value = attrs.get(key);
          // Manifest does not permit names with '.' in them.
          // Convert underscores to '.'
          // This assumes build script has appropriately set the values!
          String name = key.toString().replace('_', '.');
          props.put(name, value);
          log.debug(name + " -> " + value);
        }
      } else {
        log.warn("No " + section + " section in manifest - no properties will be retrieved");
      }
    } else {
      throw new IOException("Failed to get a manifest from" + jarName);
    }
    return props;
  }

  private static InetAddress getLocalInetAddress() {
    InetAddress result = null;
    try {
      result = InetAddress.getLocalHost();

    } catch (UnknownHostException e) {
      log.warn("Failed to determine local host address");
    }
    return result;
  }

  public static String getLocalHostname() {
    String result = "<Unknown>";
    InetAddress inetAddr = getLocalInetAddress();
    if (inetAddr != null) {
      result = inetAddr.getHostName();
    }
    return result;
  }

  public static String getLocalHostAddress() {
    String result = "<Unknown>";
    InetAddress inetAddr = getLocalInetAddress();
    if (inetAddr != null) {
      result = inetAddr.getHostAddress();
    }
    return result;
  }

  /**
   * Simple sanity test method.
   */
  public static void main(String[] argv) {
    try {
      System.out.println("LocalHostname: " + getLocalHostname());
      System.out.println("LocalHostaddr: " + getLocalHostAddress());
      Properties props = getBuildProperties();
      System.out.println("Retrieving Build information");
      Iterator it = props.keySet().iterator();
      while (it.hasNext()) {
        Object key = it.next();
        String value = (String) props.get(key);
        System.out.println(key + " -> " + value);
      }
    } catch (ComponentException e) {
      System.err.println(e);
    }
  }
}
