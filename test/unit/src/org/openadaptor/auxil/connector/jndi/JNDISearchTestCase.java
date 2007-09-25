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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link JNDISearch}.
 * 
 * @author Kris Lachor
 * @todo unit test the inner {@link MultiBaseJNDISearchResults}.
 */
public class JNDISearchTestCase extends MockObjectTestCase {
  
  private static final String SEARCH_BASE1 = "ou=people,o=myCompany.com";
  
  private static final String SEARCH_BASE2 = "ou=clients,o=myCompany.com";
  
  JNDISearch search = new JNDISearch();
  
  String [] searchBases = new String[]{SEARCH_BASE1,SEARCH_BASE2};
  
  String filter = "(sn=Smith)";
  
  String [] attributes = new String[]{"businessline", "employeenumber", "givenname", "location"};
  
  private Mock mockDirContext = new Mock(DirContext.class); 
  
  private Mock mockNamingEnumeration = new Mock(NamingEnumeration.class);
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    search.setSearchBases(searchBases);
    search.setFilter(filter);
  }

  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDISearch#execute(
   * javax.naming.directory.DirContext)}.
   * Mandatory searchBases not set.
   */
  public void testExecute1() {
    search.setSearchBases(null);
    try {
      search.execute((DirContext)mockDirContext.proxy());
    } catch (NamingException e) {
      /* exception expected */
      return;
    }
    assertTrue(false);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDISearch#execute(
   * javax.naming.directory.DirContext)}.
   */
  public void testExecute2() throws NamingException{
    mockDirContext.expects(atLeastOnce()).method("search").with(eq(SEARCH_BASE1), eq(filter), 
        eq(search.searchControls)).will(returnValue((NamingEnumeration)mockNamingEnumeration.proxy()));
    mockDirContext.expects(atLeastOnce()).method("search").with(eq(SEARCH_BASE2), eq(filter), 
        eq(search.searchControls)).will(returnValue((NamingEnumeration)mockNamingEnumeration.proxy()));
    assertNull(search.searchControls.getReturningAttributes());
    Object result = search.execute((DirContext)mockDirContext.proxy());
    assertNotNull(result);
    assertTrue(result instanceof MultiBaseJNDISearchResults);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDISearch#execute(
   * javax.naming.directory.DirContext)}.
   * With attributes set.
   */
  public void testExecute3() throws NamingException{
    search.setAttributes(attributes);
    mockDirContext.expects(atLeastOnce()).method("search").with(eq(SEARCH_BASE1), eq(filter), 
        eq(search.searchControls)).will(returnValue((NamingEnumeration)mockNamingEnumeration.proxy()));
    mockDirContext.expects(atLeastOnce()).method("search").with(eq(SEARCH_BASE2), eq(filter), 
        eq(search.searchControls)).will(returnValue((NamingEnumeration)mockNamingEnumeration.proxy()));
    /* 
     * Attributes are set on the search controls only for the duration of method execution,
     * then set back to their original values.
     */
    assertNull(search.searchControls.getReturningAttributes());
    Object result = search.execute((DirContext)mockDirContext.proxy());
    assertNotNull(result);
    assertTrue(result instanceof MultiBaseJNDISearchResults);
    assertNull(search.searchControls.getReturningAttributes());
  }

}
