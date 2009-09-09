/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.processor.script;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.util.ObjectCloner;
/**
 * Processor which executes scripts in the context of a data record.
 * <br>
 * This may be configured with a script language (default is javascript).
 * The script to be executed may be supplied as a String, or within a 
 * file whose name is supplied. 
 * The script  be executed in the context of supplied records.
 * <br>
 * 
 * @author higginse
 * 
 */
public class ScriptProcessor extends Component implements IDataProcessor, IMetadataAware {
  private static final Log log =LogFactory.getLog(ScriptProcessor.class);
  public static final String DEFAULT_LANGUAGE="js"; //Javascript is the default language.
  public static final String DEFAULT_DATA_BINDING="oa_data"; //Bound name for data records
  public static final String DEFAULT_METADATA_BINDING="oa_metadata"; //Bound name for data records
  public static final String DEFAULT_LOG_BINDING="oa_log"; //Bound name for logging

  protected ScriptEngine scriptEngine;
  protected String language=DEFAULT_LANGUAGE;
  protected String script;
  protected String scriptFilename;
  protected CompiledScript compiledScript;
  protected boolean compile = true;
  protected Object lastResult = null;
  protected String dataBinding = DEFAULT_DATA_BINDING;
  protected String metadataBinding = DEFAULT_METADATA_BINDING;
  protected String logBinding= DEFAULT_LOG_BINDING;
  //This allows additional bindings
  protected Map additionalBindings=null;
  
  protected Map metadata = null;

  //Mechanism by which Objects are cloned.
  //This is due to change when a more general-purpose 
  //cloning strategy has been devised
  //ToDo: Remove this (promote to Router or similar)
  private ObjectCloner cloner=new ObjectCloner();
  
  private boolean synchronised=false;

  /**
   * If true, then converted values will always be wrapped
   * in an enclosing Object[], even if the result is already
   * an array.
   * The default value for this have been changed (after 3.3)
   * to false.
   * Note: Individual subclasses may well override this default
   * for their own purposes.
   * 
   */
  protected boolean boxReturnedArrays = false;

  //Flag to log just once, the JVM Version -
  // Either 1.5 and earlier or  1.6 and later
  //Scripting implementation changed between versions.
  //See Issue [SC53]
  protected boolean jvmVersionLogged=false;

  //Name of factory method for getting array (1.5) or List (1.6+) of ScriptEngineFactories
  private static final String GET_ENGINE_FACTORIES="getEngineFactories";
  //Name of factory method for getting array (1.5) or List (1.6+) of SriptEngine aliases
  private static final String GET_NAMES_METHOD="getNames";

  public ScriptProcessor() {
    super();
  }

  public ScriptProcessor(String id) {
    super(id);
  }
  /**
   * This associates the incoming data record with a bound name within
   * the script. 
   * <BR>
   * Defaults to {@link #DEFAULT_DATA_BINDING}
   * @param dataBinding
   */

  public void setDataBinding(String dataBinding) {
    this.dataBinding = dataBinding;
  }

  /**
   * This sets the name to which the script logging will
   * be bound for use within scripts.
   * <br>
   * Defaults to {@link #DEFAULT_LOG_BINDING}
   * @param logBinding
   */
  public void setLogBinding(String logBinding) {
    this.logBinding = logBinding;
  }

  /**
   * Flag to wrap returned Arrays in an enclosing Object[].
   * If true, converted result will be wrapped in an Object[]
   * even if it is already an array.
   * Note: The default behaviour has been changed from true to
   * false after release 3.3
   * @param boxReturnedArrays
   */
  public void setBoxReturnedArrays(boolean boxReturnedArrays) {
    this.boxReturnedArrays=boxReturnedArrays;
  }

  /**
   * This allows additional bindings, specified in the supplied map.
   * <br>
   * For example, it may include a reference to some other bean within
   * the adaptor.
   * <br>
   * One trivial usage might be to maintain simple state information
   * in some other object.
   * @param bindingMap Map of name to Object mappings.
   */
  public void setAdditionalBindings(Map bindingMap) {
    this.additionalBindings=bindingMap;
  }


  /**
   * This holds the last result from script execution.
   * 
   * @return Object containing the result from last execution of the script,
   *         or <code>null</code> if script has not yet executed.
   */
  protected Object getLastResult() {
    return lastResult;
  }

  /**
   * Flag to indicate whether script should be compiled, if possible.
   * <br>
   * Defaults to <code>true</code>
   * 
   * @param compile
   */
  public void setCompile(boolean compile) {
    this.compile = compile;
  }

  /**
   * Assign the script to be executed.
   * @param script
   */
  public void setScript(String script) {
    this.script = script;
  }

  /**
   * Set the name of a file which contains the script to run.
   * <br>
   * If <code>script</code> property is set, this will be
   * ignored
   * @param scriptFilename
   */
  public void setScriptFilename(String scriptFilename) {
    this.scriptFilename = scriptFilename;
  }

  /**
   * Sets the scripting language to be used.
   * <br>
   * Defaults to {@link #DEFAULT_LANGUAGE}
   * @param language
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Get the ScriptEngine as generated by the
   * ScriptEngineManager when validate() is called.
   * @return The ScriptEngine for the ScriptEngineManager
   *         (or <code>null</code> if validate has not yet been called)
   */
  public ScriptEngine getScriptEngine() {
    return scriptEngine;
  }
  
  /**
   * Used to force synchronisation on calls to the {@link #process} method.
   * @param synchronised
   */
  public void setSynchronised(boolean synchronised) {
  	this.synchronised=synchronised;
  }
  
  public boolean getSynchronised() {return synchronised;}
  
  public Object[] process(Object data) {
  	if (synchronised) {
  		synchronized(this) {//Synchronisation required
  			return doProcess(data); 		
  		}
  	}
  	else { //Nike - just do it ;-)
  		return doProcess(data);
  	}
  }
  /**
   * Process a data item.
   * It will bind the data using the configured databinding, to
   * make it available to the script.
   * 
   * The bound object will be returned in a single Element Object[]
   * <br>
   * Note: If compilation is not possible, and the script is contained in a 
   * file, then the file will be read each time a datum is being processed. This
   * should be avoided for obvious reasons :-)
   */
  protected Object[] doProcess(Object data) {
    if (data==null) { //conform to IDataProcessor contract.
      throw new NullRecordException("Null record not permitted");
    }
    //Clone it if possible.
    //data=ReflectionUtils.clone(data);
    data=cloner.clone(data);
    try {
      scriptEngine.put(metadataBinding, metadata);
      scriptEngine.put(dataBinding, data);
      if (compiledScript != null) {
        lastResult = compiledScript.eval();
      } 
      else {
        if (script != null) {
          lastResult = scriptEngine.eval(script);
        } 
        else {
          lastResult = scriptEngine.eval(new FileReader(scriptFilename));
        }
      }
      data = scriptEngine.get(dataBinding);
      if (data==null) {
        data=new Object[] {};
      }
      else {
        if (boxReturnedArrays || (!(data instanceof Object[]))){
          data = new Object[] { data }; //Wrap it in an Object array.
        }
      }
      return (Object[])data;
    } catch (ScriptException e) {
      throw new ProcessingException("failed to execute script, " + e.getMessage()
          + " line " + e.getLineNumber() + " col " + e.getColumnNumber(), e, this);
    } catch (FileNotFoundException e) {
      throw new ConnectionException("failed to load script file, " + e.getMessage()
          + scriptFilename, e, this);
    }
  }

  /**
   * Reset has no effect here unless overridden
   */
  public void reset(Object context) {
  }

  /**
   * Validate the configuration of this component.
   * <br>
   * In addition it will create a script engine, and
   * compile the supplied script if possible, via
   * a call to initialise()
   */
  public void validate(List exceptions) {
    if (exceptions==null) { //IDataProcessor requires a non-null List
      throw new IllegalArgumentException("exceptions List may not be null");
    }
    if (language == null || language.trim().length()==0) {
      exceptions.add(new ValidationException("Property scriptName may not be <null> or empty", this));
    }
    if (script == null) {
      if (scriptFilename==null) {
        exceptions.add(new ValidationException("Exactly one of script or scriptFilename property should be set", this));
      }
    }
    else { //Script has been configured
      if (scriptFilename!=null) {
        log.warn("Both script and scriptFilename have been configured. scriptFilename will be ignored!");
      }
    }
    if (exceptions.isEmpty()) {
      try {
        initialise();
      }
      catch (ValidationException ve){
        exceptions.add(ve);
      }
    }
  }

  /**
   * Utility method to log jvm version info one time only.
   * @param info String to log
   */
  private void logJVMVersion(String info) {
    if (!jvmVersionLogged) {
      log.info(info);
      jvmVersionLogged=true;
    }
  }

  /**
   * Utility method to handle different return types for Java versions.
   * <br>
   * Part of a fix for [SC53] where VMs after 1.6 behave differently
   * to 1.5 and earlier.
   * On 1.5 and earlier ScriptEngineManager.getFactories() returns
   * ScriptEngineFactory[], where as 1.6 returns a List.
   * <br>
   * This method normalises the resut to a List.
   * @param factoryData Object containing either a ScriptEngineFactory[] or List
   * @return List of ScriptEngineFactories
   */
  private  List getFactoryList(Object factoryData) {
    List factories=null;
    if (factoryData instanceof ScriptEngineFactory[] ) {
      logJVMVersion("Java 1.5 or earlier version of scripting");
      factories=Arrays.asList((ScriptEngineFactory[])factoryData);
    }
    else { //Assume it's a List
      logJVMVersion("Java 1.6 or later version of scripting");
      factories=(List)factoryData;
    }
    return factories;
  }

  /**
   * Utility method to handle different return types for Java versions.
   * <br>
   * Part of a fix for [SC53] where VMs after 1.6 behave differently
   * to 1.5 and earlier.
   * On 1.5 and earlier ScriptEngineManager.getNames() returns
   * a String[], where as 1.6 returns a List.
   * <br>
   * This method normalises the resut to a List.
   * @param factoryData Either a String[] or List
   * @return List of Strings containing names
   */

  private List getEngineNames(Object nameData) {
    List names=null;
    if (nameData instanceof String[] ) {
      logJVMVersion("Java 1.5 or earlier version of scripting");
      names=Arrays.asList((String[])nameData);
    }
    else { //Assume it's a list
      logJVMVersion("Java 1.6 or later version of scripting");
      names=(List)nameData;
    }
    return names;
  }
  
  /**
   * Get List of Engine Factories for a ScriptEngineManager by reflection.
   * <br>
   * Has to be done at runtime as JRE 1.5 and JRE 1.6 differ.
   * 1.5 will return a ScriptEngineFactory[], whilst 1.6+
   * will return a List.
   * @param mgr manager to get the list from  
   * @return List containing ScriptEngineFactories.
   */
  private List getEngineFactories(ScriptEngineManager mgr) {
    List factories=null;
    Object result=exec(mgr,GET_ENGINE_FACTORIES);
    if (result!=null) {
      factories=getFactoryList(result);
    }
    return factories;
  }
  
  /**
   * Get List of Engine Alise for a ScriptEngineFactory by reflection.
   * <br>
   * Has to be done at runtime as JRE 1.5 and JRE 1.6 differ.
   * 1.5 will return a String[], whilst 1.6+
   * will return a List.
   * @param factory to get the list from  
   * @return List of Strings containing names..
   */
 
  private List getFactoryNames(ScriptEngineFactory factory) {
    List names=null;
    Object result=exec(factory,GET_NAMES_METHOD);
    if (result!=null) {
      names=getEngineNames(result);
    }
    return names;
  }

  /**
   * Locates a ScriptEngine for the current language.
   * <br>
   * Complicated slightly by differences between implementations 
   * in java 1.5 and earlier versus 1.6 and later.
   * See Issue [SC52]
   * <br>
   * 
   * @return ScriptEngine instance for the current language
   */
  protected ScriptEngine createScriptEngine() {
    ScriptEngine engine=null;
    ScriptEngineManager mgr=new ScriptEngineManager();
    List factories=getEngineFactories(mgr);
    Iterator it=factories.iterator();
    while (it.hasNext() && (engine==null)) { //More factories to try, and no match yet.
      ScriptEngineFactory factory=(ScriptEngineFactory)it.next();
      try {
        List aliases=getFactoryNames(factory); 
        if (aliases!=null) { //If null couldn't get any for factory.
          Iterator aliasIterator=aliases.iterator();
          while (aliasIterator.hasNext()) {
            if (language.equals(aliasIterator.next())) {
              log.debug("Found matching script engine for "+language);
              engine=factory.getScriptEngine();
              break;
            }
          }
        }
        else {
          log.debug("Failed to get names for factory "+factory.getEngineName());
        }
      }
      catch (AbstractMethodError ame) {
        log.debug("Failed to interrogate Factory - "+factory.getEngineName());
      }
    }
    if (engine==null) {
      log.error("Failed to find engine for language "+language);
      throw new RuntimeException("Failed to find engine for language "+language);
    }
    return engine;
  }

  /**
   * Initialise the script engine.
   * <br>
   * This will create a script engine, and compile the supplied
   * script is compilation is possible, and enabled.
   * @throws ValidationException
   */
  private void initialise() throws ValidationException {
    log.info("Initialising script engine for language: "+language);
    log.debug("Compile flag: "+compile);
    scriptEngine = createScriptEngine();
    if (compile && scriptEngine instanceof Compilable) {
      Compilable compilableScriptEngine=(Compilable)scriptEngine;
      try {
        if (script != null) {
          log.debug("Compiling script: "+script);
          compiledScript = compilableScriptEngine.compile(script);
        } else {
          log.debug("Compiling script from file: "+scriptFilename);
          compiledScript = compilableScriptEngine.compile(new FileReader(scriptFilename));
        }
        log.info("Script compiled successfully");
      } catch (ScriptException e) {
        String failMsg="Failed to compile script, " + e.getMessage() + " line " + e.getLineNumber() + " col " + e.getColumnNumber();
        log.warn(failMsg);
        throw new ValidationException(failMsg, e, this);
      } catch (FileNotFoundException e) {
        String failMsg="Failed to compile script, " + e.getMessage();
        log.warn(failMsg);
        throw new ValidationException(failMsg, e, this);
      }
    }
    //Apply binding to allow scripts to access logging
    scriptEngine.put(logBinding, log);
    //Apply extra bindings, if any.
    applyBindings(scriptEngine,additionalBindings);
  }

  /**
   * This will attempt to bind named objects into the ScriptEngine.
   * <br>
   * The key of each Map.Entry will be used as the bound name within
   * the engine.
   * The value object will be bound to that name withing the engine.
   * @param engine ScriptEngine to apply bindings in.
   * @param bindings Map of name to Object pairings
   */
  private void applyBindings(ScriptEngine engine, Map bindings) {
    if (bindings!=null) {
      log.info("Applying additionalBindings");
      Iterator it=bindings.keySet().iterator();
      while (it.hasNext()) { 
        Object key=it.next();
        Object value=bindings.get(key);
        engine.put(key.toString(), value);
        if (log.isDebugEnabled()) {
          log.debug("Binding "+key.toString()+" -> "+ value);
        }
      }
    }
  }
  
  /**
   * Utility method to invoke a named no-arg method on an Object instance.
   * <br>
   * Used when finding script implementation methods at runtime - required
   * as scripting behaviour changed between JRE 1.5 (add on) and JRE 1.6
   * (built-in).
   * @param instance any object instance
   * @param methodName name of the no-arg method
   * @return Object containing the result of the invocation.
   */
  private static Object exec(Object instance,String methodName) {
    final Class[] EMPTY_CLASS_ARRAY=new Class[] {};
    final Object[] EMPTY_ARGS=new Object[] {};
    Object result=null;
    Class instanceClass=instance.getClass();
    log.debug("Finding method "+methodName+" for "+instanceClass.getName());
    try {
      Method method=instanceClass.getMethod(methodName, EMPTY_CLASS_ARRAY);
      result=method.invoke(instance, EMPTY_ARGS);         
    } 
    catch (Exception e) {
      log.warn("Failed to execute method "+methodName+" on "+instanceClass.getName()+": "+e);
    } 
    return result;
  }

  /**
   * @see IMetadataAware#setMetadata(Map)
   */
  public void setMetadata(Map metadata) {
    this.metadata = metadata; 
  }
  
}
