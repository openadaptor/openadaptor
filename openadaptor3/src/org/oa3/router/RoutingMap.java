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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oa3.IMessageProcessor;

public class RoutingMap implements IRoutingMap {

	public static final String DEFAULT_KEY = "*";
	
	private Set processors = new HashSet();
	
	private Map processMap = new HashMap();
	
	private Map discardMap = new HashMap();

	private Map exceptionMap = new HashMap();
	
	private IAutoboxer autoboxer;
	
	public RoutingMap(final IAutoboxer autoboxer) {
		this.autoboxer = autoboxer;
	}
	
	public RoutingMap() {
		this(new Autoboxer());
	}
	
	public void setProcessMap(Map map) {
		processMap.clear();
		populateMap(map, processMap);
	}

	public void setDiscardMap(Map map) {
		discardMap.clear();
		populateMap(map, discardMap);
	}

	public void setExceptionMap(Map map) {
		exceptionMap.clear();
		map = autoboxer.autobox(map);
		if (isMapOfMaps(map)) {
			for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (!(entry.getKey() instanceof IMessageProcessor) && !DEFAULT_KEY.equals(entry.getKey())) {
					throw new RuntimeException("key " + entry.getKey().toString() + " is not IMessageProcessor");
				}
				OrderedExceptionMap oemap = new OrderedExceptionMap((Map)entry.getValue());
				exceptionMap.put(entry.getKey(), oemap);
			}
		} else {
			OrderedExceptionMap oemap = new OrderedExceptionMap(map);
			exceptionMap.put(DEFAULT_KEY, oemap);
		}
	}

	public Collection getMessageProcessors() {
		return Collections.unmodifiableCollection(processors);
	}
	
	public List getProcessDestinations(IMessageProcessor processor) {
		List l = (List) processMap.get(processor);
		return l != null ? l : Collections.EMPTY_LIST;
	}

	public List getDiscardDestinations(IMessageProcessor processor) {
		List l = (List) discardMap.get(processor);
		return l != null ? l : Collections.EMPTY_LIST;
	}

	public List getExceptionDestinations(IMessageProcessor processor, Throwable exception) {
		OrderedExceptionMap map = (OrderedExceptionMap) exceptionMap.get(processor);
		if (map == null) {
			map = (OrderedExceptionMap) exceptionMap.get(DEFAULT_KEY);
		}
		if (map != null) {
			return map.getDestinations(exception);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * checks that the keys and values are actually IMessageProcessor instances and autoboxes
	 * single IMessageProcessor values into a unary list
	 */
	private void populateMap(Map map, Map checkedMap) {
		map = autoboxer.autobox(map);
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			verifyEntryKeyIsIMessageProcessor(entry);
			IMessageProcessor fromProcessor = (IMessageProcessor) entry.getKey();
			List processorList = autoboxIMessageProcessorList(entry.getValue());
			processors.add(fromProcessor);
			processors.addAll(processorList);
			checkedMap.put(fromProcessor, processorList);
		}
	}

	private void verifyEntryKeyIsIMessageProcessor(Map.Entry entry) {
		if (!(entry.getKey() instanceof IMessageProcessor)) {
			throw new RuntimeException("key " + entry.getKey().toString() 
					+ " " + entry.getKey().getClass().getName() + " is not IMessageProcessor");
		}
	}
	
	private boolean isMapOfMaps(Map map) {
		boolean result = map.size() > 0;
		for (Iterator iter = map.values().iterator(); iter.hasNext();) {
			result &= iter.next() instanceof Map;
		}
		return result;
	}
	
	class OrderedExceptionMap {
		private List mExceptions = new ArrayList();
		private Map mExceptionMap = new HashMap();
		
		OrderedExceptionMap(Map map) {
			for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				try {
					Class exceptionClass = Class.forName(entry.getKey().toString());
					if (Throwable.class.isAssignableFrom(exceptionClass)) {
						List processorList = autoboxIMessageProcessorList(entry.getValue());
						processors.addAll(processorList);
						mExceptionMap.put(exceptionClass, processorList);
						mExceptions.add(exceptionClass);
					} else {
						throw new RuntimeException(entry.getKey().toString() + " is not throwable");
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		List getDestinations(Throwable exception) {
			List l = (List) mExceptionMap.get(exception.getClass());
			for (Iterator iter = mExceptions.iterator(); l == null && iter.hasNext();) {
				Class exceptionClass = (Class) iter.next();
				if (exceptionClass.isAssignableFrom(exception.getClass())) {
					l = (List) mExceptionMap.get(exceptionClass);
				}
			}
			return l != null ? l : Collections.EMPTY_LIST;
		}

	}

	private static List autoboxIMessageProcessorList(Object value) {
		List list = null;
		if (value instanceof List) {
			list = (List) value;
		} else {
			list = new ArrayList();
			list.add(value);
		}
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object element = (Object) iterator.next();
			if (!(element instanceof IMessageProcessor)) {
				throw new RuntimeException("value " + element.toString() + " is not IMessageProcessor");
			}
		}
		return list;
	}

}
