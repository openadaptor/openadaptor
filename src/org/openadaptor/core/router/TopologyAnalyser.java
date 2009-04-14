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

package org.openadaptor.core.router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;

/**
 * This analyses the topology of a RoutingMap.
 * This class is currently a work in progress.
 * It takes no account of exception handling as yet.
 * @author Eddy Higgins
 *
 */
public class TopologyAnalyser  {
  private static final Log log = LogFactory.getLog(TopologyAnalyser.class);

  private Map topology;
  private List readers;
  private List writers;
  private List processors;

  public TopologyAnalyser(IRoutingMap map) {
    initialise(map);
  }
  private void initialise(IRoutingMap map) {
    buildTopology(map);
    readers=new ArrayList();
    writers=new ArrayList();
    processors=new ArrayList();
    Iterator it=topology.entrySet().iterator();
    while(it.hasNext()) {
      Map.Entry entry=(Map.Entry)it.next();
      IMessageProcessor processor=(IMessageProcessor)entry.getKey();
//      if (processor instanceof IRunnable) {
//        log.debug("Processor "+processor+" is runnable");
//      }
      TopologyInfo ti=(TopologyInfo)entry.getValue();
      boolean isReader=ti.getInputs().isEmpty();
      boolean isWriter=ti.getOutputs().isEmpty();
      boolean isProcessor=!(isReader || isWriter);
      if (isReader) {
        readers.add(processor);
        if (!(processor instanceof ReadNode)) {
          log.debug("Suspicious: Processor not ReadNode but has no inputs: "+processor);
        }
      }
      if (isWriter) {
        writers.add(processor);
        if (!(processor instanceof WriteNode)) {
          log.debug("Suspicious: Processor not WriteNode but has no outputs: "+processor);
        }
      }
      if (isProcessor) {
        processors.add(processor);
        if (processor instanceof WriteNode) {
          log.debug("Suspicious: Processor is WriteNode but has outputs: "+processor);
        }
        if (processor instanceof ReadNode) {
          log.debug("Suspicious: Processor is ReadNode but has inputs: "+processor);
        }
      }
    }
  }

  private void buildTopology(IRoutingMap map) {
    topology=new HashMap();
    Collection processors=map.getMessageProcessors();

    Iterator it=processors.iterator();
    //Get the direct connections
    while (it.hasNext()) {
      IMessageProcessor processor=(IMessageProcessor)it.next();
      link(processor,map.getProcessDestinations(processor));
      link(processor,map.getDiscardDestinations(processor));
    }
    //Now get the indirect ones
    it=processors.iterator();
  }
  
  private void link(IMessageProcessor src,List destinations) {
    TopologyInfo srcTi=getTopologyInfo(src);
    srcTi.addOutputs(destinations);
    Iterator it=destinations.iterator();
    while(it.hasNext()) {
      IMessageProcessor destination=(IMessageProcessor)it.next();
      TopologyInfo dstTi=getTopologyInfo(destination);
      dstTi.addInput(src);
    }
  }

  private List getIndirectInputs(IMessageProcessor processor) {
    List results=new ArrayList();
    findIndirect(results,processor);
    return results;
  }
  private void findIndirect(List inputs,IMessageProcessor processor) {
    TopologyInfo ti=getTopologyInfo(processor);
    Iterator it=ti.getInputs().iterator();
    while(it.hasNext()) {
      Object input= it.next();
      if (inputs.contains(input)){
        log.debug(processor+" lists "+input+ " more than once as an indirect dependent");
      }
      else {
        inputs.add(input);
        findIndirect(inputs,(IMessageProcessor)input);
      }
    }
  }

  /**
   * Retrieve the Topology Information for a given processor, creating a new entry
   * if necesary.
   * @param processor
   * @return
   */
  private TopologyInfo getTopologyInfo(IMessageProcessor processor) {
    TopologyInfo topologyInfo;
    if (!topology.containsKey(processor)) {
      topologyInfo=new TopologyInfo();
      topology.put(processor, topologyInfo);
    }
    else {
      topologyInfo=(TopologyInfo)topology.get(processor);
    }
    return topologyInfo;
  }

  public String toString() {
    StringBuffer sb=new StringBuffer("Topology[size=");
    sb.append(topology.size());
    sb.append(";Readers=").append(readers.size());
    sb.append(";Writers=").append(writers.size());
    sb.append(";Processors=").append(processors.size());
    return sb.append("]").toString();
  }
  public String toStringVerbose() {
    StringBuffer sb=new StringBuffer(toString());
    sb.append('\n');
    Iterator it=topology.keySet().iterator();
    while (it.hasNext()) {
      IMessageProcessor processor=(IMessageProcessor)it.next();
      sb.append(showIndirectInputs(processor)).append('\n');
    }
    return sb.toString();
  }
  public String showIndirectInputs(IMessageProcessor processor) {
    StringBuffer sb=new StringBuffer(processor.toString()).append(":");
    Iterator it=getIndirectInputs(processor).iterator();
    while(it.hasNext()) {
      Object next=it.next();
      sb.append(next);
      if (next instanceof IRunnable) {
        sb.append("[R]");
      }
      sb.append(',');
    }
    return sb.toString();
  }

  class TopologyInfo {
    private List inputs=new ArrayList();
    private List outputs=new ArrayList();
 
    public List getInputs() {
      return inputs;
    }
    public List getOutputs(){return outputs;}
    public void addInput(IMessageProcessor input) {
      inputs.add(input);
    }
    public void addOutput(IMessageProcessor output) {outputs.add(output);}
    //public void addInputs(Collection inputs) {this.inputs.addAll(inputs);}
    public void addOutputs(Collection outputs) {this.outputs.addAll(outputs);}	
  }
}
