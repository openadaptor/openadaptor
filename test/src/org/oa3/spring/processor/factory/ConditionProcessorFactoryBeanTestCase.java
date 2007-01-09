/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.spring.processor.factory;

import junit.framework.TestCase;
import org.oa3.auxil.processor.simplerecord.ConditionProcessor;
import org.oa3.auxil.processor.simplerecord.AttributeSetProcessor;
import org.oa3.auxil.simplerecord.ISimpleRecordAccessor;
import org.oa3.thirdparty.dom4j.Dom4jSimpleRecordAccessor;
import org.oa3.core.exception.ComponentException;

import java.util.List;
import java.util.ArrayList;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 8, 2007 by oa3 Core Team
 */
public class ConditionProcessorFactoryBeanTestCase extends TestCase {
  protected final static String IF_EXPRESSION = "{quantity} gt 3";
  protected final static String THEN_EXPRESSION = "'many'";
  protected final static String ELSE_EXPRESSION = "'few'";
  protected final static String ATTRIBUTE_NAME = "testAtt";

  /**
   * Test doing a straightforward instantiation.
   */
  public void testBasicInstantiate() {
    ConditionProcessorFactoryBean testBean = new ConditionProcessorFactoryBean();
    testBean.setIfExpression(IF_EXPRESSION);
    testBean.setThenExpression(THEN_EXPRESSION);
    testBean.setElseExpression(ELSE_EXPRESSION);
    testBean.setAttributeName(ATTRIBUTE_NAME);
    ConditionProcessor testProcessor = null;
    try {
      testProcessor = (ConditionProcessor)testBean.getObject();
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
    assertNotNull(testProcessor);
    assertEquals("IF Expression of created ConditionProcessor not set as expected.", testProcessor.getIfExpression().getExpression(), IF_EXPRESSION);
    assertEquals("Then Processor not an AttributeSetProcessor.", testProcessor.getThenProcessor().getClass().getName(), "org.oa3.auxil.processor.simplerecord.AttributeSetProcessor");
    assertEquals("Else Processor not an AttributeSetProcessor.", testProcessor.getElseProcessor().getClass().getName(), "org.oa3.auxil.processor.simplerecord.AttributeSetProcessor");
    assertEquals("Then Expression of created ConditionProcessor not set as expected.", ((AttributeSetProcessor)testProcessor.getThenProcessor()).getExpression().getExpression(), THEN_EXPRESSION);
    assertEquals("Else Expression of created ConditionProcessor not set as expected.", ((AttributeSetProcessor)testProcessor.getElseProcessor()).getExpression().getExpression(), ELSE_EXPRESSION);
    // Should validate ok.
    List exceptionList = new ArrayList();
    testProcessor.validate(exceptionList);
    assertTrue("Expected no validation problems.", exceptionList.isEmpty());
  }

  /**
   * Test that setting an accessor propagates correctly.
   */
  public void testSettingAccessor() {
    ConditionProcessorFactoryBean testBean = new ConditionProcessorFactoryBean();
    testBean.setIfExpression(IF_EXPRESSION);
    testBean.setThenExpression(THEN_EXPRESSION);
    testBean.setElseExpression(ELSE_EXPRESSION);
    testBean.setAttributeName(ATTRIBUTE_NAME);
    ISimpleRecordAccessor accessor = new Dom4jSimpleRecordAccessor();
    testBean.setSimpleRecordAccessor(accessor);
    ConditionProcessor testProcessor = null;
    try {
      testProcessor = (ConditionProcessor)testBean.getObject();
    } catch (Exception e) {
      fail("Unexpected Exceptio: " + e);
    }
    assertNotNull(testProcessor);
    assertEquals("ConditionProcessor accessor not set correctly.", testProcessor.getSimpleRecordAccessor(), accessor);
    assertEquals("Then Processor accessor not set correctly.", ((AttributeSetProcessor)testProcessor.getThenProcessor()).getSimpleRecordAccessor(), accessor);
    assertEquals("Else Processor accessor not set correctly.", ((AttributeSetProcessor)testProcessor.getElseProcessor()).getSimpleRecordAccessor(), accessor);
    // Should validate ok.
    List exceptionList = new ArrayList();
    testProcessor.validate(exceptionList);
    assertTrue("Expected no validation problems.", exceptionList.isEmpty());
  }

  /**
   * Ensure that just setting a then bracnh works ok.
   */
  public void testSettingNoElseExpression() {
    ConditionProcessorFactoryBean testBean = new ConditionProcessorFactoryBean();
    testBean.setIfExpression(IF_EXPRESSION);
    testBean.setThenExpression(THEN_EXPRESSION);
    testBean.setAttributeName(ATTRIBUTE_NAME);
    ConditionProcessor testProcessor = null;
    try {
      testProcessor = (ConditionProcessor)testBean.getObject();
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
    assertNotNull(testProcessor);
    assertEquals("IF Expression of created ConditionProcessor not set as expected.", testProcessor.getIfExpression().getExpression(), IF_EXPRESSION);
    assertEquals("Then Processor not an AttributeSetProcessor.", testProcessor.getThenProcessor().getClass().getName(), "org.oa3.auxil.processor.simplerecord.AttributeSetProcessor");
    assertEquals("Then Expression of created ConditionProcessor not set as expected.", ((AttributeSetProcessor)testProcessor.getThenProcessor()).getExpression().getExpression(), THEN_EXPRESSION);

    assertTrue("Should be no ElseProcessor", testProcessor.getElseProcessor() == null);
    // Should validate ok.
    List exceptionList = new ArrayList();
    testProcessor.validate(exceptionList);
    assertTrue("Expected no validation problems.", exceptionList.isEmpty());
  }

  /**
   * Ensure that just setting an else branch works ok.
   */
  public void testSettingNoThenExpression() {
    ConditionProcessorFactoryBean testBean = new ConditionProcessorFactoryBean();
    testBean.setIfExpression(IF_EXPRESSION);
    testBean.setElseExpression(ELSE_EXPRESSION);
    testBean.setAttributeName(ATTRIBUTE_NAME);
    ConditionProcessor testProcessor = null;
    try {
      testProcessor = (ConditionProcessor)testBean.getObject();
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
    assertNotNull(testProcessor);
    assertEquals("IF Expression of created ConditionProcessor not set as expected.", testProcessor.getIfExpression().getExpression(), IF_EXPRESSION);
    assertEquals("Else Processor not an AttributeSetProcessor.", testProcessor.getElseProcessor().getClass().getName(), "org.oa3.auxil.processor.simplerecord.AttributeSetProcessor");
    assertEquals("Else Expression of created ConditionProcessor not set as expected.", ((AttributeSetProcessor)testProcessor.getElseProcessor()).getExpression().getExpression(), ELSE_EXPRESSION);

    assertTrue("Should be no ThenProcessor", testProcessor.getThenProcessor() == null);
    // Should validate ok.
    List exceptionList = new ArrayList();
    testProcessor.validate(exceptionList);
    assertTrue("Expected no validation problems.", exceptionList.isEmpty());
  }

  /**
   * Ensure that setting no if expression produces a ConditionProcessor that fails validation correctly.
   */
  public void testSettingNoIfExpression() {
    ConditionProcessorFactoryBean testBean = new ConditionProcessorFactoryBean();
    testBean.setThenExpression(THEN_EXPRESSION);
    testBean.setElseExpression(ELSE_EXPRESSION);
    testBean.setAttributeName(ATTRIBUTE_NAME);
    ConditionProcessor testProcessor = null;
    try {
      testProcessor = (ConditionProcessor)testBean.getObject();
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
    assertNotNull(testProcessor);
    assertTrue("IF Expression of created ConditionProcessor not set as expected.", testProcessor.getIfExpression() == null);
    // Should fail validate.
    List exceptionList = new ArrayList();
    testProcessor.validate(exceptionList);
    assertTrue("Expected one validation problem.", exceptionList.size() == 1);
    assertTrue("Expected it to be a ComponentException", exceptionList.get(0) instanceof ComponentException);
  }
}
