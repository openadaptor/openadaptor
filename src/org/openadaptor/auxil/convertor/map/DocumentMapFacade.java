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
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.thirdparty.dom4j.Dom4jUtils;

/**
 * Utility class to represent Dom4j <code>Document</code> instances as <code>Map</code> instances.
 * <p>
 * This class is an enabler to facilitate us in applying generic transformations to <code>Document</code> instances.
 *
 * 
 * <UL>
 * <LI> In general, <code>put(key,get(key))</code> should result in a similar underlying Document.
 * </UL>
 *
 * <B>Notes</B>
 * <OL>
 *   <LI>The conflict between a Map an a Document which may have multiple identically named ('keyed') Elements is
 *       handled by representing such Elements as Element[] within the Map. This is likely to have implications on 
 *       subsequent ordering of Elements within the Document.
 *   <LI>Non-leaf Elements <i>cannot</i> have their text values extracted via get()
 *   <LI>The XPath expressions used are expected to be relatively simple.
 * </OL>
 * 
 * @author Eddy Higgins
 * @since Introduced post 3.2.1
 */
public class DocumentMapFacade implements MapFacade {
  protected static final Log log = LogFactory.getLog(DocumentMapFacade.class);

  private static final String CLASS_NAME=DocumentMapFacade.class.getName();
  /**
   * This is the underlying object for the accessor.
   */
  private Document document;

  /**
   * this attribute specifies the attribute which contains the java type of the
   * value. If omitted, java.lang.String is assumed.
   */
  protected String valueTypeAttributeName = null;

  //protected boolean multiValuedAttributeSupport=true;

  //BEGIN Map implementation


  //BEGIN Accessors

  /**
   * If set, this attribute specifies the name of an attribute which may then (optionally)
   * contains the java type to use when returning an Element's value. If it is not set, or if
   * if a given attribute does not have that named attribute, then gets will return
   * a <code>java.lang.String</code> representation
   * @return the name of the type attribute, or null.
   */
  public String getValueTypeAttributeName() {
    return valueTypeAttributeName;
  }

  /**
   * If set, this attribute specifies an attribute which may contains the java type to use for
   * an Element's value.
   * <p>
   * If null, or the attribute is not set within an Element, <code>java.lang.String</code> is assumed.
   * @param valueTypeAttributeName is a String containing the attribute which specifies the type
   */
  public void setValueTypeAttributeName(String valueTypeAttributeName) {
    this.valueTypeAttributeName = valueTypeAttributeName;
  }

  //END   Accessors
  /**
   * Default constructor.
   * <p>
   * In normal circumstances this should <em>only</em> be
   * used by Spring.
   */
  public DocumentMapFacade(Document document) {
    if (document==null) {
      throw new IllegalArgumentException("Document may not be null");
    }
    this.document=document;
  } 

  /**
   * Retrieve a named value from the underlying Dom4j <code>Document</code>.
   * <p>
   * The key should have the form of a simplified XPath path.
   * Note that array subscripts are <em>not</em> currently supported.
   * </p>
   * The returned value will be the text of the element identified
   * by the supplied path, unless the <code>valueTypeAttributeName</code> property
   * is set, and the selected element has this attribute. Then
   * the type of the returned value is governed by the value of the
   * type attribute - currently one of <code>Double</code>,<code>Long</code>,<code>Date</code> or <code>String</code>.
   * If <code>valueTypeAttributeName</code> attribute is not set it defaults to <code>String</code>.
   *
   * @param key a simplified XPath path
   * @return The object associated with the supplied key
   * @throws RecordException if the operation cannot be completed.
   */
  public Object oldGet(Object key) throws RecordException {
    Object value = null;
    if (document != null) {
      Node node = document.selectSingleNode(key.toString());
      if (node instanceof Element) {
        Element element = (Element) node;
        //If the element is a leaf, use element.getText(), otherwise use element.getName()
        value = Dom4jUtils.getTypedValue(element, valueTypeAttributeName, !element.elements().isEmpty());
      } else {
        if (valueTypeAttributeName != null) {
          log.warn("Cannot get type attribute of non-element node");
          value = node.getText();
        }
      }
    }
    return (value);
  }

  /**
   * Return the value corresponding to the supplied key (XPATH) expression.
   * 
   * <P>get(Object key)
   * <UL>
   *  <LI> key should be a String which contains an XPATH expression identifying an element within the document.
   *       It may identify multiple similarly named elements (children with the same name).
   *  <LI> on a non-existent element should return null.
   *  <LI> on a leaf should return the (possibly empty) text value of that leaf, never null. If there are multiple
   *       children with the same name, then an Object[] of their values is returned.
   *  <LI> on a non-leaf element will return either an Object[] or an Object, depending
   *       on the number of children the non-leaf element contains. In practice, this is likely to be 
   *       an Element[] or Element instance.
   * </UL>
   * 
   * Note: Behaviour is undefined if a mix of Leaf and non-leaf Elements are identified by the Key.
   * <P>
   * @throws NullPointerException if supplied key is null.
   * @throws InvalidXPathExpression if incorrect XPath is supplied.
   */
  public Object get(Object key) throws RecordException {
    Object value = null;
    if (document != null) {
      //Note that this may throw a NPE, which is consistent with Map.get(null)
      List nodes=document.selectNodes(key.toString());
      List values=new ArrayList(nodes.size());
      Iterator it=nodes.iterator();
      while (it.hasNext()) {
        Node node=(Node)it.next();
        if (node instanceof Element) {
          Element element = (Element) node;
          if (isLeaf(element)) {
            //values.add(element.getText());  
            values.add(Dom4jUtils.getTypedValue(element, valueTypeAttributeName, false));
          }
          else {
            List children=element.elements();
            int count=children.size();
            switch(count) {
            case 0: //Leave it at null;
              break;
            case 1:
              values.add(children.get(0)); //Single element
              break;
            default: //Convert multivalued to array for Mappification.
              values.add(children.toArray(new Element[count]));
            break;
            }
          }
        }
      }
      switch (values.size()) {
      case 0: //Leave it at null;
        break;
      case 1:
        value= values.get(0); //Return object
        break;
      default: //Convert multivalued to array for Mappification.
        value= values.toArray(new Object[values.size()]);
      break;
      }
    }
    return value;
  }

  private boolean isLeaf(Element element) {
    return element.elements().isEmpty();
  }

  /**
   * Store the suppplied object in the underlying Dom4j Document at the location defined by the supplied (XPATH) key.     
   * <BR>
   * Note - if the path does not exist, it will attempt to create it.
   * <P>
   * <B>Key</B>
   * <UL>
   *  <LI> key should be an Object which contains an XPATH expression identifying a single element within the document,
   *       or a valid path to an element which will be created.
   *  <LI> Any corresponding existing Element will be overwritten.
   * </UL>
   * Value
   * <UL>
   *  <LI> If a null value is supplied, it indicates that an Element (as opposed to an attribute value) is to be
   *       stored at the location defined by the key. Any existing value is lost.
   *  <LI> If an Element or Element[] is provided, then these will be attached as children of the identified Element.
   *  <LI> If an Object[] is provided, then multiple peer copies of the identified element will be constructed,
   *       each containing one of the values from the array
   *  <LI> If an Object is provided, then the selected Element will have it's text value set to the result of toString() on
   *       the object. If the value object is null, then "" will be substituted.    
   * </UL>
   * <B>Notes:</B>
   *  If an Element (or Element[]) is supplied as a value, and it already has a parent, the put() operation will first
   *  sever the link to the old parent, before attaching it to the identified target. This might happen for example,
   *  in a simple prune/graft operation such as:<pre>
   *       docMap.put(newXpath,docMap.get(oldXpath));
   *  </pre>
   *  
   * @param key an XPath expression identifying a location within the Document
   * @param value Object to be stored. Treated as null,Element[], Element, Object[] or Object in turn.
   * @return The previous value if any at that key.
   * @throws NullPointerException if supplied key is null.
   * @throws InvalidXPathExpression if illegal XPath is supplied.
   */

  
  public Object put(Object key, Object value) throws RecordException {
    if (key == null) {
      log.warn("<null> key value is not permitted");
      throw new RecordException("<null> key value is not permitted");
    }
    String path = key.toString().trim();
    //Remove old value, if any.
    Object oldValue=remove(key);

    Node node = getNode(document, path);
    if (!(node instanceof Element)) {
      throw new IllegalArgumentException("Node is not an Element - cannot put()");
    }
    Element element=(Element)node;
    if (value!=null) { //If null, we already have created the child!)
      if (value instanceof Element) { //Attach the value as a child element.
        add(element,new Element[] {(Element)value});
      } 
      else if (value instanceof Element[]) {//Attach each as a child
        add(element,(Element[])value);
      } 
      else if (value instanceof Object[]) { //Make n copies of element with supplied text values
        add(element,(Object[])value);
      } 
      else {
        add(element,new Object[] {value}); //Set text value to supplied.
      }
    }
    return oldValue; 
  }

  /**
   * Add child elements to an existing element.
   * <p>
   * Forces reparenting if necessary.
   * @param element - element to which the supplied ones are to be attached.
   * @param elements - Array of elements to attach as children.
   */
  private void add(Element element,Element[] elements) {
    for (int i=0;i<elements.length;i++) {
      Element current=elements[i];
      Element parent=current.getParent();
      if (parent!=null) {
        log.debug("Reparent node from "+parent.getPath());
        parent.remove(current);
      }
      element.add(current);
    }
  }
  /**
   * Set the text value of an Element (possible creating multiple peers).
   * <p>
   * It works as follows:<BR>
   * <OL>
   *   <LI>The supplied element will have it's text value set to the first value
   *   in the supplied Object array.
   *   <LI>For each remaining value in the array, an additional (peer) Element is
   *       created from the original element's parent, with a corresponding
   *       text value.
   * </OL>
   * <BR>
   * This will use the toString() method on each of the supplied objects
   * to set the text value of a corresponding number of Elements in the
   * tree. The empty String ("") is substituted for null values.
   * 
   * @param element - Element which is to have it's text value set
   * @param objects - Array of objects containing values.
   */
  private void add(Element element,Object[] objects) {
    if (objects.length>0) { //Set the first one. Easy.
      element.setText(valueAsNonNullString(objects[0]));
    }
    if (objects.length>1) { //Need to add extra elements for remainder.
      Element parent=element.getParent();
      String name=element.getName();
      for (int i=1;i<objects.length;i++) {
        parent.addElement(name).setText(valueAsNonNullString(objects[i]));
        //Note- could also have done clone() on the Element, but so far we know that it has no other attribs, so didn't bother.
        //Element next=(Element)element.clone(); //Might not be fastest. Could also remember the name, and create a new
        //element.setText(valueAsNonNullString(objects[i]));
        //parent.add(next);
      }
    }
  }

  /**
   * Remove and return an Element or value from the document, given its key.
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

//  public Object removeOld(Object key) throws RecordException {
//    if (key==null) {
//      throw new IllegalArgumentException("null is not a legal argument for remove()");
//    }
//    Object value = get(key); //Retrieve it all.
//    List nodeList=document.selectNodes(key.toString());
//    Iterator it=nodeList.iterator();
//    while (it.hasNext()) {
//      Node node=(Node)it.next();
//      node.getParent().remove(node);
//    }
//    if (nodeList.size()>0) {
//      List values=new ArrayList();
//      for (Iterator it=nodeList.iterator();it.hasNext();) {
//        Node node=(Node)it.next();
//        if (node instanceof Element) {
//          Element element = (Element) node;
//          values.add(Dom4jUtils.getTypedValue(element, valueTypeAttributeName, !element.elements().isEmpty()));
//
//          // remove only works if the node is a child, otherwise we need to use
//          // the detatch() call
//          //            document.remove(node);
//          node.detach();
//        } else { //Don't know how to proceed. //ToDo: Perhaps we should allow it anyway.
//          log.warn("elected node is not an Element instance, ignoring: " + node.toString());
//        }
//      }
//      value=values.toArray(new Object[values.size()]);
//    }
//
//    return value;
//  }


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
   * Get a node from within a Document, given an XPath expression to locate it.
   * <p/>
   *
   * If the node doesn't exist, it will attempt to create the path to it.
   * <p/>
   *
   * If the XPath supplied is just the node name (ie. no path elements) then the
   * root element of the document is assumed to be the path. The node will either
   * be found or created here.
   *
   * @param document Dom4J Document to search in
   * @param xPath expression which should identify the element.
   *
   * @return The Node corresponding to the supplied XPath string.
   *
   * @throws RecordException if the operation proves impossible or if either the
   * XPath supplied was null (or zero length) or the document to search was null
   */
  private Node getNode(Document document, String xPath) throws RecordException {
    if (xPath == null || xPath.length() == 0)
      throw new RecordException("Null or zero length XPath string passed. Cannot find node");

    if (document == null)
      throw new RecordException("Null document passed. Cannot find node");

    Node node = document.selectSingleNode(xPath);

    if (node == null) { //It doesn't exist. Have to walk down from root.

      // if the user "just" supplied an attribute name then we look for it
      // under the document root and if not found then create it
      if (xPath.indexOf("/") == -1) {
        Element root = document.getRootElement();
        Element child = root.element(xPath);

        return (child == null) ? root.addElement(xPath) : child;
      }

      // skip leading slash.
      if (xPath.startsWith("/"))
        xPath = xPath.substring(1);

      String[] steps = xPath.split("/");
      int count = steps.length;
      if (count == 0)
        throw new RecordException("Illegal path specified");

      Element current = document.getRootElement();
      if (!current.getName().equals(steps[0]))
        throw new RecordException("Document root element does not match path root");

      for (int i = 1; i < steps.length; i++) {//Walk down.
        String currentName = steps[i];
        Element child = current.element(currentName);
        if (child == null)
          current.addElement(currentName);

        current = current.element(steps[i]);
      }

      node = current;
    }

    return node;
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
    DocumentMapFacade clone = new DocumentMapFacade((Document)document.clone());
    clone.setValueTypeAttributeName(getValueTypeAttributeName());
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

  private final String valueAsNonNullString(Object o) {
    return o == null ? "" : o.toString(); 
  }

}
