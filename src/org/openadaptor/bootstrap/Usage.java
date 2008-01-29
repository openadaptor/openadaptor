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

package org.openadaptor.bootstrap;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//
//DO NOT IMPORT ANYTHING OTHER THAN java PACKAGES FOR THIS CLASS
//IT MUST BE INDEPEDENT!!!!
//

public class Usage {

  /**
   * dumps out all the classes in the jar that contains this class that have a main
   * method
   * @throws IOException 
   * @throws ClassNotFoundException 
   */
  public static void main(String args[]) throws IOException, ClassNotFoundException {
    System.err.println("This jar contains the following programs : ");
    ZipFile zipFile = new ZipFile(ClasspathBootstrapper.getJarPath());
    Enumeration e = zipFile.entries();
    while (e.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      if (entry.getName().endsWith(".class")) {
        String className = entry.getName();
        className = entry.getName().substring(0, entry.getName().indexOf(".class")).replace('/', '.');
        Class klass = Class.forName(className);
        if (isValidSubclass(ClasspathBootstrapper.class, klass) && klass != ClasspathBootstrapper.class) {
          System.err.println("  " + className);
        }
      }
    }
    System.err.println("When you run these they will bootstrap the classpath based on the location of this jar");
    System.err.println("use -D" + ClasspathBootstrapper.SYSTEM_PROPERTY_OPENADAPTOR_LIB + "=... to provide an additional location to search for classpath resources");
  }

  private static boolean isValidSubclass(Class superClass, Class subclass) {
    boolean isSubclass = superClass.isAssignableFrom(subclass);
    int mods = subclass.getModifiers();
    boolean isAbstract = Modifier.isAbstract(mods);
    boolean isPublic = Modifier.isPublic(mods);
    return isSubclass && !isAbstract && isPublic;
  }
}
