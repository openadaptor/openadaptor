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

package org.openadaptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Launch class for Openadaptor Adaptor instances.
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
  private final MiniLog log=new MiniLog();
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


  /**
   * Launch an adaptor, possibly generating CP as we go
   * @param args arts to launch with.
   */
  protected Launcher(String[] args) {
    launchArgs=extractSystemProperties(args);

    if (Boolean.getBoolean("debug")) {
      log.setLevel(MiniLog.DEBUG);
    }

    generateClasspath=Boolean.getBoolean(PROP_PREFIX+PROP_OA_GEN_CP);
    if (launchArgs.length==0) {
      launchArgs=promptForLaunchArgs();
    }
    if (launchArgs.length>0) {
      //Fudge argument into a format for SpringAdaptor.
      String[] tmpArgs=new String[launchArgs.length*2];
      for (int i=0;i<launchArgs.length;i++){
      	String arg=launchArgs[i];
      	String springPrefix= "-config";
      	//Fudge to use props files as -props instead of -config
      	String lArg=arg.toLowerCase();
      	if ( lArg.endsWith(".props") || lArg.endsWith(".properties")) {
      		log.debug("Treating"+arg+" as -props argument");
      		springPrefix="-props";
      	}
        tmpArgs[i*2]=springPrefix;
        tmpArgs[i*2+1]=launchArgs[i];
      }
      launchArgs=tmpArgs;
    }
  }

  /**
   * Simple GUI to allow user to choose an adaptor to run.
   * @param chosenFiles
   */
  private void runGUI(List chosenFiles) {
    synchronized(this){
      JFileChooser chooser;
      String userDir=System.getProperty("user.dir");
      if (userDir!=null) {
        chooser=new JFileChooser(userDir);
      }
      else {
        chooser=new JFileChooser();
      }
      chooser.setFileFilter(new AdaptorConfigFilter(new String[]{"xml","properties","props"}));
      chooser.setMultiSelectionEnabled(true);
      try { 
        chooser.showOpenDialog(null);
        File[] selection=chooser.getSelectedFiles();
        if ((selection!=null) && (selection.length>0)) {
          for (int i=0;i<selection.length;i++) {
            chosenFiles.add(selection[i].getPath());
          }
        }
      }
      catch(Throwable t) { //If it can't do GUI, then we just give up on it.
        log.warn("Failed to select config via GUI dialog - "+t.getMessage());
      }
      notifyAll(); //Notify waiting threads that the UI is done.
    }
  }

  /**
   * Get commandline arguments from user via GUI or command line.
   * This will attempt to Launch a GUI to prompt the user for Launch arguments.
   * If this fails, it will attempt to us stdin instead.
   * @return String[] containing arguments as if they were provided on the command line
   */
  private String[] promptForLaunchArgs() {
    final List chooserArgs=new ArrayList();
    String[] args=null;
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() { runGUI(chooserArgs);}
    });
    synchronized(this) {
      try {
        wait(); //Wait for GUI to let us know it's ok to continue.
      }
      catch (InterruptedException ie) {} //Ignore it
    }
    if (!chooserArgs.isEmpty()) {
      args=(String[])chooserArgs.toArray(new String[chooserArgs.size()]);
    }

    if (args==null) { //GUI either failed or didn't get anything.
      BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Please specify configuration arguments for adaptor > ");
      System.out.flush();
      try {
        String argString=br.readLine();
        args=argString.split("\t");
      }
      catch (IOException ioe) {
        log.error("Failed to get configuration arguments: "+ioe.getMessage());
      }
    }
    return args;
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
    log.info("Using OA home of "+oaHome);

    String oaExtPath=getPath(props,PROP_PREFIX+PROP_OA_EXT,oaHome,PROP_OA_EXT);
    log.debug("OA Ext location is: "+oaExtPath);
    entries.addAll(getJars(oaExtPath));

    String oaLibPath=getPath(props,PROP_PREFIX+PROP_OA_LIB,oaHome,PROP_OA_LIB);
    log.debug("OA lib location is: "+oaLibPath);
    entries.addAll(getJars(oaLibPath,OA_PRIORITISED_JARS,OA_BLACKLISTED_JARS));

    return (File[])entries.toArray(new File[entries.size()]);
  }

  private ClassLoader buildClassLoader(File[] classpathEntries) {
    URL[] urls=new URL[classpathEntries.length];
    for (int i=0;i<urls.length;i++) {
      try {
        urls[i]=classpathEntries[i].toURL();
        log.trace(urls[i]);
      } 
      catch (MalformedURLException e) {
        log.warn("Ignoring "+classpathEntries[i]+" - Failed to generate URL");
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


  private  List getJars(String path,String[] prioritisedEntries,String[] blacklistedEntries) {
    List libEntries=getJars(path);
    if (blacklistedEntries!=null) {
      //Remove any blacklisted entries from the classpath of jars.
      String[] blacklistedJars=prependBasePath(path, blacklistedEntries);
      List blacklistedFiles=new ArrayList();

      for (int i=0;i<blacklistedJars.length;i++) {
        blacklistedFiles.add(new File(blacklistedJars[i]));
      }
      libEntries.removeAll(blacklistedFiles);
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
  private List getJars(String path) {
    List result=new ArrayList();
    File dir=new File(path);
    if (dir.isDirectory()) {
      log.debug("Adding "+dir);
      result.add(dir);
      String[] files=dir.list();
      for (int i=0;i<files.length;i++) {
        File file=new File(dir,files[i]);
        if (file.isDirectory()) { //Get more by recursion.
          result.addAll(getJars(file.getAbsolutePath()));
        }
        else {
          if (usableJar(file)) {
            log.debug("Adding "+file);
            result.add(file);
          }
          else {
            log.debug("Ignoring "+file);
          }
        }
      }
    }
    else {
      log.warn("Ignoring "+dir+" (not a directory)");
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
    return value;
  }

  public void run() {
    Class launchClass=null;
    try {
      if(generateClasspath) {
        log.info("Generating classpath for adaptor ("+PROP_PREFIX+PROP_OA_GEN_CP+" is true)");
        File[] classpathEntries=deriveClasspathEntries(System.getProperties());
        log.debug("Generated classpath with "+classpathEntries.length+" entries");
        derivedClassLoader=buildClassLoader(classpathEntries);
        String classpath=buildClasspath(classpathEntries);
        log.debug("Generated classpath: "+classpath);
        System.setProperty("java.class.path",classpath);

        if (derivedClassLoader!=null) {
          log.debug("Configuring classloader for current Thread");
          Thread.currentThread().setContextClassLoader(derivedClassLoader);
        }
        launchClass=derivedClassLoader.loadClass(launchClassname);

      }
      else {
        log.info("Using default classpath for launch class ( set "+PROP_PREFIX+PROP_OA_GEN_CP+" true to auto-generate)");
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
    log.error(msg);
    throwable.printStackTrace();
    throw new RuntimeException(msg,throwable);
  }

  /**
   * openadaptor application entry point
   * @param args command line arguments for adaptor
   */
  public static void main(String[] args) {
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
   * @return List of entries with highest priority items first
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
    //String oaHomePath=libFile.getParent();
    String oaHomePath=getDecodedParentPath(libFile);
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
    //result=file.getParent();
    result=getDecodedParentPath(file);
    return result;
  }
  
  private static String getDecodedParentPath(File file) {
    String parentPath=file.getParent();
    try {
      parentPath= URLDecoder.decode(parentPath, "UTF-8");
    } 
    catch (UnsupportedEncodingException uee) {} //Ignore it - nothing we can do anyway
    return parentPath;
  }

  /**
   * Simple GUI File chooser for selecting configurations and properties files.
   * 
   * @author higginse
   *
   */
  class AdaptorConfigFilter extends FileFilter {
    List validExtensions;  
    /**
     * Create a filter for the supplied list of extensions
     *
     * @param extensions String array of allowable extensions.
     */
    public AdaptorConfigFilter(String[] extensions) {
      super();
      validExtensions=new ArrayList();
      if (extensions!=null){
        for (int i =0;i<extensions.length;i++) {
          validExtensions.add(extensions[i].toLowerCase());
        }
      }
    }

    public boolean accept(File f) {
      boolean accept=f.isDirectory();
      if (!accept) {
        String filename=f.getName();
        int ofs=filename.lastIndexOf('.');
        if ((ofs>0) && (f.length()>ofs)) {
          accept=validExtensions.contains(filename.substring(ofs+1).toLowerCase());
        }
      }
      return accept;
    }

    public String getDescription() {
      return "Adaptor configurations .xml & .properties";
    }   
  }

  class MiniLog {
    public static final int TRACE=0;
    public static final int DEBUG=1;
    public static final int INFO=2;
    public static final int WARN=3;
    public static final int ERROR=4;
    public static final int FATAL=5;

    private int level=INFO;
    private PrintStream ps;

    public MiniLog(PrintStream ps) { this.ps=ps; }
    public MiniLog() { this(System.err); }

    public void setLevel(int level) {
      this.level=level;
    }
    public void setLevel(String s) {
      if ("TRACE".equalsIgnoreCase(s)) {level=TRACE;}
      else if ("DEBUG".equalsIgnoreCase(s)) {level=DEBUG;}
      else if ("INFO".equalsIgnoreCase(s)) {level=INFO;}
      else if ("WARN".equalsIgnoreCase(s)) {level=WARN;}
      else if ("ERROR".equalsIgnoreCase(s)) {level=ERROR;}
      else if ("FATAL".equalsIgnoreCase(s)) {level=FATAL;}
      else {level=INFO;}
    }

    public void trace(Object o) {log(TRACE,o);}
    public void debug(String o) {log(DEBUG,o);}
    public void info(String o) {log(INFO,o);}
    public void warn(String o) {log(WARN,o);}
    public void error(String o) {log(ERROR,o);}
    public void fatal(String o) {log(FATAL,o);}

    private void log(int logLevel,Object o) {
      String s;
      if (o!=null) {
        s=(o instanceof String)?(String)o:o.toString();
      }
      else {
        s="<null>";
      }
      if (logLevel>=level) {
        ps.println(s);
      }
    }
  }
}
