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
package org.openadaptor.auxil.convertor.xml;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * This tests the XmlConvertors implementation.
 * 
 * @author OA3 Core Team
 */
public class XmlToOrderedMapConvertorTestCase extends AbstractTestXmlConvertor {

  protected XmlToOrderedMapConvertor xml2om;


  protected void setUp() throws Exception {
    super.setUp();
    xml2om = (XmlToOrderedMapConvertor)testProcessor;
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected IDataProcessor createProcessor() {
    return new XmlToOrderedMapConvertor();
  }

  public void testProcessRecord() {
    try {
      Object[] resultArray = xml2om.process(xmlString);
      assertTrue(resultArray.length == 1);
      IOrderedMap map = (IOrderedMap) resultArray[0];
      //System.out.println(map);
      assertEquals(om.toString(), map.toString()); // Close enough
      // map=(IOrderedMap)xml2om.convert(xmlDocument);
      resultArray = xml2om.process(xmlDocument);
      assertTrue(resultArray.length == 1);
      map = (IOrderedMap) resultArray[0];
      assertEquals(om.toString(), map.toString()); // Close enough.
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }
  
  /**
   * Convert from XML to OM to XML
   */
  public void testTwoWayConversion() {
    try {
      IDataProcessor om2xml= new OrderedMapToXmlConvertor();
      Object[] resultArray = xml2om.process(xmlString);
      assertTrue(resultArray.length == 1);
      IOrderedMap map = (IOrderedMap) resultArray[0];
      resultArray = om2xml.process(map);
      assertTrue(resultArray.length == 1);
      String xml = (String) resultArray[0];
      assertEquals(xmlString, xml);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }


  public void testInvalidInputs() {
    try {
      xml2om.process(om);
      fail("Convertor should not accept non String/Document value " + om.getClass().getName());
    } catch (RecordFormatException pe) {
      ;
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

  }

}
