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

import java.sql.Connection;
import java.sql.SQLException;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 21, 2007 by oa3 Core Team
 */

public abstract class AbstractMapWriterTests extends AbstractSQLWriterTests {


    /**
   * Ensure that with reasonable expectations set for the underlying jdbc mocks
   * the writer initialises without error.
   */
  public void testInitialise() {
    setupInitialiseExpectations(true);
    super.testInitialise();
  }

  /**
   * Set the expectations of the jdbc mocks reasonably to enable the the writer
   * to be initialised. NB this is a little circular at the moment ie. these
   * expectations have been set with respect to what the writer already does.
   *
   * @param supportsBatch Set batch support on the JDBC Layer
   */
  protected void setupInitialiseExpectations(boolean supportsBatch) {
    fail("You must set the default initialise expectations appropriately");
  }

  /**
   * Check that hasBatchSupport is set correctly. In this case the underlying
   * mock jdbc layer is set to support batching and we check that the writer
   * correctly picks up on this.
   */
  public void testHasBatchSupport() {
    setupInitialiseExpectations(true);
    testWriter.initialise((Connection) connectionMock.proxy());
    assertTrue("Batching should be enabled", testWriter.hasBatchSupport());
  }

  /**
  * Check that hasBatchSupport is set correctly. In this case the underlying
  * mock jdbc layer is set to NOT support batching and we check that the writer
  * correctly picks up on this.
  */
  public void testHasBatchSupportBatchingDisabled() {
    setupInitialiseExpectations(false);
    testWriter.initialise((Connection) connectionMock.proxy());
    assertFalse("Should not support batching at this point", testWriter.hasBatchSupport());
  }


  // Test writing a single data element (i.e. a batch of one).

  /**
   * Test writing a batch of one with batch support enabled.
   */
  public void testWriteSingleton() {
    Object[] data = setUpSingletonDataAndDataExpections();
    setupInitialiseExpectations(true);
    testWriter.initialise((Connection)connectionMock.proxy());
    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }

  /**
   * Test writing a batch of one with batch support disabled.
   */
  public void testWriteSingletonBatchingDisabled() {
    Object[] data = setUpSingletonDataAndDataExpections();
    setupInitialiseExpectations(false);
    testWriter.initialise((Connection) connectionMock.proxy());
    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }

  /**
   * This method must be overrriden to generate test data consisting of a batch of size one.
   * Associated mock expectations relating to this component must also be set.
   *
   * @return Object[]
   */
  protected Object[] setUpSingletonDataAndDataExpections() {
    fail("Data and data expectations for the test 'Write Singleton' are not set");
    return new Object[] { new Object() };
  }

  /**
   * Test writing a batch of three with batch support enabled.
   */
  public void testWriteBatch() {
    Object[] data = setupWriteBatchDataAndExpectationsBatchingEnabled();
    setupInitialiseExpectations(true);
    testWriter.initialise((Connection) connectionMock.proxy());

    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }

  /**
   * This method must be overrriden to generate test data consisting of a  batch.
   * Associated mock expectations relating to this component must also be set. Note
   * that these expectations are to be set assuming the underlying jdbc layer is configured
   * to eanble batch uploads.
   *
   * @return Object[]
   */
  protected Object[] setupWriteBatchDataAndExpectationsBatchingEnabled() {
    fail("Data and data expectations for the test 'Write Batch' are not set");
    return new Object[0];
  }

 /**
   * Test writing a batch of three with batch support disabled.
   */
  public void testWriteBatchBatchingDisabled() {
    Object[] data = setupWriteBatchDataAndExpectationsBatchingDisabled();

    setupInitialiseExpectations(false);
    testWriter.initialise((Connection) connectionMock.proxy());
    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e);
    }
  }

  /**
   * This method must be overriden to generate test data consisting of a  batch.
   * Associated mock expectations relating to this component must also be set. Note
   * that these expectations are to be set assuming the underlying jdbc layer is configured
   * to disable batch uploads.
   *
   * @return Object[]
   */
  protected Object[] setupWriteBatchDataAndExpectationsBatchingDisabled() {
    fail("Data and data expectations for the test 'Write Batch' are not set");
    return new Object[0];
  }

 /**
   * Test writing a batch of one null element with default configuration. Should throw an SQLException.
   */
  public void testWriteNullData() {
    Object[] data = setupWriteNullDataExpectations();
    try {
      testWriter.writeBatch(data);
    } catch (SQLException e) {
      return;
    }
    fail("Expected an SQLException");
  }

  /**
   * Generate test data consisting of a batch with one Null element. Associated
   * mock expectations relating to this component must also be set. This method
   * must be overriden if the specific component being tested has different
   * expectations.
   *
   * @return Object[]
   */
  protected Object[] setupWriteNullDataExpectations() {
    Object[] data = new Object [] { null };
    // Test should bail before any of these methods get called (with or without batching).
    preparedStatementMock.expects(never()).method("clearParameters");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(never()).method("close");

    setupInitialiseExpectations(false);
    testWriter.initialise((Connection)connectionMock.proxy());
    return data;
  }

  /**
   * Test writing a batch of one non-Map element. Should throw a RuntimeException.
   */
  public void testWriteNonMapData() {
    setupInitialiseExpectations(true);
    Object[] data = setupDataForWriteNonMapData();
    testWriter.initialise((Connection)connectionMock.proxy());
    try {
      testWriter.writeBatch(data);
    } catch (RuntimeException e) {
      return; // This is the expected outcome.
    } catch (SQLException e) {
      fail("Did not expect SQLException: " + e);
    }
    fail("Expected a RuntimeException");
  }

  protected Object[] setupDataForWriteNonMapData() {
    Object[] data = new Object []{"This is not a Map"};
    // Test should bail before any of these methods get called (with or without batching).
    preparedStatementMock.expects(never()).method("clearParameters");
    preparedStatementMock.expects(never()).method("executeUpdate");
    preparedStatementMock.expects(never()).method("executeBatch");
    preparedStatementMock.expects(never()).method("close");
    return data;
  }


  /**
   * Test an attempt to write without first initialising the writer. This
   * should normally never happen and shouldn't work. Currently throws a
   * NullPointerException in this scenario.
   */
  public void testWriteNotInitialised() {
    Object[] data = setupDataForWriteNotInitialised();
    try {
      testWriter.writeBatch(data);
    } catch (NullPointerException e) {
      return;
    } catch (SQLException e) {
      fail("Expected a NullPointerException");
    }
    fail("Expected a NullPointerException");
  }
  
  protected Object[] setupDataForWriteNotInitialised() {
     fail("Data and data expectations for the test 'Write Not Initialised' are not set");
    return new Object[] {};
  }



}
