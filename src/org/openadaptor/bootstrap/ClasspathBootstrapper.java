/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.bootstrap;

//
// DO NOT IMPORT ANYTHING OTHER THAN java PACKAGES FOR THIS CLASS
// IT MUST BE INDEPEDENT!!!!
//

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for boostrapping the classpath. It provides a method that accepts a
 * classname and args. It will locate the jar that {@link ClasspathBootstrapper}
 * has been loaded from, create a new classloader, load that class and calls
 * it's main method with the args.
 * 
 * The classpath of the classloader will contain the dir in which the
 * aforementioned jar resides plus all the zips and jars and sub dirs.
 * openadaptor-depends.jar is deliberately excluded and openadptor-stub.jar
 * always comes last. If the user defines the following system property
 * {@link #SYSTEM_PROPERTY_OPENADAPTOR_LIB} then the contents of this dir (and
 * subdirs) will be prepended to the classpath too.
 * 
 * @author perryj
 * 
 */
public class ClasspathBootstrapper {

  public static final String SYSTEM_PROPERTY_OPENADAPTOR_LIB = "openadaptor.lib";

  /*
   * class main method of class using reflection. creates a new classloader and
   * sets classpath according to the location of the jar from where this class
   * was loaded.
   */
  public static void main(String classname, String[] args) {
    try {
      boolean verbose = Boolean.getBoolean("verbose");
      
      // create array of urls classpath
      String libPath = getJarDirectory();
      ArrayList files = new ArrayList();
      files.add(new File(libPath, "patch.jar"));
      if (System.getProperty(SYSTEM_PROPERTY_OPENADAPTOR_LIB, null) != null) {
        files.addAll(getLibUrls(new File(System.getProperty(SYSTEM_PROPERTY_OPENADAPTOR_LIB))));
      }
      files.addAll(getLibUrls(new File(libPath)));
      files.add(new File(libPath, "openadaptor-stub.jar"));
      
      // convert files to array of urls
      URL[] urls = new URL[files.size()];
      StringBuffer classpath = new StringBuffer();
      for (int i = 0; i < urls.length; i++) {
        File file = (File) files.get(i);
        if (verbose) {
          System.err.println(ClasspathBootstrapper.class.getName() + " adding : " + file.getAbsolutePath());
        }
        urls[i] = new URL("file:" + file.getAbsolutePath());
        classpath.append(classpath.length() > 0 ? "" + File.pathSeparatorChar : "");
        classpath.append(file.getAbsolutePath());
      }
      System.setProperty("java.class.path", classpath.toString());
      
      // create url class loader, load the class from this loader and invoke main with args
      URLClassLoader classLoader = new URLClassLoader(urls);
      Thread.currentThread().setContextClassLoader(classLoader);
      Class appClass = classLoader.loadClass(classname);
      Method mainMethod = appClass.getMethod("main", new Class[] { Array.newInstance(String.class, 0).getClass() });
      mainMethod.invoke(appClass, new Object[] { args });
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  static String getJarDirectory() {
    String s = ClasspathBootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    File f = new File(s);
    if (f.isDirectory()) {
      throw new RuntimeException("This class can only be used from a jar");
    } else {
      return f.getParent();
    }
  }

  static String getJarName() {
    String s = ClasspathBootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    File f = new File(s);
    if (f.isDirectory()) {
      throw new RuntimeException("This class can only be used from a jar");
    } else {
      return f.getName();
    }
  }

  static String getJarPath() {
    String s = ClasspathBootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    File f = new File(s);
    if (f.isDirectory()) {
      throw new RuntimeException("This class can only be used from a jar");
    } else {
      return f.getAbsolutePath();
    }
  }

  private static List getLibUrls(File dir) {
    ArrayList urls = new ArrayList();
    String[] files = dir.list();
    urls.add(dir);
    for (int i = 0; i < files.length; i++) {
      File file = new File(dir, files[i]);
      if (!file.isDirectory() && addToClasspath(file)) {
        urls.add(file);
      }
    }
    for (int i = 0; i < files.length; i++) {
      File file = new File(dir, files[i]);
      if (file.isDirectory()) {
        urls.addAll(getLibUrls(file));
      }
    }
    return urls;
  }

  private static boolean addToClasspath(File file) {
    if (file.exists()) {
      if (file.getName().equals(getJarName())) {
        return false;
      }
      if (file.getName().endsWith("patch.jar")) {
        return false;
      }
      if (file.getName().endsWith("-depends.jar")) {
        return false;
      }
      if (file.getName().endsWith("-stub.jar")) {
        return false;
      }
      if (file.getName().endsWith(".jar")) {
        return true;
      }
      if (file.getName().endsWith(".zip")) {
        return true;
      }
    }
    return false;
  }
  
}
