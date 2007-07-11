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

package org.openadaptor.core.exception;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;

/**
 * A class that can be used for one of or both:
 * 
 * -redefining exceptionMap to route different exception types to different handlers
 * 
 * -a proxy to other exception processing nodes allowing for fun-outs. As a 
 *  message processor this will simply do nothing - the sole purpose of the node
 *  is to be an entry point for exception handling from which exceptions are directed
 *  to two or more 'parallel' nodes.
 * 
 * 
 * @todo html friendly javadoc
 * @author Kris Lachor
 */
public class ExceptionHandlerProxy extends Component implements IExceptionHandler{

  private static final Log logger = LogFactory.getLog(ExceptionHandlerProxy.class);
  
  private Map exceptionMap = null;
  
  /**
   * @return exception map if one was defined, null otherwise (null doesn't mean no exceptionMap exists
   *         - the Router will have a default one)
   */
  public Map getExceptionMap() {
    return exceptionMap;
  }

  /**
   * @see org.openadaptor.core.exception.IExceptionHandler#setExceptionMap(java.util.Map)
   */
  public void setExceptionMap(Map exceptionMap) {
    if(null != exceptionMap){
      logger.info("Setting user defined exceptionMap. Size = " + exceptionMap.size());
      this.exceptionMap = exceptionMap;
    }
  }

  
 /**
  * The class is a proxy and itself doesn't do any processing.
  * 
  * @return null
  */
  public Object[] process(Object data) {
    return new Object[]{data};
  }

  public void reset(Object context) {}

  public void validate(List exceptions) {}
  
}
