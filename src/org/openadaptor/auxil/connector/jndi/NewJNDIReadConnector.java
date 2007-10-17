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

package org.openadaptor.auxil.connector.jndi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.*;

import javax.naming.*;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import java.util.List;
import java.util.Map;

/**
 * This is a draft of a new JNDI read connector, ultimately meant to replace 
 * the present {@link JNDIReadConnector}.
 * Not read for use.
 *
 * @author Eddy Higgins, Kris Lachor
 * @see JNDIConnection
 * @see JNDISearch
 */
public class NewJNDIReadConnector extends AbstractJNDIReadConnector {

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

    
  
  /********* ported from JNDIEnhancementProcessor BEGIN ************/
  protected String recordKeyUsedAsSearchBase = null;

  public void setRecordKeyUsedAsSearchBase(String recordKeyUsedAsSearchBase) {
    this.recordKeyUsedAsSearchBase = recordKeyUsedAsSearchBase;
    this.enhancementProcessorMode = true;
  }

  public String getRecordKeyUsedAsSearchBase() {
    return recordKeyUsedAsSearchBase;
  }
  
  protected String recordKeySetByExistence = null;
  
  public void setRecordKeySetByExistence(String recordKeySetByExistence) {
    this.recordKeySetByExistence = recordKeySetByExistence;
    this.enhancementProcessorMode = true;
  }

  public String getRecordKeySetByExistence() {
    return recordKeySetByExistence;
  }
  
  protected Map incomingMap;
  
  public void setIncomingMap(Map incomingMap) {
    this.incomingMap = incomingMap;
    this.enhancementProcessorMode = true;
  }

  public Map getIncomingMap() {
    return incomingMap;
  }
  
  protected Map outgoingMap;
  
  public void setOutgoingMap(Map outgoingMap) {
    this.outgoingMap = outgoingMap;
  }

  public Map getOutgoingMap() {
    return outgoingMap;
  }
  
  protected String[] outgoingKeys; // derived from outgoingMap bean property
  
  protected String[] configDefinedSearchAttributes; // derived from attributes property of embedded search property
  
  protected String configDefinedSearchFilter; // derived from filter property of embedded search property
  
  /********* ported from JNDIEnhancementProcessor END ************/
  
  
  
  /********* new BEGIN ************/
  private boolean enhancementProcessorMode = false;
  
  
  public void setEnhancementProcessorMode(boolean enhancementProcessorMode) {
    this.enhancementProcessorMode = enhancementProcessorMode;
  }
  /********* new END ************/
  
  
  public NewJNDIReadConnector() {
  }

  public NewJNDIReadConnector(String id) {
    super(id);
  }

  // BEGIN Bean getters/setters

  /**
   * Assign a JNDI connection for use by the reader. Behaviour is undefiled if this is set when the reader has already
   * called connect().
   *
   * @param connection The JNDIConnection to use
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
   * <p/>
   * The dirContext is set when the underlying <code>JNDIConnection</code> object has it's connect() method invoked.
   *
   * @return DirContext DirContext, or <tt>null</tt> if it hasn't been set yet.
   */
  public DirContext getContext() {
    return _ctxt;
  }

  /**
   * Checks that the mandatory properties have been set
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    super.validate(exceptions);
    if (jndiConnection == null) {
      exceptions.add(new ValidationException("jndiConnection property is not set", this));
    }
    
    if(enhancementProcessorMode){
      // relied on to allow this class to be subclassed by code that repeats the following with a different reader:
//      search = reader.getSearch();

      // Enforce preconditions:
      if ((incomingMap == null || incomingMap.size() < 1) && (recordKeyUsedAsSearchBase == null)) {
        log.warn("Must provide an incomingKeyMap and/or set recordKeyUsedAsSearchBase.");
        exceptions.add(new ValidationException("Must provide an incomingKeyMap and/or set recordKeyUsedAsSearchBase.",
            this));
      }
      if ((outgoingMap == null || outgoingMap.size() < 1) && (recordKeySetByExistence == null)) {
        log.warn("Must provide an outgoingKeyMap and/or set recordKeyUsedForExistence.");
        exceptions.add(new ValidationException("Must provide an outgoingKeyMap and/or set recordKeyUsedForExistence.",
            this));
      }

      String[] bases = search.getSearchBases();
      if (recordKeyUsedAsSearchBase == null) {
        // Must provide a searchBase in the embedded JNDISearch:
        if (bases == null || bases.length < 1) {
          log.warn("Must provide a non-empty search.searchBases (or provide recordKeyUsedAsSearchBase).");
          exceptions.add(new ValidationException(
              "Must provide a non-empty search.searchBases (or provide recordKeyUsedAsSearchBase).", this));
        }
      } else {
        // Must not provide a searchBase in the embedded JNDISearch as well:
        if (bases != null && bases.length > 0) {
          log.warn("Must provide either a search.searchBases or a recordKeyUsedAsSearchBase (not both!).");
          exceptions.add(new ValidationException(
              "Must provide either a search.searchBases or a recordKeyUsedAsSearchBase (not both!).", this));
        }
        // Must provide an incomingMap and/or a search filter in the embedded JNDISearch (eg. "(objectclass=*"))
        String filter = search.getFilter();
        if ((incomingMap == null || incomingMap.size() < 1) && (filter == null || filter.length() == 0)) {
          log.warn("Must provide an incomingMap and/or a search.filter.");
          exceptions.add(new ValidationException("Must provide an incomingMap and/or a search.filter.", this));
        }
      }

      // Initialise derived member variables:
      if (outgoingMap == null || outgoingMap.size() < 1) {
        outgoingKeys = new String[] {};
      } else {
        outgoingKeys = (String[]) outgoingMap.keySet().toArray(new String[] {});
      }

      configDefinedSearchAttributes = search.getAttributes();
      if (configDefinedSearchAttributes == null) {
        configDefinedSearchAttributes = new String[] {};
      }

      configDefinedSearchFilter = search.getFilter();
      if (configDefinedSearchFilter != null && !configDefinedSearchFilter.startsWith("(")
          && !configDefinedSearchFilter.endsWith(")")) {
        configDefinedSearchFilter = "(" + configDefinedSearchFilter + ")";
      }

      // Setup the attributes we're interested in:
      // outgoingMap keys combined with any config defined search attributes
      int attribsSize = outgoingKeys.length + configDefinedSearchAttributes.length;

      String[] attributeNames = new String[attribsSize];

      for (int i = 0; i < outgoingKeys.length; i++) {
        attributeNames[i] = outgoingKeys[i];
      }

      for (int i = 0; i < configDefinedSearchAttributes.length; i++) {
        attributeNames[i + outgoingKeys.length] = configDefinedSearchAttributes[i];
      }

      search.setAttributes(attributeNames);

      // Connect to enrichment source:
//      reader.connect();
      connect();
    }
  }

  /**
   * Establish an external JNDI connection.
   * <p/>
   * If already connected, do nothing.
   *
   * @throws ConnectionException if an AuthenticationException or NamingException occurs
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
   * <p/>
   * If already disconnected, do nothing.
   *
   * @throws ConnectionException if a NamingException occurs.
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
   * <p/>
   * It first tests if the underlying search has already executed. If not, it executes it. It then takes the next
   * available result from the executed search, and returns it.<br>
   * If the result set is empty, then it returns <tt>null</tt> indicating that the reader is exhausted.
   *
   * @return Object[] containing an IOrderedMap of results, or <tt>null</tt>
   * @throws OAException
   */
  public Object[] next(long timeoutMs) throws OAException {
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

  /**
   * @return false if the search has not yet been performed or there are still results
   * to be processed then we are not dry
   */
  public boolean isDry() {
    try {
      if (_namingEnumeration == null || _namingEnumeration.hasMore()) {
        return false;
      }
    } catch (NamingException e) {
    }

    return true;
  }

  /**
   * @return null
   * @see {@link org.openadaptor.core.IReadConnector#getReaderContext()}
   */
  public Object getReaderContext() {
    return null;
  }

  /**
   * Takes no action.
   * 
   * @see {@link org.openadaptor.core.IReadConnector#setReaderContext(Object)}
   */
  public void setReaderContext(Object context) {
  }
}
