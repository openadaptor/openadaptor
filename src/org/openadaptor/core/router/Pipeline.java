/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

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
