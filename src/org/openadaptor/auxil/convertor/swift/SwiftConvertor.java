/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.convertor.swift;

import java.util.List;

import net.sourceforge.wife.services.ConversionService;
import net.sourceforge.wife.services.IConversionService;
import net.sourceforge.wife.swift.model.SwiftMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

/**
 * This will convert to and from various Swift Message formats.
 * 
 * @author Eddy Higgins
 */
public class SwiftConvertor extends AbstractConvertor {
	private static final Log log = LogFactory.getLog(SwiftConvertor.class);

	public static final int FORMAT_SWIFT_RAW=0;
	public static final int FORMAT_SWIFT_XML=1;
	public static final int FORMAT_SWIFT_OBJECT=2;
	
	private IConversionService conversionService=new ConversionService();
	private boolean[] outputFormat=new boolean[3];

	private boolean outputSwiftRaw;
	private boolean outputSwiftXml;
	private boolean outputSwiftMessageObject;

  public void setOutputFormat(int format){
  	if (format>=outputFormat.length) {
  		throw new IllegalArgumentException("format value of "+format+" is out of range. Valid range is 0-"+(outputFormat.length-1));
  	}
		for (int i=0;i<outputFormat.length;i++) {
			outputFormat[i]=format==i;
		}
		outputSwiftRaw=outputFormat[FORMAT_SWIFT_RAW];
		outputSwiftXml=outputFormat[FORMAT_SWIFT_XML];
		outputSwiftMessageObject=outputFormat[FORMAT_SWIFT_OBJECT];
	}
  public SwiftConvertor() {
  	setOutputFormat(FORMAT_SWIFT_RAW);
  }

	protected Object convert(Object record) throws RecordException {
		if (record == null)
			return null;
    Object result=record; //By default no conversion
		if (log.isDebugEnabled()) {
			log.debug("Incoming record is "+record.getClass().getName());
		}
		if (record instanceof SwiftMessage) {
			SwiftMessage swiftMessage=(SwiftMessage)record;
			if (outputSwiftRaw) {
				result=conversionService.getFIN(swiftMessage);
			}
			else {
				if (outputSwiftXml) {
					result=conversionService.getXml(swiftMessage);
				}
			}
		}
		else { //It's a String format - either XML or RAW
			if (record instanceof String) {
				String recordString=(String)record;
				boolean raw=recordString.startsWith("{");
				if (outputSwiftMessageObject) {
					result=getSwiftMessage(recordString,raw);
				}
				else if (outputSwiftXml && raw) {
					result=conversionService.getXml(recordString);
				}
				else if (outputSwiftRaw && (!raw)) {
					result=conversionService.getFIN(recordString);
				}
			}
			else {
				String msg="Record should be String or SwiftMessage, but is "+record.getClass().getName();
				throw new RecordFormatException(msg);
			}
		}
		return result;
	}

	
  private SwiftMessage getSwiftMessage(String msg,boolean raw) {
  	return raw?conversionService.getMessageFromFIN(msg):conversionService.getMessageFromXML(msg);
  }

}
