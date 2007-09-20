package org.openadaptor.auxil.processor.script;

import java.util.ArrayList;

import junit.framework.TestCase;

public class ScriptFilterProcessorTestCase extends TestCase {

  public void testFilterOnMatch() {
    ScriptFilterProcessor proc = new ScriptFilterProcessor();
    proc.setScriptProcessor(generateScriptProcessor());
    proc.validate(new ArrayList());
    assertTrue(proc.process("foo").length == 0);
    assertTrue(proc.process("Foo").length == 0);
    assertTrue(proc.process("FOO")[0].equals("FOO"));
  }
  
  public void testFilterOnNoMatch() {
    ScriptFilterProcessor proc = new ScriptFilterProcessor();
    proc.setScriptProcessor(generateScriptProcessor());
    proc.setFilterOnMatch(false);
    proc.validate(new ArrayList());
    assertTrue(proc.process("foo")[0].equals("foo"));
    assertTrue(proc.process("Foo")[0].equals("Foo"));
    assertTrue(proc.process("FOO").length == 0);
  }
  
  private ScriptProcessor generateScriptProcessor() {
    ScriptProcessor scriptProcessor=new ScriptProcessor();
    scriptProcessor.setLanguage("js");
    scriptProcessor.setScript("/[fF]oo/.test(data);");
    return scriptProcessor;   
  }
}
