/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IEnrichmentProcessor;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.node.EnrichmentProcessorNode;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.Node;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.node.ProcessorNode;

/**
 * Implementation of {@link IAutoboxer} that wraps the following
 * <li>{@link IReadConnector}
 * <li>{@link IDataProcessor}
 * <li>{@link IWriteConnector}
 * <br/>as
 * <li>{@link ReadNode}
 * <li>{@link ProcessorNode}
 * <li>{@link WriteNode}
 * <br/>respectively.
 * Also wraps single values as unary element lists and various other things that allow
 * users to simplify the configuration of an {@link IRoutingMap}.
 * @author perryj
 *
 */
public class Autoboxer implements IAutoboxer {

	private Map nodeMap = new HashMap();
	
	public Map autobox(Map map) {
		HashMap newMap = new HashMap();
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			newMap.put(autobox(entry.getKey()), autobox(entry.getValue()));
		}
		return newMap;
	}

	public List autobox(List list) {
		ArrayList newList = new ArrayList();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			newList.add(autobox(iter.next()));
		}
		return newList;
	}
	
	public Object autobox(Object o) {
		if (o instanceof Map) {
			o = autobox((Map)o);
		} else if (o instanceof List) {
			o = autobox((List)o);
		} else if (o instanceof IReadConnector) {
			o = autobox((IReadConnector) o);
		} else if (o instanceof IDataProcessor) {
			o = autobox((IDataProcessor) o);
		} else if (o instanceof IWriteConnector) {
			o = autobox((IWriteConnector) o);
        } else if (o instanceof IEnrichmentProcessor) {
            o = autobox((IEnrichmentProcessor) o);
        }
		return o;
	}

	public Node autobox(IDataProcessor processor) {
        Node node = (Node) nodeMap.get(processor);
		if (node == null) {
			node = new ProcessorNode();
			node.setProcessor(processor);
			nodeMap.put(processor, node);
		}
		return node;
	}

  public Node autobox(IReadConnector connector) {
  	ReadNode node = (ReadNode) nodeMap.get(connector);
  	if (node == null) {
  		node = new ReadNode();
  		node.setConnector(connector);
  		nodeMap.put(connector, node);
  	}
  	return node;
  }

  public Node autobox(IWriteConnector connector) {
  	WriteNode node = (WriteNode) nodeMap.get(connector);
  	if (node == null) {
  		node = new WriteNode();
  		node.setConnector(connector);
  		nodeMap.put(connector, node);
  	}
  	return node;
  }

  public Node autobox(IEnrichmentProcessor enrichmentProcessor) {
    EnrichmentProcessorNode node = (EnrichmentProcessorNode) nodeMap.get(enrichmentProcessor);
    if (node == null) {
        node = new EnrichmentProcessorNode();
        node.setEnrichmentProcessor(enrichmentProcessor);
        nodeMap.put(enrichmentProcessor, node);
    }
    return node;
  }



}
