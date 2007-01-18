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
package org.openadaptor.auxil.connector.mail;

import junit.framework.TestCase;

import javax.mail.MessagingException;
import java.util.List;
import java.util.ArrayList;


/**
 * Tests the MailReadConnector class.
 *
 * @see MailReadConnector
 *
 * @author Russ Fennell
 */
public class MailReadConnectorTestCase extends TestCase {

    /**
     * test the property defaults and setters as well as the property logic
     */
    public void testProperties() {
        MailReadConnector rdr = new MailReadConnector();

        // defaults
        assertFalse(rdr.processReadMessages());
        assertFalse(rdr.deleteWhenRead());
        assertFalse(rdr.createFolder());
        assertFalse(rdr.moveWhenRead());
        assertTrue(rdr.markRead());
        assertEquals(-1, rdr.getMaxPolls());


        // setters
        rdr.setProcessReadMessages(true);
        assertTrue(rdr.processReadMessages());

        rdr.setDeleteWhenRead(true);
        assertTrue(rdr.deleteWhenRead());

        rdr.setCreateFolder(true);
        assertTrue(rdr.createFolder());

        rdr.setMoveToFolder("somefolder");
        assertTrue(rdr.moveWhenRead());
        assertEquals("somefolder", rdr.getMoveToFolder());

        rdr.setMarkRead(false);
        assertFalse(rdr.markRead());

        rdr.setMaxPolls(100);
        assertEquals(100, rdr.getMaxPolls());


        // property validation
        rdr = new MailReadConnector();

        // validate with defaults should work
        List exceptions = new ArrayList();
        rdr.validate(exceptions);
        assertEquals(0, exceptions.size());

        // can't move and delete
        rdr.setDeleteWhenRead(true);
        rdr.setMoveToFolder("somefolder");
        rdr.validate(exceptions);
        assertEquals(1, exceptions.size());

        // can't move when using POP3 server as doesn't support multiple folders
        try {
            MailConnection conn = new MailConnection();
            conn.setProtocol(MailConnection.PROTOCOL_POP3);

            rdr = new MailReadConnector();
            rdr.setMoveToFolder("somefolder");
            rdr.setMailConnection(conn);

            exceptions = new ArrayList();
            rdr.validate(exceptions);
            assertEquals(1, exceptions.size());
        } catch (MessagingException e) {
            fail("Failed to detect that you are not allowed to move messages on a POP3 server");
        }

        // can't mark as read when using POP3 server. Doesn't rais an error but
        // does log a warning message
        try {
            MailConnection conn = new MailConnection();
            conn.setProtocol(MailConnection.PROTOCOL_POP3);

            rdr = new MailReadConnector();
            rdr.setMarkRead(true);
            rdr.setMailConnection(conn);

            exceptions = new ArrayList();
            rdr.validate(exceptions);
            assertEquals(0, exceptions.size());
        } catch (MessagingException e) {
            fail("Failed to detect that marking messages as READ doesn't work with POP3");
        }
    }
}
