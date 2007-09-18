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

import java.util.NoSuchElementException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to provide a bean-esque wrapping around a JNDI search configuration.
 * <p>
 * Notes: It differs from a vanilla JNDI search as follows:
 * <UL>
 * <LI> A search may configured against multiple search bases. The actual search will execute against each, 
 * in order, and consolidate the results.
 * <LI> The <tt>dn</tt> of each object found in a search may be included as an attribute of the object itself, by
 * assigning an attribute name via the <code>returnedDNAttribute</code> property. Note that behaviour is undefined 
 * if the chosen name conflicts with an existing attribute of the object.
 * <LI> Retrieved multi-valued attributes can be treated in two ways - by retrieving the first value of the 
 * attribute, or by returning an array containing all of the values. This behaviour is configured via the
 * <code>treatMultiValuedAttributesAsArray</code> property.
 * </UL>
 * <p>
 * 
 * @author Eddy Higgins
 */
public class JNDISearch {
  
  private static final Log log = LogFactory.getLog(JNDISearch.class);

  /**
   * List of Search bases to apply searches against.
   * <P>
   * (Note that they will be searched in order)
   */
  protected String[] _searchBases;

  /**
   * Search filter to be applied in searches.
   */
  protected String _filter;

  /**
   * Optional list of attributes to return.
   */
  protected String[] _attributes;

  /**
   * If set, Each result from a search will contain an attribute with this name, containing the <code>dn</code> of the
   * object. If this is not desired, just leave it as <tt>null</tt> (the default).
   */
  protected String returnedDNAttributeName = null; // By default don't return the DN

  /**
   * SearchControls to govern the search.
   */
  private SearchControls searchControls;

  /**
   * Flag to specify how to treat multi-valued attributes.
   */
  private boolean _treatMultiValuedAttributesAsArray;

  /**
   * If set and multi-valued attributes are being returned as arrays, it will join each multi-valued attribute array
   * into a single string using this string to separate each value.
   * 
   * By default we do not join arrays.
   */
  protected String joinArraysWithSeparator = null; // By default don't join arrays

  /**
   * Default constructor for a JNDI Search.
   * <p>
   * Will default to having a default SearchControls instance - as returned by new SearchControls() Will also default to
   * use the first value of multi-valued attributes.
   */
  public JNDISearch() { // No-arg constructor for beans
    searchControls = new SearchControls();// Start with defaults.
    _treatMultiValuedAttributesAsArray = false; // Todo: Review this default.
  }

  // BEGIN Bean getters/setters

  /**
   * An array of search bases to search.
   * <p>
   * These will be searched, in order, and all matching entries will be returned as a consolidated enumeration of
   * matches.
   * 
   * @param searchBases
   *          A String[] of search bases to use.
   */
  public void setSearchBases(String[] searchBases) {
    _searchBases = searchBases;
  }

  /**
   * Retrieve the current list of search bases to use.
   * 
   * @return String[] of searchBases.
   */
  public String[] getSearchBases() {
    return _searchBases;
  }

  /**
   * Set the filter to apply to searches.
   * 
   * @param filter
   *          String containing a filter definition
   */
  public void setFilter(String filter) {
    _filter = filter;
  }

  /**
   * Returns the current filter, if any which will be applied to searches.
   * 
   * @return String containing the filter, or <tt>null</tt> if none is defined.
   */
  public String getFilter() {
    return _filter;
  }

  /**
   * Set the name of the attribute which will contain the <tt>dn</tt> of each object returned as a result of search
   * execution.
   * <p>
   * If <tT>null<tt>, the <tt>dn</tt> will
   * not be included as an attribute at all.
   * @param returnedDNAttributeName Name of the attribute to hold the <code>dn</code>.
   */
  public void setReturnedDNAttributeName(String returnedDNAttributeName) {
    this.returnedDNAttributeName = returnedDNAttributeName;
  }

  /**
   * Get the name, if any of the attribute which will be used to hold <code>dn</code> values within search results.
   * <p>
   * The default value is <tt>null</tt>, i.e. do <i>not</i> return the <code>dn</code> as an attribute.
   * 
   * @return String containing the attribute name, or <tt>null</tt>
   */
  public String getReturnedDNAttributeName() {
    return returnedDNAttributeName;
  }

  /**
   * Set the list of attributes to return for matching search results.
   * <p>
   * If set, then searches will return those attribute specified here.
   * 
   * @param attributes
   *          String[] containing attribute names.
   */
  public void setAttributes(String[] attributes) {
    _attributes = attributes;
  }

  /**
   * Get the list of attributes to return for matching search results.
   * <p>
   * If set, then searches will return those specified attributes.
   * 
   * @return String[] containing attribute names.
   */
  public String[] getAttributes() {
    return _attributes;
  }

  /**
   * Assign a SearchControls instance to use for searches.
   * 
   * @param searchControls
   *          the searchControls to use. Should not be <tt>null</tt>
   * @see SearchControls for more info.
   */
  public void setSearchControls(SearchControls searchControls) {
    log.debug("Overriding default SearchControls");
    this.searchControls = searchControls;
  }

  /**
   * Retrieve the current SearchControls instance which this search will use.
   * 
   * @return SearchControls instance.
   */
  public SearchControls getSearchControls() {
    return searchControls;
  }

  /**
   * If <tt>true</tt> then multi-valued attributes will be returned as an array of values. If <tt>false</tt> then
   * the first retrieved attribute value of multi-valued attributes will be returned. All other values will be
   * discarded.
   * <p>
   * The default value is <tt>false</tt>
   * 
   * @param asArray
   *          boolean flag
   */

  public void setTreatMultiValuedAttributesAsArray(boolean asArray) {
    _treatMultiValuedAttributesAsArray = asArray;
  }

  /**
   * If <tt>true</tt> then multi-valued attributes will be returned as an array of values. If <tt>false</tt> then
   * the first retrieved attribute value of multi-valued attributes will be returned. All other values will be
   * discarded.
   * <p>
   * The default value is <tt>false</tt>
   */
  public boolean getTreatMultiValuedAttributesAsArray() {
    return _treatMultiValuedAttributesAsArray;
  }

  /**
   * 
   * <p>
   * The default value is <tt>null</tt>
   * 
   * @param valueSeparator
   *          String to use as separator for concatenating multiple values into single String (null means return
   *          multiple values as a String[]).
   */

  public void setJoinArraysWithSeparator(String valueSeparator) {
    joinArraysWithSeparator = valueSeparator;
  }

  /**
   * If <tt>true</tt> then multi-valued attributes will be returned as an array of values. If <tt>false</tt> then
   * the first retrieved attribute value of multi-valued attributes will be returned. All other values will be
   * discarded.
   * <p>
   * The default value is <tt>null</tt> which means return multiple values as a String[].
   */
  public String getJoinArraysWithSeparator() {
    return joinArraysWithSeparator;
  }

  // END Bean getters/setters

  /**
   * Execute this search against a DirContext.
   * <p>
   * The search will be run against each configured searchBase in turn, returning a NamingEnumeration will all of the
   * matching entries the search yields.
   * 
   * @param context
   *          a DirContext against which the search is to be executed.
   * @return NamingEnumeration This will contain all matching entries found.
   * @throws NamingException
   *           If searchBases does not contain at least one search base, or if the search fails for any reason.
   */
  public NamingEnumeration execute(DirContext context) throws NamingException {
    if (_searchBases == null || _searchBases.length <= 0) {
      String error = "Property searchBases may not be <null> and must have at least one value";
      log.error(error);
      throw new NamingException(error);
    }

    int searchCount = _searchBases.length; // One search for each searchBase
    NamingEnumeration[] results = new NamingEnumeration[searchCount];

    boolean useAttributes = _attributes != null;
    String[] origAttributes = searchControls.getReturningAttributes();
    if (useAttributes) {
      searchControls.setReturningAttributes(_attributes);
    }
    for (int i = 0; i < searchCount; i++) {
      String base = _searchBases[i];
      log.debug("Executing search against base: " + base + " using filter: " + _filter + " and constraints: "
          + searchControls + "");
      results[i] = context.search(base, _filter, searchControls);
    }

    if (useAttributes) { // Put the old ones back.
      searchControls.setReturningAttributes(origAttributes);
    }
    return new MultiBaseJNDISearchResults(this, results);
  }
}

/**
 * Convenience Utility class to collate the results of multiple searches as a single NamingEnumeration.
 * <p>
 * It is somewhat complicated by having to remember which searchbase yielded which results. From the user's perspective
 * this can be ignored, as this class can supply the full dn of each match (if specified in the search
 * (returnedDNAttributeName).
 */

class MultiBaseJNDISearchResults implements NamingEnumeration {
  
  //private static final Log log = LogFactory.getLog(MultiBaseJNDISearchResults.class);

  private final JNDISearch executedSearch;

  private final NamingEnumeration[] searchResults;

  private int current;

  private final String dnAttributeName; // Optimisation - only fetch it once.

  /**
   * Create a multi-base SearchResults Object from a search, and the set of Results it yielded.
   * <p>
   * Note that behaviour is undefined if supplied search and results are not related!
   * 
   * @param executedSearch
   *          JNDISearch which has executed.
   * @param searchResults
   *          The searchResults from the search. Its size must correspond exactly to the number of search bases definied
   *          in the search.
   */
  public MultiBaseJNDISearchResults(JNDISearch executedSearch, NamingEnumeration[] searchResults) {
    this.executedSearch = executedSearch;
    this.searchResults = searchResults;
    this.dnAttributeName = executedSearch.getReturnedDNAttributeName();
    current = 0;
  }

  /**
   * Returns true if the array of Enumerations isn't exhausted.
   * <p>
   * As each one is exhausted, it moves to the next, until no more remain.
   * 
   * @return <true>if the searchResults[] has an entry which hasn't been returned yet.
   * @throws NamingException
   *           if a NamingException occurs.
   */
  public boolean hasMore() throws NamingException {
      if ( current >= searchResults.length )
        return false;

    boolean result = searchResults[current].hasMore();
    if (!result) {
      result = (++current < searchResults.length) && hasMore();
    }
    return result;
  }

  /**
   * Retrieve the next match from the array of NamingEnumerations (the call to hasMore() will automagically bump it to
   * the next enumeration in the array if necessary.
   * 
   * @return Next Entry if available.
   * @throws NoSuchElementException
   *           if no more matches remain (i.e. hasMore() would have failed).
   * @throws NamingException
   *           if any other JNDI exception occurs.
   */
  public Object next() throws NamingException {
    SearchResult result = null;
    if (hasMore()) { // Something to return!
      result = (SearchResult) searchResults[current].next();
      if (dnAttributeName != null) {// Stuff in the DN
        String rdn = result.getName(); // Get the relative dn for this match
        String dn = rdn + "," + executedSearch.getSearchBases()[current]; // Construct a full dn.
        Attributes attrs = result.getAttributes();
        attrs.put(dnAttributeName, dn);
        result.setAttributes(attrs);
      }
    } else {
      throw new NoSuchElementException();
    }
    return result;
  }

  /**
   * Closes the underlying searchResults.
   * 
   * @throws NamingException
   *           if an exception occurs on closure of one of the underlying searchResults.
   */
  public void close() throws NamingException {
    for (int i = 0; i < searchResults.length; i++) {
      searchResults[i].close();
    }
  }

  /**
   * Returns <tt>true</tt> unless all of the searchResults have been exhausted.
   * 
   * @return <tt>true</tt> unless all of the searchResults have been exhausted.
   */
  public boolean hasMoreElements() {
    boolean result;
    try {
      result = hasMore();
    } catch (NamingException ne) {
      result = false;
    }
    return result;
  }

  /**
   * Return the nextElement in this Enumeration.
   * 
   * @return the next element in the Enumeration
   * @throws NoSuchElementException
   *           if the current underlying searchResult throws it.
   */
  public Object nextElement() throws NoSuchElementException {
    Object result;
    try {
      result = next();
    } catch (NoSuchElementException nsee) {
      throw nsee;
    } catch (NamingException ne) {
      result = null;
    }
    return result;
  }
}
