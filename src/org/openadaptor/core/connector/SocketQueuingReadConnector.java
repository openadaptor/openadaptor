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
 * Socket reader variation of the base queuing read connector, added extra.
 * 
 * @author Dealbus Dev
 */
public abstract class SocketQueuingReadConnector extends
		AbstractQueuingReadConnector {

	private static final Log log = LogFactory
			.getLog(SocketQueuingReadConnector.class);

	/**
	 * The pseudo transaction flag, to simulate a legacy pseudo transaction
	 * behaviour.
	 */
	private boolean pseudoTransaction = false;

	/**
	 * Indicator, if the payload for the pseudo transaction is received.
	 */
	private boolean payloadReceived = false;

	/**
	 * The pseudo transaction timeout ms. The value '0' means, to wait for a
	 * pseudo response. A value of ms greater than zero means, that the reader
	 * will fail, when no COMMIt or rollback is received.
	 */
	private int pseudoTransactionTimeoutMs = 0;

	/**
	 * implementation of {@link ITransactionalResource}, which if the component
	 * is transacted will be returned when {@link #getResource()} is called
	 */
	private SocketQueueTransactionalResource resource = new SocketQueueTransactionalResource();

	/**
	 * The used pseudo transactional resource, when theread does not work real
	 * transacted.
	 */
	private SocketQueuePseudoTransactionalResource pseudoResource = new SocketQueuePseudoTransactionalResource();

	/**
	 * Default C'Tor.
	 */
	protected SocketQueuingReadConnector() {
		// empty
	}

	/**
	 * C'Tor with given component id.
	 * 
	 * @param id
	 *            The given component id.
	 */
	protected SocketQueuingReadConnector(String id) {
		super(id);
	}

	/**
	 * Returns the pseudo transaction timeout ms.
	 * 
	 * @return pseudoTransactionTimeoutMs
	 */
	public int getPseudoTransactionTimeoutMs() {
		return pseudoTransactionTimeoutMs;
	}

	/**
	 * Set the pseudo transaction timeout ms, when given value is low then zero,
	 * then set the default to 0.
	 * 
	 * @param pseudoTransactionTimeoutMs
	 *            the new pseudo transaction timeout ms.
	 */
	public void setPseudoTransactionTimeoutMs(int pseudoTransactionTimeoutMs) {
		this.pseudoTransactionTimeoutMs = (pseudoTransactionTimeoutMs > 0 ? pseudoTransactionTimeoutMs
				: 0);
	}

	/**
	 * Get the pseudo transaction flag. Value 'true' simulates a legacy pseudo
	 * transaction behaviour.
	 * 
	 * @return The pseudo transaction flag value.
	 */
	public boolean isPseudoTransaction() {
		return pseudoTransaction;
	}

	/**
	 * Set the flag to 'true', when a pseudo transaction behaviour is used.
	 * 
	 * @param pseudoTransaction
	 *            The new pseudo transaction flag value.
	 */
	public void setPseudoTransaction(boolean pseudoTransaction) {
		this.pseudoTransaction = pseudoTransaction;
	}

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
				payloadReceived = true;
				queue.notify();
				return data;
			} else {
				// Do NOT set isDry to true as the socket writer may still be
				// writing to the socket being read, but is just a bit slow
				// Setting isDry to true here may cause the socket reader to
				// close too early
				// Need to find a way/place to test if the socket that you are
				// reading is still 'live'
				// setIsDry(true);
				payloadReceived = false;
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
		} else if (isPseudoTransaction()) {
			return pseudoResource;
		} else {
			return null;
		}
	}

	/**
	 * {@link ITransactionalResource} implementation. One of these is returned
	 * if the ReadConnector is transacted. When {@link QueueItem}s are dequeued
	 * they are added to an instance of this class. This class will set the
	 * processing state of the QueueItem and allow the queueing thread block for
	 * that event. If a transaction has not begun then it simply sets the state
	 * to COMMITTED straight away. However if a transaction has been begin then
	 * the state will only be set once commit/rollback is called on this
	 * resource.
	 */
	class SocketQueueTransactionalResource extends QueueTransactionalResource {

		public synchronized void commit() {
			super.commit();
			pseudoCommit();
		}

		public synchronized void rollback(Throwable t) {
			super.rollback(t);
			pseudoRollback();
		}

		// do pseudo commit (log and send 'COMMIT' message)
		protected void pseudoCommit() {
			log.debug("Commit called on [" + getId() + "]");
			if (isPseudoTransaction() && payloadReceived) {
				/**
				 * Otherwise we will send a COMMIT back even when there was
				 * nothing received
				 */
				log.debug("Sending COMMIT to socket");
				sendCommitRollback("COMMIT");
			}
		}

		// do pseudo rollback (log and send 'ROLLBACK' message)
		protected void pseudoRollback() {
			log.debug("Rollback called on [" + getId() + "]");
			if (isPseudoTransaction() && payloadReceived) {
				log.debug("Sending ROLLBACK to socket");
				sendCommitRollback("ROLLBACK");
			}
		}
	}

	/**
	 * The pseudo transactional resource, used when the reader does not work
	 * transacted and only pseudo transacted for legacy socket communication
	 */
	protected class SocketQueuePseudoTransactionalResource extends
			SocketQueueTransactionalResource {

		// no synchronized transaction required
		public synchronized void begin() {
		}

		// do pseudo commit
		public void commit() {
			pseudoCommit();
		}

		// do pseudo rollback
		public void rollback(Throwable e) {
			pseudoRollback();
		}
	}

	protected void sendCommitRollback(String string) {
	}
}
