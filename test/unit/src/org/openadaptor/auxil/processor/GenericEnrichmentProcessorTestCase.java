/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
 */
package org.openadaptor.auxil.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;

import org.jmock.Mock;

import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.auxil.connector.jndi.JNDISearch;
import org.openadaptor.auxil.connector.jndi.JNDIReadConnector;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.auxil.processor.GenericEnrichmentProcessor;
//import org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessorTestCase;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.node.EnrichmentProcessorNode;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Unit tests for {@link GenericEnrichmentProcessor}.
 * Many of the tests were written to ensure the {@link GenericEnrichmentProcessor} is
 * compatible with the {@link JNDIEnhancementProcessor} (now deleted) that it replaced.
 * 
 * @author Kris Lachor
 */
public class GenericEnrichmentProcessorTestCase extends MockObjectTestCase {

  GenericEnrichmentProcessor processor = new GenericEnrichmentProcessor();
  
  JNDIReadConnector mockReadConnector = new MockJNDIReadConnector();
  
  {
    processor.setReadConnector(mockReadConnector);
  }
  
  String recordKeySetByExistence = "testRecordKeySetByExistence";
  
  /* 
   * This is included here since the node is now the only component aware of
   * IEnrichmentProcessor method call sequence - this was previously  
   * implemented in JNDIEnhancementProcessor#processOrderedMap
   */
  EnrichmentProcessorNode enrichmentProcessorNode = new EnrichmentProcessorNode("testNode", processor);
  
  Map incomingMap = new HashMap();
  {
    incomingMap.put("incomingMapKey1", "incomingMapValue1");
    incomingMap.put("incomingMapKey2", "incomingMapValue2");
  }
  
  Mock mockJNDISearch;
  
  Mock mockJNDIConnection;
  
  Mock mockNamingEnumeration;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    processor.setReadConnector(mockReadConnector);
    mockNamingEnumeration = new Mock(NamingEnumeration.class);
  }

  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#getReadConnector()}.
   */
  public void testGetReadConnector() {
    assertNotNull(processor.getReadConnector());
    assertTrue(processor.getReadConnector() instanceof IReadConnector);
    processor.setReadConnector(null);
    assertNull(processor.getReadConnector());
  }
  
  
  /**
   * copy of {@link JNDIEnhancementProcessorTestCase#setValidateExpectations}
   */
  private void setValidateExpectations(){
    mockJNDISearch.expects(once()).method("getSearchBases");
    mockJNDISearch.expects(atLeastOnce()).method("getFilter");
    mockJNDISearch.expects(once()).method("getAttributes");
    mockJNDISearch.expects(once()).method("setAttributes");
  }

  /**
   * copy of {@link JNDIEnhancementProcessorTestCase#validate}
   */
  private void validate(){
    setValidateExpectations();
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.isEmpty());
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#validate
   * (java.util.List)}.
   */
  public void testValidate1() {
//  the previously set directly on enrichmentprocessor will now be set
//  on the JNDIReadConnector
//-    processor.setRecordKeyUsedAsSearchBase("foo1");
//-    processor.setRecordKeySetByExistence(recordKeySetByExistence);
//-    processor.setIncomingMap(incomingMap);
    
    mockReadConnector.setRecordKeyUsedAsSearchBase("foo1");
    mockReadConnector.setRecordKeySetByExistence(recordKeySetByExistence);
    mockReadConnector.setIncomingMap(incomingMap);
    validate();
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#validate
   * (java.util.List)}.
   * Missing (incomingMap and recordKeyUsedAsSearchBase), and missing (outgoingMap and recordKeySetByExistence)

   */
  public void testValidate2() {
    setValidateExpectations();
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 3);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#validate
   * (java.util.List)}.
   * Missing (outgoingMap and recordKeySetByExistence), incomingMap and filter
   */
  public void testValidate3() {
    setValidateExpectations();
//-    processor.setRecordKeyUsedAsSearchBase("foo1");
    mockReadConnector.setRecordKeyUsedAsSearchBase("foo1");
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 2);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#validate
   * (java.util.List)}.
   * Missing (incomingMap and filter), and missing (outgoingMap and recordKeySetByExistence)
   */
  public void testValidate4() {
    setValidateExpectations();
//-    processor.setRecordKeyUsedAsSearchBase("foo1");
//-    processor.setRecordKeySetByExistence(recordKeySetByExistence);
    mockReadConnector.setRecordKeyUsedAsSearchBase("foo1");
    mockReadConnector.setRecordKeySetByExistence(recordKeySetByExistence);
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 1);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#processSingleRecord
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * No search filter set (all of: recordKeyUsedAsSearchBase, configDefinedSearchFilter, incomingMap are empty).
   */
  public void testProcessOrderedMap1() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    try{
      enrichmentProcessorNode.processSingleRecord(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#processSingleRecord
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * Sets recordKeyUsedAsSearchBase property.
   * recordKeyUsedAsSearchBase not in the input map.
   */
  public void testProcessOrderedMap2() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    mockReadConnector.setRecordKeyUsedAsSearchBase("test");
    try{
      enrichmentProcessorNode.processSingleRecord(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#processSingleRecord
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * getTreatMultiValuedAttributesAsArray = false
   * Search returns no results.
   * 
   */
  public void testProcessOrderedMap3() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    mockReadConnector.setIncomingMap(incomingMap);
    mockReadConnector.setRecordKeyUsedAsSearchBase("foo1");
    mockReadConnector.setRecordKeySetByExistence(recordKeySetByExistence);
    mockJNDISearch.expects(once()).method("setSearchBases").with(eq(new String[]{"bar1"}));
    mockJNDISearch.expects(once()).method("setFilter");
    mockJNDISearch.expects(once()).method("getTreatMultiValuedAttributesAsArray").will(returnValue(false));
    mockJNDISearch.expects(once()).method("getJoinArraysWithSeparator");
    mockJNDISearch.expects(once()).method("execute").will(returnValue(mockNamingEnumeration.proxy()));
    mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
    validate();
    enrichmentProcessorNode.processSingleRecord(map);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#
   * tailorSearchToThisRecord(org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * 'normal' flow
   */
  public void testTailorSearchToThisRecord1() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    
    mockReadConnector.setIncomingMap(incomingMap);
    mockReadConnector.setRecordKeyUsedAsSearchBase("foo1");
    mockReadConnector.setRecordKeySetByExistence(recordKeySetByExistence);

    mockJNDISearch.expects(once()).method("setSearchBases").with(eq(new String[]{"bar1"}));
    mockJNDISearch.expects(once()).method("setFilter");
   
    validate();
    mockReadConnector.tailorSearchToThisRecord(map);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#
   * tailorSearchToThisRecord(org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * recordKeyUsedAsSearchBase not occuring in input iorderedmap
   */
  public void testTailorSearchToThisRecord2() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    
    mockReadConnector.setIncomingMap(incomingMap);
    mockReadConnector.setRecordKeyUsedAsSearchBase("foo2");
    mockReadConnector.setRecordKeySetByExistence(recordKeySetByExistence);
   
    validate();
    try{
      mockReadConnector.tailorSearchToThisRecord(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is null.
   */
  public void testPrepareParameters1(){
    IOrderedMap params = processor.prepareParameters(null);
    assertNotNull(params);
    assertTrue(params.size()==0);
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is a non-IOrderedMap.
   */
  public void testPrepareParameters2(){
    IOrderedMap params = processor.prepareParameters(new String("test"));
    assertNotNull(params);
    assertTrue(params.size()==0);
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is an IOrdredMap
   */
  public void testPrepareParameters3(){
    IOrderedMap input = new OrderedHashMap();
    input.put("field1", "value1");
    input.put("field2", "value2");
    IOrderedMap params = processor.prepareParameters(input);
    assertNotNull(params);
    assertTrue(params.size()==2);
    Object value1 = params.get("field1");
    assertTrue(value1 instanceof String);
    assertTrue(value1.equals("value1"));
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is an IOrdredMap.
   * Parameter field names specified.
   */
  public void testPrepareParameters4(){
    IOrderedMap input = new OrderedHashMap();
    input.put("field1", "value1");
    input.put("field2", "value2");
    processor.setParameterNames("field1, field2");
    IOrderedMap params = processor.prepareParameters(input);
    assertNotNull(params);
    assertTrue(params.size()==2);
    for(int i=1; i<=params.size();i++){
      Object value = params.get("field" + new Integer(i));
      assertNotNull(value);
      assertTrue(value instanceof String);
      assertTrue(value.equals("value" + new Integer(i)));
    }
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is an IOrdredMap.
   * Parameter field names specified.
   */
  public void testPrepareParameters5(){
    IOrderedMap input = new OrderedHashMap();
    input.put("field1", "value1");
    input.put("field2", "value2");
    processor.setParameterNames("field1");
    IOrderedMap params = processor.prepareParameters(input);
    assertNotNull(params);
    assertTrue(params.size()==1);
    for(int i=1; i<=params.size();i++){
      Object value = params.get("field" + new Integer(i));
      assertNotNull(value);
      assertTrue(value instanceof String);
      assertTrue(value.equals("value" + new Integer(i)));
    }
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is an IOrdredMap.
   * Parameter field names specified.
   */
  public void testPrepareParameters6(){
    IOrderedMap input = new OrderedHashMap();
    input.put("field1", "value1");
    input.put("field2", "value2");
    processor.setParameterNames("field2");
    IOrderedMap params = processor.prepareParameters(input);
    assertNotNull(params);
    assertTrue(params.size()==1);
    Object value = params.get("field2");
    assertNotNull(value);
    assertTrue(value instanceof String);
    assertTrue(value.equals("value2"));
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is an IOrdredMap.
   * Parameter field names specified, but the field doesn't occur in input (expecting params map 
   * with null values).
   */
  public void testPrepareParameters7(){
    IOrderedMap input = new OrderedHashMap();
    input.put("field1", "value1");
    input.put("field2", "value2");
    processor.setParameterNames("field3");
    IOrderedMap params = processor.prepareParameters(input);
    assertNotNull(params);
    assertTrue(params.size()==1);
    assertNull(params.get("field3"));
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#prepareParameters(Object)}.
   * Input is an IOrdredMap.
   * Parameter field names specified, one of the fields doesn't occur in input (params map with
   * only those fields that were in the input).
   */
  public void testPrepareParameters8(){
    IOrderedMap input = new OrderedHashMap();
    input.put("field1", "value1");
    input.put("field2", "value2");
    processor.setParameterNames("field1, field2, field3");
    IOrderedMap params = processor.prepareParameters(input);
    assertNotNull(params);
    assertTrue(params.size()==3);
    for(int i=1; i<=2;i++){
      Object value = params.get("field" + new Integer(i));
      assertNotNull(value);
      assertTrue(value instanceof String);
      assertTrue(value.equals("value" + new Integer(i)));
    }
    assertNull(params.get("field3"));
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#enrich(Object, Object[]).
   * Reader returned no data.
   */
  public void testEnrich1(){
    Object input = new Object();
    
    /* null */
    Object [] result = processor.enrich(input, null);
    assertNotNull(result);
    assertTrue(result.length==1);
    assertEquals(result[0], input);
    
    /* empty array */
    result = processor.enrich(input, new Object[0]);
    assertNotNull(result);
    assertTrue(result.length==1);
    assertEquals(result[0], input);
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#enrich(Object, Object[]).
   * Reader returned an object.
   */
  public void testEnrich2(){
    Object input = new Object();
    Object extraData = new Object();
    Object [] result = processor.enrich(input, new Object[]{extraData});
    assertNotNull(result);
    assertTrue(result.length==1);
    assertEquals(result[0], extraData); 
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#enrich(Object, Object[]).
   * Input is an Object, returned enrichment data is an array of Objects.
   */
  public void testEnrich3(){
    Object input = new Object();
    Object data1 = new Object(), data2 = new Object(), data3 = new Object();
    Object [] result = processor.enrich(input, new Object[]{data1, data2, data3});
    assertNotNull(result);
    assertTrue(result.length==3);
    assertEquals(result[0], data1); 
    assertEquals(result[1], data2); 
    assertEquals(result[2], data3); 
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#enrich(Object, Object[]).
   * Input is an IOrdredMap. Reader returned an IOrdredMap.
   * The enrichmentElementName property is unset.
   */
  public void testEnrich4(){
    IOrderedMap input = new OrderedHashMap();
    input.put("foo1", "bar1");
    IOrderedMap data = new OrderedHashMap();
    data.put("foo2", "bar2");
    Object [] result = processor.enrich(input, new Object[]{data});
    assertNotNull(result);
    assertTrue(result.length==1);
    assertTrue(result[0] instanceof IOrderedMap);
    IOrderedMap resultMap = (IOrderedMap) result[0];
    assertTrue(resultMap.size()==2);
    assertEquals(resultMap.get("foo1"), "bar1");
    assertEquals(resultMap.get("foo2"), "bar2");
  }
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#enrich(Object, Object[]).
   * Input is an IOrdredMap. Reader returned an IOrdredMap. The enrichmentElementName property 
   * is set.
   */
  public void testEnrich_enrichmentElementName(){
    processor.setEnrichmentElementName("enrichmentData");
    IOrderedMap input = new OrderedHashMap();
    input.put("foo1", "bar1");
    IOrderedMap data = new OrderedHashMap();
    data.put("foo2", "bar2");
    Object [] result = processor.enrich(input, new Object[]{data});
    assertNotNull(result);
    assertTrue(result.length==1);
    assertTrue(result[0] instanceof IOrderedMap);
    IOrderedMap resultMap = (IOrderedMap) result[0];
    assertTrue(resultMap.size()==2);
    assertEquals(resultMap.get("foo1"), "bar1");
    Object [] enrichmentData = (Object []) resultMap.get("enrichmentData");
    assertTrue(enrichmentData.length==1);
    assertTrue(enrichmentData[0] instanceof IOrderedMap);
    IOrderedMap enrichmentDataMap = (IOrderedMap) enrichmentData[0];
    assertEquals(enrichmentDataMap.get("foo2"), "bar2");
  }
  
  
  /**
   * Tests {@link org.openadaptor.auxil.processor.GenericEnrichmentProcessor#enrich(Object, Object[]).
   * 'discardInput' property set to true.
   */
  public void testEnrich_DiscardInput(){
    processor.setDiscardInput(true);
    IOrderedMap input = new OrderedHashMap();
    input.put("foo1", "bar1");
    IOrderedMap data = new OrderedHashMap();
    data.put("foo2", "bar2");
    Object [] result = processor.enrich(input, new Object[]{data});
    assertNotNull(result);
    assertTrue(result.length==1);
    assertTrue(result[0] instanceof IOrderedMap);
    IOrderedMap resultMap = (IOrderedMap) result[0];
    assertTrue(resultMap.size()==1);
    assertEquals(resultMap.get("foo2"), "bar2");
  }
  
  /**
   * Inner mock.
   */
  class MockJNDIReadConnector extends JNDIReadConnector{
    
    public MockJNDIReadConnector() {
      mockJNDISearch = mock(JNDISearch.class, "mockJNDISearch");
      mockJNDIConnection = mock(JNDIConnection.class, "mockJNDIConnection");
      setSearch((JNDISearch)mockJNDISearch.proxy());
      setJndiConnection((JNDIConnection) mockJNDIConnection.proxy());
      setEnrichmentProcessorMode(true);
    }

    public void fun(){
      System.out.println();
    }
    
    public void connect() {}
   
    public JNDISearch getSearch() {
      return this.search;
    }        
  }
  
}