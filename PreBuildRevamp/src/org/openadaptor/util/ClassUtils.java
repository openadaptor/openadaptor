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

package org.openadaptor.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassUtils {

  public static void main(String[] args) {
    {
      Class baseClass = org.openadaptor.core.IReadConnector.class;
      System.err.println(baseClass.getName());
      for (Iterator iter = getSubClassesFromClasspath(baseClass).iterator(); iter.hasNext();) {
        Class c = (Class) iter.next();
        System.err.println("  " + c.getName());
      }; 
    }
    {
      Class baseClass = org.openadaptor.core.IWriteConnector.class;
      System.err.println(baseClass.getName());
      for (Iterator iter = getSubClassesFromClasspath(baseClass).iterator(); iter.hasNext();) {
        Class c = (Class) iter.next();
        System.err.println("  " + c.getName());
      }; 
    }
    {
      Class baseClass = org.openadaptor.core.IDataProcessor.class;
      System.err.println(baseClass.getName());
      for (Iterator iter = getSubClassesFromClasspath(baseClass).iterator(); iter.hasNext();) {
        Class c = (Class) iter.next();
        System.err.println("  " + c.getName());
      }; 
    }
  }
  
  public static Collection getSubClassesFromClasspath(Class superClass) {
    String classpath = System.getProperty("java.class.path");
    return getSubClassesFromClasspath(superClass, classpath);
  }
  
  public static List getSubClassesFromClasspath(Class superClass, String classpath) {
    ArrayList classes = new ArrayList();
    String[] pathElements = classpath.split(File.pathSeparator);
    for (int i = 0; i < pathElements.length; i++) {
      File file = new File(pathElements[i]);
      if (file.exists() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
        classes.addAll(getSubClassesFromZip(superClass, file));
      } else if (file.exists() && file.isDirectory()) {
        classes.addAll(getSubClassesFromDir(superClass, file));
      }
    }
    return classes;
  }

  public static Collection getSubClassesFromZip(Class superClass, File file) {
    ArrayList classes = new ArrayList();
    try {
      ZipFile zipFile = new ZipFile(file);
      Enumeration e = zipFile.entries();
      while (e.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) e.nextElement();
        if (entry.getName().endsWith(".class")) {
          Class subclass = classForNameNoThrow(pathToClassName(entry.getName()));
          if (isValidSubclass(superClass, subclass)) {
            classes.add(subclass);
          }
        }
      }
    } catch (IOException e) {
    }
    return classes;
  }

  public static Collection getSubClassesFromDir(Class superClass, File dir) {
    return getSubClassesFromDir(superClass, dir, null);
  }
  
  public static Collection getSubClassesFromDir(Class superClass, File dir, String packageName) {
    ArrayList classes = new ArrayList();
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (!files[i].isDirectory()) {
        if (files[i].getName().endsWith(".jar") || files[i].getName().endsWith(".zip")) {
          classes.addAll(getSubClassesFromZip(superClass, files[i]));
        }
        else {
          if (files[i].getName().endsWith(".class")) {
            Class subclass = classForNameNoThrow(pathToClassName(createPackageName(packageName, "") + files[i].getName()));
            if (isValidSubclass(superClass, subclass)) {
              classes.add(subclass);
            }
          }
        }
      } else {
        classes.addAll(getSubClassesFromDir(superClass, files[i], createPackageName(packageName, files[i].getName())));
      }
    }
    return classes;
  }
  
  private static String pathToClassName(String path) {
    return path.substring(0, path.indexOf(".class")).replace('/', '.');
  }

  private static String createPackageName(String packageName, String dir) {
    if (packageName == null) {
      return dir;
    } else {
      return packageName + "." + dir;
    }
  }
  private static Class classForNameNoThrow(String className) {
    try {
      return Class.forName(className);
    } catch (Throwable ex) {
    }
    return Object.class;
  }

  private static boolean isValidSubclass(Class superClass, Class subclass) {
    boolean isSubclass = superClass.isAssignableFrom(subclass);
    int mods = subclass.getModifiers();
    boolean isAbstract = Modifier.isAbstract(mods);
    boolean isPublic = Modifier.isPublic(mods);
    return isSubclass && !isAbstract && isPublic;
  }
  
}
