/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core;

import java.util.List;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.processor.GenericEnrichmentProcessor;
import org.openadaptor.core.exception.ValidationException;

/**
 * Enrichment processors in Openadaptor implement a pattern often called Content Enricher.
 * Content Enricher uses information inside the incoming message to retrieve data from
 * an external source. After it retrieves the required data from the resource, it 
 * appends data to the message. The original information from the incoming message
 * may be carried over into the resulting message or may no longer be needed, depending
 * on the needs of the receiving component. 
 * 
 * Enrichment processors are often used to references contained in a message. In order
 * to keep messages small and easy to manage, simple references are often passed as an
 * adaptor message rather than complete objects with all data elements. These references
 * usually take the form of keys or unique IDs. When the message needs to be processed
 * by an adaptor, the required data items need to be retrieved based on object references
 * included in the original message. 
 * 
 * Interface that represents an enrichment processor. An enrichment processor 
 * has access to a read connector that will query an external resource for 
 * data that enrichment processor will then use to enrich its input data.
 * 
 * @author Kris Lachor
 * @since 3.4
 * @see GenericEnrichmentProcessor
 */
public interface IEnrichmentProcessor  {
  
  /**
   * Underlying read connector. The read connector must be capable of accepting parameters
   * and use them to customise the query to the external resource. The {@link IEnrichmentReadConnector}
   * is an abstraction of this capability.
   * 
   * @return the underlying read connector.
   */
  IEnrichmentReadConnector getReadConnector();
 
  /**
   * Builds a map with data that IEnrichmentReadConnector will use to parameterise 
   * the query.
   *
   * @return data to parameterise IEnrichmentReadConnector's query
   */
  IOrderedMap prepareParameters(Object input);
  
  /**
   * Merges original input with additional data obtained from the underlying 
   * IEnrichmentReadConnector.
   * 
   * @param input original input record used to parametrise query in
   *        IEnrichmentReadConnector
   * @param additionalData data returned by IEnrichmentReadConnector
   */
  Object [] enrich(Object input, Object [] additionalData);
  
  
  /**
   * This method checks that the current state of an implementation is
   * "meaningful". Implementations of {@link IEnrichmentProcessor} are typically beans with
   * zero-arg constructors.  Implementations are encouraged to add exception to the
   * list parameter rather than throwing them. This allows the calling code to collate
   * the exceptions. If the implementation is an {@link IComponent} then the exceptions
   * should be a {@link ValidationException}.
   *
   * @param exceptions collection to which exceptions should be added. Must not be null
   * @throws IllegalArgumentException if null argument provided
   */
  void validate(List exceptions);
  
}
