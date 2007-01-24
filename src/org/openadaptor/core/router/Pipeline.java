package org.openadaptor.core.router;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;

/**
 * simplified router for when there is a single sequence of IMessageProcessors
 * and optionally a single exception processor.
 * 
 * @author perryj
 *
 */
public class Pipeline extends AbstractRouter {
 
  public Pipeline() {super();}

  public Pipeline(String id) {
    super(id);
  }

  //ToDo: Add-back ExceptionProcessor capability.

  public void setProcessors(List processorList){
    // create process map from list of processors
    Map processMap = new HashMap();
    IMessageProcessor previous = null;
    //for (int i = 0; i < processors.length; i++) {
    for (Iterator it=processorList.iterator();it.hasNext();) { 
      IMessageProcessor processor = (IMessageProcessor) autoboxer.autobox(it.next());
      if (previous != null) {
        processMap.put(previous, processor);
      } else {
        if (!(processor instanceof ReadNode)) {
          throw new RuntimeException("first element of pipeline is not a ReadNode/IReadConnector");
        }
      }
      previous = processor;
    }
    if (!(previous instanceof WriteNode)) {
      throw new RuntimeException("last element of pipeline is not an WriteNode/IWriteConnector");
    }   
    routingMap.setProcessMap(processMap);
   
  }
  public void setExceptionProcessor(Object exceptionProcessor){
    Object boxed=autoboxer.autobox(exceptionProcessor);
    if (!(boxed instanceof IMessageProcessor)) {
      throw new RuntimeException("exception processor must be an instance of IMessageProcessor");
    }
    Map exceptionMap = new HashMap();
    exceptionMap.put(java.lang.Exception.class.getName(), exceptionProcessor);
    routingMap.setExceptionMap(exceptionMap);    
  }
}
