package org.openadaptor.core.router;

import java.util.HashMap;
import java.util.Map;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.adaptor.AdaptorInpoint;
import org.openadaptor.core.adaptor.AdaptorOutpoint;

/**
 * simplified router for when there is a single sequence of IMessageProcessors
 * and optionally a single exception processor.
 * 
 * @author perryj
 *
 */
public class Pipeline extends Router {

  private Autoboxer autoboxer = new Autoboxer();
  
  public Pipeline(Object[] processors) {
    this(processors, null);
  }
  
  public Pipeline(Object[] processors, IMessageProcessor exceptionProcessor) {
    super();

    RoutingMap routingMap = new RoutingMap();

    // create process map from list of processors
    Map processMap = new HashMap();
    IMessageProcessor previous = null;
    for (int i = 0; i < processors.length; i++) {
      IMessageProcessor processor = (IMessageProcessor) autoboxer.autobox(processors[i]);
      if (previous != null) {
        processMap.put(previous, processor);
      } else {
        if (!(processor instanceof AdaptorInpoint)) {
          throw new RuntimeException("first element of pipeline is not an adaptor inpoint");
        }
      }
      previous = processor;
    }
    if (!(previous instanceof AdaptorOutpoint)) {
      throw new RuntimeException("last element of pipeline is not an adaptor outpoint");
    }
    
    routingMap.setProcessMap(processMap);
    
    // create exception map from exceptionProcessor
    if (exceptionProcessor != null) {
      Map exceptionMap = new HashMap();
      exceptionMap.put(java.lang.Exception.class.getName(), exceptionProcessor);
      routingMap.setExceptionMap(exceptionMap);
    }
    
    setRoutingMap(routingMap);
  }
}
