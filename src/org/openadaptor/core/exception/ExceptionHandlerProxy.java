package org.openadaptor.core.exception;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;

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
public class ExceptionHandlerProxy implements IExceptionHandler{

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
  public Response process(Message msg) {
    return null;
  }
}
