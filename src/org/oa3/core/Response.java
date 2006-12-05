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

package org.oa3.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.oa3.core.exception.MessageException;

public class Response {

	public static final Response EMPTY = new Response();
  
  private List currentBatch = null;
	private List batches = new ArrayList();
	private boolean containsDiscards = false;
	private boolean containsExceptions = false;

	public void addOutput(Object data) {
		if (currentBatch == null || currentBatch.getClass() != OutputBatch.class) {
			currentBatch = new OutputBatch();
			batches.add(currentBatch);
		}
		currentBatch.add(data);
	}
	
	public void addDiscardedInput(Object data) {
		if (currentBatch == null || currentBatch.getClass() != DiscardBatch.class) {
			currentBatch = new DiscardBatch();
			batches.add(currentBatch);
		}
		currentBatch.add(data);
		containsDiscards = true;
	}
	
	public void addoutputs(List batch) {
		for (Iterator iter = batch.iterator(); iter.hasNext();) {
			Object data = (Object) iter.next();
			addOutput(data);
		}
	}

	public void addDiscardedInputs(List batch) {
		for (Iterator iter = batch.iterator(); iter.hasNext();) {
			Object data = (Object) iter.next();
			addDiscardedInput(data);
		}
	}

	public void addExceptions(List batch) {
		for (Iterator iter = batch.iterator(); iter.hasNext();) {
			MessageException exception = (MessageException) iter.next();
			addException(exception);
		}
	}

	public void addException(MessageException exception) {
		if (currentBatch == null || currentBatch.getClass() != ExceptionBatch.class) {
			currentBatch = new ExceptionBatch();
			batches.add(currentBatch);
		}
		currentBatch.add(exception);
		containsExceptions = true;
	}
	
	public List getBatches() {
		return Collections.unmodifiableList(batches);
	}
	
	public Object[] getCollatedOutput() {
		List l = getCollatedBatches(OutputBatch.class);
		return l.toArray(new Object[l.size()]);
	}
	
	public Object[] getCollatedDiscards() {
		List l = getCollatedBatches(OutputBatch.class);
		return l.toArray(new Object[l.size()]);
	}
	
	public MessageException[] getCollatedExceptions() {
		List l = getCollatedBatches(ExceptionBatch.class);
		return (MessageException[]) l.toArray(new MessageException[l.size()]);
	}
	
	private List getCollatedBatches(Class c) {
		ArrayList output = new ArrayList();
		for (Iterator iter = batches.iterator(); iter.hasNext();) {
			List batch = (List) iter.next();
			if (batch.getClass() == c) {
				output.addAll(batch);
			}
		}
		return output;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Iterator iter = batches.iterator(); iter.hasNext();) {
			List batch = (List) iter.next();
			buffer.append(buffer.length() > 0 ? "," : "");
			buffer.append(batch.toString());
		}
		return buffer.toString();
	}
	
	public boolean containsExceptions() {
		return containsExceptions;
	}
	
	public boolean containsDiscards() {
		return containsDiscards;
	}
	
	public boolean isEmpty() {
		return batches.size() == 0;
	}
	
	public class OutputBatch extends ArrayList {
		private static final long serialVersionUID = 1L;
		public Object[] getOutput() {
			return toArray(new Object[size()]);
		}
		public String toString() {
			return size() + " output(s)";
		}
	}
	
	public class DiscardBatch extends ArrayList {
		private static final long serialVersionUID = 1L;
		public Object[] getDiscard() {
			return toArray(new Object[size()]);
		}
		public String toString() {
			return size() + " discard(s)";
		}
	}
	
	public class ExceptionBatch extends ArrayList {
		private static final long serialVersionUID = 1L;
		public MessageException[] getMessageExceptions() {
			return (MessageException[]) toArray(new MessageException[size()]);
		}
		public String toString() {
			return size() + " exception(s)";
		}
	}

}
