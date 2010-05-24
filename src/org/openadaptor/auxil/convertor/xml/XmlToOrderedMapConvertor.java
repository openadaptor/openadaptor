/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.thirdparty.dom4j.Dom4jUtils;

/**
 * This class may be used to convert XML Documents (dom4j/XML Strings) into OrderedMaps. <p/>
 * 
 * It achieves this by selecting the Elements of interest via an optional XPATH query and converts each Element
 * recursively into Ordered Maps. <p/>
 * 
 * If no XPATH query is specified, the entire document is converted, resulting in a single ordered map, with a single
 * entry whose name is the root tag, and whose value is the converted data.
 * 
 * @author Eddy Higgins
 */
public class XmlToOrderedMapConvertor extends AbstractConvertor {

  private static final Log log = LogFactory.getLog(XmlToOrderedMapConvertor.class);

  protected String xpathExpression;

  protected String valueTypeAttributeName;

  boolean omitTopLevelElementTag = false;

  protected boolean throwRecordExceptionOnBadSelection = false;

  protected String storeXMLInAttributeName;

  private String origXML;

  /**
   * Sets the XPath expression to be used to select elements to convert
   *
   * @param xpathExpression standard Xpath expression
   */
  public void setExpression(String xpathExpression) {
    this.xpathExpression = xpathExpression;
  }

  /**
   * @return the XPath expression to be used to select elements to convert
   */
  public String getExpression() {
    return xpathExpression;
  }

  /**
   * @return the name of the attribute in elements that is used to determine the
   * data type of the element value. Default to null which means a String type.
   */
  public String getValueTypeAttributeName() {
    return valueTypeAttributeName;
  }

  /**
   * Sets the name of the attribute in elements that is used to determine the data
   * type of the element value. Default to null which means a String type.
   *
   * @param valueTypeAttributeName
   */
  public void setValueTypeAttributeName(String valueTypeAttributeName) {
    this.valueTypeAttributeName = valueTypeAttributeName;
  }

  /**
   * If true, this means that each selected element will be converted into a single
   * ordered map, with exactly one entry - the element tag as name, and the value
   * is an ordered map with the elements under the root etc.
   * <p/>
   *
   * If false, then an ordered map will be returned containing all of the chidren.
   * The default value is false (- this is to avoid losing data unexpectedly)
   *
   * @param omit true to return a single ordered map
   */
  public void setOmitTopLevelElementTag(boolean omit) {
    this.omitTopLevelElementTag = omit;
  }

  /**
   * @return true if the processed selection is wrapped inside an attribute naamed
   * as the root node name.
   */
  public boolean getOmitTopLevelElementTag() {
    return omitTopLevelElementTag;
  }

  /**
   * Toggle whether convertor will throw an exception if the XPath Selection
   * fails to select Nodes. Default is false.
   *
   * @param throwRecordExceptionOnBadSelection
   */
  public void setThrowRecordExceptionOnBadSelection(boolean throwRecordExceptionOnBadSelection) {
    this.throwRecordExceptionOnBadSelection = throwRecordExceptionOnBadSelection;
  }

  /**
   * @return true if an exception is to be thrown when an XPath selection fails.
   */
  public boolean getThrowRecordExceptionOnBadSelection() {
    return throwRecordExceptionOnBadSelection;
  }

  /**
   * @return the name of the attribute used to store the incoming XML in or null.
   */
  public String getStoreXMLInAttributeName() {
    return storeXMLInAttributeName;
  }

  /**
   * If set then an attribute (as per the supplied name) will be added to the root of the outgoing
   * OrderedMap. It will contain the string representation of the incoming XML. Defaults to null
   * which means that the XML is NOT be added to the OrderedMap.
   * <p/>
   *
   * Warning: no effort is made to check if the attribute exists already. That is left for the user
   * to arrange.
   *
   * @param storeXMLInAttributeName the name of the attribute to create
   */
  public void setStoreXMLInAttributeName(String storeXMLInAttributeName) {
    this.storeXMLInAttributeName = storeXMLInAttributeName;
  }

  /**
   * Converts the supplied DOM document or XML String into an ordered map(s). If the <code>
   * xpathExpression</code> property is set then this is used to select the elements to be
   * converted. Otherwise the entire document is used.
   *
   * @param record containing an XML Document (dom4j) or a String containing an XML Document
   *
   * @return Data from the document, converted into OrderedMap, or OrderedMap[] or null
   *
   * @throws RecordException if conversion fails
   *
   * @throws RecordFormatException if the record is not a String or a Document
   *
   * @see Dom4jUtils
   */
  protected Object convert(Object record) throws RecordException {
    if (record == null)
      return null;

    if (!(record instanceof String) && !(record instanceof Document))
      throw new RecordFormatException("Unsupported record passed of type [" + record.getClass().getName() + "]. "
          + "Can only process XML Strings or DOM Documents");

    // we store the incoming XML so that we can insert it into the outgoing ordered maps
    // if necessary. We do this here before the XPath code gets a chance to run and prune
    // the XML. Used in the convertElementsToMaps() call.
    origXML = (record instanceof String) ? (String) record : ((Document) record).asXML();

    Document xmlDocument = Dom4jUtils.getDocument(record);
    if (xmlDocument == null)
      return null;

    Element[] selection;
    if ((xpathExpression == null) || (xpathExpression.trim().length() == 0)) {
      log.debug("No XPATH expression supplied - entire document will be used");
      selection = new Element[] { xmlDocument.getRootElement() };
    } else {
      log.debug("Selecting elements using XPATH expression: " + xpathExpression);
      selection = selectElements(xmlDocument, xpathExpression);
    }

    return convertElementsToMaps(selection, omitTopLevelElementTag);
  }

  /**
   * Select Elements using an XPATH expression. Firstly the expression is used to select
   * Nodes. Then the Element subset of selected Nodes is returned.
   *
   * @param node the root node containing the elements to select
   *
   * @param xpathExpression the XPath expression used to selected the elements
   *
   * @return a possibly empty Element[]
   *
   * @throws RecordException if there was an XPath error
   */
  private Element[] selectElements(Node node, String xpathExpression) throws RecordException {
    List selectedNodes = node.selectNodes(xpathExpression);
    List selectedElements = new ArrayList();

    Iterator it = selectedNodes.iterator();
    while (it.hasNext()) {
      Object entry = it.next();
      if (entry instanceof Node) {
        if (entry instanceof Element)
          selectedElements.add(entry);
      } else {
        String exceptionDetail = "XPATH selection using expression " + xpathExpression + " failed to return nodes.";
        if (throwRecordExceptionOnBadSelection) {
          log.warn(exceptionDetail);
          log.warn("set throwRecordExceptionOnBadSelection to false to prevent an exception in this case.");
          throw new RecordException(exceptionDetail);
        } else {
          log.warn(exceptionDetail);
          log.warn("set throwRecordExceptionOnBadSelection to true to force an exception in this case.");
        }
      }
    }

    return (Element[]) selectedElements.toArray(new Element[selectedElements.size()]);
  }

  /**
   * Convert an array of Elements into an array of Ordered Maps. If only one Map is returned
   * don't return it as an array.
   * <p/>
   *
   * If the storeXMLInAttributeName property has been set then we need to add an attribute to
   * the root of each OrderedMap returned that contains the incoming XML string.
   *
   * @param elements array of elements to be converted
   * @param omitElementTag if false then the resulting array of ordered maps will be nested
   * inside a root map with a single attribute named as the element name.
   *
   * @return Object containing an OrderedMap, or OrderedMap[]
   *
   * @throws RecordException if there was a problem creating the ordered map
   */
  private Object convertElementsToMaps(Element[] elements, boolean omitElementTag) throws RecordException {
    int count = elements.length;
    IOrderedMap[] maps = new IOrderedMap[count];

    for (int i = 0; i < elements.length; i++) {
      Element element = elements[i];
      IOrderedMap data = generateMapFromXml(element);

      // add the original incoming XML to the outgoing ordered map if required
      if (storeXMLInAttributeName != null) {
        log.info("Adding incoming XML to [" + storeXMLInAttributeName + "]");
        data.put(storeXMLInAttributeName, origXML);
      }

      if (omitElementTag) {//Just return the raw Map underneath
        maps[i] = data;

      } else {//Need to add a top level OM for the element itself.
        IOrderedMap tmp = new OrderedHashMap();
        tmp.put(element.getName(), data);
        maps[i] = tmp;
      }
    }

    Object result;
    switch (maps.length) {
    case 1:
      result = maps[0];
      break;
    default:
      result = maps;
    }

    return result;
  }

  /**
   * Convert an Element into an orderedMap. essentially loops through each child
   * node of the supplied element and add a corresponding attribute to the ordered
   * map.
   *
   * @param current the element to convert
   *
   * @return the equivalent IOrderedMap
   *
   * @throws RecordException if there was a problem decoding the value
   */
  private IOrderedMap generateMapFromXml(Element current) throws RecordException {
    IOrderedMap map = new OrderedHashMap(); //ToDo: Decouple OHM Dependency - i.e. make this factory generated.

    //This is faster than iterator
    for (int i = 0, size = current.nodeCount(); i < size; i++) {
      Node node = current.node(i);

      //It will need to be added
      if (node instanceof Element) {
        Element element = (Element) node;
        String name = element.getName();
        log.debug("Mapping Element:" + name);

        insert(map, name, generateValue(element));
      }
    }

    return map;
  }

  /**
   * Inserts an attribute name/value pair into the supplied map. If the map already
   * contains an attribute with the same name then we need to convert it into an
   * arry of values (if it isn;t one already) and add the new value.
   *
   * @param map the IOrderedMap to be updated
   * @param name the attribute name
   * @param value the attribute value
   */
  private void insert(IOrderedMap map, String name, Object value) {
    //Wasn't there before so add it
    if (!map.containsKey(name)) {
      map.put(name, value);

      // MultiValued. That means we have more work to do.
    } else {
      Object[] newValues;
      Object oldValue = map.get(name);

      //Assume there were already several
      if (oldValue instanceof Object[]) {
        Object[] oldValues = (Object[]) oldValue;
        int oldSize = oldValues.length;
        newValues = new Object[oldSize + 1];
        System.arraycopy(oldValues, 0, newValues, 0, oldSize);

        //There's only one. There will be two!
      } else {
        newValues = new Object[2];
        newValues[0] = oldValue;
      }

      //Add the new value to the Array and replace the old value with the new one
      newValues[newValues.length - 1] = value;

      int oldIndex = map.keys().indexOf(name);
      map.remove(oldIndex);
      map.add(oldIndex, name, newValues);
    }
  }

  /**
   * Convert the element into a Java data type based on the value of the attribute
   * named <code>valueTypeAttributeName</code>. Elements without this attribute are
   * assumed to be Strings
   *
   * @param element
   *
   * @return the Java object corresponding to the element type
   *
   * @throws RecordException if the type could not be obtained
   *
   * @see Dom4jUtils
   */
  private Object generateValue(Element element) throws RecordException {
    Object result;
    if (element.elements().isEmpty()) {//No children
      //Return the value of element.getText() applying type if necessary
      result = Dom4jUtils.getTypedValue(element, valueTypeAttributeName, false);
    } else {
      IOrderedMap valueMap = new OrderedHashMap();
      List children = element.elements();
      for (int i = 0; i < children.size(); i++) {
        Element child = (Element) children.get(i);
        insert(valueMap, child.getName(), generateValue(child));
      }
      result = valueMap;
    }
    return result;
  }
}
