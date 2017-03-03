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

package org.openadaptor.auxil.connector.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;


/**
 * Simple helper class to read in the properties specific to creating a connection
 * to a remote mail server (either via POP3 or IMAP protocols) and provides some
 * utilities to manipulate folders and mail messages.
 * <p />
 *
 * Note: The POP3 specification does not support multiple folders so if you use the
 * MoveToFolder property you will get an error and the adaptor will exit. The move
 * function only works with IMAP servers.
 *
 * @author Russ Fennell
 */
public class MailConnection {
    private static final Log log = LogFactory.getLog(MailConnection.class);

    public static final String PROTOCOL_POP3    = "pop3";
    public static final String PROTOCOL_IMAP    = "imap";

    private String host                 = null;
    private String port                 = "110";
    private String user                 = null;
    private String password             = null;
    private String protocol             = PROTOCOL_POP3;

    private Store store;


    /**
     * @param host the mail server name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param port the TCP/IP port to use (default = 110)
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @param user username to authenticate with
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @param password password to authenticate with
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param protocol either pop3/imap (default = pop3)
     */
    public void setProtocol(String protocol) throws MessagingException {
        if ( PROTOCOL_POP3.equalsIgnoreCase(protocol) || PROTOCOL_IMAP.equalsIgnoreCase(protocol) )
            this.protocol = protocol;
        else
            throw new MessagingException("Unknown protocol [" + protocol + "]. " +
                    "Use either " + PROTOCOL_POP3 + " or " + PROTOCOL_IMAP);
    }


    // getters
    public String getHost()             { return host; }
    public String getUser()             { return user; }
    public String getPassword()         { return password; }
    public String getProtocol()         { return protocol; }
    public String getPort()             { return port; }
    public boolean isConnected()        { return (store != null && store.isConnected()); }


    /**
     * Creates a connection to the remote server
     *
     * @throws MessagingException if there is a comms error
     */
    public void connect() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getDefaultInstance(props, null);
        store = session.getStore(protocol);
        store.connect(host, user, password);

        log.debug("Connected to:     " + host);
        log.debug("  Port:           " + port);
        log.debug("  Username:       " + user);
        log.debug("  Protocol:       " + protocol);
        log.debug("  Default folder: " + store.getDefaultFolder());
        log.debug("  URL name:       " + store.getURLName());
    }


    /**
     * Closes the connection to the mail server
     *
     * @throws MessagingException if there was a problem
     */
    public void disconnect() throws MessagingException {
        if ( isConnected() )
            store.close();

        log.debug("Disconnected from mail server");
    }


    /**
     * Opens the named folder in READ/WRITE mode
     *
     * @throws MessagingException if there is a comms error or if the folder could not
     * be found or we failed to create it
     */
    public Folder openFolder(String fldr, boolean create) throws MessagingException {
        if ( store == null || ! store.isConnected() )
            connect();

        Folder f = store.getFolder(fldr);
        log.debug("Folder [" + fldr + "] exists? " + f.exists());

        // Note that a Folder object is returned even if the named folder does not
        // physically exist on the Store so we have to test for it explicitly.
        if ( ! f.exists() ) {
            // we've not been asked to create the folder so this is an error
            if ( ! create )
                throw new MessagingException("Error opening folder [" + fldr + "]: Folder not found");

            // try to create the folder
            if ( ! f.create(Folder.HOLDS_MESSAGES) )
                throw new MessagingException("Failed to create folder [" + fldr + "]");

            log.info("Created folder [" + fldr + "]");
        }

        f.open(Folder.READ_WRITE);
        log.debug("Folder [" + fldr + "] opened");

        return f;
    }


    /**
     * Attempts to close the supplied folder. If expunge is true then all messages
     * marked for deletion will be expunged from the folder.
     *
     * @throws MessagingException if there was a problem
     */
    public void closeFolder(Folder fldr, boolean expunge) throws MessagingException {
        if ( fldr == null || ! fldr.isOpen() )
            return;

        fldr.close(expunge);
        log.debug("Folder [" + fldr.getName() + "] closed. " +
                "Deleted messages will " + (expunge ? "not " : "") + "be expunged");
    }


    /**
     * Marks the supplied message as being read.
     *
     * @throws MessagingException if there was a problem
     */
    public void markMsgRead(Message msg) throws MessagingException {
        msg.setFlag( Flags.Flag.SEEN, true );
        log.debug("Message marked as read");
    }


    /**
     * @return true if the read flag has been set on the supplied message
     *
     * @throws MessagingException if there was a problem
     */
    public boolean isReadFlagSet(Message msg) throws MessagingException {
        return msg.isSet(Flags.Flag.SEEN);
    }


    /**
     * Resets the read flag for the supplied message.
     *
     * @throws MessagingException if there was a problem
     */
    public void resetMsgReadFlag(Message msg, boolean origState) throws MessagingException {
        msg.setFlag(Flags.Flag.SEEN, origState);
        log.debug("Message reset to " + (origState ? "" : "un") + "read");
    }


    /**
     * Marks the supplied message for deletion. The message will actually be
     * deleted when the folder it is in is closed.
     *
     * @throws MessagingException if there was a problem
     */
    public void deleteMsg(Message msg) throws MessagingException {
        msg.setFlag(Flags.Flag.DELETED, true);
        log.debug("Message marked for deletion from [" + msg.getFolder() + "]");
    }


    /**
     * Copies the supplied message to the destination folder as defined in the
     * properties file
     *
     * @throws MessagingException if the destination folder cannot be found
     */
    public void copyMsgToFolder(Message msg, String folder, boolean create) throws MessagingException {
        Folder dest = openFolder(folder, create);
        Folder source = msg.getFolder();

        source.copyMessages(new Message[]{msg}, dest);
        log.debug("Copied message to [" + dest.getName() + "]");

        // as we are moving the message to another folder, we can assume that we
        // do not want to delete it so we don't need to expunge the folder
        closeFolder(dest, false);
    }


    /**
     * @return the body from the supplied message or null if there was a problem
     *
     * @throws MessagingException if there was a problem reading the message status
     * @throws IOException if the body content could not be retrieved
     */
    public Object getBody(Message msg) throws MessagingException, IOException {
        if ( msg != null && !msg.isExpunged() && !msg.isSet(Flags.Flag.DELETED ) )
            return msg.getContent();

        return null;
    }

}
