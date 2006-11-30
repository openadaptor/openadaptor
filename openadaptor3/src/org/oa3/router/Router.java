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

package org.oa3.router;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.IMessageProcessor;
import org.oa3.Message;
import org.oa3.MessageException;
import org.oa3.Response;
import org.oa3.Response.DiscardBatch;
import org.oa3.Response.ExceptionBatch;
import org.oa3.Response.OutputBatch;

public class Router implements IMessageProcessor {

	private static Log log = LogFactory.getLog(Router.class);
	
	private IRoutingMap routingMap;
	
	public Router() {
		super();
	}
	
	public Router(final IRoutingMap routingMap) {
		this();
		this.routingMap = routingMap;
	}
	
	public void setRoutingMap(final IRoutingMap routingMap) {
		this.routingMap = routingMap;
	}
	
	public Response process(Message msg) {
		return process(msg, routingMap.getProcessDestinations((IMessageProcessor)msg.getSender()));
	}
	
	private Response process(Message msg, List destinations) {
		if (log.isDebugEnabled()) {
			logRoutingDebug((IMessageProcessor)msg.getSender(), destinations);
		}
		for (Iterator iter = destinations.iterator(); iter.hasNext();) {
			IMessageProcessor node = (IMessageProcessor) iter.next();
			Response response = node.process(msg);
			processResponse(node, response);
		}
		return new Response();
	}
	
	private void processResponse(IMessageProcessor node, Response response) {
		List batches = response.getBatches();
		for (Iterator iter = batches.iterator(); iter.hasNext();) {
			List batch = (List) iter.next();
			if (batch instanceof OutputBatch) {
				processOutput(node, ((OutputBatch)batch).getOutput());
			} else if (batch instanceof DiscardBatch) {
				processDiscards(node, ((DiscardBatch)batch).getDiscard());
			} else if (batch instanceof ExceptionBatch) {
				processExceptions(node, ((ExceptionBatch)batch).getMessageExceptions());
			}
		}
	}

	private void processOutput(IMessageProcessor node, Object[] output) {
		if (output.length > 0) {
			process(new Message(output, node), routingMap.getProcessDestinations(node));
		}
	}

	private void processDiscards(IMessageProcessor node, Object[] discardedInput) {
		log.info(node.toString() + " discarded " + discardedInput.length + " input(s)");
		if (discardedInput.length > 0) {
			process(new Message(discardedInput, node), routingMap.getDiscardDestinations(node));
		}
	}

	private void processExceptions(IMessageProcessor node, MessageException[] exceptions) {
		log.warn(node.toString() + " caught " + exceptions.length + " exceptions");
		for (int i = 0; i < exceptions.length; i++) {
			List destinations = routingMap.getExceptionDestinations(node, exceptions[i].getException());
			if (destinations.size() > 0) {
				process(new Message(exceptions[i], node), destinations);
			} else {
				throw new RuntimeException(exceptions[0].getException());
			}
		}
	}

	private void logRoutingDebug(IMessageProcessor sender, List destinations) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator iter = destinations.iterator(); iter.hasNext();) {
			IMessageProcessor node = (IMessageProcessor) iter.next();
			buffer.append(buffer.length() > 0 ? "," : "");
			buffer.append(node.toString());
		}
		log.debug("[" + sender.toString() + "]->[" + buffer.toString() + "]");
	}

}
