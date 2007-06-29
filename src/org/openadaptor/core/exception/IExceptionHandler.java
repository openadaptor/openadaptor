package org.openadaptor.core.exception;

import java.util.Map;

import org.openadaptor.core.IMessageProcessor;

/**
 * Interface for OA exception handler that allows for setting a custom
 * exception map. 
 * 
 * @author Kris Lachor
 */
public interface IExceptionHandler extends IMessageProcessor{
  
  /**
   * Sets a bespoke exception map.
   * 
   * @param map a bespoke exception map
   */
  void setExceptionMap(Map map);

  /** 
   * @return a bespoke exception map
   */
  Map getExceptionMap();
  
}
