package org.openadaptor.auxil.processor.script;

import java.util.ArrayList;
import java.util.List;

import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;

public class ScriptFilterProcessorTestCase extends AbstractTestIDataProcessor {

  protected IDataProcessor createProcessor() {
    ScriptFilterProcessor processor=new ScriptFilterProcessor();
    processor.setScriptProcessor(generateScriptProcessor());
    processor.validate(new ArrayList()); //This is necessary to initialise the scriptengine.
    return processor;
  }

  public void testValidation() {
    ScriptFilterProcessor processor=(ScriptFilterProcessor)testProcessor;
    List exceptions=new ArrayList();

    //Good config
    processor.validate(exceptions);
    assertTrue(exceptions.isEmpty());

    //No scriptProcessor set as yet
    exceptions.clear();
    processor.setScriptProcessor(null);
    processor.validate(exceptions);
    assertFalse(exceptions.isEmpty());
  }

  public void testProcessRecord() {
    ScriptFilterProcessor processor = (ScriptFilterProcessor)testProcessor;

    //Test filtering with filterOnMatch=true
    assertTrue(processor.process("foo").length == 0); //Should get filtered
    assertTrue(processor.process("Foo").length == 0); //Should get filtered
    assertTrue(processor.process("FOO")[0].equals("FOO")); //Should pass through   

    //Test filtering with filterOnMatch=false;
    processor.setFilterOnMatch(false);
    processor.validate(new ArrayList()); //not really necesary
    assertTrue(processor.process("foo")[0].equals("foo"));
    assertTrue(processor.process("Foo")[0].equals("Foo"));
    assertTrue(processor.process("FOO").length == 0);
  }

  private ScriptProcessor generateScriptProcessor() {
    ScriptProcessor scriptProcessor=new ScriptProcessor();
    scriptProcessor.setLanguage("js");
    scriptProcessor.setScript("/[fF]oo/.test(data);");
    return scriptProcessor;   
  }

}
