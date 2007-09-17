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
package org.openadaptor.auxil.connector.jdbc.writer;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.openadaptor.core.IComponent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 13, 2007 by oa3 Core Team
 */

abstract public class AbstractSQLWriterTests extends MockObjectTestCase {

  protected ISQLWriter testWriter;
  protected Mock connectionMock;
  protected Mock componentMock;

  abstract protected ISQLWriter instantiateTestWriter();

  abstract protected void setMocksFor(ISQLWriter writer);

  protected void setUp() throws Exception {
    super.setUp();
    testWriter = instantiateTestWriter();
    setMocksFor(testWriter);
    connectionMock = mock(Connection.class);
    componentMock = mock(IComponent.class);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testWriter = null;
    connectionMock = null;
  }

  public void testInitialise() {
    try {
      testWriter.initialise((Connection)connectionMock.proxy());
    } catch (Exception e) {
      fail("Did not expect an exception here." + e);
    }
  }

  public void testValidate() {
    List exceptions = new ArrayList();
    testWriter.validate(exceptions, (IComponent)componentMock.proxy());
    assertTrue("Should be no validation errors", exceptions.size() == 0);
  }
}
