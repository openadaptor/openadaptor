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

package org.openadaptor.auxil.processor.script;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;

public class ScriptProcessor extends Component implements IDataProcessor {

  private ScriptEngine scriptEngine;
  private String language;
  private String script;
  private String scriptFilename;
  private CompiledScript compiledScript;
  private boolean compile = true;
  private Object lastResult = null;
  private String dataBinding = "data";
  
  public ScriptProcessor() {
    super();
  }

  public ScriptProcessor(String id) {
    super(id);
  }

  public void setDataBinding(String dataBinding) {
    this.dataBinding = dataBinding;
  }

  protected Object getLastResult() {
    return lastResult;
  }

  public void setCompile(boolean compile) {
    this.compile = compile;
  }

  public void setCompiledScript(CompiledScript compiledScript) {
    this.compiledScript = compiledScript;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public void setScriptEngine(ScriptEngine scriptEngine) {
    this.scriptEngine = scriptEngine;
  }

  public void setScriptFilename(String scriptFilename) {
    this.scriptFilename = scriptFilename;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public synchronized Object[] process(Object data) {
    try {
      scriptEngine.put(dataBinding, data);
      if (compiledScript != null) {
        lastResult = compiledScript.eval();
      } else {
        if (script != null) {
          lastResult = scriptEngine.eval(script);
        } else {
          lastResult = scriptEngine.eval(new FileReader(scriptFilename));
        }
      }
      data = scriptEngine.get(dataBinding);
      return data != null ? new Object[] {data} : new Object[0];
    } catch (ScriptException e) {
      throw new ProcessingException("failed to compile script, " + e.getMessage()
          + " line " + e.getLineNumber() + " col " + e.getColumnNumber(), e, this);
    } catch (FileNotFoundException e) {
      throw new ConnectionException("failed load script file, " + e.getMessage()
          + scriptFilename, e, this);
    }
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
    if (language == null) {
      exceptions.add(new ValidationException("scriptName property not set", this));
    }
    if (script == null && scriptFilename == null) {
      exceptions.add(new ValidationException("script or scriptFilename property must be set", this));
    }
    if (!exceptions.isEmpty()) {
      return;
    }
    ScriptEngineManager manager = new ScriptEngineManager();
    scriptEngine = manager.getEngineByName(language);
    if (compile && scriptEngine instanceof Compilable) {
      try {
        if (script != null) {
          compiledScript = ((Compilable)scriptEngine).compile(script);
        } else {
          compiledScript = ((Compilable)scriptEngine).compile(new FileReader(scriptFilename));
        }
      } catch (ScriptException e) {
        exceptions.add(new ValidationException("failed to compile script, " + e.getMessage()
            + " line " + e.getLineNumber() + " col " + e.getColumnNumber(), e, this));
      } catch (FileNotFoundException e) {
        exceptions.add(new ValidationException("failed load script file, " + e.getMessage()
            + scriptFilename, e, this));
      }
    }
  }

}
