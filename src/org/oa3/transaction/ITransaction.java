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
package org.oa3.transaction;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/transaction/ITransaction.java,v 1.8 2006/08/11 15:13:24 kscully Exp $
 * Rev:  $Revision: 1.8 $
 * Created Jun 10, 2005 by Kevin Scully
 */

/**
 * Interface which describes a simple oa3 Transaction. The register/deregister methods require
 * a suitable Transaction Resource. This MAY vary depending on the underlying transaction mechanism
 * used. (E.g. for JTA this would be an XAResource)
 */
public interface ITransaction {
    /**
     * Start a transaction
     */
    public void begin();

    /**
     * Do whatever is needed to commit the open Transaction(s).
     */
    public void commit();

    /**
     * Rollback all open transactions (if possible)
     */
    public void rollback();

    public void enlist(Object resource);

    public void delistForCommit(Object resource);

    public void delistForRollback(Object resource);

    /** Use to mark a transaction so that rollback is the only valid operation that can be performed. */
    public void setRollbackOnly();

}
