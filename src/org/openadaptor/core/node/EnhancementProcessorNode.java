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
package org.openadaptor.core.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.IEnhancementProcessor;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.exception.MessageException;

/**
 * 
 * 
 * @author Kris Lachor
 * @since Post 3.3
 * TODO javadocs
 */
public final class EnhancementProcessorNode extends Node  implements IMessageProcessor{

  private static final Log log = LogFactory.getLog(EnhancementProcessorNode.class);
  
  IEnhancementProcessor enhancementProcessor;
  
  /**
   * 
   *
   */
  public EnhancementProcessorNode() {
    super();
  }

  /**
   * 
   * @param id
   */
  public EnhancementProcessorNode(String id) {
    super(id);
  }
  
  /**
   * 
   * @param id
   * @param processor
   */
  public EnhancementProcessorNode(String id, IEnhancementProcessor processor) {
    super(id);
    this.enhancementProcessor = processor;
  }

  /**
   * 
   * @param input
   * @return
   */
  public Object [] processSingleInput(Object input){
    IOrderedMap parameters = enhancementProcessor.prepareParameters((IOrderedMap)input);
    enhancementProcessor.getReadConnector().setQueryParameters(parameters);
    Object [] additionalData = enhancementProcessor.getReadConnector().next(1000);
    Object [] outputs = enhancementProcessor.enhance((IOrderedMap)input, additionalData);
    return outputs;
  }
  
  /**
   * 
   */
  public Response process(Message msg) {
    
    Response response = new Response();
    
    Object[] inputs = msg.getData();
    
    // call processor for each element in the batch
    // collate discarded data and exceptions
    
    for (int i = 0; i < inputs.length; i++) {
        try {
            Object[] outputs = processSingleInput(inputs[i]);
   
            if (outputs != null && outputs.length > 0) {
                for (int j = 0; j < outputs.length; j++) {
                    response.addOutput(outputs[j]);
                }
            } else {
                response.addDiscardedInput(inputs[i]);
            }
        } catch (Exception e) {
            response.addException(new MessageException(inputs[i], e, getId()));
        }
    }
        
    if (log.isTraceEnabled()) {
        log.trace(getId() + " processed " + inputs.length + " input(s) = [" + response.toString() + "]");
    }
    
    // if node is chained and there are no exceptions then
    // delegate to next IMessageProcessor in the chain
//    
//    if (messageProcessor != null) {
//      if (!response.containsExceptions()) {
//        if (!response.isEmpty()) {  // Don't pass on the message if there is no data
//          msg = new Message(response.getCollatedOutput(), this, msg.getTransaction());
//                  response = callChainedMessageProcessor(msg);
//        }
//    } else {
//      //Fix for SC22: Invalid cast
//      Object[] exceptions=response.getCollatedExceptions();
//      MessageException exception=(MessageException)exceptions[0];
//      throw new RuntimeException(exception);
//          }
//    }
    
    return response;
  }

  public IEnhancementProcessor getEnhancementProcessor() {
    return enhancementProcessor;
  }

  public void setEnhancementProcessor(IEnhancementProcessor enhancementProcessor) {
    this.enhancementProcessor = enhancementProcessor;
    //TODO
    this.enhancementProcessor.getReadConnector().connect();
  }

  
}
