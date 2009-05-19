package org.openadaptor.core;

import java.util.Map;

/**
 * Interface for components (connectors and processors) that need access to message
 * metadata.
 * 
 * Components that implement this interface will have metadata injected by 
 * the OA framework for each messages that passes through the component.
 * 
 * @author Kris Lachor
 */
public interface IMetadataAware {
   
  /**
   * Method will be called by the OA framework for each messages that passes through
   * the {@link IMetadataAware} component.
   * 
   * @param metadata - a set of key value pairs that may be used to pass information
   *        to components down the adaptor pipeline.
   */
  void setMetadata(Map metadata);
  
}
