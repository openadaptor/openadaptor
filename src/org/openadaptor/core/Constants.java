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
