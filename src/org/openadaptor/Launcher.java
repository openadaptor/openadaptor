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

package org.openadaptor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Launch class for Openadaptor Adaptor instances.
 * <br>
 * <B>CURRENTLY THIS IS A PROROTYPE AND NOT FOR PRODUCTION USE</B>
 * <br>
 * The Launcher will work as follows:
 * <br>
 * It will establish a classpath based on the value of System property 
 * oa.home if specified, or by assuming it to be the parent directory
 * of the directory in which the jar from which Launcher was loaded resides.
 * <br>
 * This will only work if Launch was packaged as expected in openadaptor.jar in
 * the lib directory of the distribution.
 * 
 * Several overrides are possible, either as System properties, or as options
 * supplied to the main class of Launcher.
 * <br>
 * Paths are added to the classpath in the following order:
 * <UL>
 *  <LI>ext - External libs (user and 3rd party libs)</LI>
 *  <LI>lib - openadaptor & support libraries</LI>
 * </UL>
 * <br>
 * Notes:<br>
 * This class should only reference standard java classes, or classes which
 * reside in the same packaged jar as itself (i.e. openadaptor.jar). This
 * is necessary to allow the classpath bootstrapping to work correctly. 
 *
 * @author higginse
 *
 */
public class Launcher implements Runnable {
  protected static final String PROP_PREFIX="oa.";
  public static final String PROP_OA_GEN_CP="genclasspath";
  public static final String PROP_OA_HOME="home";
  public static final String PROP_OA_LIB="lib";
  public static final String PROP_OA_EXT="ext";

  public static final String OA_JAR="openadaptor.jar";
  public static final String OA_DEPENDS_JAR="openadaptor-depends.jar";
  public static final String OA_SPRING_JAR="openaadaptor-spring.jar";
  //Jars to blacklist
  public static final String OA_BOOTSTRAP_JAR="bootstrap.jar";
  public static final String OA_STUB_JAR="openadaptor-stub.jar";
  public static final String OA_SRC_JAR="openadaptor-src.jar";

  private static final String[] OA_PRIORITISED_JARS={OA_SPRING_JAR,OA_DEPENDS_JAR,OA_JAR,""};
  //Blacklist of libraries to ignore in lib (some can be removed when they are no longer generated)
  private static final String[] OA_BLACKLISTED_JARS={OA_SRC_JAR,OA_BOOTSTRAP_JAR,OA_STUB_JAR};

  /**
   * Preferred launch class
   */
  private static final String SPRING_LAUNCHER_CLASSNAME="org.openadaptor.spring.SpringAdaptor";
  private static final Class[] STRING_ARRAY_CLASS=new Class[] { String[].class};

  private String launchClassname=SPRING_LAUNCHER_CLASSNAME;

  private String[] launchArgs;

  private String oaHome=null;
  private ClassLoader derivedClassLoader=null;

  private boolean generateClasspath=false;

  protected Launcher(String[] args) {
    launchArgs=extractSystemProperties(args);
    generateClasspath=Boolean.getBoolean(PROP_PREFIX+PROP_OA_GEN_CP);
    //Fudge argument into a format for SpringAdaptor.
    String[] tmpArgs=new String[launchArgs.length*2];
    for (int i=0;i<launchArgs.length;i++){
      tmpArgs[i*2]="-config";
      tmpArgs[i*2+1]=launchArgs[i];
    }
    launchArgs=tmpArgs;
  }

  private File[] deriveClasspathEntries(Properties props) {
    List entries=new ArrayList();
    oaHome=props.getProperty(PROP_PREFIX+PROP_OA_HOME);
    if (oaHome==null) {
      oaHome=deriveOAHomePath();
    }
    else {
      File f=new File(oaHome);
      if (f.exists() && f.isDirectory()){
        try {
          oaHome=f.getCanonicalPath();
        }
        catch(IOException ioe) {
          throw new RuntimeException("Failed to resolve "+oaHome+"("+PROP_PREFIX+PROP_OA_HOME+")",ioe);
        }
      }
      else {
        throw new RuntimeException(oaHome+" ("+PROP_PREFIX+PROP_OA_HOME+") is not a valid directory");
      }
    }
    System.out.println("Using OA home of "+oaHome);

    String oaExtPath=getPath(props,PROP_PREFIX+PROP_OA_EXT,oaHome,PROP_OA_EXT);
    System.out.println("OA Ext Location is: "+oaExtPath);
    entries.addAll(getJars(oaExtPath));

    String oaLibPath=getPath(props,PROP_PREFIX+PROP_OA_LIB,oaHome,PROP_OA_LIB);
    System.out.println("OA Lib Location is: "+oaLibPath);
    entries.addAll(getJars(oaLibPath,OA_PRIORITISED_JARS,OA_BLACKLISTED_JARS));

    return (File[])entries.toArray(new File[entries.size()]);
  }

  private ClassLoader buildClassLoader(File[] classpathEntries) {
    URL[] urls=new URL[classpathEntries.length];
    for (int i=0;i<urls.length;i++) {
      try {
        urls[i]=classpathEntries[i].toURL();
        System.out.println(urls[i]);
      } 
      catch (MalformedURLException e) {
        System.err.println("Ignoring "+classpathEntries[i]+" - Failed to generate URL");
      }
    }
    //Make sure the parent is specified as null here.
    URLClassLoader loader=new URLClassLoader(urls,null); //The null is necessary here!
    return loader;
  }

  private String buildClasspath(File[] classpathEntries) {
    StringBuffer cp=new StringBuffer();
    for (int i=0;i<classpathEntries.length;i++) {
      String entry=classpathEntries[i].getAbsolutePath();
      cp.append(entry).append(File.pathSeparatorChar);
    }
    if (cp.length()>0) {
      cp.deleteCharAt(cp.length()-1);
    }
    return cp.toString();
  }


  private static List getJars(String path,String[] prioritisedEntries,String[] blacklistedEntries) {
    List libEntries=getJars(path);
    if (blacklistedEntries!=null) {
      //Remove any blacklisted entries from the classpath of jars.
      String[] blacklistedJars=prependBasePath(path, blacklistedEntries);
      List blacklistedFiles=new ArrayList();
      
      for (int i=0;i<blacklistedJars.length;i++) {
        blacklistedFiles.add(new File(blacklistedJars[i]));
      }
      //System.err.println("Entries before blacklisting: "+libEntries.size());
      libEntries.removeAll(blacklistedFiles);
      //System.err.println("Entries after blacklisting:  "+libEntries.size());
    }
    if ((prioritisedEntries!=null) && (prioritisedEntries.length>0)){
      String[] prioritisedJars=prependBasePath(path,prioritisedEntries);
      prioritise(libEntries, prioritisedJars);
    }
    return libEntries;
  }
  /**
   * Recursively Return a list of the jars (and .zips) for a given path.
   * @param path
   * @return
   */
  private static List getJars(String path) {
    List result=new ArrayList();
    File dir=new File(path);
    if (dir.isDirectory()) {
      System.out.println("Adding "+dir);
      result.add(dir);
      String[] files=dir.list();
      for (int i=0;i<files.length;i++) {
        File file=new File(dir,files[i]);
        if (file.isDirectory()) { //Get more by recursion.
          result.addAll(getJars(file.getAbsolutePath()));
        }
        else {
          if (usableJar(file)) {
            System.out.println("Adding "+file);
            result.add(file);
          }
          else {
            System.out.println("Ignoring "+file);
          }
        }
      }
    }
    else {
      System.out.println("Ignoring "+dir+" (not a directory)");
    }
    return result;
  }

  private static boolean usableJar(File file) {
    String fileName=file.getName().toLowerCase();
    boolean result=fileName.endsWith(".jar") || fileName.endsWith(".zip");
    return result;
  }

  private static final String getPath(Properties props,String propName,String oaHome,String defaultPath) {
    String value=defaultPath;
    if (props.containsKey(propName)) { //Need to read the value instead
      value=props.getProperty(propName);
    }
    if (value!=null) {
      if(!value.startsWith(File.separator)) { //Relative
        value=oaHome+File.separator+value;
      }     
    }
//    if (!value.endsWith(File.separator)){ //Make it end with separator - url loading likes this.
//      value=value+File.separator;
//    }
    return value;
  }

  public void run() {
    Class launchClass=null;
    try {
      if(generateClasspath) {
        System.out.println("Generating classpath...");
        File[] classpathEntries=deriveClasspathEntries(System.getProperties());
        System.out.println("Generated classpath with "+classpathEntries.length+" entries");
        derivedClassLoader=buildClassLoader(classpathEntries);
        String classpath=buildClasspath(classpathEntries);
        System.out.println("Generated classpath: "+classpath);
        System.setProperty("java.class.path",classpath);
        
        if (derivedClassLoader!=null) {
          System.out.println("Configuring classloader for current Thread");
          Thread.currentThread().setContextClassLoader(derivedClassLoader);
        }
        launchClass=derivedClassLoader.loadClass(launchClassname);
        
      }
      else {
        System.out.println("Using default classpath for launch class");
        launchClass = Class.forName(launchClassname);
      }
      Method mainMethod = launchClass.getMethod("main", STRING_ARRAY_CLASS);
      mainMethod.invoke(launchClass, new Object[] { launchArgs });
    } 
    catch (ClassNotFoundException cnfe) {
      fail("Failed to find launch class: "+launchClassname,cnfe);
    } 
    catch (SecurityException se) {
      fail("Failed to get main method ",se);
    } 
    catch (NoSuchMethodException nsme) {
      fail("Failed to get main method ",nsme);
    } 
    catch (IllegalArgumentException iae) {
      fail("Failed to invoke main method",iae);
    } 
    catch (IllegalAccessException iae) {
      fail("Failed to invoke main method",iae);
    } 
    catch (InvocationTargetException ite) {
      fail("Failed to invoke main method",ite);;
    }
  }

  private void fail(String msg,Throwable throwable) {
    throwable.printStackTrace();
    throw new RuntimeException(msg,throwable);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.err.println("THIS CLASS IS NOT FOR PRODUCTION USE - IT IS STILL A PROTOTYPE");
    new Launcher(args).run();
  }

  private static String[] extractSystemProperties(String[] argsAndOptions) {
    List configs=new ArrayList();
    for (int i=0;i<argsAndOptions.length;i++){   
      String arg=argsAndOptions[i];
      if (arg.startsWith("-")){
        arg=arg.substring(1);
        if (++i<argsAndOptions.length) {
          System.setProperty(arg,argsAndOptions[i]);
        }
      }
      else {
        configs.add(arg);
      }
    }
    return (String[])configs.toArray(new String[configs.size()]);
  }

  /**
   * Reorder a list of values to have prioritised entries first, if they
   * exist.
   * <UL>
   * <LI> Later entries in the priorityarray will have higher priorities than 
   * those which are earlier.
   * <LI>Values which exist in the priority array have higher priority than 
   * entries which do not.
   * </UL>
   * <br>
   * E.g,with input - b,x,f,d,z,e,r
   * 
   * prioritise(input,{x,y,z}) yields z,x,b,f,d,e,r
   * prioritise(input,{a,b,c}) yields b,x,f,d,z,e,r
   * @param values List which will be prioritised
   * @param priorities List containing priorities, from low to high
   */
  private static void prioritise(List values,final String[] priorities) {
    final List prioList=Arrays.asList(priorities);
    final Comparator libComparator=new Comparator() {
      public int compare(Object a, Object b) {
        int result=0; //Don't do anything
        if ((a !=null) && (b!=null)) {
          result=prioList.indexOf(b.toString())-prioList.indexOf(a.toString());
        }
        return result;
      }   
    };
    Collections.sort(values,libComparator);
  }

  /**
   * Prepend a base path to an array of Strings.
   * @param path
   * @param entries
   * @return
   */
  private static String[] prependBasePath(String path,String[] entries){
    String[] result=new String[entries.length];
    for (int i=0;i<result.length;i++) {
      String entry=entries[i];
      String sep=((entry==null) || (entry.length()==0))?"":File.separator;
      result[i]=path+sep+entries[i];
    }
    return result;
  }

  /**
   * Derive the base install path of oa.
   * <br>
   * It works by guessing that the root directory of the install
   * is the parent of the directory containing the jar from which this class
   * is loaded.
   * <br>
   * Note that it will not work if this class was not located within a jar in the
   * correct structure.
   * @return String containing the path to the openadaptor install dir.
   */
  public static String deriveOAHomePath() {
    String libDir=getClassJarDirectory(Launcher.class);
    File libFile=new File(libDir);
    String oaHomePath=libFile.getParent();
    return oaHomePath;
  }

  private static URL getClassLocation(Class classToLocate) {
    return classToLocate.getProtectionDomain().getCodeSource().getLocation();
  }

  private static File getClassFile(Class classToLocate) {
    return new File(getClassLocation(classToLocate).getPath());
  }

  private static String getClassJarDirectory(Class classToLocate) {
    String result=null;

    File file=getClassFile(classToLocate);
    result=file.getParent();
    return result;
  }
}
