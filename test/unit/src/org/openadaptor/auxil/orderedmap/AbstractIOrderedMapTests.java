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
package org.openadaptor.auxil.orderedmap;

import junit.framework.TestCase;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 25, 2007 by oa3 Core Team
 */

public abstract class AbstractIOrderedMapTests extends TestCase {

    protected static final String KEY_ONE = "Key One";
    protected static final String KEY_TWO = "Key Two";
    protected static final String KEY_THREE = "Key Three";

    protected static final String CHILD_KEY_A ="A";
    protected static final String CHILD_KEY_B ="B";
    protected static final String CHILD_KEY_C ="C";

    protected static final String[] KEYS=new String[] {KEY_ONE ,KEY_TWO, KEY_THREE};

    protected static final String RECORD_ONE="field one";
    protected static final String RECORD_TWO="field two";
    protected static final String RECORD_THREE="field three";
    protected static final String RECORD=RECORD_ONE+RECORD_TWO+RECORD_THREE;

    protected static final String RECORD_ONE_A ="One-A";
    protected static final String RECORD_ONE_B ="One-B";
    protected static final String RECORD_TWO_C ="Two-C";

    protected IOrderedMap testMap;
    protected IOrderedMap hierarchicalTestMap;

    protected void setUp() throws Exception {
        super.setUp();
        testMap = createInstance();
        populateTestMap(testMap);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testMap = null;
    }

    abstract protected IOrderedMap createInstance();

    protected void populateTestMap(IOrderedMap map) {
        map.put(KEY_ONE, RECORD_ONE);
        map.put(KEY_TWO, RECORD_TWO);
        map.put(KEY_THREE, RECORD_THREE);
    }
    protected void populateHierarchicalTestMap(IOrderedMap map) {
        IOrderedMap child=createInstance();
        child.put(CHILD_KEY_A,RECORD_ONE_A);
        child.put(CHILD_KEY_B,RECORD_ONE_B);
        map.put(KEY_ONE,child);
        IOrderedMap child2=createInstance();
        child2.put(CHILD_KEY_C,RECORD_TWO_C);
    }
    /** Test containsKey works */
    public void testContainsKey() {
        assertTrue("This should be true.", testMap.containsKey(KEY_ONE));
        assertFalse("This should be false", testMap.containsKey("This cannot possibly be a key XXXXXX") );
    }

    public void testAdd() {
        int numKeys = testMap.keySet().size();
        int testMapInitialSize = testMap.size();
        String nextVal = "This is the next value";
        try {
            testMap.add(nextVal);
            // List is zero indexed so we can do this. i.e the map's old size is now index of last element.
            assertEquals("Didn't get the value I added.", nextVal, testMap.get(testMapInitialSize));
        }
        catch (Exception e) {
            fail("Unexpected exception adding a next value: " + e );
        }
        assertTrue("Number of keys should be one greater than started with.", (testMap.keySet().size() == numKeys + 1));
    }

    /** Test add at existing index works. */
    public void testAddinsert(){
        int indexToTest = 1; // zero indexed means this is second spot in list
        String newSecondValue = "This is the new second value";
        Object originalSecondValue = testMap.get(indexToTest);
        try {
            testMap.add(indexToTest, newSecondValue);
            assertEquals("Didn't get the value I added.", newSecondValue, testMap.get(indexToTest));
            assertEquals("Orginal second value hasn't been moved post insert.", originalSecondValue, testMap.get(indexToTest+1));
        }
        catch (Exception e) {
            fail("Unexpected exception replacing the second value: " + e );
        }
    }

    public void testAddImmediatelyAfterEnd() {
        int numKeys = testMap.keySet().size();
        int testMapInitialSize = testMap.size();
        String nextVal = "This is the next value";
        try {
            testMap.add(testMapInitialSize, nextVal); // List is zero indexed so we can do this
            assertEquals("Didn't get the value I added.", nextVal, testMap.get(testMapInitialSize));
        }
        catch (Exception e) {
            fail("Unexpected exception adding a next value: " + e );
        }
        assertTrue("Number of keys should be one greater than started with.", (testMap.keySet().size() == numKeys + 1));

        // Looking to ensure orderedkeys in sync with Map.
        IndexOutOfBoundsException expected = null;
        try {
            testMap.get(testMapInitialSize+1);
        }
        catch(IndexOutOfBoundsException e) { expected = e; }
        assertNotNull("Expected an IndexOutOfBoundsException here.", expected);

    }

    public void testAddWellAfterEndOfList() {
        // At this point entry 11 doesn't exist. There should only be four. No idea what's supposed to happen.
        int elevenPlusIndex = testMap.size() + 10; // zero indexed remember
        String elevenPlusVal = "This is the eleventh plus value";
        IndexOutOfBoundsException expected = null;
        try {
            testMap.add(elevenPlusIndex, elevenPlusVal);
            assertEquals("Didn't get the value I added.", elevenPlusVal, testMap.get(elevenPlusIndex));
        }
        catch (IndexOutOfBoundsException e) {
            expected = e;
        }

        if (expected == null) {
            fail("Expected IndexOutOfBoundsException not thrown.");
        }
    }

    /** Test variations on put don't cause oddities */
    public void testPutExistingKey() {
        Integer newSecondValue = new Integer(2) ;
        try {
            testMap.put(KEY_TWO, newSecondValue);
            assertEquals("Didn't get the value I added.", newSecondValue, testMap.get(KEY_TWO));
        }
        catch (Exception e) {
            fail("Unexpected exception putting a value to KEY_TWO: " + e );
        }
    }
    public void testPutNonexistentKey() {
        Integer newValue = new Integer(4);
        String thisKeyDoesntExist="Fred was here";
        try {
            testMap.put(thisKeyDoesntExist, newValue);
            assertEquals("Didn't get the value I added.", newValue, testMap.get(thisKeyDoesntExist));
        }
        catch (Exception e) {
            fail("Unexpected exception putting a value to nonexistent key: " + e );
        }
    }

    /**
     * List add semantics of a map.put(...) is to add values to end of list. Check this holds
     */
    public void testPutAppendsToList() {
        String newKey = "New Key";
        String newValue = "New Value";

        testMap.put(newKey, newValue);

        Object indexedValue = testMap.get(testMap.size() - 1); // zero indexed, this should be last element
        assertEquals("Indexed value not the same as put value.", newValue, indexedValue);
    }

    /** Check the equivalent List and Map gets return the same objects */
    public void testGets() {
        for (int i = 0; i<KEYS.length; i++) {
            Object listGet = testMap.get(i);
            Object mapGet = testMap.get(KEYS[i]);
            assertEquals("Equivalent List and Map gets not the same", listGet, mapGet);
        }
    }

    /**
     * Test the add(int,Object,Object) method.
     */
    public void testAddIndexKeyValue() {
        testMap.add(1,"newKey","newValue");
        assertEquals("newKey",testMap.keys().get(1));
        assertEquals(KEY_TWO,testMap.keys().get(2));
        assertEquals("newValue",testMap.get(1));
        assertEquals(RECORD_TWO,testMap.get(2));
    }

    /**
     *  Test the keys() method.
     */
    public void testKeys() {
        List keys=testMap.keys();
        assertEquals(KEY_ONE,keys.get(0));
        assertEquals(KEY_TWO,keys.get(1));
        assertEquals(KEY_THREE,keys.get(2));
        testMap.put("anotherKey","anotherValue");
        //As the collection is backed by the map - the keys List should also reflect the change.
        assertEquals("anotherKey",keys.get(3));
        testMap.add(1,"YetAnotherValue");
        assertEquals("YetAnotherValue",testMap.get(keys.get(1)));
        assertEquals(KEY_TWO,keys.get(2));
    }

    /**
     * test the values() method
     */
    public void testValues() {
        String NEW_KEY="INSERTED_KEY";
        String NEW_VALUE="INSERTED_VALUE";
        String[] expectedBefore= new String[] {RECORD_ONE,RECORD_TWO,RECORD_THREE};
        String[] expectedAfter= new String[] {RECORD_ONE,NEW_VALUE,RECORD_TWO,RECORD_THREE};
        Collection values=testMap.values();
        int i=0;
        Iterator it=values.iterator();
        while(it.hasNext()){
            assertEquals(expectedBefore[i++],(String)it.next());
        }

        testMap.add(1,NEW_KEY,NEW_VALUE);
        values=testMap.values();
        i=0;
        it=values.iterator();
        while(it.hasNext()){
            assertEquals(expectedAfter[i++],(String)it.next());
        }
    }
    /**
     * Test the indexOf() method.
     */
    public void testIndexOf() {
        for (int index=0;index<testMap.size();index++) {
            Object old=testMap.get(index);
            assertEquals(testMap.indexOf(old),index);
        }
        assertEquals(-1,testMap.indexOf("THIS SHOULD NOT EXIST IN THE MAP"));
    }
    /**
     * Test the clone() method works as advertised.
     */
    public void testClone() {
        IOrderedMap clone = (IOrderedMap)testMap.clone();
        //clone != testMap must be true
        assertFalse("Original and Clone must not be identical", (testMap == clone));
        // clone and testMap must be equal
        assertTrue("Original and Clone must be equal", testMap.equals(clone));
        // clone and testMap should be instances of the same class
        assertTrue("Clone and Original must be instances of the same class", clone.getClass() == testMap.getClass() );
    }
}
