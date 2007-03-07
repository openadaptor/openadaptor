package org.openadaptor.util;

//
// DO NOT IMPORT ANYTHING OTHER THAN java PACKAGES FOR THIS CLASS
// IT MUST BE INDEPEDENT!!!!
//

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
 * {@value #SYSTEM_PROPERTY_OPENADAPTOR_LIB} then the contents of this dir (and
 * subdirs) will be prepended to the classpath too.
 * 
 * @author perryj
 * 
 */
public abstract class ClasspathBootstrapper {

  public static final String SYSTEM_PROPERTY_OPENADAPTOR_LIB = "openadaptor.lib";

  public static void main(String classname, String[] args) throws ClassNotFoundException, SecurityException,
      NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
      MalformedURLException, InstantiationException {

    boolean verbose = Boolean.getBoolean("verbose");
    
    // create array of urls classpath
    String libPath = getJarDirectory();
    ArrayList files = new ArrayList();
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
      System.err.println(ClasspathBootstrapper.class.getName() + " adding : " 
          + (verbose ? file.getAbsolutePath() : file.getName()));
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
  }

  private static String getJarDirectory() {
    String s = ClasspathBootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    File f = new File(s);
    if (f.isDirectory()) {
      throw new RuntimeException("This class can only be used from a jar");
    } else {
      return f.getParent();
    }
  }

  private static String getJar() {
    String s = ClasspathBootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    File f = new File(s);
    if (f.isDirectory()) {
      throw new RuntimeException("This class can only be used from a jar");
    } else {
      return f.getName();
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
      if (file.getName().equals(getJar())) {
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
