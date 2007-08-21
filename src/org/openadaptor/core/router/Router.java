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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.exception.IExceptionHandler;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.node.Node;

/**
 * An {@link IMessageProcessor} implementation that uses an {@link IRoutingMap} to
 * process a message by calling a sequence of IMessageProcessors.
 * 
 * @author perryj
 * @see IRoutingMap
 */
public class Router extends AbstractRouter implements IMessageProcessor { 

  private static final String DEFAULT_EXCEPTION_CLASSNAME = Exception.class.getName();

  /**
   * This contains the list of exceptions for which an ExceptionHandler,
   * if configured will be used.
   */
  private List exceptionList;
  
  /**
   * Flag to indicate that the processMap has already been configured.
   * <p>
   * Used to avoid using both setProcessMap and setProcessors simultaneously.
   */
  private boolean processMapConfigured=false;
  /**
   * Flag to indicate that the exceptionMap has already been configured.
   * <p>
   * Used to avoid using both setExceptionMap and setExceptionProcessor simultaneously.
   */
  private boolean exceptionMapConfigured=false;

  public Router() {
    super();
    init();
  }

  public Router(String id) {
    super(id);
    init();
  }

  public Router(RoutingMap routingMap){
    super();
    init();
    this.routingMap=routingMap;
  }

  /**
   * Common initialisation for all constructors.
   */
  private void init() {
    exceptionList=new ArrayList();
    exceptionList.add(DEFAULT_EXCEPTION_CLASSNAME);
  }

  /**
   * Sets the processMap which defines how to route output from one
   * adaptor component to anothers. 
   * The keys must be IMessageProcessors and the values Lists of 
   * IMessageProcessors. However this setter will do a fair amount
   * of autoboxing to make the caller's life slightly easier. Non list
   * values will automatically be boxed into a list. Key which are not
   * actually IMessageProcessors but are Connectors or Processors will
   * be automatically boxed in a Node. There is a default Autoboxer
   * but this can be overriden.
   * 
   * @param map
   * @see IMessageProcessor
   * @see Node
   */
  public void setProcessMap(Map map) {
    if (processMapConfigured) {
      throw new RuntimeException("Only one of processMap and processors properties may be configured");
    }
    processMapConfigured=true;
    routingMap.setProcessMap(map);
  }

  /**
   * Sets the discardMap which defines how to route discarded input from one
   * adaptor component to anothers. 
   * The keys must be IMessageProcessors and the values Lists of 
   * IMessageProcessors. However this setter will do a fair amount
   * of autoboxing to make the caller's life slightly easier. Non list
   * values will automatically be boxed into a list. Key which are not
   * actually IMessageProcessors but are Connectors or Processors will
   * be automatically boxed in a Node. There is a default Autoboxer
   * but this can be overriden.
   * 
   * @param map
   * @see IMessageProcessor
   * @see Node
   */
  public void setDiscardMap(Map map) {
    routingMap.setDiscardMap(map);
  }
  
  /**
   * Sets the exceptionMap which defines how to route MessageExceptions from one
   * adaptor component to anothers. 
   * The keys must be IMessageProcessors and the values Maps of Maps. Where the keys
   * are exception classnames and the values List of IMessageProcessors
   * However this setter will do a fair amount of autoboxing to make the caller's life 
   * slightly easier. Non list values will automatically be boxed into a list. 
   * Values which are not actually IMessageProcessors but are Connectors or Processors will
   * be automatically boxed in a Node. 
   * If the parameters is not a map of maps then value is interpreted as the exceptin map
   * for all components.
   * There is a default Autoboxer but this can be overriden.
   * 
   * @param map
   * @see MessageException
   * @see IMessageProcessor
   * @see Node
   */
  private void setExceptionMap(Map map) {
    if (exceptionMapConfigured) {
      throw new RuntimeException("Only one of exceptionMap and exceptionProcessor properties may be configured");
    }
    routingMap.setExceptionMap(map);
  }

  public void setProcessors(List processorList){
    // create process map from list of processors
    if ((processorList==null) || (processorList.isEmpty())) { 
      throw new RuntimeException("Null or empty processorList is not permitted");
    }
    Object[] processors=processorList.toArray(new Object[processorList.size()]);
    Map processMap = new HashMap();
    for (int i=1;i<processors.length;i++){
      processMap.put(processors[i-1],processors[i]);
    }
    setProcessMap(processMap);
  }
 
  
  /**
   * This allows configuration of a single 'catch-all' processor for exceptions.
   * <p>
   * the list of exceptions may be configured via the exceptionList property.
   * If not it will default to the single exception DEFAULT_EXCEPTION_CLASSNAME
   * 
   * @param exceptionProcessor
   */
  public void setExceptionProcessor(Object exceptionProcessor){
    
    /* 
     * Autobox but first check if it hasn't already been boxed in 
     * the processMap.
     */
    IMessageProcessor boxed = routingMap.getIfAlreadyAutoboxed(exceptionProcessor);
    if(null == boxed){
       boxed = (IMessageProcessor) autoboxer.autobox(exceptionProcessor);
    }
    routingMap.setBoxedExceptionProcessor(boxed);
    
    /* Set exceptionMap - check if a custom map was defined on the IExceptionHandler. */
    if( exceptionProcessor instanceof IExceptionHandler ){
      IExceptionHandler exceptionHandler = (IExceptionHandler) exceptionProcessor;
      Map exceptionMap = exceptionHandler.getExceptionMap();
      if(null != exceptionMap){
        setExceptionMap(exceptionMap);
        return;
      }
    }
    
    /* If not then populate the exceptionMap from the default exceptionList. */
    Map exceptionMap=new HashMap();
    Iterator it=exceptionList.iterator();
    while(it.hasNext()) {
      exceptionMap.put(it.next(), boxed);
    }
    setExceptionMap(exceptionMap);
  }


}
