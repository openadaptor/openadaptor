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
package org.openadaptor.auxil.processor.map;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Unit tests for AttributeMapProcessor.
 * 
 * Incomplete.
 * @author higginse
 * @since Introduced Post 3.2.1
 */

public class AttributeMapProcessorTestCase extends AbstractTestMapProcessor {
  
  private static final String KEY_ONE="one";
  private static final String VALUE_ONE="oneval";
  private static final String KEY_TWO="two";
  private static final String VALUE_TWO="twoval";
  private static final String KEY_NEW_ONE="new_one";
  private static final String KEY_L_1="l1";
  private static final String KEY_L_2_1="l2-1";
  private static final String KEY_L_2_2="l2-2";
  
  private static final String VAL_L_1=KEY_L_1+"-val";
  private static final String VAL_L_2_1=KEY_L_2_1+"-val";
  private static final String VAL_L_2_2=KEY_L_2_2+"-val";
  
  protected static final String XML_ROOT="root";
  protected static final String XML_ROOT_XPATH="/"+XML_ROOT;
  protected static final String KEY_ONE_XPATH=XML_ROOT_XPATH+"/"+KEY_ONE;
  protected static final String KEY_TWO_XPATH=XML_ROOT_XPATH+"/"+KEY_TWO;
  protected static final String KEY_NEW_ONE_XPATH=XML_ROOT_XPATH+"/"+KEY_NEW_ONE;
  
  protected static final String KEY_L1_XPATH=XML_ROOT_XPATH+"/"+KEY_L_1;
  protected static final String KEY_L2_1_XPATH=KEY_L1_XPATH+"/"+KEY_L_2_1;
  protected static final String KEY_L2_12XPATH=KEY_L1_XPATH+"/"+KEY_L_2_2;

  protected AbstractMapProcessor createInstance(){
    Map attributeMap=new HashMap();
    attributeMap.put(KEY_ONE_XPATH,KEY_NEW_ONE_XPATH);
    return generateProcessor(attributeMap);
  }
  
  protected AttributeMapProcessor generateProcessor(Map map){
    AttributeMapProcessor processor=new AttributeMapProcessor();
    processor.setMap(map);
    return processor;
  }
  
  /**
   * This verifies mapping behaviour against XML Documents.
   * <UL>
   * <LI> Check that type and cardinality does not change i.e.
   *      Check that the output array contains a single Document instance.
   * <LI> Check that a single mapping from one key to another works, ie
   *      the new key now has the value, and the old key has gone.
   * </UL>
   *
   */
  public void testXmlDocumentProcessing() {
    Document doc=buildXmlDocument(XML_ROOT);
    System.out.println("Incoming XML:"+doc.asXML());
    Object[] results=processor.process(doc);
    System.out.println("Outgoing XML:"+((Document)results[0]).asXML());
    assertTrue("processor should produce a single output record",(results!=null && results.length==1));
    Document output=(Document)results[0];
    assertNotNull("New key should have value",output.selectSingleNode(KEY_NEW_ONE_XPATH));
    assertNull("Old key should not have value",output.selectSingleNode(KEY_ONE_XPATH));
  }

  public void testNestedXmlDocumentProcessing() {
    Map attributeMap=new HashMap();
    attributeMap.put(KEY_L2_1_XPATH, KEY_NEW_ONE_XPATH);
    Document doc=buildXmlDocument(XML_ROOT);
    System.out.println("Incoming XML:"+doc.asXML());
    Object[] results=processor.process(doc);
    System.out.println("Outgoing XML:"+((Document)results[0]).asXML());
    assertTrue("processor should produce a single output record",(results!=null && results.length==1));
    Document output=(Document)results[0];
    assertNotNull("New key should have value",output.selectSingleNode(KEY_NEW_ONE_XPATH));
    assertNull("Old key should not have value",output.selectSingleNode(KEY_ONE_XPATH));
  }

  private Document buildXmlDocument(String root) {
    Document doc=DocumentHelper.createDocument();
    Element rootElement=DocumentHelper.createElement(root);
    doc.setRootElement(rootElement);
    rootElement.add(createElement(KEY_ONE,VALUE_ONE));
    rootElement.add(createElement(KEY_TWO,VALUE_TWO));
    rootElement.add(createElement(KEY_ONE,"AnotherValue"));
    Element l1=createElement(KEY_L_1,VAL_L_1);
    l1.add(createElement(KEY_L_2_1,VAL_L_2_1));
    l1.add(createElement(KEY_L_2_2,VAL_L_2_2));
    rootElement.add(l1);
    return doc;
  }
  
  
  private Element createElement(String name,String value) {
    Element element=DocumentHelper.createElement(name);
    element.setText(value);
    return element;
  }

}
