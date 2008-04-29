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

package org.openadaptor.util.ant;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * This validates Spring configurations.
 * @author higginse
 *
 */
public class SpringConfigValidateTask extends Task {
  //If ignoreStubExceptions is set, then this system property will be set to true.
  public static final String IGNORE_STUB_EXCEPTION_FLAG="openadaptor.exception.stub.ignore";
  
  // Edit this to add new configs to the exclusion list
  // Should really be done differently, list in the build file, property file, tag in the config something else sensible.
  private static String[] EXCLUDED_FILES={"spring-local-txn-demo.xml"};

  private static String SPRING_FACTORY_CLASSNAME = "org.springframework.beans.factory.xml.XmlBeanFactory";
  private static String SPRING_BEAN_DEFINITION_CLASSNAME = "org.springframework.beans.factory.config.BeanDefinition";
  private static String SPRING_RESOURCE_CLASSNAME = "org.springframework.core.io.Resource";
  private static String SPRING_URLRESOURCE_CLASSNAME = "org.springframework.core.io.UrlResource";
  private static final String SPRING_GET_BEAN_METHOD = "getBean";
  private static final String SPRING_GET_BEAN_DEFINITION_METHOD = "getBeanDefinition";
  private static final String SPRING_GET_BEAN_NAMES_METHOD = "getBeanDefinitionNames";
  private List filesets = new ArrayList();
  private Path classpath;
  private boolean ignoreStubExceptions=false;

  public void addFileset(FileSet set) {
    filesets.add(set);
  }

  public void setClasspathRef(Reference r) {
    classpath = new Path(getProject());
    classpath.setRefid(r);
  }

  /**
   * flag to indicate whether or not {@link StubException} can be ignored.
   * <br>
   * All it does in practice is set system property {@link IGNORE_STUB_EXCEPTION_FLAG} to
   * the supplied value.
   * It is the responsibility of the code itself to interpret this flag as it sees fit.
   * @see LegacyUtils for an example.
   * @param ignore
   */
  public void setIgnoreStubExceptions(boolean ignore) {
    this.ignoreStubExceptions=ignore;
  }

  public void execute() throws BuildException {
    int processedFiles=0;
    ArrayList excludedFiles = new ArrayList();
    //
    // create new class loader and use reflection to get constructors and method
    // for instantiating a spring factory and forcing named beans to be loaded
    //

    AntClassLoader loader = getProject().createClassLoader(classpath);
    loader.setParent(getProject().getCoreLoader());
    loader.setParentFirst(false);
    loader.addJavaLibraries();
    loader.setIsolated(true);
    loader.setThreadContextLoader();

    Constructor resourceConstructor;
    Constructor factoryConstructor;
    Method getBeanNamesMethod;
    Method getBeanDefinitionMethod;
    Method getBeanMethod;
    Method getBeanIsAbstractMethod;

    try {
      loader.forceLoadClass(SPRING_FACTORY_CLASSNAME);
      Class factoryClass = Class.forName(SPRING_FACTORY_CLASSNAME, true, loader);
      Class beanDefinitionClass = Class.forName(SPRING_BEAN_DEFINITION_CLASSNAME, true, loader);
      Class resourceClass = Class.forName(SPRING_RESOURCE_CLASSNAME, true, loader);
      Class urlResourceClass = Class.forName(SPRING_URLRESOURCE_CLASSNAME, true, loader);

      resourceConstructor = urlResourceClass.getConstructor(new Class[] {String.class});
      factoryConstructor = factoryClass.getConstructor(new Class[] {resourceClass});

      getBeanNamesMethod = factoryClass.getMethod(SPRING_GET_BEAN_NAMES_METHOD, new Class[] {});
      getBeanDefinitionMethod = factoryClass.getMethod(SPRING_GET_BEAN_DEFINITION_METHOD, new Class[] {String.class});
      getBeanMethod = factoryClass.getMethod(SPRING_GET_BEAN_METHOD, new Class[] {String.class});
           
      getBeanIsAbstractMethod = beanDefinitionClass.getMethod("isAbstract", new Class[] {});

    } catch (Throwable t) {
      t.printStackTrace();
      throw new BuildException(t);
    }

    //Set flag which indicates if stub exceptions may be ignored.
    System.setProperty(IGNORE_STUB_EXCEPTION_FLAG, Boolean.toString(ignoreStubExceptions));

    //
    // iterate thru spring config files, forcing named beans to be loaded
    //
    ArrayList failedFiles = new ArrayList();
    for (Iterator iter = filesets.iterator(); iter.hasNext();) {
      FileSet fileSet = (FileSet) iter.next();
      DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
      String[] files = ds.getIncludedFiles();
      for (int i = 0; i < files.length; i++) {       
        if (isExcluded(files[i])) {
          File dir = fileSet.getDir(getProject());
          excludedFiles.add("file:" + dir.getAbsolutePath() + "/" + files[i]);
         } 
        else {
          // HACK - reset registration url property
          System.setProperty("openadaptor.registration.url", "");
          File dir = fileSet.getDir(getProject());       
          String configUrl = "file:" + dir.getAbsolutePath() + "/" + files[i];
          processedFiles++;
          try {
            Object resource = resourceConstructor.newInstance(new Object[] {configUrl});
            Object factory = factoryConstructor.newInstance(new Object[] {resource});
  
            String[] beanNames = (String[]) getBeanNamesMethod.invoke(factory, new Object[] {});
            for (int j = 0; j < beanNames.length; j++) {
              Object beanDefinition = getBeanDefinitionMethod.invoke(factory, new Object[] {beanNames[j]});
              Boolean isAbstract = (Boolean)getBeanIsAbstractMethod.invoke(beanDefinition, new Object[] {});
              // We instantiate only if the bean name is not abstract.
              if (!isAbstract.booleanValue()) {
                getBeanMethod.invoke(factory, new Object[] {beanNames[j]});
              }
            }       
          } catch (Throwable e) {
            e.printStackTrace();
            failedFiles.add(configUrl);
          }
        }
      }
    }

    //
    // if any failed then report this and throw an exception
    //

    loader.resetThreadContextLoader();
    loader.cleanup();

    if (!failedFiles.isEmpty()) {
      for (Iterator iter = failedFiles.iterator(); iter.hasNext();) {
        String filename = (String) iter.next();
        System.err.println("Invalid Spring file: "+filename);
      }
      String msg="Detected "+ failedFiles.size()+" invalid Spring configurations(s) from a total of "+processedFiles+".";
      System.err.println(msg);
      throw new BuildException(msg);
    }
    else {
      System.out.println("Processed "+processedFiles+" Spring configurations");
    }
    if(!excludedFiles.isEmpty()){
      System.out.println("Excluded "+excludedFiles.size()+" Spring configuration(s) from check.");
      for (Iterator iter = excludedFiles.iterator(); iter.hasNext();) {
        String filename = (String) iter.next();
        System.out.println("Excluded: "+filename);
      }     
    }
  }
  
  private boolean isExcluded(String fileName) {
    //System.out.println("Testing ["+fileName+"] for exclusion.");
    boolean excluded = false;
    for (int i = 0; i < EXCLUDED_FILES.length; i++) {
      if (fileName.indexOf(EXCLUDED_FILES[i]) >= 0) {
        excluded = true;
        System.out.println("File ["+fileName+"] Excluded from Checks.");
      }      
    }
    return excluded; 
  }


}
