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
package org.oa3.auxil.processor.simplerecord;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/processor/modify/AttributeRemoveProcessor.java,v 1.6 2006/10/20 12:27:14 higginse
 * Exp $ Rev: $Revision: 1.6 $ Created Oct 03, 2006 by Eddy Higgins
 */
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.simplerecord.ISimpleRecord;
import org.oa3.core.exception.RecordException;

/**
 * Processor which removes an attribute from an <code>ISimpleRecord</code>.
 * <p>
 * It will use either the attribute name, or an expression which is expected to evaluate to an attribute name, to
 * determine which attribute to remove.
 * <p>
 * Note: Exactly one of attributeName and expression must be set.
 * 
 * @author Eddy Higgins.
 */
public class AttributeRemoveProcessor extends AttributeModifyProcessor {

  public static final Log log = LogFactory.getLog(AttributeRemoveProcessor.class);

  // BEGIN Bean getters/setters

  // END Bean getters/setters

  /**
   * This will remove a named attribute, or an attribute whose name is derived from an evaluated expression, from the
   * supplied <code>ISimpleRecord</code>.
   * <p>
   * Note that it is illegal to specify both an attribute name, and an expression.
   * 
   * @param simpleRecord
   *          The record from which the attribute should be removed.
   * @return the record, having removed the attribute.
   * @throws org.oa3.processor.RecordException
   *           if the attribute cannot be removed.
   */
  // ToDo: Decide behaviour if attribute didn't exist)
  public ISimpleRecord modifySimpleRecord(ISimpleRecord simpleRecord) throws RecordException {
    // Validate should already have checked that either attributeName or expression is set.
    simpleRecord.remove(attributeName != null ? attributeName : expression.evaluate(simpleRecord));
    return simpleRecord;
  }

  public void validate(List exceptions) {
    // /Make sure either attributeName or expression are set.
    Exception e = checkExactlyOneOfProperty(new String[] { attributeName, "expression" }, new boolean[] {
        attributeName != null, expression != null });
    if (e != null) {
      exceptions.add(e);
    }
  }

  /**
   * Provide a <code>String</code> represention of the attribute removal modification.
   * 
   * @return <code>String</code> represention of the attribute removal modification.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("remove (");
    sb.append(attributeName != null ? attributeName : expression.toString());
    return sb.append(")").toString();
  }
}
