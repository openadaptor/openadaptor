/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.processor.map;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.map.MapFacade;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.RecordException;

/**
 * Change keys in an incoming Map, to one or more new keys using a supplied mapping.
 * <p>
 * The configured Map contains key names, and the new (key) values they are to
 * be mapped to.
 * <p>
 * Note that a given key may be mapped to multiple new keys, by supplying a 
 * List (of new key names) in place of a single value.
 * 
 * Note also that the mapping may not preserve ordering of mapped keys in
 * the output record - for example a mapped field in an OrderedMap will result
 * in a new entry being appended to the end of the existing fields (with the 
 * original entry having been removed)
 * 
 * @author Eddy Higgins
 * @since Introduced Post 3.2.1
 */
public class AttributeMapProcessor extends AbstractMapProcessor {
  private static final Log log = LogFactory.getLog(AbstractMapProcessor.class);

  private static final String DEFAULT_CLONE_METHOD ="clone"; 
  protected Map map;

  //ToDo: Decide if this will be used.
  protected String mapCloneMethod = DEFAULT_CLONE_METHOD;
  
  protected boolean deleteMappedKeys=true;
  protected boolean retainMappedEntriesOnly=false;

  // Bean Properties

  public Map getMap() {
    return map;
  }

  public void setMap(Map map) {
    this.map = map;
  }
  /**
   * If true, then delete original key:value pairings if a mapping exists.
   * <br>
   * Existing keys which are not contained in the mapping are not modified.
   * <br>
   * The default is true - i.e. delete mapped keys
   * @param deleteMappedKeys
   */
  public void setRemoveMappedKeys(boolean deleteMappedKeys) {
    this.deleteMappedKeys=deleteMappedKeys;
  }
  
  /**
   * If true, entries in the original map which are not referenced in the
   * mapping, will no longer exist in the outgoing record.
   * <p>
   * The default is false - i.e. unreferenced entries will pass to the
   * output record unmodified.
   * 
   * @param retainMappedEntriesOnly
   */
  public void setRetainMappedEntriesOnly(boolean retainMappedEntriesOnly){
    this.retainMappedEntriesOnly=retainMappedEntriesOnly;
  }


  // End Bean Properties

  /**
   * Map key names in an incoming Map record, according to the supplied Map.
   * <br>
   * This will check the record for each key in the supplied map. If
   * the key exists, the value will get a new key within the record,
   * as determined by the map's value for that key. 
   * <br>
   *  
   * @param incoming A record as a Map implementation
   * @return Object[] with mapped entries.
   * @throws RecordException
   * 
   */
  public Object[] processMap(Map incoming) throws RecordException {
    Object clone=cloneMap(incoming);
    if (! (clone instanceof Map)) {
      log.warn("Clone method did not return a Map instance");
      throw new RecordException("Clone method did not return a Map instance");
    }
    Map outgoing = (Map)clone;
    Iterator it = incoming.keySet().iterator();
    while (it.hasNext()){
      Object key=it.next();
      if (map.containsKey(key)) { //Then we need to modify it.
        Object value=incoming.get(key);
        Object newKey=map.get(key); //Get the new value(s) for key
        if (newKey instanceof List) {
          Iterator keys=((List)newKey).iterator();
          while (keys.hasNext()) {
            outgoing.put(keys.next(), value);
          }
        }
        else {
          outgoing.put(newKey,value);
        } 
        if (deleteMappedKeys) {
          outgoing.remove(key);  
        }
      }
      else { //Not referenced by the mapping
        if (retainMappedEntriesOnly) {
          outgoing.remove(key);
        }
      }
    }
    return new Object[]{outgoing} ;
  }

  public void validate(List exceptions) {
    Exception e = checkMandatoryProperty("map", map != null);
    if (e != null)
      exceptions.add(e);
  }

  protected Object cloneMap(Map incoming) {
    if (incoming instanceof IOrderedMap) {
      return ((IOrderedMap)incoming).clone();
    }
    if (incoming instanceof MapFacade) {
      return ((MapFacade)incoming).clone();
    }
    if (incoming instanceof HashMap) {
      return ((HashMap)incoming).clone();
    }
    try {
      Method cloneMethod=incoming.getClass().getMethod(mapCloneMethod,(Class[])null);
      return (cloneMethod.invoke(incoming,(Object[])null));
    }
    catch (NoSuchMethodException nsme) {
      log.warn("Unable to find clone method  "+mapCloneMethod+"(). "+nsme.getMessage());
    }
    catch (InvocationTargetException ite){
      log.warn("Unable to invoke clone method "+mapCloneMethod+"(). "+ite.getMessage());      
    }
    catch (IllegalAccessException iae) {
      log.warn("Failed to invoke clone method "+mapCloneMethod+"(). "+iae.getMessage()); 
    }
    log.warn("Unable to clone incoming map - the original might get modified!");
    return incoming;
  }

}
