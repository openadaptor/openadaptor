/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.convertor.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.util.XmlUtils;

/**
 * Utility class to represent Dom4j <code>Document</code> instances as <code>Map</code> instances.
 * <p>
 * This Facade allows Dom4j Documensts to be treated (within reason) as Maps, using XPath Strings
 * as keys into the Document Elements,Attributes & Values.
 * 
 * <UL>
 * <LI> In general, <code>put(key,get(key))</code> should result in a similar underlying Document.
 * </UL>
 *
 * <B>Notes</B>
 * <OL>
 *   <LI>The XPath expressions used are expected to be relatively simple, particularly when using
 *       put(key,value) - XPath is really intended for retrieval only!
 * </OL>
 * 
 * @author Eddy Higgins
 * @since Introduced post 3.2.1
 */
public class Dom4JDocumentMapFacade implements MapFacade {
  protected static final Log log = LogFactory.getLog(Dom4JDocumentMapFacade.class);

  private static final String CLASS_NAME=Dom4JDocumentMapFacade.class.getName();
  /**
   * This is the underlying object which this Facade fronts.
   */
  private Document document;

  //BEGIN Map implementation

  //BEGIN Accessors

  //END   Accessors
  /**
   * Create a new Facade for an existing Document.
   */
  public Dom4JDocumentMapFacade(Document document) {
    if (document==null) {
      throw new IllegalArgumentException("Document may not be null");
    }
    this.document=document;
  } 


  /**
   * Return the value corresponding to the supplied key (XPATH) expression.
   * 
   * <P>get(Object key)
   * <UL>
   *  <LI> key should be a String which contains an XPATH expression identifying zero or more Nodes.     
   *  <LI> If no Node is selected it will return null.
   *  <LI> For each Element selected, a reference to the Element will be returned
   *  <LI> For attribute, and text selections, a String representation of those Nodes will be
   *       returned;
   *  <LI> If more than one value is selected, an array will be returned.
   * </UL>
   * Note: This is really only intended for relatively simple XPath queries.
   * <P>
   * @throws NullPointerException if supplied key is null.
   * @throws InvalidXPathExpression if incorrect XPath is supplied.
   */
  public Object get(Object key) throws RecordException {
    Object result=null;
    if (document!=null) {
      //Note that this may throw a NPE, which is consistent with Map.get(null)
      List nodes=document.selectNodes(key.toString()); 
      if (log.isDebugEnabled()) {
        log.debug(nodes.size()+" Nodes were selected for key: "+key);
      }
      switch(nodes.size()) {
      case 0: //No action. Result is already null
        break;
      case 1: //Single result to return.
        result=getSingleValuedResult((Node)nodes.get(0));
        break;
      default:
        Object[] values=new Object[nodes.size()];
      for (int i=0;i<values.length;i++) {
        values[i]=getSingleValuedResult((Node)nodes.get(i));
      }
      result=values;
      break;
      }
    }
    return result;
  }

  /** 
   * Extract value from a single node
   * <br>
   * <UL>
   *  <LI>If an Element return the element reference.
   *  <LI>If an Attribute return the attribute value
   *  <LI>If text (or CDATA) return it.
   *  <LI>If null return null.
   * @param node Node from which to extract a value
   * @return Reference to Element, or String representation of value.
   */
  private Object getSingleValuedResult(Node node) {
    Object result=null;
    switch(node.getNodeType()) {
    case Node.ELEMENT_NODE:
      result=(Element)node;
      break;
    case Node.ATTRIBUTE_NODE:
      result=((Attribute)node).getValue();
      break;
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE: //Not sure about this one!
      result=node.getText();
      break;
    }
    return result;
  }

  /**
   * Store a value using the supplied XPath as a key.
   * key *must* correspond to at most one Node.
   * put(key,null) on an existing key will remove it.
   * put(key,value) on an existing key will replace the existing value with the new one.
   */
  public Object put(Object key, Object value) throws RecordException {
    Object oldValue=null;
    if (key == null) {
      log.warn("<null> key value is not permitted");
      throw new RecordException("<null> key value is not permitted");
    }
    String path = key.toString().trim();
    List matches=document.selectNodes(path);
    if (matches.isEmpty()) {//See if we can create it!
      log.debug("key "+key+" does not match any existing Node. Attempting to create path to it.");
      Node node=XmlUtils.create(path,document);   
      if (node!=null) {
        matches=new ArrayList();
        matches.add(node);
      }
      //log.debug(node.asXML());
    }
    switch(matches.size()) {
    case 0: //Nothing selected (or created). Have to give up
      throw new RecordException("Failed to retrieve, or create entry for key "+key);
      //break;
    case 1: //We have something to go on.
      oldValue=modifyExisting((Node)matches.get(0),value);
      break;
    default:
      throw new RecordException(matches.size()+"Nodes match key "+path+". At most one Node should match");
    //break;
    }
    return oldValue;
  }

  private Object modifyExisting(Node existing,Object value) {
    Object old=null;
    if (value instanceof Element) {
      old=modify(existing,(Element)value);
    } 
    else if (value instanceof Attribute) {
      old=modify(existing,(Attribute)value);
    }
    else if (value !=null) {
      old=modify(existing,value.toString());
    }
    else {
      old=existing;
      existing.getParent().remove(existing);
    }
    return old;
  }

  private Object modify(Node node,Element element) {
    Object old=null;
    if (node instanceof Element) {
      old=replace((Element)node,element);
    }
    else {
      throw new RecordException("Target of put(x,Element) must be empty or an existing Element. Got: "+node);
    }
    return old;
  }

  private Object modify(Node node,Attribute attribute) {
    Object old=null;
    if (node instanceof Element) { //Set or replace value of attribute on Element
      Element element=(Element)node;
      String attrName=attribute.getName();
      Attribute existing=element.attribute(attrName);
      if (existing!=null) {
        element.remove(existing);
        old=existing;
      }
      element.add(attribute);
    }
    else  if (node instanceof Attribute) { //Modify the value of the existing attribute.
      Attribute existing=(Attribute)node;
      old=DocumentHelper.createAttribute(null, existing.getName(), existing.getValue()); 
      existing.setValue(attribute.getValue());
    }
    else {
      throw new RecordException("Target of put(x,Attribute) must be empty or an existing Element or Attribute. Got: "+node);
    }
    return old;
  }

  private Object modify(Node node,String value) {
    Object old=null;
    if (node instanceof Element) { //Set the text of the element
      Element element=(Element)node;
      old=element.getText();
      element.setText(value);
    }
    else if (node instanceof Attribute) {//Test the value of the attribute
      Attribute attribute=(Attribute)node;
      old=attribute.getValue();
      attribute.setValue(value);
    }
    else if (node instanceof Text) {
      Text text=(Text)node;
      old=node.getText();
      text.setText(value);
    }
    return old;
  }

  private Object replace(Element oldElement,Element newElement) {
    Element parent=oldElement.getParent();
    parent.remove(oldElement);
    parent.add(newElement);
    return oldElement;
  }


  /**
   * Remove and return Nodes from the document, given an XPath key.
   * <p>
   * If the node isn't found, then no action is taken.
   * @param key corresponding to the object being searched for.
   * @return The Object which has been removed, or null if not found.
   * @throws RecordException if the operation cannot be completed.
   */
  public Object remove(Object key) throws RecordException {
    if (key==null) {
      throw new IllegalArgumentException("null is not a legal argument for remove()");
    }
    Object value = get(key); //Retrieve it all.
    List nodeList=document.selectNodes(key.toString());
    Iterator it=nodeList.iterator();
    while (it.hasNext()) {
      Node node=(Node)it.next();
      node.getParent().remove(node);
    }
    return value;
  }

  /**
   *  Returns <tt>true</tt> if the underlying <code>Document</code> contains an element whose path
   *  is specified by the provided key.
   * <p>
   * @param key An XPath-like String to identify an Element
   * @return <tt>true</tt> if the <code>Document</code> contains the named element.
   */
  public boolean containsKey(Object key) {
    Node node = document.selectSingleNode(key.toString());
    return (node != null);
  }


  /**
   * Remove all document except the root Element itself, if any.
   */
  public void clear() {
    // Get rid of everything below the root. (Assuming that there is a root)
    if (document.getRootElement() != null) {
      document.getRootElement().clearContent();
    }
  }

  /**
   * Returns a String representation of the backing Document XML.
   */
  public String toString() {
    return document.asXML();
  }

  //BEGIN Map implementaion
  //Todo: Fill out the missing methods here!!!
  public boolean containsValue(Object arg0) {
    throw new UnsupportedOperationException(CLASS_NAME+" does not support containsValue()");
  }
  public Set entrySet() {
    throw new UnsupportedOperationException(CLASS_NAME+" does not support entrySet()");
  }
  public boolean isEmpty() {
    throw new UnsupportedOperationException(CLASS_NAME+" does not support isEmpty()");
  }

  /**
   * This returns a depth-first list of XPATH expressions identifying the entire Element tree
   * in the backing Document.
   */
  public Set keySet() {
    return generateKeySet(document.getRootElement());
    //throw new UnsupportedOperationException(CLASS_NAME+" does not support keySet()");
  }
  public void putAll(Map arg0) {
    throw new UnsupportedOperationException(CLASS_NAME+" does not support putAll()");
  }
  public int size() {
    throw new UnsupportedOperationException(CLASS_NAME+" does not support size()");
  }
  public Collection values() {
    throw new UnsupportedOperationException(CLASS_NAME+" does not support values()");
  }
  //END Map implementation


  //BEGIN MapFacade implementation

  /**
   * This returns the underlying Object that this class is fronting as
   * a <code>Map</code>.
   * It will return a Dom4j <code>Document</code>
   *
   * @return Dom4jDocument or String (as outlined above)
   */
  public Object getUnderlyingObject() {
    return document;
  }

  /**
   * Clone the underlying the Dom4j Document, and any facade properties it may have.
   * @return DocumentMapFacade with identical properties, and cloned <code>Document</code>.
   */
  public Object clone() {
    Dom4JDocumentMapFacade clone = new Dom4JDocumentMapFacade((Document)document.clone());
    return clone;
  }


  //END MapFacade implementation

  /**
   * Generate the XPATH representation for the current Element,
   * and all of it's child Elements as a set of keys.
   */
  private Set generateKeySet(Element element) {
    Set keySet=new HashSet();
    //Add the element.
    keySet.add(element.getPath());
    //Now recursively add all it's children
    Iterator it =element.elements().iterator();
    while (it.hasNext()) {
      keySet.addAll(generateKeySet((Element)it.next()));
    }     
    return keySet;
  }

}
