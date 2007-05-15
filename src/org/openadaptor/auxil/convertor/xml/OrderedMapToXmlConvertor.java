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

package org.openadaptor.auxil.convertor.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Converts {@link IOrderedMap}s to xml documents or xml strings
 * <p>
 * Note that Map keys containing forward slashes are not permitted as
 * Element tags in XML. These will be mapped according to the value
 * of mappedSlashValue. If this property hasn't been explicitly set,
 * a warning issues each time a conversion is made. If it is set, no
 * warning will issue.
 * The default value for mappedSlashValue is DEFAULT_SLASH_SUBSTITE
 * @author Eddy Higgins
 */
public class OrderedMapToXmlConvertor extends AbstractConvertor {
  private static final Log log = LogFactory.getLog(OrderedMapToXmlConvertor.class);

  public static final String DEFAULT_ROOT_ELEMENT_TAG = "root";
  //Forward slash is not allowed in a an element name
  public static final char FORWARD_SLASH='/';
  protected static final String FORWARD_SLASH_AS_STRING=new StringBuffer(FORWARD_SLASH).toString();

  public static final String DEFAULT_SLASH_SUBSTITUTE="_sl_";

  // name, if any for root element. See convert() for info on it's effect
  protected String rootElementTag = null;

  // if true (default) then return a String containing the Xml. Otherwise returns
  // an org.dom4j.Document.
  protected boolean returnXmlAsString = true;

  // TODO Decide if this is, in fact the best default.
  protected String encoding = EncodingAwareObject.ISO_8859_1; 

  protected String mappedSlashValue=DEFAULT_SLASH_SUBSTITUTE;
  private boolean slashMappedExplicitly=false;

  public OrderedMapToXmlConvertor() {
    super();
  }

  public OrderedMapToXmlConvertor(String id) {
    super(id);
  }

  /**
   * @return the name of the root element or null. Default to null
   */
  public String getRootElementTag() {
    return rootElementTag;
  }

  /**
   * @return true if a string will be returned. false if a DOM document. Defaults to true
   */
  public boolean getReturnXmlAsString() {
    return returnXmlAsString;
  }

  /**
   * @return the text encoding that will be used. Default is ISO-8859-1
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Sets the name of the root element
   * 
   * @param s
   */
  public void setRootElementTag(String s) {
    this.rootElementTag = s;
  }

  /**
   * Sets a flag to determine whether a Dom4J Document, or XML String is returned by this processor.
   * 
   * @param returnXmlAsString
   *          true to return a String, false to return a DOM document
   */
  public void setReturnXmlAsString(boolean returnXmlAsString) {
    this.returnXmlAsString = returnXmlAsString;
  }

  /**
   * Sets the text encoding to use.
   * 
   * @param s
   *          "UTF-8", "ISO-8859-1", "US-ASCII", etc.
   * 
   * @see EncodingAwareObject
   */
  public void setEncoding(String s) {
    this.encoding = s;
  }

  /**
   * Assign a substutionValue for slashes in element names.
   * <P>
   * Forward slashes are not permitted as part of an element name.
   * Dom4j Doesn't prevent them, but it could cause a problem when
   * subsequently parsing the converted XML
   * @param mappedSlashValue
   */
  public void setMappedSlashValue(String mappedSlashValue) {
    if ((mappedSlashValue==null)|| (mappedSlashValue.indexOf(FORWARD_SLASH_AS_STRING)>=0)) {
      log.warn("Cannot set mappedSlashValue to <null> or "+FORWARD_SLASH+". Ignored");
    } 
    else {
      this.mappedSlashValue=mappedSlashValue;
      slashMappedExplicitly=true;
    }
  }

  public String getMappedSlashValue() {
    return mappedSlashValue;
  }

  /**
   * Convert an OrderedMap into xml Representation. Depending on the returnXmlAsString Property it will return a String
   * representation of the Xml, or an org.dom4j.Document.
   * 
   * @param record
   *          IOrderedMap
   * 
   * @return String containing an XML representation of the supplied ordered map
   * 
   * @throws RecordException
   *           if the record is not an IOrderedMap
   */
  public Object convert(Object record) throws RecordException {
    if (!(record instanceof IOrderedMap)) {
      if (record instanceof Map) {
        record = new OrderedHashMap((Map)record);
      } else {
        throw new RecordFormatException("Record is not an IOrderedMap. Record: " + record);
      }
    }

    return convertOrderedMapToXml((IOrderedMap) record, returnXmlAsString);
  }

  /**
   * Performs the actual conversion. Will recursively add each element of the map as
   * 
   * @param map
   *          the map to be converted
   * @param returnAsString
   *          if true then the Dom4j Document is returned
   * 
   * @return the xml (or Dom4j Document) corresponding to the supplied OrderedMap
   * 
   * @throws RecordException
   *           if the conversion fails
   */
  private Object convertOrderedMapToXml(IOrderedMap map, boolean returnAsString) throws RecordException {
    Object result = null;

    // Create a Document to hold the data.
    Document doc = DocumentHelper.createDocument();
    if (encoding != null) {
      // Doesn't seem to have any effect here, so output formatter also sets it
      doc.setXMLEncoding(encoding);
      log.debug("Document encoding now " + doc.getXMLEncoding());
    }

    String rootTag = rootElementTag;

    if (rootTag != null) {
      log.debug("Using Supplied root tag - unset rootElementTag property to disable");
    } else { // Try and derive it. Must have a single entry, whose value is itself an OM.
      log.debug("rootElementTag property is not set. Deriving root tag from data.");
      if (map.size() == 1) { // Might be able to derive root tag.
        rootTag = (String) map.keys().get(0);
        Object value = map.get(rootTag);
        if (value instanceof IOrderedMap) { // Bingo we're in.
          log.debug("Deriving rootElementTag property from map (set rootElementTag property explicitly to prevent this");
          map = (IOrderedMap) value; // Move down a level as we're adding it here.
        } else {// No go -be safe and add our own root.
          log.warn("Failed to derive root tag. Using default of "
              + OrderedMapToXmlConvertor.DEFAULT_ROOT_ELEMENT_TAG);
          rootTag = OrderedMapToXmlConvertor.DEFAULT_ROOT_ELEMENT_TAG;
        }
      } else {// More than one top level entry. Give up and default.
        log.warn("Top level has more than one entry. Using default of "
            + OrderedMapToXmlConvertor.DEFAULT_ROOT_ELEMENT_TAG);
        rootTag = OrderedMapToXmlConvertor.DEFAULT_ROOT_ELEMENT_TAG;
      }
    }

    log.debug("Document root tag will be: " + rootTag);

    // Prime the root tag.
    Element root = doc.addElement(rootTag);

    Iterator it = map.keys().iterator();
    while (it.hasNext()) {
      String key = it.next().toString();
      Object value = map.get(key);
      addElement(root, key, value);
    }
    // document done. Phew.
    if (returnAsString) { // Darn, need to output the Document as a String.
      StringWriter sw = new StringWriter();
      OutputFormat outputFormat = OutputFormat.createCompactFormat();
      if (encoding != null) {
        log.debug("Output Format encoding as " + encoding);
        outputFormat.setEncoding(encoding); // This definitely sets it in the header!
      }
//    outputFormat.setOmitEncoding(true);
//    outputFormat.setSuppressDeclaration(true);
      XMLWriter writer = new XMLWriter(sw, outputFormat);
      try {
        writer.write(doc);
      } catch (IOException ioe) {
        log.warn("Failed to write the XML as a String");
        throw new RecordFormatException("Failed to write the XML as a String. Reason: " + ioe.toString(), ioe);
      }
      result = sw.toString();
    } else {
      result = doc;
    }
    return result;
  }

  /**
   * Add a child Element to a parent. If the element is an array, then Add each of them as a separate Element.
   * 
   * @param parent
   *          Element to add the child(ren) to.
   * @param name
   *          Child(ren)'s element name
   * @param value
   *          value(s) to add
   */
  private void addElement(Element parent, String name, Object value) {
    // Need to create multiple elements. The joys of recursion.
    if (value instanceof Object[]) {
      Object[] values = (Object[]) value;
      for (int i = 0; i < values.length; i++)
        addElement(parent, name, values[i]);
    }
    // Only one element to add
    else {
      Element child = parent.addElement(generateElementName(name));
      if (value instanceof IOrderedMap) {
        IOrderedMap map = (IOrderedMap) value;
        Iterator it = map.keys().iterator();
        while (it.hasNext()) {
          String key = (String) it.next();
          addElement(child, key, map.get(key));
        }
      } else {
        if (value == null)
          child.addText("");
        else
          child.addText(value.toString());
      }
    }
  }

  /**
   * Convert supplied name to legal Element Name.
   * @param key
   * @return
   */
  private String generateElementName(String key) {
    String result=key;
    if (key!=null && (key.indexOf(FORWARD_SLASH_AS_STRING)>=0) ){
      char[] chars=key.toCharArray();
      StringBuffer sb=new StringBuffer();
      for (int i=0;i<chars.length;i++){
        char ch=chars[i];
        if (ch==FORWARD_SLASH) {
          sb.append(mappedSlashValue);
        }
        else {
          sb.append(ch);
        }
      }
      result= sb.toString();
      if (!slashMappedExplicitly) {
        log.warn("Implict conversion of Element name from "+key+" -> "+result);
      }
    }
    return result;
  }
}