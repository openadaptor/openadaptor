package org.oa3.thirdparty.velocity;

import org.oa3.util.ResourceUtil;

import junit.framework.TestCase;

public class VelocityProcessorTestCase extends TestCase {

  public void testEmbeddedTemplate() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplate("foo${data}bar");
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));
  }
  
  public void testTemplateFile() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplateFile(ResourceUtil.getResourcePath(this, "test.vm"));
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("test"));
  }
}
