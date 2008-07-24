/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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
package org.openadaptor.thirdparty.w3c;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.xml.serializer.dom3.LSSerializerImpl;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Converts W3C {@link Document}s to XML strings.
 * 
 * @author cawthorng
 */
public class W3CDocumentToXmlConvertor extends AbstractConvertor {

  private boolean prettyPrint;
  
  /* (non-Javadoc)
   * @see org.openadaptor.auxil.convertor.AbstractConvertor#convert(java.lang.Object)
   */
  protected Object convert(Object record) {
    if (!(record instanceof Document)) {
      throw new RecordFormatException("Record is not an org.w3c.dom.Document. Record: " + record);
    }
    
    Document dom = (Document) record;
    
    LSSerializer serializer = new LSSerializerImpl();
    SimpleDOMErrorHandler errorHandler = new SimpleDOMErrorHandler();
    serializer.getDomConfig().setParameter("error-handler", errorHandler);
    serializer.getDomConfig().setParameter("format-pretty-print", Boolean.valueOf(prettyPrint));
    LSOutput output = new StringWriterLSOutput(dom.getInputEncoding());
    
    try {
      if (!serializer.write(dom.getDocumentElement(), output)) {
	throw new RecordException("Could not convert document: " + errorHandler.getError().getMessage());
      }
    } catch (LSException e) {
      throw new RecordException("Could not convert document", e);
    }

    return output.getCharacterStream().toString();
  }

  /**
   * Determines whether the converted XML should be a pretty-print 
   * formatted String or not.
   * 
   * @return <code>true</code> if the converted XML should be a 
   * 	pretty-print formatted String, <code>false</code> otherwise
   */
  public boolean isPrettyPrint() {
    return prettyPrint;
  }

  /**
   * Sets whether the converted XML should be a pretty-print 
   * formatted String or not.
   * 
   * @param prettyPrint <code>true</code> if the converted XML should be a 
   * 	pretty-print formatted String, <code>false</code> otherwise
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * Implementation of {@link LSOutput} that writes XML documents 
   * to a {@link StringWriter}.
   */
  private class StringWriterLSOutput implements LSOutput {
    private final Writer writer = new StringWriter();
    private final String encoding;
    
    private StringWriterLSOutput(String encoding) {
      this.encoding = encoding;
    }
    
    public OutputStream getByteStream() {
      return null;
    }

    public Writer getCharacterStream() {
      return writer;
    }

    public String getEncoding() {
      return encoding;
    }

    public String getSystemId() {
      return null;
    }

    public void setByteStream(OutputStream byteStream) {
    }

    public void setCharacterStream(Writer characterStream) {
    }

    public void setEncoding(String encoding) {
    }

    public void setSystemId(String systemId) {
    }
  }
  
  /**
   * Simple implementation of a {@link DOMErrorHandler}.
   *
   * @author cawthorng
   */
  private class SimpleDOMErrorHandler implements DOMErrorHandler {
    private DOMError error;
    
    public boolean handleError(DOMError error) {
      this.error = error;
      return false;
    }
    
    public DOMError getError() {
      return error;
    }
  }
}