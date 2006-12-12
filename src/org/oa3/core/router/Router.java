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

package org.oa3.core.router;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.IMessageProcessor;
import org.oa3.core.Message;
import org.oa3.core.Response;
import org.oa3.core.Response.DiscardBatch;
import org.oa3.core.Response.ExceptionBatch;
import org.oa3.core.Response.OutputBatch;
import org.oa3.core.exception.MessageException;
import org.oa3.core.lifecycle.ILifecycleComponent;
import org.oa3.core.lifecycle.ILifecycleComponentContainer;
import org.oa3.core.lifecycle.ILifecycleComponentManager;
import org.oa3.core.transaction.ITransaction;

public class Router implements IMessageProcessor,ILifecycleComponentContainer {

	private static Log log = LogFactory.getLog(Router.class);
	
	private IRoutingMap routingMap;
  private ILifecycleComponentManager componentManager;
	
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
	//BEGIN implementation of ILifecycleComponentContainer 
  
	public void setComponentManager(ILifecycleComponentManager manager) {
    if (componentManager!=null){
      throw new RuntimeException("ComponentManager has already been set for this router");
    }
    this.componentManager=manager;
    registerComponents();
  }
  //END   implementation of ILifecycleComponentContainer 

  public Response process(Message msg) {
		return process(msg, routingMap.getProcessDestinations((IMessageProcessor)msg.getSender()));
	}
  
  private void registerComponents() {
    for (Iterator it=routingMap.getMessageProcessors().iterator();it.hasNext();){
      Object processor=it.next();
      if (processor instanceof ILifecycleComponent){
        componentManager.register((ILifecycleComponent)processor);
      } else {
        log.debug("Not registering (non-ILifecycleComponent) processor "+processor);
      }
    }
  }
	
	private Response process(Message msg, List destinations) {
		if (log.isDebugEnabled()) {
			logRoutingDebug((IMessageProcessor)msg.getSender(), destinations);
		}
		for (Iterator iter = destinations.iterator(); iter.hasNext();) {
			IMessageProcessor node = (IMessageProcessor) iter.next();
			Response response = node.process(msg);
			processResponse(node, response, msg.getTransaction());
		}
		return new Response();
	}
	
	private void processResponse(IMessageProcessor node, Response response, ITransaction transction) {
		List batches = response.getBatches();
		for (Iterator iter = batches.iterator(); iter.hasNext();) {
			List batch = (List) iter.next();
			if (batch instanceof OutputBatch) {
				processOutput(node, ((OutputBatch)batch).getOutput(), transction);
			} else if (batch instanceof DiscardBatch) {
				processDiscards(node, ((DiscardBatch)batch).getDiscard(), transction);
			} else if (batch instanceof ExceptionBatch) {
				processExceptions(node, ((ExceptionBatch)batch).getMessageExceptions(), transction);
			}
		}
	}

	private void processOutput(IMessageProcessor node, Object[] output, ITransaction transaction) {
		if (output.length > 0) {
      Message msg = new Message(output, node, transaction);
			process(msg, routingMap.getProcessDestinations(node));
		}
	}

	private void processDiscards(IMessageProcessor node, Object[] discardedInput, ITransaction transaction) {
		log.info(node.toString() + " discarded " + discardedInput.length + " input(s)");
		if (discardedInput.length > 0) {
      Message msg = new Message(discardedInput, node, transaction);
      process(msg, routingMap.getDiscardDestinations(node));
		}
	}

	private void processExceptions(IMessageProcessor node, MessageException[] exceptions, ITransaction transaction) {
		log.warn(node.toString() + " caught " + exceptions.length + " exceptions");
		for (int i = 0; i < exceptions.length; i++) {
			List destinations = routingMap.getExceptionDestinations(node, exceptions[i].getException());
			if (destinations.size() > 0) {
        Message msg = new Message(exceptions[i], node, transaction);
        process(msg, destinations);
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
