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

import junit.framework.TestCase;
import org.oa3.core.exception.ComponentException;
import org.oa3.core.exception.RecordException;


/**
 * A suite of tests for the ThrowExceptionProcessor.
 *
 * @see ThrowExceptionProcessor
 *
 * @author Russ Fennell
 */
public class ThrowExceptionProcessorTestCase extends TestCase {

    private static String DEFAULT_MESSAGE = ThrowExceptionProcessor.DEFAULT_EXCEPTION_MESSAGE + " [null]";


    /**
     * Checks that the processor throws an exception and uses the default
     * properties correctly
     */
    public void testProcessor() {
        // As we are using the same processor then we MUST call reset or
        // set the exception to null between tests or the process method
        // will just reuse the old exception
        ThrowExceptionProcessor p = new ThrowExceptionProcessor();


        // defaults - should throw a RecordException with a default message
        try {
            p.process(null);
        } catch (Exception e) {
            assertTrue(e instanceof RecordException);
            assertEquals(DEFAULT_MESSAGE, e.getMessage());
        }


        // exception string
        try {
            p.reset(null);

            p.setExceptionMessage("hello world");
            p.process(null);
        } catch (Exception e) {
            assertTrue(e instanceof RecordException);
            assertEquals("hello world [null]", e.getMessage());
        }


        // exception class name (unchecked)
        try {
            p.reset(null);

            p.setExceptionClassName("java.lang.NullPointerException");
            p.process(null);
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
            assertEquals(DEFAULT_MESSAGE, e.getMessage());
        }


        // exception class name (checked)
        try {
            p.reset(null);

            p.setExceptionClassName("org.oa3.core.exception.ComponentException");
            p.process(null);
        } catch (Exception e) {
            assertTrue(e instanceof ComponentException);

            // note: the ComponentException overrides the getMessage() call and
            // prepends the component ID. In this case the component is null so
            // we just get the separator prepended
            assertEquals(":" + DEFAULT_MESSAGE, e.getMessage());
        }

    }
}
