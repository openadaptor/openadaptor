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

package org.openadaptor.auxil.convertor.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordConversionException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Factory for MapFacade generator implementations.
 * 
 * @author higginse
 * @since Introduced post 3.2.1
 */
public class MapFacadeFactory  {

  private static final Log log = LogFactory.getLog(MapFacadeFactory.class);

  private Map registeredFacades;
  private Map cachedFacades;
  //ToDo: cache misses also - for performance.
  
  /**
   * This is the default facadeGenerator for Dom4J Document objects.
   */
  public static final IFacadeGenerator DEFAULT_DOM4J_FACADE_GENERATOR =
    new IFacadeGenerator() {
    public MapFacade generateFacade(Object object) {
      if (object==null) {
        throw new NullRecordException("Object may not be null. It must be a Dom4j Document");
      }
      if (! (object instanceof Document)) {
        throw new RecordFormatException("Supplied object is not a Dom4j Document");
      }
      return new DocumentMapFacade((Document)object);
    }
  };

  /**
   * Create a new facade factory.
   * 
   */
  public MapFacadeFactory() {
    registeredFacades=new HashMap();
    cachedFacades=new HashMap();
    Map preCannedEntries=new HashMap();
    preCannedEntries.put("org.dom4j.Document",DEFAULT_DOM4J_FACADE_GENERATOR);
    setRegisteredFacades(preCannedEntries);
  }

  public MapFacade generateMapFacade(Object object) {
    if (object==null) {
      fail("Cannot obtain facade for null object");
    }
    IFacadeGenerator generator=null;
    //Find the class of the object requiring a facade generator.
    Class objectClass=object.getClass();
    //Check if it is directly registered
    if (registeredFacades.containsKey(objectClass)){
      //Yes - obtain the generator, and use it to generate a facade;
      generator =(IFacadeGenerator)registeredFacades.get(objectClass);
    }
    else { //Not directly registered - check cache
      if (cachedFacades.containsKey(objectClass)) {
        generator=(IFacadeGenerator)cachedFacades.get(objectClass);
      }
      else { //Not cached - now have to check each registered generator.
        Iterator entries=registeredFacades.entrySet().iterator();
        while (entries.hasNext()) {
          Map.Entry entry=(Map.Entry)entries.next();
          Class entryClass=(Class)entry.getKey();
          //If the object can be cast to the entries class, we're in business!
          if (entryClass.isAssignableFrom(objectClass)) {
            //Bingo!
            if (log.isDebugEnabled()) {
              log.debug("Found suitable entry "+entryClass.getName()+" to handle "+objectClass.getName());
            }
            generator=(IFacadeGenerator)entry.getValue();
            //Cache the match for faster future lookups (no linear search through entries)
            cachedFacades.put(objectClass, generator);
          }
        }
      }
    }
    if (generator ==null) {
      fail("Failed to obtain a facade generator for object with class "+objectClass.getName());
    }
    return generator.generateFacade(object);
  }

  protected void fail(String message) throws RecordConversionException {
    log.warn(message);
    throw new RecordConversionException(message);
  }

  //BEGIN Accessors

  /* Disabled for now - it's not symmetrical - (keys are classes really)
  public Map getRegisteredFacades() {
    return Collections.unmodifiableMap(registeredFacades);
  }
   */
  /**
   * Note: If an existing entry class is specified it will overwrite the original.
   * @param facades
   */
  public void setRegisteredFacades(Map facades) {
    Iterator it=facades.entrySet().iterator();
    while(it.hasNext()) {
      Map.Entry entry=(Map.Entry)it.next();
      Object key=entry.getKey();
      Object value=entry.getValue();
      if (!(key instanceof String)) {
        throw new RuntimeException("Facade Key must be a String containing a class name");
      }
      if (!(value instanceof IFacadeGenerator)) {
        throw new RuntimeException("Value must be an IFacadeGenerator instance ");
      }
      String className=(String)key; 
      IFacadeGenerator generator=(IFacadeGenerator)value;
      try {
        register(className,generator);      
      }
      catch (ClassNotFoundException cnfe) {
        log.warn("Failed to register facade generator "+generator+" for class "+className);
      }
    }
  }

  //END   Accessors
  private void register(String objectClassName,IFacadeGenerator facadeGenerator) throws ClassNotFoundException {
    register(Class.forName(objectClassName),facadeGenerator);
  }

  private void register(Class objectClass,IFacadeGenerator facadeGenerator)  {
    if (registeredFacades.containsKey(objectClass)){
      log.warn("Overriding existing entry for class "+objectClass.getName());
    }
    registeredFacades.put(objectClass, facadeGenerator);
  }

}
