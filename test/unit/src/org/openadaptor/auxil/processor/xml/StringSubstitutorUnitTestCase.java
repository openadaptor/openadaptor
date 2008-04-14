package org.openadaptor.auxil.processor.xml;

import java.util.ArrayList;
import junit.framework.TestCase;

public class StringSubstitutorUnitTestCase  extends TestCase{

  private static String inputData = "<actorRelationType>LEGAL_HIERARCHY_RELATION</actorRelationType>\n"+
    "<description>TestCompany \u000c United Systems : 23\u0001% belongs to XYZ</description>\n"+
    "<actorRelAttribute>59</actorRelAttribute>";

  private static String expectAllStripped = "<actorRelationType>LEGAL_HIERARCHY_RELATION</actorRelationType>\n"+
    "<description>TestCompany  United Systems : 23% belongs to XYZ</description>\n"+
    "<actorRelAttribute>59</actorRelAttribute>";

  private static String expectFirstStripped = "<actorRelationType>LEGAL_HIERARCHY_RELATION</actorRelationType>\n"+
    "<description>TestCompany  United Systems : 23\u0001% belongs to XYZ</description>\n"+
    "<actorRelAttribute>59</actorRelAttribute>";

  private static String expectAllSubstituted = "<actorRelationType>LEGAL_HIERARCHY_RELATION</actorRelationType>\n"+
    "<description>TestCompany SomeValue United Systems : 23SomeValue% belongs to XYZ</description>\n"+
    "<actorRelAttribute>59</actorRelAttribute>";

  private static String expectFirstSubstituted = "<actorRelationType>LEGAL_HIERARCHY_RELATION</actorRelationType>\n"+
    "<description>TestCompany SomeValue United Systems : 23\u0001% belongs to XYZ</description>\n"+
    "<actorRelAttribute>59</actorRelAttribute>";


  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }


  public void testStripAll() {
    StringSubstitutor x = new StringSubstitutor();
    x.setPattern("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f]");
    x.setReplaceall(true);
    x.setReplacement("");
    x.validate(new ArrayList());
    
    Object[] result = x.process(inputData);
    
    assertEquals(1, result.length);
    assertEquals(expectAllStripped, result[0]);
  }

  public void testStripFirst() {
    StringSubstitutor x = new StringSubstitutor();
    x.setPattern("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f]");
    x.setReplaceall(false);
    x.setReplacement("");
    x.validate(new ArrayList());
    
    Object[] result = x.process(inputData);
    
    assertEquals(1, result.length);
    assertEquals(expectFirstStripped, result[0]);
  }

  public void testSubstituteAll() {
    StringSubstitutor x = new StringSubstitutor();
    x.setPattern("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f]");
    x.setReplaceall(true);
    x.setReplacement("SomeValue");
    x.validate(new ArrayList());
    
    Object[] result = x.process(inputData);
    
    assertEquals(1, result.length);
    assertEquals(expectAllSubstituted, result[0]);
  }

  public void testSubstituteFirst() {
    StringSubstitutor x = new StringSubstitutor();
    x.setPattern("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f]");
    x.setReplaceall(false);
    x.setReplacement("SomeValue");
    x.validate(new ArrayList());
    
    Object[] result = x.process(inputData);
    
    assertEquals(1, result.length);
    assertEquals(expectFirstSubstituted, result[0]);
  }

}