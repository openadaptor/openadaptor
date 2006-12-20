package org.oa3.thirdparty.velocity;

import org.oa3.util.ResourceUtil;

import junit.framework.TestCase;

public class VelocityProcessorTestCase extends TestCase {

//  public void xtest() {
//    VelocityProcessor processor = new VelocityProcessor();
//    processor.setTemplate("foobar");
//    Object[] data = processor.process("test");
//    System.err.println(data[0]);
//  }
  
  public void test2() {
    VelocityProcessor processor = new VelocityProcessor();
    processor.setTemplate(ResourceUtil.getResourcePath(this, "test.vm"));
    Object[] data = processor.process("test");
    assertTrue(data.length == 1);
    System.err.println(data[0]);
    assertTrue(data[0].equals("test"));
  }
}
