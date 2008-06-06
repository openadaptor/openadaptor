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
public class Dom4JDocumentMapFacadeTestCase extends AbstractTestMapFacade {
  private static final Log log = LogFactory.getLog(Dom4JDocumentMapFacadeTestCase.class);

  protected static final String[] XML= {
    "<root><foo>45</foo></root>",
    "<root><foo><bar>leaf</bar></foo></root>",
    "<root><foo><bar>leaf1</bar><bar>leaf2</bar></foo></root>",
    "<root><A><AB/><AC><AC1>ac1val</AC1></AC><AD>adval</AD></A><B><BA/></B></root>"
  };

  protected static final String SAMPLE_XML = 
    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
    "<bookstore>"+
    "<book>"+
    "  <title lang=\"eng\">Cosmic Banditos</title>\n"+
    "  <price>29.99</price>\n"+
    "</book>\n"+
    "<book>\n"+
    "  <title lang=\"eng\">Long Way down</title>\n"+
    "  <price>39.95</price>\n"+
    "</book>\n"+
    "</bookstore>\n";

  protected static final int[] KEY_COUNT= { 2,3,3,8};

  protected Document[] docs;
  protected MapFacade[] facades;
  protected Document testDoc;
  protected MapFacade testFacade;

  protected void setUp() throws Exception {
    testDoc=generateDocument(SAMPLE_XML);
    testFacade=new Dom4JDocumentMapFacade(testDoc);
    docs=new Document[XML.length];
    facades=new MapFacade[XML.length];
    for (int i=0;i<docs.length;i++) {
      docs[i]=generateDocument(XML[i]);
      facades[i]=new Dom4JDocumentMapFacade(docs[i]);
    }
    super.setUp();
  }

  protected MapFacade createInstance(){
    return testFacade;
  }

  protected static Document generateDocument(String text) {  
    try {
      return DocumentHelper.parseText(text);
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
//    log.info(((Document)facades[i].getUnderlyingObject()).asXML());
//    Iterator it=keys.iterator();
//    while (it.hasNext()) {
//    Object key=it.next();
//    log.info(key+"->"+facades[i].get(key));
//    }
      assertEquals("Incorrect number of keys in key set",keys.size(),KEY_COUNT[i]);
    }
  }

  public void testGet() {
    logTest("get()");
    //Test select of a text value
    //Longhand version ...
    assertEquals("Cosmic Banditos",testFacade.get("/bookstore/book[1]/title/child::text()"));
    //Shorthand version ...
    assertEquals("Cosmic Banditos",testFacade.get("/bookstore/book[1]/title/text()"));
    //Test select of an attribute value
    //Longhand version...
    assertEquals("eng",testFacade.get("/bookstore/book[2]/title[@lang]/attribute::lang"));
    //Shorthand version...
    assertEquals("eng",testFacade.get("/bookstore/book[2]/title/@lang"));
    //assertEquals("eng",testFacade.get("//@lang/text()"));    
    Object[] books=(Object[])testFacade.get("/bookstore/book");
    assertEquals(2,books.length);
  }

  private Element generateTestBook(String bookTitle,String bookLanguage,String bookPrice) {
    Element book=DocumentHelper.createElement("book");
    Element price=DocumentHelper.createElement("price");
    Element title=DocumentHelper.createElement("title");
    title.setText(bookTitle);
    if (bookLanguage!=null) {
      title.addAttribute("lang", bookLanguage);
    }
    if (bookPrice!=null) {
      price.setText(bookPrice);
    }
    book.add(title);
    book.add(price);
    return book;
  }
  
  public void testPut() {
    logTest("put()");
    Element testBook=generateTestBook("Test Title", "fr", null);
    assertEquals("eng",testFacade.get("/bookstore/book[1]/title/@lang"));
    testFacade.put("/bookstore/book[1]", testBook); //replace existing entry with this.
    assertEquals("eng",testFacade.get("/bookstore/book[1]/title/@lang"));
    //Object[] books=(Object[])testFacade.get("/bookstore/book");    
    //assertEquals(3,books.length);
    log.info(((Document)testFacade.getUnderlyingObject()).asXML());
  }
  
  public void testAttributePut() {
    logTest("put() [Attribute]");
    String newLanguage="fr";
    //Test modification of attribute value
    assertEquals("eng",testFacade.get("/bookstore/book[1]/title/@lang"));
    testFacade.put("/bookstore/book[1]/title/@lang", newLanguage);
    assertEquals(newLanguage,testFacade.get("/bookstore/book[1]/title/@lang"));
    //Test addition of new attribute, existing element
    testFacade.put("/bookstore/book[2]/title/@currency","EUR"); 
    assertEquals("EUR",testFacade.get("/bookstore/book[2]/title/@currency"));
    //Test modification of newly added attribute.
    testFacade.put("/bookstore/book[2]/title/@currency","GBP");
    assertEquals("GBP",testFacade.get("/bookstore/book[2]/title/@currency"));    
    //Test removal of attribute
    testFacade.put("/bookstore/book[2]/title/@currency", null);
    log.debug(((Document)testFacade.getUnderlyingObject()).asXML());
    assertNull(testFacade.get("/bookstore/book[2]/title/@currency"));
  }
  
  public void testElementPut() {
    logTest("put() [Element]");
    String leafVal="testVal";
    String key;
    Object retrieved;
    //Leaf put()
    MapFacade facade=facades[0];
    log.info("Testing put() of a leaf value");
    key="/root/foo/text()";
    facade.put(key, leafVal);
//  log.info(((Document)facade.getUnderlyingObject()).asXML());
    assertEquals("Returned leaf value does not match put() value",leafVal,facade.get(key));

    log.info("Testing put() of a new leaf Element");
    //Create a new leaf element
    key="/root/foo/bar";
    facade.put(key, ""); //Null indicates just create the Element.
    retrieved=facade.get(key);
    assertEquals("Expected empty element",((Element)retrieved).getName(),"bar");
//  log.info(((Document)facade.getUnderlyingObject()).asXML());

    log.info("Testing put() of an existing Element (retrieved earlier)");
    key+="/text()"; 
    facade.put(key, "MyVal");
    retrieved=facade.get(key);
    assertEquals("Retrieved value should match expected","MyVal",retrieved);
    key="/root/another";
    facade.put(key, retrieved);
    assertNotNull(facade.get(key));
  }


  public void testElementGet() {
    logTest("get()");
    //Leaf get()
    assertEquals("45",facades[0].get("/root/foo/text()"));

    //Non-leaf get() - Should return an Element (the leaf)
    Object value=facades[1].get("/root/foo/*");
    assertTrue("Get on a single-valued non-leaf",value instanceof Element);
    Element element=(Element)value;
    assertTrue("Expected value not returned",element.getName().equals("bar"));

    //Get leaves (i.e. not their values)
    value=facades[2].get("/root/foo/*");
    assertTrue("Get on a multi-valued leaf",value instanceof Object[]);
    Object[] values=(Object[])value;
    assertTrue("Expected 2 values",values.length==2);
    for (int i=0;i<values.length;i++) {
      assertTrue("Expected an element instance",values[i] instanceof Element);
    }

    //Leaf - multi-values.
    value=facades[2].get("/root/foo/bar/text()");
    assertTrue("Get on a multi-valued leaf",value instanceof Object[]);
    Object[] data=(Object[])value;
    assertTrue(data.length==2); 
    assertEquals("leaf1",data[0]);
    assertEquals("leaf2",data[1]); 
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
    String cutKey="/root/A";
    String pasteKey="/root/B/*";
    Object pruned=facade.remove(cutKey);   
    facade.put(pasteKey,pruned);
    assertEquals("Grafted should equal pruned",facade.get("/root/B/A"),pruned);
  }

}

