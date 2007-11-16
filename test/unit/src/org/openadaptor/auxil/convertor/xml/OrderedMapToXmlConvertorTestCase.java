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

import org.dom4j.Document;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * This tests the XmlConvertors implementation.
 * 
 * @author higginse
 */
public class OrderedMapToXmlConvertorTestCase extends AbstractTestXmlConvertor {

  protected OrderedMapToXmlConvertor om2xml;


  protected void setUp() throws Exception {
    super.setUp();
    om2xml = (OrderedMapToXmlConvertor)testProcessor;
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected IDataProcessor createProcessor() {
    return new OrderedMapToXmlConvertor();
  }

  public void testProcessRecord() {
    try {
      Object[] resultArray = om2xml.process(om);
      assertTrue(resultArray.length == 1);
      String xml = (String) resultArray[0];

      assertEquals(xmlString, xml);

      om2xml.setReturnXmlAsString(false);
      resultArray = om2xml.process(om);
      assertTrue(resultArray.length == 1);
      Document doc = (Document) resultArray[0];

      String encoding = doc.getXMLEncoding();
      assertEquals(AbstractTestXmlConvertor.docAsString(xmlDocument, encoding), AbstractTestXmlConvertor.docAsString(doc,
          encoding));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }
  
  public void testOrderedMapToXmlWithSlashes(){
    try {
      Object[] resultArray = om2xml.process(generateTestOrderedMapWithSlashes());
      assertTrue(resultArray.length == 1);
      String xml = (String) resultArray[0];
      System.out.println(xml);
      String expected="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+'\n'+
      "<A_sl_A><B_sl_B><Y_sl_Y>Y_VAL</Y_sl_Y><Z_sl_Z>Z_VAL</Z_sl_Z></B_sl_B>"+
      "<C_sl_C><Y_sl_Y>Y_VAL</Y_sl_Y><Z_sl_Z>Z_VAL</Z_sl_Z></C_sl_C><C_sl_C>"+
      "<Y_sl_Y>Y_VAL2</Y_sl_Y><Z_sl_Z>Z_VAL2</Z_sl_Z></C_sl_C></A_sl_A>";
      assertEquals(expected,xml);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }    
  }
  
  public void testMappedSlashValueProperty() {
    try {
      om2xml.setMappedSlashValue(".");
      Object[] resultArray = om2xml.process(generateTestOrderedMapWithSlashes());
      assertTrue(resultArray.length == 1);
      String xml = (String) resultArray[0];
      System.out.println(xml);
      String expected="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+'\n'+
      "<A.A><B.B><Y.Y>Y_VAL</Y.Y><Z.Z>Z_VAL</Z.Z></B.B>"+
      "<C.C><Y.Y>Y_VAL</Y.Y><Z.Z>Z_VAL</Z.Z></C.C><C.C>"+
      "<Y.Y>Y_VAL2</Y.Y><Z.Z>Z_VAL2</Z.Z></C.C></A.A>";
      assertEquals(expected,xml);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }      
  }
  
  public void testInvalidInputs() {
    try {
      om2xml.convert(xmlString);
      om2xml.process(xmlString);
      fail("Convertor should not accept non IOrderedMap value " + om.getClass().getName());
    } catch (RecordFormatException pe) {
      ;
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }
  }
}
