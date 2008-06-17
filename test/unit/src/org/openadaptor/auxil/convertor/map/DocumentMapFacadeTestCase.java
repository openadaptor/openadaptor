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
package org.openadaptor.auxil.convertor.map;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Unit tests for DocumentMapFacade implementation
 * @author higginse
 *
 */
public class DocumentMapFacadeTestCase extends AbstractTestMapFacade {
  private static final Log log = LogFactory.getLog(DocumentMapFacadeTestCase.class);

  protected static final String[] XML= {
    "<root><foo>45</foo></root>",
    "<root><foo><bar>leaf</bar></foo></root>",
    "<root><foo><bar>leaf1</bar><bar>leaf2</bar></foo></root>",
    "<root><A><AB/><AC><AC1>ac1val</AC1></AC><AD>adval</AD></A><B><BA/></B></root>",
    "<root xmlns='foo'><A><AB/><AC><AC1>ac1val</AC1></AC><AD>adval</AD></A><B><BA/></B></root>"
  };

  protected static final int[] KEY_COUNT= { 2,3,3,8,8 };

  protected Document[] docs;
  protected MapFacade[] facades;
  
  protected void setUp() throws Exception {
    docs=new Document[XML.length];
    facades=new MapFacade[XML.length];
    for (int i=0;i<docs.length;i++) {
      docs[i]=generateDocument(i);
      facades[i]=new DocumentMapFacade(docs[i]);
    }
    super.setUp();
  }

  protected MapFacade createInstance(){
    return facades[0];
  }

  protected static Document generateDocument(int index) {
    try {
      return DocumentHelper.parseText(XML[index]);
    }
    catch (DocumentException de) {
      fail(de.toString());
      return null;
    }
  }

  public void testKeySet() {
    logTest("keySet()");
    for (int i=0;i<XML.length;i++) {
      //MapFacade facade=generator.generateFacade(generateDocument(i));
      Set keys=facades[i].keySet();
//      log.info(((Document)facades[i].getUnderlyingObject()).asXML());
//      Iterator it=keys.iterator();
//      while (it.hasNext()) {
//        Object key=it.next();
//        log.info(key+"->"+facades[i].get(key));
//      }
      assertEquals("Incorrect number of keys in key set",keys.size(),KEY_COUNT[i]);
    }
  }

  public void testGet() {
    logTest("get()");
    //Leaf get()
    assertEquals("45",facades[0].get("/root/foo"));

    //Non-leaf get() - Should return an Element (the leaf)
    Object value=facades[1].get("/root/foo");
    assertTrue("Get on a single-valued non-leaf",value instanceof Element);
    Element element=(Element)value;
    assertTrue("Expected value not returned",element.getName().equals("bar"));

    //Get leaves (i.e. not their values)
    value=facades[2].get("/root/foo");
    assertTrue("Get on a multi-valued leaf",value instanceof Element[]);
    Element[] elements=(Element[])value;
    assertTrue(elements.length==2);

    //Leaf - multi-values.
    value=facades[2].get("/root/foo/bar");
    assertTrue("Get on a multi-valued leaf",value instanceof Object[]);
    Object[] data=(Object[])value;
    assertTrue(data.length==2); 
    assertEquals("leaf1",data[0]);
    assertEquals("leaf2",data[1]); 
  }

  public void testPut() {
    logTest("put()");
    String leafVal="testVal";
    String key;
    Object retrieved;
    //Leaf put()
    MapFacade facade=facades[0];
    log.info("Testing put() of a leaf value");
    key="/root/foo";
    facade.put(key, leafVal);
//    log.info(((Document)facade.getUnderlyingObject()).asXML());
    assertEquals("Returned leaf value does not match put() value",leafVal,facade.get(key));
    
    log.info("Testing put() of a new leaf Element");
    //Create a new leaf element
    key=key+"/bar";
    facade.put(key, null); //Null indicates just create the Element.
    retrieved=facade.get(key);
    assertEquals("Expected empty value",retrieved,"");
//    log.info(((Document)facade.getUnderlyingObject()).asXML());
      
    log.info("Testing put() of an existing Element (retrieved earlier)");
    facade.put(key, "MyVal");
    retrieved=facade.get(key);
    key="/root/another";
    facade.put(key, retrieved);
    assertNotNull(facade.get(key));
//    log.info(((Document)facade.getUnderlyingObject()).asXML());
   
    //Test that put returns the old value
    //Test that put doesn't retain any old stuff
  }
  
  public void testRemove() {
    logTest("remove()");
    //Remove Leaf
    //Remove Non-Leaf
    //
    MapFacade facade=facades[0];
    String key="/root/foo";
    Set keys=facade.keySet();
    Object expected=facade.get(key);
    Object data=facade.remove(key);
    Set remainingKeys=facade.keySet();
    assertEquals(expected,data);
    assertTrue("Should have less keys now",keys.size()>remainingKeys.size());
    
    assertFalse("Element should no longer exist",facade.containsKey(key));
    assertTrue("Element should no longer exist",facade.get(key)==null);
  }
  
  public void testPruneGraft() {
    logTest("prune/graft");
    MapFacade facade=facades[3];
//    log.info(((Document)facade.getUnderlyingObject()).asXML());
    String cutKey="/root/A";
    String pasteKey="/root/B";
    facade.put(pasteKey,facade.get(cutKey));
//    log.info(((Document)facade.getUnderlyingObject()).asXML());
 }
  
  public void testGetWithNamespace() {
    logTest("getNS");
    MapFacade facade=facades[4]; // This one has a default namespace set
    String defaultNSPrefix = ((DocumentMapFacade)facade).getDefaultNamespacePrefix();
    Object value = facade.get("/"+defaultNSPrefix+":root/"+defaultNSPrefix+":A/"+defaultNSPrefix+":AC/"+defaultNSPrefix+":AC1");
    assertEquals("ac1val", value); // With the namespace prefix we find the value
    Object nullValue = facade.get("/root/A/AC/AC1");
    assertEquals(null, nullValue); // Without the namespace prefix we don't
  }
   
}

