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

package org.openadaptor.auxil.processor.xml;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.util.URLUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates each record (assumed to be an XML document in string form) against
 * the schema defined in the configuration file.
 * <p />
 *
 * By default the schema URL is verified during the initialisation phase. For web
 * based schemas this may take a while. You can turn this feature off by setting
 * the forcingURLValidation property to be false
 * <p />
 *
 * This processor uses the Xerces parser and requires the following jars to be
 * in the classpath - xerces.jar, xml-apis.jar
 *
 * @author Russ Fennell
 */
public class XmlValidator extends Component implements IDataProcessor {

  public static final Log log = LogFactory.getLog(XmlValidator.class);

  private String schemaURL = null;

  private boolean forcingURLValidation = true;

  private SAXParser parser = new SAXParser();

  private InputSource in = new InputSource();

  /**
   * @return the URL for the validating schema
   */
  public String getSchemaURL() {
    return schemaURL;
  }

  /**
   * @return if true then the schema URL is verified during the initialisation phase
   */
  public boolean isForcingURLValidation() {
    return forcingURLValidation;
  }

  /**
   * Sets the location of the validating schema
   *
   * @param url
   */
  public void setSchemaURL(String url) {
    this.schemaURL = url;
  }

  /**
   * Set to true if you want the schema URL to be verified during the initialisation
   * phase.
   *
   * @param b defaults to true
   */
  public void setForcingURLValidation(boolean b) {
    this.forcingURLValidation = b;
  }

  /**
   * Take the record, ensure it's a string and validate it against the schema
   * defined in the config file.
   *
   * @param record the XML to be validated
   *
   * @return Object[] with zero or more records, resulting from the processing
   *         operation.
   *
   * @throws RecordException if the record is not a string or does not contain
   * valid XML or if it fails to be validated against the schema
   */
  public Object[] process(Object data) {
    if (!(data instanceof String)) {
      throw new RuntimeException("data is not an XML string");
    }

    try {
      in.setCharacterStream(new StringReader((String)data));
      parser.parse(in);
    } catch (Exception e) {
      throw new ProcessingException("xml is invalid", e, this);
    }

    log.debug("XML validated");
    return new Object[] {data};
  }

  /**
   * Hook to perform any validation of the component properties required by the
   * implementation. Defult behaviour should be a no-op.
   *
   * @return empty list
   */
  public void validate(List exceptions) {
    
    if (forcingURLValidation) {
      try {
        URLUtils.validateURLAsDataSource(schemaURL);
      } catch (RuntimeException e) {
        exceptions.add(e);
      }
    }

    // set up the parser to use validation and set the schema location
    try {
      parser.setFeature("http://xml.org/sax/features/validation", true);
      parser.setFeature("http://apache.org/xml/features/validation/schema", true);
      parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
      parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schemaURL);
    } catch (Exception e) {
      exceptions.add(e);
    }

    // we must have some sort of error handler or the parser will simply
    // ignore any schema errors
    parser.setErrorHandler(new OAXMLParserErrorHandler());

    log.debug("parser initialised");
  }

  /**
   * Hook to allow the processor to be reset. Does nothing.
   */
  public void reset(Object context) {
  }

  // INNER CLASSES

  /**
   * Handles any validation errors encountered by the parser. Warnings are written
   * to the logs while errors and fatal errors cause SAXExceptions to be thrown. 
   */
  private class OAXMLParserErrorHandler implements ErrorHandler {
    public void warning(SAXParseException e) throws SAXException {
      log.warn(e.toString());
    }

    public void error(SAXParseException e) throws SAXException {
      throw new SAXException(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
      throw new SAXException(e);
    }
  }
}
