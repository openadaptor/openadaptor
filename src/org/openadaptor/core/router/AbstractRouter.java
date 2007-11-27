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

package org.openadaptor.core.router;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openadaptor.core.transaction.ITransaction;

/**
 * Shared implementation of a {@link Router} and {@link Pipeline}.
 */
public class AbstractRouter extends Component implements ILifecycleComponentContainer {

  private static Log log = LogFactory.getLog(AbstractRouter.class);

  protected RoutingMap routingMap=new RoutingMap();
  
  private ILifecycleComponentManager componentManager;

  private Map componentMap = new HashMap();
  
  protected IAutoboxer autoboxer=new Autoboxer();
  
  private boolean logDiscardAsInfo = false;
  
  public AbstractRouter() {
    super();
  }

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
          log.info(id + " registered with " + getId());
          componentMap.put(id, processor);
        } 
        else {
          log.info(processor.toString() + " has no id");
        }
      }
    }
  }

  /**
   * Process an incoming msg.
   * <br>
   * This will typically lookup all the IMesssageProcessors which 
   * should be receiving this message, and ask each in turn to
   * process it.
   * @param msg Incoming Message
   * @return Response, usually empty.
   */
  public Response process(Message msg) {
    return process(msg, routingMap.getProcessDestinations((IMessageProcessor)msg.getSender()));
  }

  /**
   * Pass the message to a list of IMessageProcessors, in turn.
   * @param msg
   * @param destinations
   * @return Response - Should be an empty Response assuming all goes well.
   */
  private Response process(Message msg, List destinations) {
    if (log.isDebugEnabled()) {
      logRoutingDebug((IMessageProcessor)msg.getSender(), destinations);
    }
    for (Iterator iter = destinations.iterator(); iter.hasNext();) {
      IMessageProcessor processor = (IMessageProcessor) iter.next();
      process(msg,processor);
    }
    return new Response();
  }
  
  /**
   * Pass a message to an indivual MessageProcessor for processing
   * @param msg Message to be processed
   * @param processor target which should be processing the message.
   */
  private void process(Message msg, IMessageProcessor processor) {
    processResponse(processor,processor.process(msg),msg.getTransaction());
  }

  private void processResponse(IMessageProcessor node, Response response, ITransaction transaction) {
    List batches = response.getBatches();
    for (Iterator iter = batches.iterator(); iter.hasNext();) {
      DataBatch batch = (DataBatch) iter.next();
      if (batch instanceof OutputBatch) {
        process(new Message(batch.getData(),node,transaction),routingMap.getProcessDestinations(node));
      } 
      else if (batch instanceof DiscardBatch) {
        if (logDiscardAsInfo) {
          log.info(node.toString() + " discarded " + batch.size() + " input(s)");
        } 
        else {
          log.debug(node.toString() + " discarded " + batch.size() + " input(s)");
        }
        process(new Message(batch.getData(),node,transaction),routingMap.getDiscardDestinations(node));
      } 
      else if (batch instanceof ExceptionBatch) {
        processExceptions(node, batch.getData(), transaction);
      }
    }
  }
 
  private void processExceptions(IMessageProcessor node, Object[] exceptions, ITransaction transaction) {
    log.warn(node.toString() + " caught " + exceptions.length + " exceptions");
    for (int i = 0; i < exceptions.length; i++) {
      MessageException messageException=(MessageException)exceptions[i];
      List destinations = routingMap.getExceptionDestinations(node, messageException.getException());
      if (destinations.size() > 0) {
        Message msg = new Message(messageException, node, transaction);
        process(msg, destinations);
      } else {
        log.error("uncaught exception from " + node.toString(), messageException.getException());
        throw new RuntimeException("No route defined for Exception "+messageException.getException());
      }
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

  public void setLogDiscardAsInfo(boolean logDiscardAsInfo) {
    this.logDiscardAsInfo = logDiscardAsInfo;
  }
 
// Disabled - these are no longer used. 
//  public Response process(Message msg, String destinationId) throws MessageException {
//    IMessageProcessor processor = (IMessageProcessor) componentMap.get(destinationId);
//    if (processor != null) {
//      return process(msg, processor);
//    } else {
//      throw new RuntimeException("no processor with id " + destinationId);
//    }
//  }
//
//  private Response process(Message msg, IMessageProcessor processor) throws MessageException {
//    Response response = processor.process(msg);
//    if (response.containsExceptions()) {
//      throw (MessageException) response.getCollatedExceptions()[0];
//    } else {
//      processResponse(processor, response, msg.getTransaction());
//      return new Response();
//    }
//  }

}
