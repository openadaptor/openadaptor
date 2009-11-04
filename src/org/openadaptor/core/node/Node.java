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

package org.openadaptor.core.node;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IEnrichmentProcessor;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.jmx.Administrable;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.ILifecycleListener;
import org.openadaptor.core.lifecycle.LifecycleComponent;
import org.openadaptor.auxil.metrics.ComponentMetricsFactory;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;
import org.openadaptor.core.recordable.ISimpleComponentMetrics;
import org.openadaptor.core.router.Router;

/**
 * The class which brings together the {@link IMessageProcessor} and
 * {@link ILifecycleComponent} interfaces. Specific data processing is actually
 * delegated to an {@link IDataProcessor} but the Node is responsbile for the
 * following
 * 
 * <li>propogation of lifecycle management to the IDataProcessor
 * <li>data un-batching
 * <li>transaction enlisting
 * <li>exception capture
 * <li>discard capture
 * 
 * <br/><br/>This allows implementations of {@link IDataProcessor},
 * {@link IReadConnector}, {@link IWriteConnector} and {@link IEnrichmentProcessor}
 * to be as lightweight as possible and free from the specifics of the adaptor framework.
 * 
 * <br/>It is also possible to "chain" a Node to another IMessageProcessor, this
 * is how {@link ReadNode}s initiate new message processing. This is different
 * from {@link ProcessorNode}s and {@link WriteNode} which process
 * {@link Message}s.
 * 
 * @author perryj
 * @see Adaptor
 * @see Router
 */
public class Node extends LifecycleComponent implements IMessageProcessor, Administrable, IRecordableComponent {
     
	private static final Log log = LogFactory.getLog(Node.class);
	
	private IMessageProcessor messageProcessor;

	private IDataProcessor processor;

    /** Metrics associated with this node. */
    private IComponentMetrics metrics = (IComponentMetrics) ComponentMetricsFactory.newStandardMetrics(this);
    
    /**
     * Constructor.
     * 
     * @param id an identifier.
     * @param processor an IDataProcessor 'wrapped' by this node. 
     * @param next 
     */
	public Node(final String id, final IDataProcessor processor, final IMessageProcessor next) {
		super(id);
		this.processor = processor != null ? processor : IDataProcessor.NULL_PROCESSOR;
		this.messageProcessor = next;
	}

	public Node(final String id, final IDataProcessor processor) {
		this(id, processor, null);
	}

	public Node(String id) {
		this(id, null, null);
	}
	
	public Node() {
		this(null, null, null);
	}
	
	public void setMessageProcessor(IMessageProcessor processor) {
		this.messageProcessor = processor;
	}

    
    /**
     * Sets a processor. Also adds it as a listener to lifecycle changes of this node
     * if the processor is a {@link ILifecycleListener}.
     * 
     * @param processor
     */
	public void setProcessor(IDataProcessor processor) {
		this.processor = processor;
        
        /* 
         * If the processor is interested in this node's state
         * changes, add it as a listener 
         */
        if(processor instanceof ILifecycleListener){
          addListener((ILifecycleListener)processor);
        }
	}
    
    public IDataProcessor getProcessor(){
        return this.processor;
    }
    
    /**
     * Processes individual record of input data. 
     * This was put in a separate method since some Nodes (that extend from this one)
     * may want to do it in a different way.
     * 
     * @param record input record
     * @return result from the processor.
     */
    protected Object [] processSingleRecord(Object record){
      return processor.process(record);
    }
    

	public Response process(Message msg) {
      
		metrics.recordMessageStart(msg);
      
		Response response = new Response();
		
        response.setMatadata(msg.getMetadata());
        
        if(processor instanceof IMetadataAware){
          ((IMetadataAware) processor).setMetadata(msg.getMetadata());
        }
        
		Object[] inputs = msg.getData();
		
		// call processor for each element in the batch
		// collate discarded data and exceptions
		
		for (int i = 0; i < inputs.length; i++) {
			try {
                Object[] outputs = processSingleRecord(inputs[i]);
				if (outputs != null && outputs.length > 0) {
					for (int j = 0; j < outputs.length; j++) {
						response.addOutput(outputs[j]);
					}
                    metrics.recordMessageEnd(msg, response);
				} else {
					response.addDiscardedInput(inputs[i]);
                    metrics.recordDiscardedMsgEnd(msg);
				}
			} catch (Exception e) {
				response.addException(new MessageException(inputs[i], msg.getMetadata(), e, getId(), fetchThreadName()));
                metrics.recordExceptionMsgEnd(msg);
			}
		}
			
		if (log.isTraceEnabled()) {
			log.trace(getId() + " processed " + inputs.length + " input(s) = [" + response.toString() + "]");
		}
		
		// if node is chained and there are no exceptions then
		// delegate to next IMessageProcessor in the chain
		
		if (messageProcessor != null) {
          if (!response.containsExceptions()) {
            if (!response.isEmpty()) {  // Don't pass on the message if there is no data
              msg = new Message(response.getCollatedOutput(), this, msg.getTransaction(), msg.getMetadata());
          	  response = callChainedMessageProcessor(msg);
            }
          } else {
            //Fix for SC22: Invalid cast
            Object[] exceptions=response.getCollatedExceptions();
            MessageException exception=(MessageException)exceptions[0];
            throw new RuntimeException(exception);
          }
		}
		
		return response;
	}

  protected Response callChainedMessageProcessor(Message msg) {
    return messageProcessor.process(msg);
  }
  
  public void validate(List exceptions) {
  	super.validate(exceptions);
  	processor.validate(exceptions);
  }
  
  protected void resetProcessor(Object context) {
    processor.reset(context);
  }

  protected String getProcessorId() {
    if (processor instanceof IComponent) {
      return ((IComponent)processor).getId();
    }
    return null;
  }
  
  protected String fetchThreadName() {
    return Thread.currentThread().getName();
  }
  
  /**
   * @return metrics associated with this Node.
   * @see IRecordableComponent
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

  protected IRecordableComponent getRecordableComponent(){
    return this;
  }
  
  protected void setMetrics(IComponentMetrics metrics){
    this.metrics = metrics;
  }
  
  /**
   * @return inner class instance that implements Component.AdminMBean.
   */
  public Object getAdmin() {
    return new Admin();
  }

  /**
   * MBean interface exposed via JMX console.
   */
  public interface AdminMBean extends LifecycleComponent.AdminMBean, ISimpleComponentMetrics{
  }
 
  /**
   * Implementation of the interface exposed via JMX. 
   */
  public class Admin extends LifecycleComponent.Admin implements AdminMBean {

    /**
     * @see ISimpleComponentMetrics#getProcessTime()
     */
    public String getProcessTime() {
      return getMetrics().getProcessTime();
    }

    /**
     * @see ISimpleComponentMetrics#getIntervalTime()
     */
    public String getIntervalTime() {
      return getMetrics().getIntervalTime();
    }

    /**
     * @see ISimpleComponentMetrics#getUptime()
     */
    public String getUptime() {
      return getMetrics().getUptime();
    }

    /**
     * @see ISimpleComponentMetrics#getInputMsgs()
     */
    public String getInputMsgs() {
      return getMetrics().getInputMsgs();
    }

    /**
     * @see ISimpleComponentMetrics#getOutputMsgs()
     */
    public String getOutputMsgs() {
      return getMetrics().getOutputMsgs();
    }

    /**
     * @see ISimpleComponentMetrics#getDiscardsAndExceptions()
     */
    public String getDiscardsAndExceptions() {
      return getMetrics().getDiscardsAndExceptions();
    }
    
    /**
     * @see ISimpleComponentMetrics#setMetricsEnabled(boolean)
     */
    public void setMetricsEnabled(boolean metricsEnabled) {
      getMetrics().setMetricsEnabled(metricsEnabled);
    }

    /**
     * @see ISimpleComponentMetrics#isMetricsEnabled()
     */
    public boolean isMetricsEnabled() {
      return getMetrics().isMetricsEnabled();
    }
  }
}
