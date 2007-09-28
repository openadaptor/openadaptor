package org.openadaptor.auxil.processor.script;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;

public class ScriptProcessorTestCase extends AbstractTestIDataProcessor {
  private static final Log log =LogFactory.getLog(ScriptProcessorTestCase.class);

  private static String FOOBAR="foobar";
  
  //Pick up default binding for most examples.
  protected String binding=ScriptProcessor.DEFAULT_DATA_BINDING;

  protected IDataProcessor createProcessor() {
    return new ScriptProcessor();
  }

  public void testProcessRecord() {
    log.debug("--- BEGIN testProcessRecord ---");
   ScriptProcessor processor=(ScriptProcessor)testProcessor;
    processor.setScript(binding+";");
    processor.validate(new ArrayList()); //Validation also initialised the script engine.
    checkResult(FOOBAR,processor.process(FOOBAR));
    Object[] result = processor.process("hello world");
    assertTrue(result.length == 1);
    assertFalse(result[0].equals(FOOBAR));


    processor.setScript(binding+" = \"hello \" + "+binding+";");
    processor.validate(new ArrayList());
    checkResult("hello foobar",processor.process(FOOBAR));

    processor.setScript(binding+"= null;");
    processor.validate(new ArrayList());
    assertTrue(processor.process(FOOBAR).length == 0);

    processor.setScript("if ("+binding+" == \"foo\") { "+binding+" = \"bar\";}");
    processor.validate(new ArrayList());

    checkResult("bar",processor.process("foo"));
    checkResult("FOO",processor.process("FOO"));
    log.debug("--- END testProcessRecord ---");
 }

  public void testValidation() {
    ScriptProcessor processor=(ScriptProcessor)testProcessor;
    List exceptions=new ArrayList();

    //No script set as yet
    processor.validate(exceptions);
    assertFalse(exceptions.isEmpty());

    //Illegal script
    exceptions.clear();
    processor.setScript("This is invalid script");
    processor.validate(exceptions);
    assertFalse(exceptions.isEmpty());

    //Good script
    exceptions.clear();
    processor.setScript(binding+";");
    processor.validate(exceptions);
    assertTrue(exceptions.isEmpty());

    //No Language (i.e. clear default)
    exceptions.clear();  
    processor.setLanguage(null);
    processor.validate(exceptions);
    assertFalse(exceptions.isEmpty());

  }
//Utility (and convenience) methods for validation of script output.
  private void checkResult(Object expected,Object[] returned) {
    checkResult(new Object[] {expected},returned);
  }
  /**
   * Check that the returned result exactly matches the expected result.
   * @param expected
   * @param returned
   */
  private void checkResult(Object[] expected,Object[] returned) {
    if (expected==null) {
      assertNull(returned);
    }
    else {
      assertNotNull(returned);
      assertTrue(expected.length==returned.length);
      for (int i=0;i<expected.length;i++) {
        assertEquals(expected[i], returned[i]);
      }
    }
  }

}
