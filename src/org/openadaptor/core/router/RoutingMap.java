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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.node.Node;

/**
 * The default implementation of {@link IRoutingMap}.
 * 
 * A RoutingMap describes how adaptor components (IMessageProcessors) are linked 
 * together. It actually holds 3 different maps.
 * 
 * <li>The <code>processMap</code> defines mapping between an IMessageProcessor and the list of 
 * IMessageProcessors that should process its output.
 * 
 * <li>The <code>discardMap</code> defines mapping between an IMessageProcessor and the list of 
 * IMessageProcessors that should process its discarded input.
 * 
 * <li>The <code>exceptionMap</code> defines mapping between an IMessageProcessor and the list of 
 * IMessageProcessors that should process its MessageExceptions.
 * 
 * <br/><br/>This makes heavy use of "autoboxing" to reduce some of the complexity
 * for more basic configurations. See comments for setProcessMap, setDiscardMap, setExceptionMap
 * 
 * @author perryj
 * @see IMessageProcessor
 * @see MessageException
 * @see IAutoboxer
 */
public class RoutingMap implements IRoutingMap,Cloneable {

  public static final String DEFAULT_KEY = "*";

  private Set processors = new HashSet();

  private Map processMap = new HashMap();

  private Map discardMap = new HashMap();

  private Map exceptionMap = new HashMap();

  private IAutoboxer autoboxer;

  private IMessageProcessor entryExceptionProcessor = null;

  /**
   * A set of message processors that can participate in exception handling.
   * Exceptions thrown by any of these processors will not be handled regardless
   * of the exceptionProcessor being set up. 
   */
  private Set exceptionProcessors = new HashSet();

  public RoutingMap(final IAutoboxer autoboxer) {
    this.autoboxer = autoboxer;
  }

  public RoutingMap() {
    this(new Autoboxer());
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
    processMap.clear();
    populateMap(map, processMap);
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
    discardMap.clear();
    populateMap(map, discardMap);
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
   * If the parameters is not a map of maps then value is interpreted as the exception map
   * for all components.
   * There is a default Autoboxer but this can be overriden.
   * 
   * @param map
   * @see MessageException
   * @see IMessageProcessor
   * @see Node
   */
  public void setExceptionMap(Map map) {
    exceptionMap.clear();
    map = autoboxer.autobox(map);

    if (isMapOfMaps(map)) {
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        if (!(entry.getKey() instanceof IMessageProcessor) && !DEFAULT_KEY.equals(entry.getKey())) {
          throw new RuntimeException("key " + entry.getKey().toString() + " is not IMessageProcessor");
        }
        OrderedExceptionToProcessorsMap oemap = new OrderedExceptionToProcessorsMap((Map)entry.getValue());
        exceptionMap.put(entry.getKey(), oemap);
      }
    } else {
      OrderedExceptionToProcessorsMap oemap = new OrderedExceptionToProcessorsMap(map);
      exceptionMap.put(DEFAULT_KEY, oemap);
    }
  }

  public Map getExceptionMap(){
    return Collections.unmodifiableMap(exceptionMap);
  }

  public Collection getMessageProcessors() {
    return Collections.unmodifiableCollection(processors);
  }

  public List getProcessDestinations(IMessageProcessor processor) {
    List l = (List) processMap.get(processor);
    return l != null ? l : Collections.EMPTY_LIST;
  }

  public List getDiscardDestinations(IMessageProcessor processor) {
    List l = (List) discardMap.get(processor);
    return l != null ? l : Collections.EMPTY_LIST;
  }

  private void findExceptionProcessors(){
    if(this.entryExceptionProcessor != null){
      exceptionProcessors.add(entryExceptionProcessor);
      IMessageProcessor exceptionProcessor = (IMessageProcessor) entryExceptionProcessor;
      findExceptionProcessorsRecurs(exceptionProcessor, exceptionProcessors);
    }
  }

  private void findExceptionProcessorsRecurs(IMessageProcessor exceptionProcessor, Set exceptionProcessors){
    List destinations = getProcessDestinations(exceptionProcessor);
    if(destinations != null && !destinations.isEmpty()){
      Iterator it = destinations.iterator();
      while(it.hasNext()){
        IMessageProcessor nextExceptionProcessor = ((IMessageProcessor) it.next());
        exceptionProcessors.add(nextExceptionProcessor);
        /* Recursion */
        findExceptionProcessorsRecurs(nextExceptionProcessor, exceptionProcessors);
      }
    }
  }
  
  /**
   * Checks if the <code>processor</code> is part of the exceptionProcessor pipeline.
   * 
   * @return true if <code>processor</code> occurs anywhere in the exceptionProcessor
   *         pipeline. False it it doesn't occur or the excedptionProcessor 
   *         is not set.
   */
  protected boolean isAnExceptionProcessor(IMessageProcessor processor){
    if( entryExceptionProcessor!=null ){
      if(exceptionProcessors.isEmpty()){
        findExceptionProcessors();
      }
      if(exceptionProcessors.contains(processor)){
        return true;
      }
    }  
    return false;
  }

  /**
   * Gets destinations (a list of message processors) for a given processor and exception.
   * 
   * @return a list of IMessageProcessors
   */
  public List getExceptionDestinations(IMessageProcessor processor, Throwable exception) {
    OrderedExceptionToProcessorsMap map = (OrderedExceptionToProcessorsMap) exceptionMap.get(processor);

    /* 
     * Exceptions thrown from any of the nodes that can participate in exception handling
     * won't be handled. 
     */   
    if( isAnExceptionProcessor(processor) ){
      return Collections.EMPTY_LIST;
    }


    if (map == null ) {
      map = (OrderedExceptionToProcessorsMap) exceptionMap.get(DEFAULT_KEY);
    }
    if (map != null) {
      return map.getDestinations(exception);
    } else {
      return Collections.EMPTY_LIST;
    }
  }
  
  


  /**
   * Checks if <code>processor</code> has already been autoboxed in 
   * the processMap. Returns the box if found.
   * 
   * @param processor a processor
   * @return boxed processor if any can be found
   */
  protected IMessageProcessor getIfAlreadyAutoboxed(Object processor){
    Object boxed = null;
    Iterator ite = processMap.keySet().iterator();
    while(ite.hasNext()){
      Object key = ite.next();
      if(! (key instanceof Node)) continue;
      Node node = (Node) key;
      if(node.getProcessor().equals(processor)){
        boxed = node;
        break;
      }
    }
    return (IMessageProcessor) boxed;  
  }

  /**
   * Checks that the keys and values are actually IMessageProcessor instances and autoboxes
   * single IMessageProcessor values into a unary list
   */
  private void populateMap(Map map, Map checkedMap) {
    map = autoboxer.autobox(map);
    for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      verifyEntryKeyIsIMessageProcessor(entry);
      IMessageProcessor fromProcessor = (IMessageProcessor) entry.getKey();
      List processorList = autoboxIMessageProcessorList(entry.getValue());
      processors.add(fromProcessor);
      processors.addAll(processorList);
      checkedMap.put(fromProcessor, processorList);
    }
  }

  private void verifyEntryKeyIsIMessageProcessor(Map.Entry entry) {
    if (!(entry.getKey() instanceof IMessageProcessor)) {
      throw new RuntimeException("key " + entry.getKey().toString() 
          + " " + entry.getKey().getClass().getName() + " is not IMessageProcessor");
    }
  }

  private boolean isMapOfMaps(Map map) {
    boolean result = map.size() > 0;
    for (Iterator iter = map.values().iterator(); iter.hasNext();) {
      result &= iter.next() instanceof Map;
    }
    return result;
  }


  private List autoboxIMessageProcessorList(Object value) {
    List list = null;
    if (value instanceof List) {
      list = (List) value;
    } else {
      list = new ArrayList();
      list.add(value);
    }
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      Object element = (Object) iterator.next();
      if (!(element instanceof IMessageProcessor)) {
        throw new RuntimeException("value " + element.toString() + "(" 
            + element.getClass().getName() + ") is not IMessageProcessor");
      }
    }
    return list;
  }


  /**
   * Maps exceptions to their processors. Keys are exception classes
   * (java.lang.Class), values are Lists of IMessageProcessors.
   */
  protected class OrderedExceptionToProcessorsMap extends LinkedHashMap {
    /**
     * Assign default version as it is Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor. Iterates through <code>map</code>, creates exception
     * instances based on their class names (map's keys), autoboxes
     * corresponding message processors (map's values) and stores the resulting
     * pair (key: exception classes, value: list of processors) in itself.
     * 
     * @param map Map where keys are exception class names (Strings), values are
     *          exception processors (anything that implements
     *          IMessageProcessor).
     */
    protected OrderedExceptionToProcessorsMap(Map map) {
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        try {
          Class exceptionClass = Class.forName(entry.getKey().toString());
          if (Throwable.class.isAssignableFrom(exceptionClass)) {
            List processorList = autoboxIMessageProcessorList(entry.getValue());
            processors.addAll(processorList);
            put(exceptionClass, processorList);
          } else {
            throw new RuntimeException(entry.getKey().toString()
                + " is not throwable");
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }

    /**
     * Finds a list of processors for an exception.
     * 
     * @param exception an instance of Throwable
     * @return list of processors, if any, configured for <code>exception</code>
     */
    protected List getDestinations(Throwable exception) {
      List destinations = (List) get(exception.getClass());
      for (Iterator iter = keySet().iterator(); destinations == null && iter.hasNext();) {
        Class exceptionClass = (Class) iter.next();
        if (exceptionClass.isAssignableFrom(exception.getClass())) {
          destinations = (List) get(exceptionClass);
        }
      }
      return destinations != null ? destinations : Collections.EMPTY_LIST;
    }
  } //ExceptionMap


  public void setBoxedExceptionProcessor(IMessageProcessor boxedExceptionProcessor) {
    this.entryExceptionProcessor = boxedExceptionProcessor;
  }
  //Override Object.clone() to implement Cloneable
  public Object clone() throws CloneNotSupportedException {
    RoutingMap obj = (RoutingMap) super.clone();
    obj.processors = Collections.unmodifiableSet(processors);
    obj.processMap = Collections.unmodifiableMap(processMap);
    obj.discardMap = Collections.unmodifiableMap(discardMap);
    obj.exceptionMap = Collections.unmodifiableMap(exceptionMap);
    return obj;
  }

}
