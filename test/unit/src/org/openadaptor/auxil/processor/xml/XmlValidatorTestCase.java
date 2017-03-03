package org.openadaptor.auxil.processor.xml;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openadaptor.auxil.processor.xml.XmlValidator;
import org.openadaptor.util.ResourceUtil;

public class XmlValidatorTestCase extends TestCase {

  XmlValidator validator = new XmlValidator();
  protected static final String RESOURCE_LOCATION = "test/unit/src/";

  public void testNoSchemaFile() {
    List exceptions = new ArrayList();
    validator.validate(exceptions);
    assertFalse("failed to throw exceptions", exceptions.isEmpty());
  }

  public void testMissingSchemaFile() {
    String s = ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "schemaX.xsd");
    validator.setSchemaURL("file:" + s);
    List exceptions = new ArrayList();
    validator.validate(exceptions);
    assertFalse("unexpected exceptions", exceptions.isEmpty());
  }

  public void testValid() {
    validateSchemaFile();
    try {
      String s = ResourceUtil.readFileContents(this, "input.xml");
      validator.process(s);
    } catch (Exception e) {
      e.printStackTrace();
      fail("process exception");
    }
  }

  public void testinvalid() {
    validateSchemaFile();
    try {
      String s = ResourceUtil.readFileContents(this, "output.xml");
      validator.process(s);
      fail("failed to throw exception for invalid xml");
    } catch (Exception e) {
    }
  }

  private void validateSchemaFile() {
    String s = ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "schema.xsd");
    validator.setSchemaURL(s);
    List exceptions = new ArrayList();
    validator.validate(exceptions);
    assertTrue("unexpected exceptions", exceptions.isEmpty());
  }

}
