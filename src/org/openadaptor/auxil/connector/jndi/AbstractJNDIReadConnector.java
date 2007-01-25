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

package org.openadaptor.auxil.connector.jndi;

import java.util.List;

import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;

/**
 * Abstract base class for connectors which use JNDI searches.
 */
public abstract class AbstractJNDIReadConnector extends Component implements IReadConnector {

  protected AbstractJNDIReadConnector() {
  }

  protected AbstractJNDIReadConnector(String id) {
    super(id);
  }
 
  public void validate(List exceptions) {
  }
  
  //private static final Log log = LogFactory.getLog(AbstractJNDIReader.class);

  /**
   * This is the search class which will be used.
   */
  protected JNDISearch search;

  // BEGIN Bean getters/setters

  /**
   * Returns the JNDISearch instance which this Reader will use.
   * 
   * @return JNDISearch instance.
   */
  public JNDISearch getSearch() {
    return search;
  }

  /**
   * Assign a <code>JNDISearch</code> instance to this Reader.
   * 
   * @param search
   *          the search to associate with this Reader.
   */
  public void setSearch(JNDISearch search) {
    this.search = search;
  }
  // END Bean getters/setters
}
