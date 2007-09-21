package org.openadaptor.auxil.processor.script;

import java.util.ArrayList;
import java.util.List;

import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;

public class ScriptProcessorTestCase extends AbstractTestIDataProcessor {

  private static String FOOBAR="foobar";
  protected IDataProcessor createProcessor() {
    return new ScriptProcessor();
  }

  public void testProcessRecord() {
    ScriptProcessor processor=(ScriptProcessor)testProcessor;
    processor.setScript("data;");
    processor.validate(new ArrayList()); //Validation also initialised the script engine.
    checkResult(FOOBAR,processor.process(FOOBAR));
    Object[] result = processor.process("hello world");
    assertTrue(result.length == 1);
    assertFalse(result[0].equals(FOOBAR));


    processor.setScript("data = \"hello \" + data;");
    processor.validate(new ArrayList());
    checkResult("hello foobar",processor.process(FOOBAR));

    processor.setScript("data = null;");
    processor.validate(new ArrayList());
    assertTrue(processor.process(FOOBAR).length == 0);

    processor.setScript("if (data == \"foo\") { data = \"bar\";}");
    processor.validate(new ArrayList());

    checkResult("bar",processor.process("foo"));
    checkResult("FOO",processor.process("FOO"));
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
    processor.setScript("data;");
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
