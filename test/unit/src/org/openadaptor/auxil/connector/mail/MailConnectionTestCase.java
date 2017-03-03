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


/**
 * Runs some simple tests to verify the MailConnection class.
 *
 * @see MailConnection
 *
 * @author Russ Fennell
 */
public class MailConnectionTestCase extends TestCase {

    private static final String HOST        = "localhost";
    private static final String PORT        = "110";
    private static final String USER        = "user";
    private static final String PASSWORD    = "pass";
    private static final String PROTOCOL    = "pop3";


    /**
     * test the property defaults and setters
     */
    public void testProperties() {
        MailConnection conn = new MailConnection();

        // defaults
        assertEquals(MailConnection.PROTOCOL_POP3, conn.getProtocol());
        assertEquals(PORT, conn.getPort());


        // setters
        conn.setHost(HOST);
        assertEquals(HOST, conn.getHost());

        conn.setUser(USER);
        assertEquals(USER, conn.getUser());

        conn.setPort(PORT);
        assertEquals(PORT, conn.getPort());

        conn.setPassword(PASSWORD);
        assertEquals(PASSWORD, conn.getPassword());

        try {
            conn.setProtocol(PROTOCOL);
            assertEquals(PROTOCOL, conn.getProtocol());
        } catch (MessagingException e) {
            fail("Failed to set protocol");
        }

        // unsupported protocol
        try {
            conn.setProtocol("foo");
            fail("Failed to detect unsupported protocol");
        } catch (MessagingException e) {
            assertTrue(true);
        }

    }
}
