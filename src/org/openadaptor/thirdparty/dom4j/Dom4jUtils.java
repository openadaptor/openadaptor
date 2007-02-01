/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.thirdparty.dom4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Common Dom4j Utilities for OA3
 * 
 * @author Eddy Higgins
 */
public class Dom4jUtils {
  private static final Log log = LogFactory.getLog(Dom4jUtils.class);

  private Dom4jUtils() {
  } // No instantiation allowed.

  /**
   * This is used to generate Date objects from strings.
   */
  private static DateFormat df = new SimpleDateFormat();

  /**
   * This will parse an XML String into a Dom4j Document. If it fails it will throw a RecordFormatException
   * 
   * @param xmlString
   *          An XML document as a String
   * @return Dom4j document representing the XML.
   */
  private static Document createDom4jDocument(String xmlString) throws RecordFormatException {
    try {
      log.debug("Parsing XML string into dom4j document");
      if (xmlString != null) {
        xmlString = xmlString.trim();
      }
      return (DocumentHelper.parseText(xmlString));
    } catch (DocumentException de) {
      throw new RecordFormatException(de.toString(), de);
    }
  }

  /**
   * Get a dom4j Document from a record. Valid records may contain an XML String,a Dom4j Document, or null If not, a
   * RecordFormatException will be thrown. Note that if a null input record is supplied, null will be returned. //ToDo:
   * Agreee null behaviour with rest of openadaptor team
   * 
   * @param record
   * @return Document possibly having generated it from an XML String
   * @throws RecordFormatException
   */
  public static Document getDocument(Object record) throws RecordFormatException {
    Document document = null;
    if (record != null) {
      if (record instanceof String) {
        document = createDom4jDocument((String) record);
      } else if (record instanceof Document) {
        document = (Document) record;
      } else {
        throw new RecordFormatException("Unable to parse record as XML");
      }
    }
    return document;
  }

  /**
   * Return a Java object given an element's text value (or tag name) If typeAttributeName is not null, and that
   * attribute is specified, then it will use the value to determine the Object to return. If typeAttributeName is null,
   * or the attribute is not set, then the String value of the element is returned. Currently it supports the following
   * mappings:
   * <UL>
   * <LI>Double yields java.lang.Double</LI>
   * <LI>Long yields java.lang.Long</LI>
   * <LI>String yields java.lang.String</LI>
   * </UL>
   * If unable to generate the appropriate class (e.g. if it failed to parse a number, or date), a RecordFormatException
   * will be thrown.
   * 
   * @param element
   *          Element whose text value, (or tag) is being typed
   * @param typeAttributeName
   *          (optional) name of attribute which contains type value
   * @return Object containing a String,Date,Long or Double value.
   * @throws RecordFormatException
   *           if unable to create appropriate Object from the value.
   */
  public static Object getTypedValue(Element element, String typeAttributeName, boolean useTagName)
      throws RecordFormatException {
    if (element == null) {
      log.warn("cannot extract value from <null> element");
      throw new RecordFormatException("cannot extract value from <null> element");
    }
    String xmlValue = useTagName ? element.getName() : element.getText();
    Object result = xmlValue;
    if (typeAttributeName != null) {
      Attribute typeAttribute = element.attribute(typeAttributeName);
      if (typeAttribute != null) {
        result = asTypedValue(xmlValue, typeAttribute.getText());
      }
    }
    return result;
  }

  /**
   * Return the supplied String value as a java Object given the requested type. //ToDo: Probably better to make this
   * factory based, and allow user-specified ones also.
   * 
   * @param value
   *          String value
   * @param type
   *          one of Double,Long,Date,String or null.
   * @return one of java.lang.String (default), java.lang.Double,java.lang.Long, java.util.Date
   * @throws RecordFormatException
   *           If an unrecognised type is specified, or conversion isn't possible
   */
  public static final Object asTypedValue(String value, String type) throws RecordFormatException {
    Object result = value;
    if (type != null) {// Need to apply it.
      String exceptionMessage = null;
      Throwable t = null;
      try {
        if ("Double".equalsIgnoreCase(type))
          result = new Double(value);
        else if ("Long".equalsIgnoreCase(type))
          result = new Long(value);
        else if ("Date".equalsIgnoreCase(type))
          result = df.parse(value);
        else if (!"String".equalsIgnoreCase(type))
          log.warn("Unknown type " + type + " specified. Value will convert as a String");
      } catch (ParseException pe) {
        exceptionMessage = "Failed to get typed value for " + value + ". Exception: " + pe.getMessage();
        t = pe;
      } catch (NumberFormatException nfe) {
        exceptionMessage = "Failed to get typed value for " + value + ". Exception: " + nfe.getMessage();
        t = nfe;
      }
      if (exceptionMessage != null) {
        log.warn(exceptionMessage);
        throw new RecordFormatException(exceptionMessage, t);
      }
    }
    return result;
  }
}
