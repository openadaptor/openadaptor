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

package org.openadaptor.auxil.orderedmap;

import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.simplerecord.ISimpleRecord;

/**
 * This extends the <code>Map</code> interface to add <code>List</code>-like behaviour.
 * <p>
 * IOrderedMaps are primarily intended to provide access to tabular data.
 * <p>
 * Unfortunately it's not possible to extend both <code>Map</code> and <code>List</code> interfaces as both specify
 * remove() methods with conflicting signatures.
 * <p>
 * In essence, an IOrderedMap is one where in addition to providing Map functionality, the order of the data in the map
 * is also managed.
 * <p>
 * For example, when entries are added via the put(key,value) method, the order in which they are added is preserved.
 * Also, if an entry is added without a key, then one is auto-generated for it.
 * <p>
 * Note also that the current implementation does not attempt to give complete coverage of the List interface, only
 * those methods which we currently see as necessary. This may well change as OA3 matures.
 * 
 * @author Eddy Higgins
 * @see ISimpleRecord
 */
public interface IOrderedMap extends Map, ISimpleRecord {
  // BEGIN chosen methods from List interface
  // public int size();
  // public boolean isEmpty();
  // public boolean contains(Object object) ;
  // public Iterator iterator();
  // public Object[] toArray() ;
  // public Object[] toArray(Object[] objects) ;

  /**
   * Inserts the specified mapping (key->value) at the specified position in this list.
   * <p>
   * Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their
   * indices).
   * 
   * @param index
   *          index at which the specified element is to be inserted.
   * @param key
   *          the key to be associated with the element.
   * @param value
   *          element to be inserted.
   * @throws IndexOutOfBoundsException
   *           if index out of range (index < 0 || index >= size()).
   */
  public void add(int index, Object key, Object value);

  /**
   * Inserts the specified element at the specified position in this list, having associated it with an auto-generated
   * key.
   * <p>
   * Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their
   * indices). Other than for key generation, this should have identical behaviour to java.util.List.add(int i,Object
   * object)
   * 
   * @param index
   *          index at which the specified element is to be inserted.
   * @param object
   *          element to be inserted.
   * @see java.util.List
   */
  public void add(int index, Object object);

  /**
   * Add an object, associating it with an auto-generated key in the process.
   * <p>
   * To keep it <code>List</code>-like, it returns a <code>boolean</code>. A viable alternative might be to return
   * the auto-generated key.
   * 
   * @param object
   *          the object to add.
   * @return true if the add succeeded, false otherwise.
   */
  public boolean add(Object object); // ToDo: Discuss option of cheating and returning key.

  // Clashes with Map!
  // public boolean remove(Object object) ;

  // public boolean containsAll(Collection collection);
  // public boolean addAll(Collection collection);
  // public boolean addAll(int i, Collection collection);
  // public boolean removeAll(Collection collection) ;
  // public boolean retainAll(Collection collection) ;
  // public void clear();

  /**
   * Return a shallow copy of this implementation of IOrderedMap.
   * <p>
   * The copy maintains the correct key order. Keys and Values are not copied. The clone() method is provided
   * IOrderedMap interface to force implementors to implement it correctly.
   * <p>
   * The return type is Object to avoid potential conflicts with superclass implementations of IOrderedMap
   * implemenatations.
   * 
   * @return Object containing a clone of the current ordered map.
   */
  public Object clone();

  /**
   * Returns the element at the specified position in this map.
   * <p>
   * This should have similar behaviour to java.util.List.get(int i);
   * 
   * @param index
   *          the index of the required value.
   * @return the value stored at the index
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >= size())
   * @see java.util.List
   */
  public Object get(int index);

  /**
   * Returns the index in this list of the specified element, or -1 if this list does not contain this element.
   * 
   * @param object
   *          element to search for.
   * @return the index in this list of the occurrence of the specified element, or -1 if this list does not contain this
   *         element.
   */
  public int indexOf(Object object);

  /**
   * Get an ordered <code>List</code> of the keys.
   * <p>
   * Differs from keySet() in that it guarantees that the ordering of the keys in the list matches their order in the
   * <code>IOrderedMap</code>
   * 
   * @return Ordered List containing the keys for the values in the map.
   */
  public List keys();

  /**
   * Replaces the element at the specified position in this list with the specified element.
   * <p>
   * This should have similar behaviour to java.util.List.set(int i,Object object)
   * 
   * @param index
   *          the index of the element to replace.
   * @param object
   *          the element to be stored at the specified position
   * @return the value previously stored at the specified position
   * @see java.util.List
   */
  public Object set(int index, Object object);

  /**
   * Removes the element at the specified position in this list.
   * <p>
   * Shifts any subsequent elements to the left (subtracts one from their indices). Returns the element that was removed
   * from the list. This should have similar behaviour to java.util.List.remove(int)
   * 
   * @param index
   *          the index of the element to removed.
   * @return the element previously at the specified position.
   * @see java.util.List
   */
  public Object remove(int index);

  // public int lastIndexOf(Object object);
  // public ListIterator listIterator() ;
  // public ListIterator listIterator(int i) ;
  // public List subList(int i, int i1);
  // END chosen methods from List interface

}
