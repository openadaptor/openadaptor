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

package org.openadaptor.auxil.convertor.array;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractMapConvertor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * General purpose convertor for converting Maps into Object arrays
 * 
 * @author Eddy Higgins
 */
public class MapToArrayConvertor extends AbstractMapConvertor {
	private final static Log log = LogFactory.getLog(MapToArrayConvertor.class);
	/**
	 * Verifies that records are Maps and delegates to
	 * array conversion method.
	 * @return Object[] of values from the incoming array
	 * @throws RecordFormatException if incoming record is not an Map instance
	 */
	protected Object convert(Object record) {
		if (record instanceof Map) {
			return convert((Map)record);
		}
		else {
			throw new RecordFormatException("Map expected - got "+record);
		}
	}

	/**
	 * Convert an Object array into an OrderedMap of name/value pairs
	 * @param values incoming array to be converted
	 * @return Map with named values
	 */
	protected Object[] convert(Map values) {
		Object[] result;
		if (values == null) {
			throw new NullRecordException("Null values not permitted");
		}
		if (fieldNames==null) {
			result=values.values().toArray(new Object[values.size()]);
		}
		else { //Need to match data to field names
			int count=fieldNames.length;
			result=new Object[count];
			for (int i=0;i<count;i++) {
				String key=fieldNames[i];
				if (padMissingFields || values.containsKey(key)){
					result[i]=values.get(key);
				}
				else {
					String msg="Field "+key+" is missing (enable padMissingFields property to substitute nulls)";
					log.warn(msg);
					throw new RecordFormatException(msg);
				}
			}
		}
		return result;
	}
}
