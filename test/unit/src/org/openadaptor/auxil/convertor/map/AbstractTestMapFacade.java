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
package org.openadaptor.auxil.convertor.map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interface tests for  MapFacade
 * @author higginse
 *
 */
public abstract class AbstractTestMapFacade extends TestCase {
  private static final Log log = LogFactory.getLog(AbstractTestMapFacade.class);
 
  protected MapFacade facade;
  protected void setUp() throws Exception {
    super.setUp();
    facade=createInstance();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    facade=null;
  }

  /**
   * Delegate creation of IFacadeGenerator to implementation
   * @return
   */
  abstract protected MapFacade createInstance();

  //BEGIN Common testing of MapFacade behaviour
  
  public void testGetUnderlyingObject(){
    logTest("getUnderlyingObject()");
    assertNotNull(facade.getUnderlyingObject());
  }

  public void testNullGet() {
    logTest("get(<null>) ");
    try {
      facade.get(null);
      fail("Should have thrown a NullPointerException");
    }
    catch (NullPointerException npee) {} //Expected
  }
  
  public void testClone() {
    MapFacade clone=(MapFacade)facade.clone();
    assertTrue(clone != facade);
    assertTrue(clone.getClass() == facade.getClass());
    //assertTrue(facade.getUnderlyingObject().equals(clone.getUnderlyingObject()));
  }
  //END   Common testing of MapFacade behaviour

  protected void logTest(String name) {
    log.info("--- Testing "+name+" ---");
  }

}
