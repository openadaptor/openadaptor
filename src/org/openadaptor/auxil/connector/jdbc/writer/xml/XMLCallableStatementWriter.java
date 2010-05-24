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

package org.openadaptor.auxil.connector.jdbc.writer.xml;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.exception.ValidationException;

/**
 * Writer which will call a stored procedure to write records to a 
 * database.
 * <p>
 * Note that if outputColumns is not supplied, the order of the arguments
 * in the stored procedure will be inferred from the order of the elements
 * under the root node of the source XML document.
 *
 * @author cawthorng
 */
public class XMLCallableStatementWriter extends AbstractXMLWriter {

  private static final Log log = LogFactory.getLog(XMLCallableStatementWriter.class);

  private String procName;

  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, IComponent comp) {
    if (StringUtils.isBlank(procName)) {
      exceptions.add(new ValidationException("The [procName] property must be supplied", comp));
    }
    
    if (outputColumns == null) {
      log.warn("outputColumns undefined - records *MUST* be IOrderedMap instances");
    }
  }

  /**
   * This creates a reusable Prepared Statement for calls to the configured stored proc.
   */
  protected void initialiseReusablePreparedStatement() {
    initialiseReusableStoredProcStatement(procName);
  }

  /**
   * Sets the stored procedure name that will be used to write the XML to
   * the database.
   * Generally may also be prefixed with a schema name (e.g. myschema.myproc) but
   * this behaviour is db vendor dependent.
   * Note also that Oracle's package.proc syntax will not work - use packageName property
   * instead to specify a package name.
   * @param procName the String name of the stored procedure to call
   */
  public void setCallableStatement(String procName) {
    this.procName = procName;
  }
  
  /**
   * Optional argument to specify a package name for a CallableStatement.
   * Optional, and only makes sense for Oracle databases.
   * 
   * @param oraclePackageName - String containing the name of package to which
   *                            the stored procedure belongs.
   */
  public void setPackageName(String oraclePackageName) {
    this.oraclePackage=oraclePackageName;
  }

}
