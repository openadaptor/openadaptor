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
package org.oa3.thirdparty.dom4j;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/collections/Dom4jSimpleRecordAccessor.java,v 1.12 2006/11/09 11:44:20 higginse
 * Exp $ Rev: $Revision: 1.12 $ Created Sep 18, 2006 by Eddy Higgins
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.oa3.auxil.simplerecord.ISimpleRecord;
import org.oa3.auxil.simplerecord.ISimpleRecordAccessor;
import org.oa3.core.exception.RecordException;

/**
 * Utility class to represent Dom4j <code>Document</code> instances as <code>ISimpleRecord</code> instances.
 * <p>
 * This class is an enabler to facilitate us in applying generic transformations to <code>Document</code> instances.
 * 
 * @author Eddy Higgins
 */
public class Dom4jSimpleRecordAccessor implements ISimpleRecordAccessor, ISimpleRecord {

  private static final Log log = LogFactory.getLog(Dom4jSimpleRecordAccessor.class);

  /**
   * This is the underlying object for the accessor.
   */
  private Document document;

  /**
   * this attribute specifies the attribute which contains the java type of the value. If omitted, java.lang.String is
   * assumed.
   */
  protected String valueTypeAttributeName = null;

  /**
   * This influences how getRecord() returns its underlying record. If true, it will return a dom4j
   * <code>Document</code> if one was originally supplied or an XML <code>String</code> if a <code>String</code>
   * was originally supplied. If <tt>false</tt>, it will always return a Dom4j <code>Document</code> object.
   * </p>
   * The default value is <tt>true</tt>, for defencive reasons (princible of least surprise I guess). Note that could
   * be inefficient in cases where further XML-related processing of the data happens downstream, and the incoming data
   * records XML Strings ,as they will be converted into dom4j, processed, and converted back to XML Strings for
   * compatibility.
   * 
   */
  protected boolean preserveIncomingXMLFormat = true;

  /**
   * Internal flag to remember incoming record format.
   */
  protected boolean incomingWasString = false;

  // BEGIN Accessors

  /**
   * If set, this attribute specifies the name of an attribute which may then (optionally) contains the java type to use
   * when returning an Element's value. If it is not set, or if if a given attribute does not have that named attribute,
   * then gets will return a <code>java.lang.String</code> representation
   * 
   * @return the name of the type attribute, or null.
   */
  public String getValueTypeAttributeName() {
    return valueTypeAttributeName;
  }

  /**
   * If set, this attribute specifies an attribute which may contains the java type to use for an Element's value.
   * <p>
   * If null, or the attribute is not set within an Element, <code>java.lang.String</code> is assumed.
   * 
   * @param valueTypeAttributeName
   *          is a String containing the attribute which specifies the type
   */
  public void setValueTypeAttributeName(String valueTypeAttributeName) {
    this.valueTypeAttributeName = valueTypeAttributeName;
  }

  /**
   * if <tt>true</tt> then calls to getRecord() will return a record in the same format as was used by
   * asSimplerecord(), otherwise it will return a Dom4j Document.
   * <p>
   * The default is <tt>true</tt>, to avoid confusion whereby an incoming XML <code>String</code> might end up as
   * an outgoing Dom4j <code>Document</code> unexpectedly.
   * 
   * @return Current value for this flag.
   */
  public boolean getPreserveIncomingXMLFormat() {
    return preserveIncomingXMLFormat;
  }

  /**
   * Sets preferred behaviour when converting Records. if set to <tt>true</tt> then calls to getRecord() will return a
   * record in the same format as was used by asSimplerecord(), otherwise it will return a Dom4j Document.
   * <p>
   * The default is true, to avoid confusion whereby an incoming XML <code>String</code> might end up as an outgoing
   * Dom4j <code>Document</code> unexpectedly. In practice, it might be better to set it to false, or to ensure that
   * asSimpleRecord() is provided with a Dom4j <code>Document</code>, for efficiency reasons. This is much more
   * likely to avoid implicit (and expensive) conversions to and from XML Strings inadvertently.
   * 
   * @param preserveIncomingXMLFormat
   *          Boolean flag indicating whether getRecord() should return the same record format as asSimpleRecord() got.
   */
  public void setPreserveIncomingXMLFormat(boolean preserveIncomingXMLFormat) {
    this.preserveIncomingXMLFormat = preserveIncomingXMLFormat;
  }

  // END Accessors
  /**
   * Default constructor.
   * <p>
   * In normal circumstances this should <em>only</em> be used by Spring.
   */
  public Dom4jSimpleRecordAccessor() {
  } // Default constructor is for bean use only.

  // BEGIN Implementation of ISimpleRecord

  /**
   * Retrieve a named value from the underlying Dom4j <code>Document</code>.
   * <p>
   * The key should have the form of a simplified XPath path. Note that array subscripts are <em>not</em> currently
   * supported.
   * </p>
   * The returned value will be the text of the element identified by the supplied path, unless the
   * <code>valueTypeAttributeName</code> property is set, and the selected element has this attribute. Then the type
   * of the returned value is governed by the value of the type attribute - currently one of <code>Double</code>,<code>Long</code>,<code>Date</code>
   * or <code>String</code>. If <code>valueTypeAttributeName</code> attribute is not set it defaults to
   * <code>String</code>.
   * 
   * @param key
   *          a simplified XPath path
   * @return The object associated with the supplied key
   * @throws RecordException
   *           if the operation cannot be completed.
   */
  public Object get(Object key) throws RecordException {
    Object value = null;
    if (document != null) {
      Node node = document.selectSingleNode(key.toString());
      if (node instanceof Element) {
        Element element = (Element) node;
        // If the element is a leaf, use element.getText(), otherwise use element.getName()
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
   * Store the suppplied object in the underlying Dom4j Document at the location defined by the supplied key.
   * <p>
   * Note - if the path does not exist, it will attempt to create it.
   * <p>
   * Currently the value stored simply uses the Object's toString() method. //ToDo: allow it to set the type attribute
   * also.
   * 
   * @param key
   *          an XPath like expression with the path.
   * @param value
   *          Object to be stored.
   * @return The Object which has just been stored.
   * @throws RecordException
   *           if the operation cannot be completed.
   */
  public Object put(Object key, Object value) throws RecordException {
    if (key == null) {
      log.warn("<null> key value is not permitted");
      throw new RecordException("<null> key value is not permitted");
    }

    String path = key.toString().trim();
    Node node = getNode(document, path);
    node.setText(value == null ? null : value.toString());

    return value;
  }

  /**
   * Remove and return an attribute from the document, given it's key.
   * <p>
   * If the node isn't found, then no action is taken.
   * 
   * @param key
   *          corresponding to the object being searched for.
   * @return The Object which has been removed, or null if not found.
   * @throws RecordException
   *           if the operation cannot be completed.
   */
  public Object remove(Object key) throws RecordException {
    Object value = null;
    Node node = document.selectSingleNode(key.toString());
    if (node instanceof Element) {
      Element element = (Element) node;
      value = Dom4jUtils.getTypedValue(element, valueTypeAttributeName, !element.elements().isEmpty());
      document.remove(node);
    } else { // Don't know how to proceed. //ToDo: Perhaps we should allow it anyway.
      throw new RecordException("selected node is not an Element instance: " + node.toString());
    }
    return value;
  }

  /**
   * Returns <tt>true</tt> if the underlying <code>Document</code> contains an element whose path is specified by
   * the provided key.
   * <p>
   * 
   * @param key
   *          An XPath-like String to identify an Element
   * @return <tt>true</tt> if the <code>Document</code> contains the named element.
   */
  public boolean containsKey(Object key) {
    Node node = document.selectSingleNode(key.toString());
    return (node != null);
  }

  /**
   * Shallow copy this accessor, but also the Dom4j Document it encapsulates.
   * 
   * @return Dom4jSimpleRecordAccessor with identical properties, and cloned <code>Document</code>.
   */
  public Object clone() {
    Dom4jSimpleRecordAccessor clone = new Dom4jSimpleRecordAccessor();
    clone.document = (Document) document.clone();
    clone.setValueTypeAttributeName(getValueTypeAttributeName());
    return clone;
  }

  /**
   * This returns the underlying Object that this class is fronting as an <code>ISimpleRecord</code>. It will return
   * an XML <code>String</code> if asSimpleRecord() was originally provided with one, and
   * <code>preserveIncomingXMLFormat</code> property is <tt>true</tt>. Otherwise it will return a Dom4j
   * <code>Document</code>
   * 
   * @return Dom4jDocument or String (as outlined above)
   */
  public Object getRecord() {
    if (preserveIncomingXMLFormat && incomingWasString) { // It was created from a String.
      return document.asXML();
    } else { // Return the dom4j Document
      return document;
    }
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

  // END Implementation of ISimpleRecord

  /**
   * Get a node from within a Document, given an XPath expression to locate it.
   * <p>
   * If the node doesn't exist, it will attempt to create the path to it.
   * 
   * @param document
   *          Dom4J Document to search in
   * @param xPath
   *          expression which should identify the element.
   * @return The Node corresponding to the supplied XPath string.
   * @throws RecordException
   *           if the operation proves impossible.
   */
  private static Node getNode(Document document, String xPath) throws RecordException {
    Node node = document.selectSingleNode(xPath);
    if (node == null) { // It doesn't exist. Have to walk down from root.

      if (xPath.startsWith("/") && (xPath.length() > 1)) { // Skip leading slash.
        xPath = xPath.substring(1);
      }
      String[] steps = xPath.split("/");
      int count = steps.length;
      if (count == 0) {
        throw new RecordException("Illegal path specified");
      }
      Element current = document.getRootElement();
      if (!current.getName().equals(steps[0])) {
        throw new RecordException("Document root element does not match path root");
      }
      for (int i = 1; i < steps.length; i++) {// Walk down.
        String currentName = steps[i];
        Element child = current.element(currentName);
        if (child == null) {
          current.addElement(currentName);
        }
        current = current.element(steps[i]);
      }
      node = current;
    }
    return node;
  }

  /**
   * Get an <code>ISimpleRecord</code>view on the supplied record Object.
   * <p>
   * This approximates the inverse of getRecord(), subject to the influence (of preserveIncomingXMLFormat)
   * 
   * @param record
   *          The object to be represented as an ISimpleRecord. Must be an XMLString or a Dom4J <code>Document</code>
   * @return ISimpleRecord view on the underlying XML Document.
   * @throws RecordException
   *           if the record cannot be represented as an ISimpleRecord
   */
  public ISimpleRecord asSimpleRecord(Object record) throws RecordException {
    Dom4jSimpleRecordAccessor sra = new Dom4jSimpleRecordAccessor();
    // Set flag to remind us what the incoming record looked like.
    incomingWasString = record instanceof String;
    sra.document = Dom4jUtils.getDocument(record);
    // Pass on the typeAttributeName setting.
    sra.setValueTypeAttributeName(getValueTypeAttributeName());
    return sra;
  }
}
