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

package org.oa3.connector;

import org.oa3.Component;

public class TestReadConnector extends Component implements IReadConnector {

	private int count = 0;

	private int batchSize = 1;

	private int intervalMs = 0;

	private int maxSend = 1;

	private String dataString = "test data %n";
	
	private int exceptionFrequency = 0;

	public TestReadConnector() {
	}

	public TestReadConnector(String id) {
		super(id);
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public void setIntervalMs(final int intervalMs) {
		this.intervalMs = intervalMs;
	}

	public void setMaxSend(final int maxSend) {
		this.maxSend = maxSend;
	}

	public String getDataString() {
		return dataString;
	}

	public void setDataString(String dataString) {
		this.dataString = dataString;
	}

	public void setExceptionFrequency(int frequency) {
		exceptionFrequency = frequency;
	}
	
  public boolean isDry() {
    return count >= maxSend;
  }
  
	public Object[] next(long timeoutMs) {

    if (isDry()) {
      return null;
    }

		// sleep configured time
		if (intervalMs > 0) {
			try {
				Thread.sleep(intervalMs);
			} catch (InterruptedException e) {
			}
		}

		// allocate and populate next batch
		Object[] data = new String[count + batchSize >= maxSend ? maxSend - count : batchSize];
		for (int i = 0; i < data.length; i++) {
			count++;
			if (exceptionFrequency > 0 && (count % exceptionFrequency == 0)) {
				throw new RuntimeException("configured runtime exception");
			}
			
			data[i] = dataString.replaceAll("%n", String.valueOf(count));
		}
		
		return data;
	}

	public void connect() {
	}

	public void disconnect() {
	}

}
