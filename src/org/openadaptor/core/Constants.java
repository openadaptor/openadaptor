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

package org.openadaptor.core;

public final class Constants {
	public static final String DEFAULT_DATA_BINDING = "oa_data"; //Bound name for data records
	public static final String DEFAULT_METADATA_BINDING = "oa_metadata"; //Bound name for data records
	public static final String DEFAULT_LOG_BINDING = "oa_log"; //Bound name for logging

	public static final String NO_CAUSE_EXCEPTION     = "No cause exception detected.";
	public static final String UNKNOWN_ADAPTOR_NAME   = "Unknown";
	public static final String UNKNOWN_COMPONENT_NAME = "Unknown";
	public static final String TIMESTAMP               = "TIMESTAMP";
	public static final String EXCEPTION_CLASS         = "EXCEPTION_CLASS_NAME";
	public static final String EXCEPTION_MESSAGE       = "EXCEPTION_MESSAGE";
	public static final String CAUSE_EXCEPTION_CLASS   = "CAUSE_EXCEPTION_CLASS_NAME";
	public static final String CAUSE_EXCEPTION_MESSAGE = "CAUSE_EXCEPTION_MESSAGE";
	public static final String STACK_TRACE             = "STACK_TRACE";
	public static final String ADAPTOR_NAME            = "ADAPTOR_NAME";
	public static final String COMPONENT               = "ORIGINATING_COMPONENT";
	public static final String THREAD_NAME             = "THREAD_NAME";
	public static final String DATA_TYPE               = "DATA_TYPE";
	public static final String DATA                    = "DATA";
	public static final String METADATA                = "METADATA";
	public static final String FIXED                   = "FIXED";
	public static final String REPROCESSED             = "REPROCESSED";
	
	/* System properties*/
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");
	
	
	private Constants(){
		//this prevents even the native class from 
		//calling this constructor as well :
		throw new AssertionError();
	}
}
