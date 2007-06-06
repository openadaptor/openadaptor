package org.openadaptor.thirdparty.velocity;

import junit.framework.TestCase;

import org.openadaptor.util.ResourceUtil;

public class VelocityProcessorTestCase extends TestCase {
  protected static final String RESOURCE_LOCATION = "test/unit/src/";

  public void testEmbeddedTemplate() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplateString("foo${data}bar");
    processor.validate(null);
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));
  }
  
  public void testTemplateFile() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplateFile(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test.vm"));
    processor.validate(null);
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("test"));
  }
}
