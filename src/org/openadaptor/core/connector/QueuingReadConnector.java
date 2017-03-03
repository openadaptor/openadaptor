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

package org.openadaptor.core.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.transaction.ITransactionalResource;

/**
 * Base class for connectors that don't really poll. This provides the core
 * implementation for queuing data as it arrives from the specific transport
 * (socket, webservice, http, etc...) The data is dequeued by calls to next().
 * 
 * Calls to {@link #enqueue} block until the data is dequeued. If data is
 * dequeued within a transaction (i.e. {@link ITransactionalResource#begin()}
 * has been called on this component's {@link ITransactionalResource}) then the
 * call to enqueue will block until the transaction completes.
 * 
 * By default the queue size is unlimited, but a queue limit can be set, as can
 * the behaviour for if the queue size ever reaches that limit.
 * 
 * @author perryj, Dealbus Dev
 * 
 */
public abstract class QueuingReadConnector extends AbstractQueuingReadConnector {

	protected static final Log log = LogFactory
			.getLog(QueuingReadConnector.class);

	/**
	 * Default C'tor.
	 */
	protected QueuingReadConnector() {
		// empty
	}

	/**
	 * C'Tor with given component id.
	 * 
	 * @param id
	 *            The given component id.
	 */
	protected QueuingReadConnector(String id) {
		super(id);
	}

	/**
	 * implementation of {@link ITransactionalResource}, which if the component
	 * is transacted will be returned when {@link #getResource()} is called
	 */
	private QueueTransactionalResource resource = new QueueTransactionalResource();

	/**
	 * this dequeues the data
	 */
	public Object[] next(long timeoutMs) {
		synchronized (queue) {

			if (queue.size() == 0) {
				try {
					queue.wait(timeoutMs);
				} catch (InterruptedException e) {
					log.error("Thread interruped", e);
				}
			}

			Object[] data = new Object[Math.min(batchSize, queue.size())];
			for (int i = 0; i < data.length; i++) {
				QueueItem item = (QueueItem) queue.remove(0);
				resource.add(item);
				data[i] = item.data;
			}

			if (data.length > 0) {
				queue.notify();
				return data;
			} else {
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openadaptor.core.transaction.ITransactional#getResource()
	 */
	public Object getResource() {
		if (isTransacted) {
			return resource;
		} else {
			return null;
		}
	}

}