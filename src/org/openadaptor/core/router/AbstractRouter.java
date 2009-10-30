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
import org.openadaptor.auxil.metrics.ComponentMetricsFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.Response.DataBatch;
import org.openadaptor.core.Response.DiscardBatch;
import org.openadaptor.core.Response.ExceptionBatch;
import org.openadaptor.core.Response.OutputBatch;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.ILifecycleComponentContainer;
import org.openadaptor.core.lifecycle.ILifecycleComponentManager;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.util.ObjectCloner;

/**
 * Shared implementation of a {@link Router} and {@link Pipeline}.
 */
public class AbstractRouter extends Component implements ILifecycleComponentContainer, IRecordableComponent {

  private static Log log = LogFactory.getLog(AbstractRouter.class);

  protected RoutingMap routingMap=new RoutingMap();
  
  private ILifecycleComponentManager componentManager;

  private Map componentMap = new HashMap();
  
  protected IAutoboxer autoboxer=new Autoboxer();
  
  private IComponentMetrics metrics = ComponentMetricsFactory.newAggregateMetrics(this);
  
  /**
   * If set to true, discarding messages will be looged in INFO level. Default level is DEBUG
   */
  private boolean logDiscardAsInfo = false;
  
  private boolean ignoreExceptionProcessorErrors = false;

  /** Metrics disabled by default .*/
  private boolean metricsEnabled = false;
  
  /** Recording message history disabled by default */
  private boolean historyEnabled = false;
  
  /** See description at the setter method. */
  private boolean cloneMetadataOnFanout = false;
  
  /** 
   * Cloner for optional cloning of message metadata 
   * 
   * @see #setCloneMetadataOnFanout(boolean)
   */
  private ObjectCloner cloner=new ObjectCloner();
  
  /**
   * Constructor.
   */
  public AbstractRouter() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id - a component id.
   */
  public AbstractRouter(String id) {
    super(id);
  }
  
  protected void setRoutingMap(final RoutingMap routingMap) {
    this.routingMap = routingMap;
  }
  
  protected RoutingMap getRoutingMap(){
    return this.routingMap;
  }

  public void setComponentManager(ILifecycleComponentManager manager) {
    this.componentManager=manager;
    registerComponents();
  }

  private void registerComponents() {
    componentMap.clear();
    for (Iterator it=routingMap.getMessageProcessors().iterator();it.hasNext();){
      Object processor=it.next();
      if (processor instanceof ILifecycleComponent){
        componentManager.register((ILifecycleComponent)processor);
      } 
      else {
        log.info("Not registering (non-ILifecycleComponent) processor "+processor);
      }
      if (processor instanceof IComponent) {
        String id = ((IComponent)processor).getId();
        if (id != null) {
          String routerId = getId()!=null ? getId() : "Router";
          log.info(id + " registered with " + routerId);
          componentMap.put(id, processor);
        } 
        else {
          log.info(processor.toString() + " has no id");
        }
      }
    }
  }

  /**
   * Process an incoming message.
   * <br>
   * This will typically lookup all the IMesssageProcessors which 
   * should be receiving this message, and ask each in turn to
   * process it.
   * 
   * @param msg Incoming Message
   * @return Response, usually empty.
   */
  public Response process(Message msg) {
    // First look to see if the (real) componentMap has a component with the same ID as the message sender.
    // If this is the case then this is going to be the one in the routing map.
    IMessageProcessor realSender = (IMessageProcessor) componentMap.get(((IComponent)msg.getSender()).getId());
    if (realSender == null ) { 
      // Looks like the componentMap was not set up properly. Use the sender in the message.
      // Basically we are reverting to the way things worked prior to this change.
      realSender = (IMessageProcessor)msg.getSender();
    }
    // return process(msg, routingMap.getProcessDestinations((IMessageProcessor)msg.getSender()));
    metrics.recordMessageStart(msg);
    recordMessageHistory(msg, realSender);
    
    Response response = process(msg, routingMap.getProcessDestinations(realSender));
    metrics.recordMessageEnd(msg, response);
    return response;
  }

  /**
   * Pass the message to a list of IMessageProcessors, in turn. If dealing with a fan-out
   * (splitting of processing pipeline into branches) it optionally, i.e. depending
   * of the value of <code>cloneMetadataOnFanout</code> flag, clone the metadata to 
   * prevent sharing it between components in different branches. 
   * 
   * @param msg
   * @param destinations
   * @return Response - Should be an empty Response assuming all goes well.
   */
  private Response process(Message msg, List destinations) {
    if (log.isDebugEnabled()) {
      logRoutingDebug((IMessageProcessor)msg.getSender(), destinations);
    }
   
    Map [] metadataClones = null;
    boolean fanout = destinations!=null && destinations.size()>1;
    
    /* 
     * If fan-out and cloneMetadataOnFanout is ON create copies of meta data 
     * for every branch. 
     */
    if(fanout && cloneMetadataOnFanout){
      metadataClones = new Map[destinations.size()];
      for(int i=0; i< destinations.size(); i++){
        metadataClones[i] = (Map) cloner.clone(msg.getMetadata());
      }
    }
    int destinationNo = 0;
    for (Iterator iter = destinations.iterator(); iter.hasNext();) {
      IMessageProcessor processor = (IMessageProcessor) iter.next();
      
      /* Overwrite metadata with a clone if necessary*/
      if(fanout && cloneMetadataOnFanout){
        msg.setMetadata(metadataClones[destinationNo++]);
      }
      
      /* Record message history */
      recordMessageHistory(msg, processor);
      
      process(msg,processor);
    }
    return new Response();
  }
  
  /**
   * Pass a message to an individual MessageProcessor for processing
   * @param msg Message to be processed
   * @param processor target which should be processing the message.
   */
  protected void process(Message msg, IMessageProcessor processor) {
    processResponse(processor,processor.process(msg),msg.getTransaction());
  }

  private void processResponse(IMessageProcessor node, Response response, ITransaction transaction) {
    List batches = response.getBatches();
    for (Iterator iter = batches.iterator(); iter.hasNext();) {
      DataBatch batch = (DataBatch) iter.next();
      if (batch instanceof OutputBatch) {
        process(new Message(batch.getData(),node,transaction, response.getMatadata()),routingMap.getProcessDestinations(node));
      } 
      else if (batch instanceof DiscardBatch) {
        if (logDiscardAsInfo) {
          log.info(node.toString() + " discarded " + batch.size() + " input(s)");
        } 
        else {
          log.debug(node.toString() + " discarded " + batch.size() + " input(s)");
        }
        process(new Message(batch.getData(),node,transaction, response.getMatadata()),routingMap.getDiscardDestinations(node));
      } 
      else if (batch instanceof ExceptionBatch) {
        processExceptions(node, batch.getData(), transaction);
      }
    }
  }
 
  private void processExceptions(IMessageProcessor node, Object[] exceptions, ITransaction transaction) {
    log.warn(node.toString() + " caught " + exceptions.length + " exception(s). Passing to the exception processor.");
    for (int i = 0; i < exceptions.length; i++) {
      MessageException messageException=(MessageException)exceptions[i];
      List destinations = routingMap.getExceptionDestinations(node, messageException.getException());
      if (destinations.size() > 0) {
        Message msg = new Message(messageException, node, transaction, null);
        process(msg, destinations);
      } else {
        log.error("uncaught exception from " + node.toString(), messageException.getException());
        
        /* 
         * Check if the exception in question was from the exceptionProcessor (if one is set)
         * and if so - if it should be ignored.
         */
        if(routingMap.isAnExceptionProcessor(node) && ignoreExceptionProcessorErrors){
          log.error("Ignoring exception from the exceptionProcessor", messageException.getException());	
        }
        else{
          throw new RuntimeException("No route defined for Exception "+messageException.getException());
        }
      }
    }
    log.info("Processing exceptions complete.");
  }
  
  /**
   * Records message history in the metadata if the historyEnabled flag is on.
   * 
   * @param msg currently processed message.
   * @param processor currently processing IMessageProcessor.
   */
  private void recordMessageHistory(Message msg, IMessageProcessor processor){
	if(!historyEnabled || msg==null || msg.getMetadata()==null){
      return;
	}
	Object history = msg.getMetadata().get(Message.MESSAGE_HISTORY_KEY);
	List historyList = null;
    if(history==null){
	  historyList = new ArrayList();
	  msg.getMetadata().put(Message.MESSAGE_HISTORY_KEY, historyList);
    }
    else{
      historyList = (List) history;
    }
    
    if(processor instanceof IComponent){
	  historyList.add(((IComponent) processor).getId());
    }
    else{
      historyList.add(Message.UNNAMED_MESSAGE_PROCESSOR);
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
  
  public Collection getMessageProcessors() {
    return routingMap.getMessageProcessors();
  }

  /**
   * If set to true, discarding messages will be looged in INFO level. Default level is DEBUG
   */
  public void setLogDiscardAsInfo(boolean logDiscardAsInfo) {
    this.logDiscardAsInfo = logDiscardAsInfo;
  }

  /**
   * If set to true and an exception processor is set on the router, all exceptions
   * thrown from the exception processor will only be logged - they won't cause the 
   * adaptor to shut down instantaneously. Default is false.
   */
  public void setIgnoreExceptionProcessorErrors(
		boolean ignoreExceptionProcessorErrors) {
	this.ignoreExceptionProcessorErrors = ignoreExceptionProcessorErrors;
  }

  /**
   * @see IRecordableComponent#getMetrics()
   */
  public IComponentMetrics getMetrics() {
    return metrics;
  }

  /**
   * @see IRecordableComponent#isMetricsEnabled()
   */
  public boolean isMetricsEnabled() {
    return metrics.isMetricsEnabled();
  }

  /**
   * @see IRecordableComponent#setMetricsEnabled(boolean)
   */
  public void setMetricsEnabled(boolean metricsEnabled) {
    metrics.setMetricsEnabled(metricsEnabled);
  }

  /**
   * If set to true, metadata will be cloned on every entry to a new branch in the pipeline (fanout).
   * For efficiency reasons (many adaptors may use fan-outs but not use the metadata 
   * feature) the default value is false.
   */
  public void setCloneMetadataOnFanout(boolean cloneMetadataOnFanout) {
    this.cloneMetadataOnFanout = cloneMetadataOnFanout;
  }

  /**
   * @return flag stating if history is recorded for each message traversing an adaptor.
   */
  public boolean isHistoryEnabled() {
	return historyEnabled;
  }

  /**
   * Enables recording history for each message passing through an adaptor.
   * By default it is disabled.
   */
  public void setHistoryEnabled(boolean historyEnabled) {
	this.historyEnabled = historyEnabled;
  }
  
}
