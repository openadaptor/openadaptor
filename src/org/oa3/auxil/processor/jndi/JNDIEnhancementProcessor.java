/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.auxil.processor.jndi;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/processor/jndi/JNDIEnhancementProcessor.java,v 1.16 2006/11/08 10:41:43 shirea Exp $
 * Rev:  $Revision: 1.16 $
 * Created Nov 23, 2005 by Eddy Higgins
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.connector.jndi.AbstractJNDIReader;
import org.oa3.auxil.connector.jndi.JNDIReader;
import org.oa3.auxil.connector.jndi.JNDISearch;
import org.oa3.auxil.connector.jndi.JNDIUtils;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.processor.orderedmap.AbstractOrderedMapProcessor;
import org.oa3.core.exception.OAException;
import org.oa3.core.exception.RecordException;

/**
 * Processor to update incoming records with data from a directory.
 *
 * @author Eddy Higgins
 * @author Andrew Shire
 */
/* Done: Needed to enrich JndiEnhancementProcessor:
                + search root driven by dn in incomingKeyMap (when present in values)
                      - issue warning if overriding explicit JNDISearch searchBases;
                + filter uses values in incomingKeyMap
                      - derive filter using actual incoming values and AND it with any explicit JNDISearch filter;
                + explicitly ask for just the attributes in outgoingKeyMap keys (performance and operational attributes)
                      - merge outgoingKeyMap keys with list of attributes directly specified on JNDISearch;
                + to get dn to appear in results it must be set on JNDISearch.returnedDNAttributeName,
                      and
                + refactor: merge JNDIExistenceProcessor into it.
                + refactor: make it cleaner to subclass with just a different AbstractJNDIReader.
  */

/* TODO: Need to enrich JNDISearch:
                + schema: allow explicit statement of which attributes are multi-valued (if missing treat all as multi-valued).
  */
public class JNDIEnhancementProcessor extends AbstractOrderedMapProcessor {
    
  private static final Log log = LogFactory.getLog(JNDIEnhancementProcessor.class.getName());

    // internal state
    protected JNDISearch search; //Convenience, obtained directly from reader.
    protected String[] outgoingKeys; // derived from outgoingMap bean property
    protected String configDefinedSearchFilter; // derived from filter property of embedded search property
    protected String[] configDefinedSearchAttributes; // derived from attributes property of embedded search property

    // bean properties:
    protected AbstractJNDIReader reader;
    protected Map incomingMap;
    protected Map outgoingMap;
    protected String recordKeyUsedAsSearchBase = null;
    protected String recordKeySetByExistence = null;
    protected String valueIfExists ="true";
    protected String valueIfDoesNotExist ="false";

    //BEGIN Bean getters/setters
    public void setReader(JNDIReader reader) {
        this.reader=reader;
    }

    public JNDIReader getReader() {
        return (JNDIReader)reader;
    }

    public void setIncomingMap(Map incomingMap) {
        this.incomingMap = incomingMap;
    }

    public Map getIncomingMap(){
        return incomingMap;
    }

    public void setOutgoingMap(Map outgoingMap) {
        this.outgoingMap = outgoingMap;
    }

    public Map getOutgoingMap(){
        return outgoingMap;
    }

    public void setRecordKeyUsedAsSearchBase(String recordKeyUsedAsSearchBase) {
        this.recordKeyUsedAsSearchBase = recordKeyUsedAsSearchBase;
    }

    public String getRecordKeyUsedAsSearchBase() {
        return recordKeyUsedAsSearchBase;
    }

    public void setRecordKeySetByExistence(String recordKeySetByExistence) {
        this.recordKeySetByExistence = recordKeySetByExistence;
    }

    public String getRecordKeySetByExistence() {
        return recordKeySetByExistence;
    }

    public void setValueIfExists(String valueIfExists) {
        this.valueIfExists =valueIfExists;
    }

    public String getValueIfExists() {
        return valueIfExists;
    }

    public void setValueIfDoesNotExist(String valueIfDoesNotExist) {
        this.valueIfDoesNotExist =valueIfDoesNotExist;
    }

    public String getValueIfDoesNotExist() {
        return valueIfDoesNotExist;
    }
    //END   Bean getters/setters


    public Object[] processOrderedMap(IOrderedMap orderedMap) throws RecordException {
        Object[] result=null;

        tailorSearchToThisRecord(orderedMap);

        try {
            IOrderedMap[] matches=getMatches(); //This should now have an array of IOrderedMaps to work with
            if (matches==null) {
                log.debug("Enrichment search returned no results");

                // So simply pass original data through un-enhanced:
                result = new IOrderedMap[1];
                result[0] = (IOrderedMap)orderedMap.clone();

                // And set existence flag to does not exist:
                if (recordKeySetByExistence != null) {
                    ((IOrderedMap)result[0]).put(recordKeySetByExistence, valueIfDoesNotExist);
                }
            }
            else {
                int size=matches.length;
                log.debug("Enrichment search returned "+size+" results");
                result=new IOrderedMap[size];
                for ( int i=0;i<size;i++)
                {
                    IOrderedMap outgoing= (IOrderedMap)orderedMap.clone();

                    // Enrich outgoing record according to outgoingMap:
                    if (outgoingMap != null && outgoingMap.size()>0)
                    {
                        Iterator outgoingMapIterator = outgoingMap.entrySet().iterator();
                        while (outgoingMapIterator.hasNext())
                        {
                            Map.Entry entry = (Map.Entry) outgoingMapIterator.next();
                            Object outKeyValue=matches[i].get(entry.getKey());
                            if (outKeyValue == null) {
                                // attribute has a null value, only write it if that is because attribute is present:
                                if (matches[i].containsKey(entry.getKey())) {
                                    outgoing.put(entry.getValue(), outKeyValue);
                                }
                            } else {
                                // attribute has a real value, so use it:
                                outgoing.put(entry.getValue(), outKeyValue.toString());
                            }
                        }
                    }

                    // And set existence flag to exists:
                    if (recordKeySetByExistence != null)
                    {
                        outgoing.put(recordKeySetByExistence, valueIfExists);
                    }

                    log.debug("OutputMap: "+outgoing);
                    result[i]=outgoing;
                }
            }
        }
        catch (Exception e) {
            log.info("RecordException of "+e.getMessage());
            if (log.isDebugEnabled()) e.printStackTrace();
            throw new RecordException(e.getMessage(),e);
        }
        return result;
    }


    protected IOrderedMap[] getMatches() throws Exception {
        IOrderedMap[] results=null;
        boolean treatMultiValuedAttributesAsArray=search.getTreatMultiValuedAttributesAsArray();
        String joinArraysWithSeparator=search.getJoinArraysWithSeparator();
        NamingEnumeration current=search.execute(((JNDIReader)reader).getContext());
        ArrayList resultList=new ArrayList();
        while (current.hasMore()) {
            resultList.add(JNDIUtils.getOrderedMap((SearchResult)current.next(),treatMultiValuedAttributesAsArray,joinArraysWithSeparator));
        }
        if (resultList.size()>0){
            results=(IOrderedMap[])resultList.toArray(new IOrderedMap[resultList.size()]);
        }
        return results;
    }


    public void tailorSearchToThisRecord(IOrderedMap orderedMapRecord) throws RecordException
    {
        // Use a dynamic search base from the incoming record?
        if (recordKeyUsedAsSearchBase != null)
        {
            Object incomingBase = orderedMapRecord.get(recordKeyUsedAsSearchBase);
            if ( (incomingBase == null) || !(incomingBase instanceof CharSequence) )
            {
                log.warn("Empty search base produced: recordKeyUsedAsSearchBase missing from this record: "+orderedMapRecord);
                throw new RecordException("Empty search base produced: recordKeyUsedAsSearchBase missing from this record.");
            }
            if (!(incomingBase instanceof String)) {
                incomingBase = incomingBase.toString();
            }
            search.setSearchBases(new String[] { (String)incomingBase });
        }


        // Set up the search filter to use all incomingMap values (GDS attribute names) with
        // any corresponding record values (if null then use "*").
        StringBuffer searchFilter=new StringBuffer();
        if (incomingMap != null)
        {
            if (incomingMap.size() > 1)
            {
                searchFilter.append("(&");
            }
            Iterator incomingMapIterator = incomingMap.entrySet().iterator();
            while (incomingMapIterator .hasNext())
            {
                Map.Entry entry = (Map.Entry) incomingMapIterator .next();
                Object recordValue=orderedMapRecord.get(entry.getKey());
                if (recordValue != null) {
                    searchFilter.append("(").append(entry.getValue()).append("=").append(recordValue).append(")");
                }
            }
            if (incomingMap.size() > 1)
            {
                searchFilter.append(")");
            }
        }
        // Combine it with any config defined search filter (e.g. it might restrict objectclass)
        if (configDefinedSearchFilter != null && configDefinedSearchFilter.length() > 0)
        {
            if (incomingMap != null) {
                searchFilter.insert(0, "(&");
            }
            searchFilter.append(configDefinedSearchFilter);
            if (incomingMap != null) {
                searchFilter.append(")");
            }
        }
        // Sanity check (don't want to do unconstrained searches):
        if (searchFilter.length()==0) {
            log.warn("Empty search filter produced: probably missing incomingMap keys in record: "+orderedMapRecord);
            throw new RecordException("Empty search filter produced: probably missing incomingMap keys in tbis record.");
        }
        // Set this updated filter:
        search.setFilter(searchFilter.toString());
    }


    public void enforcePreconditions() throws OAException
    {
        // Enforce preconditions:
        if ((incomingMap == null || incomingMap.size()<1) && (recordKeyUsedAsSearchBase == null))
        {
            log.warn("Must provide an incomingKeyMap and/or set recordKeyUsedAsSearchBase.");
            throw new OAException("Must provide an incomingKeyMap and/or set recordKeyUsedAsSearchBase.");
        }
        if ((outgoingMap == null || outgoingMap.size()<1) && (recordKeySetByExistence == null))
        {
            log.warn("Must provide an outgoingKeyMap and/or set recordKeyUsedForExistence.");
            throw new OAException("Must provide an outgoingKeyMap and/or set recordKeyUsedForExistence.");
        }

        String[] bases = search.getSearchBases();
        if (recordKeyUsedAsSearchBase == null) {
            // Must provide a searchBase in the embedded JNDISearch:
            if (bases == null || bases.length < 1) {
                log.warn("Must provide a non-empty search.searchBases (or provide recordKeyUsedAsSearchBase).");
                throw new OAException("Must provide a non-empty search.searchBases (or provide recordKeyUsedAsSearchBase).");
            }
        } else {
            // Must not provide a searchBase in the embedded JNDISearch as well:
            if (bases != null && bases.length > 0) {
                log.warn("Must provide either a search.searchBases or a recordKeyUsedAsSearchBase (not both!).");
                throw new OAException("Must provide either a search.searchBases or a recordKeyUsedAsSearchBase (not both!).");
            }
            // Must provide an incomingMap and/or a search filter in the embedded JNDISearch (eg. "(objectclass=*"))
            String filter = search.getFilter();
            if ((incomingMap == null || incomingMap.size()<1) && (filter == null || filter.length()==0))
            {
                log.warn("Must provide an incomingMap and/or a search.filter.");
                throw new OAException("Must provide an incomingMap and/or a search.filter.");
            }
        }

        // Initialise derived member variables:
        if (outgoingMap == null || outgoingMap.size()<1) {
            outgoingKeys = new String[] { };
        } else {
            outgoingKeys = (String[]) outgoingMap.keySet().toArray(new String [] {});
        }

        configDefinedSearchAttributes = search.getAttributes();
        if (configDefinedSearchAttributes == null) {
            configDefinedSearchAttributes = new String[] { };
        }

        configDefinedSearchFilter = search.getFilter();
        if (configDefinedSearchFilter != null && !configDefinedSearchFilter.startsWith("(") && !configDefinedSearchFilter.endsWith(")"))
        {
            configDefinedSearchFilter = "(" + configDefinedSearchFilter + ")";
        }


        // Setup the attributes we're interested in:
        // outgoingMap keys combined with any config defined search attributes
        int attribsSize = outgoingKeys.length + configDefinedSearchAttributes.length;

        String[] attributeNames = new String[attribsSize];

        for (int i=0; i < outgoingKeys.length; i++)
        {
            attributeNames[i] = outgoingKeys[i];
        }

        for (int i=0; i < configDefinedSearchAttributes.length; i++)
        {
            attributeNames[i + outgoingKeys.length] = configDefinedSearchAttributes[i];
        }

        search.setAttributes(attributeNames);
    }


    /**
     * @throws OAException
     */
    public void initialise() throws OAException
    {
        // relied on to allow this class to be subclassed by code that repeats the following with a different reader:
        search=reader.getSearch();

        enforcePreconditions();

        // Connect to enrichment source:
        reader.connect();
    }

    protected static String toString(String[] attributeNames) {
        String result=null;
        if (attributeNames!=null) {
            int length=attributeNames.length;
            if (length>0){
                StringBuffer sb=new StringBuffer("[");
                for (int i=0;i<length;i++) {
                    sb.append(attributeNames[i]);
                    sb.append(",");
                }
                sb.setCharAt(sb.length()-1,']');
                result=sb.toString();
            }
        }
        return result;
    }

}
