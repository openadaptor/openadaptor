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

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link JNDIReadConnector} and {@link AbstractJNDIReadConnector}.
 * 
 * @author Kris Lachor
 */
public class JNDIReadConnectorTestCase extends MockObjectTestCase {

  private JNDIReadConnector readConnector = new JNDIReadConnector();
  
  private MockJNDIConnection mockJNDIConnection = new MockJNDIConnection();
  
  private MockJNDISearch mockSearch = new MockJNDISearch();
  
  private Mock mockDirContext = new Mock(DirContext.class); 
  
  private Mock mockNamingEnumeration = new Mock(NamingEnumeration.class);
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    readConnector.setJndiConnection(mockJNDIConnection);
    readConnector.setSearch(mockSearch);
  }

  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDIReadConnector#validate(java.util.List)}.
   */
  public void testValidate() {
    List exceptions = new ArrayList();
    readConnector.validate(exceptions);
    assertTrue(exceptions.isEmpty());
    readConnector.setJndiConnection(null);
    readConnector.validate(exceptions);
    assertTrue(exceptions.size()==1);
    exceptions = new ArrayList();
    readConnector.setSearch(null);
    readConnector.validate(exceptions);
    assertTrue(exceptions.size()==2);
  }

  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDIReadConnector#connect()}.
   */
  public void testConnect() {
    assertTrue(mockJNDIConnection.connectCounter==0);
    readConnector.connect();
    assertTrue(mockJNDIConnection.connectCounter==1);
  }

  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDIReadConnector#disconnect()}.
   */
  public void testDisconnect() {
    readConnector.connect();
    mockDirContext.expects(once()).method("close");
    readConnector.disconnect();
  }

  /**
   * Test method for {@link org.openadaptor.auxil.connector.jndi.JNDIReadConnector#next(long)}.
   */
  public void testNext() {
    readConnector.connect();
    assertTrue(mockSearch.executeCounter==0);
    assertFalse(readConnector._searchHasExecuted);
    mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
    readConnector.next(1000);
    assertTrue(mockSearch.executeCounter==1);
    assertTrue(readConnector._searchHasExecuted);
    /* another call should not run the search again */
    mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
    readConnector.next(1000);
    assertTrue(mockSearch.executeCounter==1);
  }

  
  /* Inner mock of {@link JNDIConnection} */
  class MockJNDIConnection extends JNDIConnection {
    int connectCounter = 0;
    public DirContext connect() {
      connectCounter++;
      return (DirContext) mockDirContext.proxy();
    }
    public DirContext connectAlternate() {
      return null;
    }   
  }
  
  /* Inner mock of {@link JNDISearch} */
  class MockJNDISearch extends JNDISearch{
    int executeCounter = 0;
    public NamingEnumeration execute(DirContext context) throws NamingException {
      executeCounter++;
      return (NamingEnumeration)  mockNamingEnumeration.proxy();
    }    
  }
}
