package org.openadaptor.thirdparty.velocity;

import junit.framework.TestCase;
import org.openadaptor.util.ResourceUtil;

import java.util.ArrayList;

public class VelocityProcessorTestCase extends TestCase {
  protected static final String RESOURCE_LOCATION = "test/unit/src/";

  public void testEmbeddedTemplate() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplateString("foo${data}bar");
    processor.validate(new ArrayList());
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));
  }

  public void testTemplateFile() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplateFile(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test.vm"));
    processor.validate(new ArrayList());
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("test"));
  }

  /**
   * Test that setting and unsetting merge logging has an effect on the processor.
   * We can't really test the resulting status of the engine but can at least test
   * that it still works.
   */
  public void testSetMergeLogging() {
    VelocityProcessor trueProcessor = new VelocityProcessor();
    trueProcessor.setMergeLogging(true);
    assertTrue(trueProcessor.isMergeLogging());

    trueProcessor.setTemplateString("foo${data}bar");
    trueProcessor.validate(new ArrayList());
    Object[] data = trueProcessor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));

    VelocityProcessor falseProcessor = new VelocityProcessor();
    falseProcessor.setMergeLogging(false);
    assertFalse(falseProcessor.isMergeLogging());

    falseProcessor.setTemplateString("foo${data}bar");
    falseProcessor.validate(new ArrayList());
    data = falseProcessor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));

  }

  /**
   * Test that setting and unsetting the velocity logging category has an effect on the processor.
   * We can't really test the resulting status of the engine but can at least test that it still works.
   */
  public void testTestCategory() {
    VelocityProcessor velocityProcessor = new VelocityProcessor();
    velocityProcessor.setMergeLogging(true);
    assertTrue(velocityProcessor.isMergeLogging());
    assertEquals("velocity", velocityProcessor.getCategory()); // This is the default

    velocityProcessor.setTemplateString("foo${data}bar");
    velocityProcessor.validate(new ArrayList());
    Object[] data = velocityProcessor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));

    VelocityProcessor testProcessor = new VelocityProcessor();
    testProcessor.setMergeLogging(true);
    testProcessor.setCategory("test");
    assertTrue(testProcessor.isMergeLogging());
    assertEquals("test", testProcessor.getCategory());

    testProcessor.setTemplateString("foo${data}bar");
    testProcessor.validate(new ArrayList());
    data = testProcessor.process("test");
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("footestbar"));
  }


}
