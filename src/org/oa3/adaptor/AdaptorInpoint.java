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

package org.oa3.adaptor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.Message;
import org.oa3.State;
import org.oa3.connector.IReadConnector;
import org.oa3.node.Node;

public class AdaptorInpoint extends Node implements IAdaptorInpoint {

	private static final Log log = LogFactory.getLog(AdaptorInpoint.class);
	
	private IReadConnector connector;
	private boolean enabled = true;
	private int exitCode = 0;
	
	public AdaptorInpoint() {
		super();
	}

	public AdaptorInpoint(String id) {
		super(id);
	}

	public AdaptorInpoint(final String id, final IReadConnector connector) {
		this(id);
		this.connector = connector;
	}
	
	public void setConnector(final IReadConnector connector) {
		this.connector = connector;
	}
	
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}
	
	public void validate(List exceptions) {
		super.validate(exceptions);
		if (connector == null) {
			exceptions.add(new RuntimeException(toString() + " does not have a connector"));
		}
	}

	public void start() {
		if (enabled) {
			connector.connect();
			super.start();
		} else {
			log.info(toString() + " is not enabled");
		}
	}
	
	public void stop() {
		connector.disconnect();
		super.stop();
	}

	public void run() {
		if (!isState(State.RUNNING)) {
			log.warn(toString() + " has not been started");
			exitCode = 0;
		}
		try {
			log.info(toString() + " running");
			while (isState(State.RUNNING)) {
				try {
					Object[] data = connector.next();
					if (data == null) {
						break;
					}
					process(new Message(data, this));
				} catch (Throwable e) {
					log.error(getId() + " stopping, uncaught exception", e);
					exitCode = 1;
					stop();
				}
			}
		} finally {
			log.info(toString() + " no longer running");
			stop();
		}
	}

	public void setAdaptor(Adaptor adaptor) {
		setNext(adaptor);
	}

	public int getExitCode() {
		return exitCode;
	}
}
