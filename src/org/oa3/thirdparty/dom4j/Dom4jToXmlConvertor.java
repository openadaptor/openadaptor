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
 * File: $Header: /cvs/oa3/src/org/oa3/processor/convertor/Dom4jToXmlConvertorProcessor.java,v 1.5 2006/10/20 15:21:35
 * fennelr Exp $ Rev: $Revision: 1.5 $ Created Jun 30, 2006 by Eddy Higgins
 */
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.oa3.auxil.converter.AbstractConverter;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;

/**
 * Converts XML documents (as Strings) to Dom4j Documents
 * 
 * @author Eddy Higgins
 */
public class Dom4jToXmlConvertor extends AbstractConverter {
  private static final Log log = LogFactory.getLog(Dom4jToXmlConvertor.class);

  // If true then the outputXML will be pretty-printed
  private boolean prettyPrint;

  private OutputFormat outputFormat = null;

  /**
   * Get current prettyprint setting.
   * 
   * @return true if pretty print is turned on
   */
  public boolean getPrettyPrint() {
    return prettyPrint;
  }

  /**
   * If true then the outputXML will be pretty-printed
   * 
   * @param prettyPrint
   *          true to turn pretty prin ton
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * Implements IRecordConvertor method
   * 
   * @param record
   *          dom4j Document objectcontaining an XML Document (dom4j) or a
   * @return Object (String) containing XML data
   * @throws org.oa3.processor.RecordFormatException
   *           if conversion fails
   */
  protected Object convert(Object record) throws RecordException {
    if (!(record instanceof Document))
      throw new RecordFormatException("Record is not a dom4j Document. Record: " + record);

    // xmlString=((Document)record).asXML();
    String xmlString = asString((Document) record);
    log.debug("dom4j Document successfully converted to XML String");

    return xmlString;
  }

  /**
   * Converts the supplied DOM document into its XML equivalent. If the <code>prettyPrint</code> has been set then the
   * XML is returned formatted with indents and newlines
   * 
   * @param document
   *          the DOM document to be converted
   * 
   * @return the XML
   */
  private String asString(Document document) {
    // Easy peasy.
    if (!prettyPrint)
      return document.asXML();

    if (outputFormat == null) {
      outputFormat = OutputFormat.createPrettyPrint();
      outputFormat.setIndent("\t");
    }

    String xml;
    StringWriter writer = new StringWriter();
    XMLWriter out = new XMLWriter(writer, outputFormat);

    try {
      out.write(document);
      xml = writer.toString();
    } catch (IOException ioe) { // Should never happen, but if it does, give up on pretty print.
      log.warn("Failed to pretty print - falling back to default");
      xml = document.asXML();
    }

    return xml;
  }

}
