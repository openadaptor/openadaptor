package org.openadaptor.auxil.processor.script;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ScriptProcessorTestCase extends TestCase {

  public void test() {
    ScriptProcessor proc = new ScriptProcessor();
    {
      List exceptions = new ArrayList();
      proc.validate(exceptions);
      assertFalse(exceptions.isEmpty());
    }
    proc.setScript("blah blah blah");
    {
      List exceptions = new ArrayList();
      proc.validate(exceptions);
      assertFalse(exceptions.isEmpty());
    }
    proc.setLanguage("js");
    {
      List exceptions = new ArrayList();
      proc.validate(exceptions);
      assertFalse(exceptions.isEmpty());
    }
    proc.setScript("data;");
    {
      List exceptions = new ArrayList();
      proc.validate(exceptions);
      assertTrue(exceptions.isEmpty());
    }
    {
      Object[] result = proc.process("foobar");
      assertTrue(result.length == 1);
      assertTrue(result[0].equals("foobar"));
    }
    {
      Object[] result = proc.process("hello world");
      assertTrue(result.length == 1);
      assertFalse(result[0].equals("foobar"));
    }
    proc.setScript("data = \"hello \" + data;");
    proc.validate(new ArrayList());
    {
      Object[] result = proc.process("foobar");
      assertTrue(result.length == 1);
      assertTrue(result[0].equals("hello foobar"));
    }
    proc.setScript("data = null;");
    proc.validate(new ArrayList());
    {
      Object[] result = proc.process("foobar");
      assertTrue(result.length == 0);
    }
    proc.setScript("if (data == \"foo\") { data = \"bar\";}");
    proc.validate(new ArrayList());
    {
      Object[] result = proc.process("foo");
      assertTrue(result.length == 1);
      assertTrue(result[0].equals("bar"));
    }
    {
      Object[] result = proc.process("FOO");
      assertTrue(result.length == 1);
      assertTrue(result[0].equals("FOO"));
    }
  }
}
