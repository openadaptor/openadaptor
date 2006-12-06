package org.oa3.auxil.xml.processor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oa3.auxil.processor.xml.XmlValidator;
import org.oa3.util.FileUtils;

public class XmlValidatorTestCase extends TestCase {

  XmlValidator validator = new XmlValidator();

  public void testNoSchemaFile() {
    List exceptions = new ArrayList();
    validator.validate(exceptions);
    assertFalse("failed to throw exceptions", exceptions.isEmpty());
  }

  public void testMissingSchemaFile() {
    String url = "file:" + XsltProcessorTestCase.PATH + "/schemaX.xsd";
    validator.setSchemaURL(url);
    List exceptions = new ArrayList();
    validator.validate(exceptions);
    assertFalse("unexpected exceptions", exceptions.isEmpty());
  }

  public void testValid() {
    validateSchemaFile();
    try {
      String s = FileUtils.getFileContents(XsltProcessorTestCase.PATH + "/input.xml");
      validator.process(s);
    } catch (Exception e) {
      e.printStackTrace();
      fail("process exception");
    }
  }

  public void testinvalid() {
    validateSchemaFile();
    try {
      String s = FileUtils.getFileContents(XsltProcessorTestCase.PATH + "/output.xml");
      validator.process(s);
      fail("failed to throw exception for invalid xml");
    } catch (Exception e) {
    }
  }

  private void validateSchemaFile() {
    String url = "file:" + XsltProcessorTestCase.PATH + "/schema.xsd";
    validator.setSchemaURL(url);
    List exceptions = new ArrayList();
    validator.validate(exceptions);
    assertTrue("unexpected exceptions", exceptions.isEmpty());
  }

}
