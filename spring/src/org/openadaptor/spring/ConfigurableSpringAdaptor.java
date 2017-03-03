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

package org.openadaptor.spring;

import org.springframework.context.support.GenericApplicationContext;

/**
 * The configurable spring adaptor is a helper class to extend the spring
 * adaptor with the ability to use a pre-configured application context with the
 * additional default configuration.
 * 
 * This could be useful for tests and other scenarios and gives the ability to
 * modify or register bean configurations before the adaptor runs.
 * 
 * @author OA 3 Core Team
 */
public class ConfigurableSpringAdaptor extends SpringAdaptor {

	/**
	 * Constructor for creating a configurable spring adaptor, which uses the
	 * given application context merged with the default spring adaptor
	 * configuration.
	 * 
	 * @param ctx
	 *            The given application context.
	 */
	public ConfigurableSpringAdaptor(GenericApplicationContext ctx) {
		this.internalContext = ctx;
	}
	
    /** The empty default constructor. */
    public ConfigurableSpringAdaptor() {
          // empty
    }

	/**
	 * This is an utility method. Developers should not modify this context
	 * after the SpringAdaptor was started.
	 * 
	 * @return The context.
	 */
    protected GenericApplicationContext getContext() {
		return getInternalContext();
	}

}
