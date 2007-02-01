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

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ProcessingException;

/**
 * This class is a connector which will generate IOrderedMaps from the results of a JNDI search.
 * <p>
 * It should be configured with an appropriate JNDIConnection and JNDISearch objects.
 * 
 * @see JNDIConnection
 * @see JNDISearch
 * @author Eddy Higgins
 */
public class JNDIReadConnector extends AbstractJNDIReadConnector {
  
  private static final Log log = LogFactory.getLog(JNDIReadConnector.class);

  // internal state:
  /**
   * Direcory Context for this reader.
   */
  protected DirContext _ctxt;

  /**
   * Naming enumeration which holds the results of an executed search.
   */
  protected NamingEnumeration _namingEnumeration;

  /**
   * Flag to indicate whether or not the search has already run.
   */
  protected boolean _searchHasExecuted = false;

  // bean properties:
  /**
   * JNDIConnection which this reader will use
   */
  protected JNDIConnection jndiConnection;

  public JNDIReadConnector() {
  }

  public JNDIReadConnector(String id) {
    super(id);
  }
  

  // BEGIN Bean getters/setters

  /**
   * Assign a JNDI connection for use by the reader. Behaviour is undefiled if this is set when the reader has already
   * called connect().
   * 
   * @param connection
   *          The JNDIConnection to use
   */
  public void setJndiConnection(JNDIConnection connection) {
    jndiConnection = connection;
  }

  /**
   * Return the JNDIConnection for this reader.
   * 
   * @return JNDIConnection instance.
   */
  public JNDIConnection getJndiConnection() {
    return jndiConnection;
  }

  // END Bean getters/setters

  // Public accessors:

  /**
   * Return the dirContext for this reader.
   * <p>
   * The dirContext is set when the underlying <code>JNDIConnection</code> object has it's connect() method invoked.
   * 
   * @return DirContext DirContext, or <tt>null</tt> if it hasn't been set yet.
   */
  public DirContext getContext() {
    return _ctxt;
  }

  /**
   * Establish an external JNDI connection.
   * <p>
   * If already connected, do nothing.
   * 
   * @throws org.openadaptor.control.ComponentException
   *           if an AuthenticationException or NamingException occurs
   */
  public void connect() {
    try {
      _ctxt = jndiConnection.connect();
    } catch (AuthenticationException ae) {
      log.warn("Failed JNDI authentication for principal: " + jndiConnection.getSecurityPrincipal());
      throw new ConnectionException("Failed to Authenticate JNDI connection - " + ae.toString(), ae, this);
    } catch (NamingException ne) {
      log.warn(ne.getMessage());
      throw new ConnectionException("Failed to establish JNDI connection - " + ne.toString(), ne, this);
    }
    log.info(getId() + " connected");
  }

  /**
   * Disconnect external JNDI connection.
   * <p>
   * If already disconnected, do nothing.
   * 
   * @throws org.openadaptor.control.ComponentException
   *           if a NamingException occurs.
   */
  public void disconnect() {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
    if (_ctxt != null) {
      try {
        _ctxt.close();
      } catch (NamingException ne) {
        log.warn(ne.getMessage());
      }
    }
    log.info(getId() + " disconnected");
  }

  /**
   * Return the next record from this reader.
   * <p>
   * It first tests if the underlying search has already executed. If not, it executes it. It then takes the next
   * available result from the executed search, and returns it.<br>
   * If the result set is empty, then it returns <tt>null</tt> indicating that the reader is exhausted.
   * 
   * @return Object[] containing an IOrderedMap of results, or <tt>null</tt>
   * @throws ComponentException
   */
  public Object[] next(long timeoutMs) throws ComponentException {
    Object[] result = null;
    try {
      if (!_searchHasExecuted) {
        log.info("Executing JNDI search - " + search.toString());
        _namingEnumeration = search.execute(_ctxt);
        _searchHasExecuted = true;
      }
      if (_namingEnumeration.hasMore()) {
        IOrderedMap map = JNDIUtils.getOrderedMap((SearchResult) _namingEnumeration.next(), search
            .getTreatMultiValuedAttributesAsArray(), search.getJoinArraysWithSeparator());

        result = new Object[] { map };
      }
    } catch (CommunicationException e) {
      throw new ConnectionException(e.getMessage(), e, this);
    } catch (ServiceUnavailableException e) {
      throw new ConnectionException(e.getMessage(), e, this);
    } catch (NamingException e) {
      throw new ProcessingException(e.getMessage(), e, this);
    }
    return result;
  }

  public boolean isDry() {
    return false;
  }

  public Object getReaderContext() {
    return null;
  }
}
