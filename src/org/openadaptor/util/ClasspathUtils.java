/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Utility class for dealing with classpath resources
 * @author higginse
 */
public class ClasspathUtils {
  private static final Log log =LogFactory.getLog(ClasspathUtils.class);

  private static ClassLoader CLASS_LOADER=ClasspathUtils.class.getClassLoader();
  private static int BUF_SIZ=4096;

  /**
   * Load a text resource from the classpath, given a ordered list of resources to try.
   * <br>
   * It will try each of the resources in order, and return as soon as one is
   * successfully loaded.
   * @param candidateAbsoluteResourceNames
   * @return String containing null if resource could not be loaded, or the text contents
   *         of the resource
   */
  public static String loadStringFromClasspath(String[] candidateAbsoluteResourceNames) {
    Object result=loadResource(candidateAbsoluteResourceNames,new StreamLoader() {
      public Object load(InputStream is) throws IOException {
        return getContents(is);
      }
    });
    return (String)result;
  }

  /**
   * Load properties from a resource from the classpath, given a ordered list of resources to try.
   * <br>
   * It will try each of the resources in order, and return as soon as one is
   * successfully loaded.
   * @param candidateAbsoluteResourceNames
   * @return String containing null if resource could not be loaded, or the text contents
   *         of the resource
   */
  public static Properties loadPropertiesFromClasspath(String[] candidateAbsoluteResourceNames) {
    Object result=loadResource(candidateAbsoluteResourceNames,new StreamLoader() {
      public Object load(InputStream is) throws IOException {
        Properties props=new Properties();
        props.load(is);
        return props;
      }
    });
    return (Properties)result;
  }

  private static Object loadResource(String[] candidateAbsoluteResourceNames,StreamLoader loader) {
    Object result=null;
    if (candidateAbsoluteResourceNames!=null) {
      for (int i=0;i<candidateAbsoluteResourceNames.length && (result==null);i++) {
        String absoluteResourceName=candidateAbsoluteResourceNames[i];
        InputStream is=CLASS_LOADER.getResourceAsStream(absoluteResourceName);
        if (is!=null) {
          try {
            result=loader.load(is);
            log.info("Loaded classpath resource: "+absoluteResourceName);
          }
          catch (IOException ioe) {
            log.debug("Resource not available from: "+absoluteResourceName+" ("+ioe.toString()+")");
          }
        }
        else {
          log.debug("Failed to load resource from: "+absoluteResourceName);
        }
      }
    }     
    return result;
  }

  private static String getContents(InputStream is) throws IOException {
    return getContents(new InputStreamReader(is));
  }

  private static String getContents(Reader reader) throws IOException {
    char[] buf=new char[BUF_SIZ];
    StringBuffer sb = new StringBuffer();
    int len = 0;
    try {
      while ((len = reader.read(buf, 0, buf.length)) != -1) {
        sb.append(buf, 0, len);
      }
    } 
    finally {
      if (reader != null) {
        try {
          reader.close();
        } 
        catch (IOException e) {  
          log.warn("Failed to cleanly close Reader "+reader);
        }
      }
    }
    return sb.toString();
  }

  /**
   * Utility to find where a class was originally loaded from.
   * This may be handy to debug issues where it seems an incorrect
   * class is being loaded (e.g. where the same class may exist in 
   * multiple jars).
   * It returns null if the class was loaded from JRE (I think!)
   * @param cls The class to lookup
   * @return URL where class was loaded from, or null if JRE
   * @since 3.4.5
   */
  public static URL getClassOrigin(Class cls) {
    URL result=null;
    ProtectionDomain domain=cls.getProtectionDomain();
    if (cls!=null) {
      CodeSource source=domain.getCodeSource();
      if (source!=null) {
        result= source.getLocation();
      }
    }
    return result;
  }
  /**
   * Find where an object's class was loaded.
   * This is just a thin wrapper around {@link #getClassOrigin(Class)}.
   * @param o Object to be looked up
   * @return URL of the location where the class of the object was loaded from.
   * @since 3.4.5
   */
  public static URL getClassOrigin(Object o) {
    return getClassOrigin(o.getClass());
  }

  private interface StreamLoader {
    public Object load(InputStream is) throws IOException;
  }
}
