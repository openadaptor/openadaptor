/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.node;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.Response.DiscardBatch;
import org.openadaptor.core.Response.ExceptionBatch;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.router.Pipeline;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.transaction.ITransactional;

/**
 * This class should be used to "wrap" an {@link IWriteConnector}. It handles
 * the following
 * <li>propogation of lifecycle management to the {@link IWriteConnector}
 * <li>delegation of data processing by delivering it to the
 * {@link IWriteConnector}
 * 
 * <br/><br/>This represents the "outpoint" of an Adaptor. Typically an
 * {@link Adaptor} is managing the lifecycle of a WriteNode.
 * 
 * By virtue of it's subclass, this class can also have an
 * {@link IDataProcessor} configured, if this the case then data is processed by
 * the {@link IDataProcessor} before being delegated to the
 * {@link IWriteConnector}. This by-passes any exception / discard management
 * that can be configured in delegates such as {@link Router} / {@link Pipeline}.
 * 
 * @author perryj
 * @see Adaptor
 * @see IWriteConnector
 * 
 */
public class WriteNode extends Node {

	private static final Log log = LogFactory.getLog(WriteNode.class);
	
	private IWriteConnector connector;
	private boolean unbatch = false;
	
	public WriteNode() {
		super();
	}
	
	public WriteNode(String id) {
		super(id);
	}

	public WriteNode(String id, final IWriteConnector connector) {
		super(id);
		this.connector = connector;
	}

	public void setConnector(final IWriteConnector connector) {
		this.connector = connector;
	}
	
	public void setUnbatch(boolean unbatch) {
		this.unbatch = unbatch;
	}
	
	public void validate(List exceptions) {
		super.validate(exceptions);
		if (connector == null) {
			exceptions.add(new RuntimeException(toString() + " does not have a connector"));
		} else {
		  connector.validate(exceptions);
    }
	}
	
	public void start() {
		connector.connect();
		super.start();
	}
	
	public void stop() {
    setState(State.STOPPING);
		connector.disconnect();
		super.stop();
	}
	
	public Response process(Message msg) {
    
    Object resource = null;
    if (connector instanceof ITransactional) {
      resource = ((ITransactional)connector).getResource();
      if (resource != null) {
        log.debug(getId() + " enlisting in transaction");
        msg.getTransaction().enlist(resource);
      }
    }
    
		Response processorResponse = super.process(msg);

		Response response = new Response();
		
		// all we can do is copy processor discards and exceptions
		// into the response to this call
		List batches = processorResponse.getBatches();
		for (Iterator iter = batches.iterator(); iter.hasNext();) {
			List batch = (List) iter.next();
			if (batch instanceof DiscardBatch) {
				response.addDiscardedInputs(batch);
			} else if (batch instanceof ExceptionBatch) {
				response.addExceptions(batch);
			}
		}

		// the output from the processor forms the input to the connector
		// so call the connector and update the response with the results
		Object[] inputs = processorResponse.getCollatedOutput();
		if (unbatch || inputs.length == 1) {
			for (int i = 0; i < inputs.length; i++) {
				try {
					Object output = connector.deliver(new Object[] {inputs[i]});
					if (output != null) {
						response.addOutput(output);
					}
				} catch (Exception ex) {
					log.info(getId() + " caught " + ex.getClass().getName() + ":" + ex.getMessage());
					response.addException(new MessageException(inputs[i], ex));
				}
			}
		} else {
			Object output = connector.deliver(inputs);
			if (output != null) {
				response.addOutput(output);
			}
		}

    if (resource != null) {
      msg.getTransaction().delistForCommit(resource);
    }
    
		return response;
	}

  public String getId() {
    String id = super.getId();
    if (id == null && connector instanceof IComponent) {
      return ((IComponent)connector).getId();
    }
    return id;
  }
  
  public String toString() {
    return getId();
  }

}
