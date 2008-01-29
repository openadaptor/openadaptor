/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.processor.simplerecord;

import java.util.List;

import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;

/**
 * Processor which sets an attribute value to the result of an evaluated Expression.
 * 
 * @author Eddy Higgins
 */
public class AttributeSetProcessor extends AttributeModifyProcessor {

  // private static final Log log = LogFactory.getLog(AttributeSetProcessor.class);

  // BEGIN Bean getters/setters

  // END Bean getters/setters

  /**
   * This will evaluate an expression, and put the result in the configured attribute.
   * <p>
   * If the attribute already exists, it is replaced. If not this modifier attempts to create it.
   * 
   * @param simpleRecord
   *          record to be modified.
   * @return Modified ISimpleRecord record after modification
   * @throws RecordException
   *           if the modification fails.
   */
  public ISimpleRecord modifySimpleRecord(ISimpleRecord simpleRecord) throws RecordException {
    // Validate should ensure that both attributeName and expression are non-null
    simpleRecord.put(attributeName, expression == null ? null : expression.evaluate(simpleRecord));
    return simpleRecord;
  }

  public void validate(List exceptions) {
    super.validate(exceptions);

    Exception e = checkMandatoryProperty(attributeName, attributeName != null);
    if (e != null) {
      exceptions.add(e);
    }
    Exception e2 = checkMandatoryProperty("expression", expression != null);
    if (e2 != null) {
      exceptions.add(e2);
    }
  }

  /**
   * Provide a <code>String</code> represention of the attribute value assignment.
   * 
   * @return <code>String</code> represention of the attribute value assignment.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(attributeName).append(" = ");
    sb.append(expression == null ? "<null>" : expression.toString());
    return sb.toString();
  }
}
