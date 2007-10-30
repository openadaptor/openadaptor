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
package org.openadaptor.auxil.processor.jndi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;

import org.jmock.Mock;

import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.auxil.connector.jndi.JNDISearch;
import org.openadaptor.auxil.connector.jndi.NewJNDIReadConnector;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.RecordException;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Draft of unit tests for {@link NewJNDIEnhancementProcessor}.
 * 
 * @author Kris Lachor
 * @todo ported only testValidatex() methods from JNDIEnhancementProcessorTestCase,
 *   all other are remaining
 */
public class NewJNDIEnhancementProcessorTestCase extends MockObjectTestCase {

  NewJNDIEnhancementProcessor processor = new NewJNDIEnhancementProcessor();
  
  NewJNDIReadConnector mockReadConnector = new MockNewJNDIReadConnector();
  
  String recordKeySetByExistence = "testRecordKeySetByExistence";
  
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
   * Test method for {@link org.openadaptor.auxil.processor.jndi.NewJNDIEnhancementProcessor#getReadConnector()}.
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
   * Test method for {@link org.openadaptor.auxil.processor.jndi.NewJNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testValidate1()
   */
  public void testValidate1() {
//  the previously set directly on enhancementprocessor will now be set
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
   * Test method for {@link org.openadaptor.auxil.processor.jndi.NewJNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * Missing (incomingMap and recordKeyUsedAsSearchBase), and missing (outgoingMap and recordKeySetByExistence)
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testValidate2()
   * 
   */
  public void testValidate2() {
    setValidateExpectations();
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 3);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * Missing (outgoingMap and recordKeySetByExistence), incomingMap and fiter
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testValidate3()
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
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * Missing (incomingMap and filter), and missing (outgoingMap and recordKeySetByExistence)
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testValidate4()
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
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#processOrderedMap
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * No search filter set (all of: recordKeyUsedAsSearchBase, configDefinedSearchFilter, incomingMap are empty).
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testProcessOrderedMap1()
   */
  public void testProcessOrderedMap1() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    try{
      processor.process(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#processOrderedMap
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * Sets recordKeyUsedAsSearchBase property.
   * recordKeyUsedAsSearchBase not in the input map.
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testProcessOrderedMap2()
   */
  public void testProcessOrderedMap2() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    mockReadConnector.setRecordKeyUsedAsSearchBase("test");
    try{
      processor.process(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#processOrderedMap
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * getTreatMultiValuedAttributesAsArray = false
   * Search returns no results.
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testProcessOrderedMap3()
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
    processor.process(map);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#
   * tailorSearchToThisRecord(org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * 'normal' flow
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testTailorSearchToThisRecord1()
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
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#
   * tailorSearchToThisRecord(org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * recordKeyUsedAsSearchBase not occuring in input iorderedmap
   * 
   * ported from {@link JNDIEnhancementProcessorTestCase#testTailorSearchToThisRecord2()
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
  
  
  
//  /**
//   * Test method for {@link org.openadaptor.auxil.processor.jndi.NewJNDIEnhancementProcessor#validate(java.util.List)}.
//   */
//  public void testValidate() {
//    List exceptions = new ArrayList();
//  }


  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.NewJNDIEnhancementProcessor#process(java.lang.Object)}.
   */
  public void testProcess() {
  }
  
//
//  /**
//   * Test method for {@link org.openadaptor.auxil.processor.jndi.NewJNDIEnhancementProcessor#reset(java.lang.Object)}.
//   */
//  public void testReset() {
//    fail("Not yet implemented");
//  }
//

  /**
   * Inner mock. 
   * The only difference between this and the equivalent in JNDIEnhancementProcessorTestCase
   * is this extends NewJNDIReadConnector rather than JNDIReadConnector.
   */
  class MockNewJNDIReadConnector extends NewJNDIReadConnector{
    
    public MockNewJNDIReadConnector() {
      mockJNDISearch = mock(JNDISearch.class, "mockJNDISearch");
      mockJNDIConnection = mock(JNDIConnection.class, "mockJNDIConnection");
      setSearch((JNDISearch)mockJNDISearch.proxy());
      setJndiConnection((JNDIConnection) mockJNDIConnection.proxy());
      setEnhancementProcessorMode(true);
    }

    public void connect() {}
    
    public Object[] next(long timeoutMs) throws OAException {
       return null;
    }

    public JNDISearch getSearch() {
      return this.search;
    }        
  }
  
}