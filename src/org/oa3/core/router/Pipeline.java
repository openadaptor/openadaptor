package org.oa3.core.router;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oa3.core.IMessageProcessor;
import org.oa3.core.adaptor.AdaptorInpoint;
import org.oa3.core.adaptor.AdaptorOutpoint;

/**
 * simplified router for when there is a single sequence of IMessageProcessors
 * and optionally a single exception processor.
 * 
 * @author perryj
 *
 */
public class Pipeline extends Router {

  private Autoboxer autoboxer = new Autoboxer();
  
  public Pipeline(List processors) {
    this(processors, null);
  }
  
  public Pipeline(List processors, IMessageProcessor exceptionProcessor) {
    super();

    RoutingMap routingMap = new RoutingMap();

    // create process map from list of processors
    Map processMap = new HashMap();
    IMessageProcessor previous = null;
    for (Iterator iter = processors.iterator(); iter.hasNext();) {
      IMessageProcessor processor = (IMessageProcessor) autoboxer.autobox(iter.next());
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
