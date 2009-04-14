/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.processor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ComponentException;

/**
 * Utility {@link IDataProcessor} to provide synchronised access to any configured
 * processor.
 * <br>
 * This processor may prove useful when access to a given processor from more than one thread 
 * needs to be managed. The obvious example of this is when multiple read connectors are driving
 * messages through the same processor (i.e. multiple threads sharing the same processing
 * component).
 * <br>
 * It works by simply synchronising acccess to the configured processor.
 * @author Eddy Higgins
 */
public class SyncProcessor extends Component implements IDataProcessor {

	private static Log log = LogFactory.getLog(SyncProcessor.class);

	static String name = SyncProcessor.class.getName();

	/**
	 * Configured data processor.
	 * It is mandatory
	 */
	private IDataProcessor processor;

	/**
	 * Assigns the {@link IDataProcessor} which is to be synchronised
	 */
	public void setProcessor(IDataProcessor processor) {
		this.processor = processor;
	}

	/**
	 * returns the configured {@link IDataProcessor}
	 */
	public IDataProcessor getProcessor() {
		return (processor);
	}

	//END   Bean getters/setters

	/**
	 * Ensures that processor property has been set, and delegates the  
	 * validate call to it.
	 * <p/>
	 *
	 */
	public void validate(List exceptions) {
		if ( processor == null ) {
			log.warn("No processor configured. The attribute is mandatory");
			exceptions.add(new ComponentException("processor property is missing, but is mandatory",this) );
		} 
		else {
			processor.validate(exceptions);
		}
	}

	/**
	 * delegates the call to configured {@link IDataProcessor}s
	 */
	public void reset(Object context) {
		processor.reset(context);
	}

	/**
	 * Make a synchronised call the to the process method of the 
	 * configured processor.
	 * @param record - Object containing the record
	 *
	 * @return record array, empty if the record is to be skipped.
	 */
	public Object[] process(Object record) {
		synchronized (processor) {
			return processor.process(record);
		}
	}

	public String toString() {
		String processorString=processor==null?"<null>":processor.toString();
		return ("SyncProcessor "+processorString);
	}
}
