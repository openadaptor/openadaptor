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
package org.openadaptor.legacy.converter.dataobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DataObjectException;
import org.openadaptor.doxml.GenericXMLReader;
import org.openadaptor.auxil.convertor.AbstractConvertor;

import java.util.Iterator;
import java.util.Map;

/**
 * Convert XML into DataObjects <B>Note</B>: Usage of this class depends on the availability of a legacy openadaptor
 * jar to do the conversions, as openadaptor 3 doesn't directly support dataobjects, or DOXML.
 *
 * @author Eddy Higgins
 */
public class XmlToDataObjectConvertor extends AbstractConvertor {

  private static final Log log = LogFactory.getLog(XmlToDataObjectConvertor.class);
  protected GenericXMLReader reader = new GenericXMLReader();

  /**
   * This converts a supplied XML (String or dom4j Document) into a DataObject[] <B>Note</B>: Usage of this method
   * depends on the availability of a legacy openadaptor jar to do the conversions, as openadaptor 3 doesn't directly support
   * dataobjects.
   *
   * @param record containing XML
   * @return XMl representation of the data
   * @throws org.openadaptor.processor.RecordException
   *          if conversion fails
   */
  protected Object convert(Object record) throws RecordException {
    String xml = null;
    Object result = null;
    if (record instanceof Document) { // Convert it into a String
      xml = ((Document) record).asXML();
    } else {
      if (record instanceof String) { // Already parsed.
        xml = (String) record;
      }
    }
    if (xml != null) {
      try {
        result = reader.fromString(xml);
      } catch (Exception e) {
        String reason = "Failed to convert " + record == null ? "<null>" : record + ". Exception - " + e;
        XmlToDataObjectConvertor.log.warn(reason);
        throw new RecordException(reason, e);
      }
    } else {
      throw new RecordFormatException("Record is not an XML String (or dom4j Document). Record: " + record);
    }
    return result;
  }
  // END Abstract Convertor Processor implementation

  public void setAttributes(Map attributeMap) {
    for (Iterator iter = attributeMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      try {
        reader.setAttributeValue((String) entry.getKey(), (String) entry.getValue());
      } catch (DataObjectException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }
  }
}
