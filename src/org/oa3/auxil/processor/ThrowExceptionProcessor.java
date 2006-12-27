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
package org.oa3.auxil.processor;

import org.oa3.core.exception.RecordException;
import org.oa3.core.IDataProcessor;
import org.oa3.core.Component;
import org.oa3.core.IComponent;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.lang.reflect.Constructor;
import java.util.*;


/**
 * This processsor does nothing except throw exceptions for every record it
 * processes.
 * <p />
 *
 * By default it will throw a RecordException with the record embedded in the
 * exception's message. The exception can be configured directly or by
 * supplying a string representing the fully qualified name of the Exception
 * class.
 * <p />
 *
 * Note: This is generally fine however there is one issue. We can throw any
 * unchecked exceptions but because of the <code>processRecord</code> method
 * signature we can only throw RecordExceptions (or it's subclasses). If any
 * other checked Exception is configured we wrap it in a RecordException and
 * throw that instead.
 * <p />
 *
 * Note: if you use the <code>exceptionClassName<code> property then the exception
 * must have a single String constructor. Otherwise we cannot instantioate it :-(
 *
 *
 * @author Russ Fennell, Kevin Scully
 */
public class ThrowExceptionProcessor extends Component implements IDataProcessor {
    private static final Log log = LogFactory.getLog(ThrowExceptionProcessor.class);


    public static String DEFAULT_EXCEPTION_MESSAGE      = "Unknown exception thrown";
    public static String DEFAULT_EXCEPTION_CLASSNAME    = "org.oa3.core.exception.RecordException";


    private String exceptionClassName = DEFAULT_EXCEPTION_CLASSNAME;
    private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;
    private Exception exception = null;


    /**
     * @param exceptionClassName Fully qualified name of Exception class to be
     * instantiated. Defaults to "org.oa3.processor.RecordException"
     */
    public void setExceptionClassName(String exceptionClassName) {
        this.exceptionClassName = exceptionClassName;
    }


    /**
     * @param exceptionMessage String that forms the message body of the
     * exception. Defaults to "Sample exception"
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }


    /**
     * @param exception Real exception object. This takes priority if set.
     * Defaults to null.
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }


    /**
     * Throws an exception for every record processed.
     *
     * @param data the record being processed
     *
     * @return never returns as we throw an exception!
     */
    public Object[] process(Object data) {
        log.info("Processing: [" + data + "]");

        // Only generate an Exception if none has been directly configured.
        if ( exception == null)
          exception = generateException(exceptionMessage + " [" + data + "]");

        // Can throw the exception only if it is:
        // a) a Runtime Exception
        // b) a RecordException or a subclass of it.
        if ( exception instanceof RecordException)
          throw (RecordException) exception;

        if ( exception instanceof RuntimeException )
          throw (RuntimeException) exception;

        // OK we have an exception we can't throw directly 'cos of this method's
        // signature. What we do is wrap it in a RecordException and throw that
        // instead.
        throw new RecordException("Wrapping the real exception.", exception);
    }


    /**
     * No op.
     *
     * @param exceptions
     */
    public void validate(List exceptions) {
    }


    /**
     * Resets the exception, exceptionClassName and exceptionMessage fields
     *
     * @param context
     */
    public void reset(Object context) {
        exception           = null;
        exceptionClassName  = DEFAULT_EXCEPTION_CLASSNAME;
        exceptionMessage    = DEFAULT_EXCEPTION_MESSAGE;
    }


    /**
     * Generate an exception based on configuration. If the exceptionClassName is
     * set then we try to find a constructor for the exception that will allow us
     * to pass the supplied message text but if this fails then we try forn OA
     * type exception constructor (two paramaters; a message and a reference to
     * the originating component which we will set to null) and if all that fails
     * then we try for a default constructor with no params.
     *
     * @param message The exception message.
     *
     * @return Generated exception or if an exception is thrown when creating the
     * one we are interested in then we return it instead.
     */
    private Exception generateException(String message) {
        log.debug("Creating exception: " + exceptionClassName);

        try {
            Class exceptionClass = Class.forName(exceptionClassName);

            // we try for a construtor that takes a single string parameter. If
            // this fails then we try forn OA type exception constructor (two
            // paramaters; a message and a reference to the originating component)
            // and if all that fails then we try for a default constructor.
            Constructor messageConstructor;
            Object[] params;
            try {
                messageConstructor = exceptionClass.getConstructor(new Class[]{String.class});
                params = new Object[] {message};

            } catch (NoSuchMethodException e) {
                try {
                    log.debug("No constructor found with single string paramater. Trying OA constructor");
                    messageConstructor = exceptionClass.getConstructor(new Class[]{String.class, IComponent.class});
                    params = new Object[]{message, null};

                } catch (NoSuchMethodException e1) {
                    log.debug("No constructor found with string/component paramaters. Trying default");
                    messageConstructor = exceptionClass.getConstructor(new Class[]{});
                    params = new Object[]{};
                }
            }

            return (Exception) messageConstructor.newInstance(params);

        } catch (Exception e) {
            // OK This isn't going to work so we use the error exception instead
            log.debug("Couldn't find constructor for [" + exceptionClassName + "] so will return: " + e.getClass().getName());
            return e;
        }
    }
}
