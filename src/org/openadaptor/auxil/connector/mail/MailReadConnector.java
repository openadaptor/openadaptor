/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

import java.io.IOException;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Read connector that polls for mail messages. Essentially, we poll the named
 * folder for "new" messages. The body of the mail message is treated as the input to
 * the pipeline. Everything else is discarded (e.g Subject, to/from fields, attachments,
 * etc). Once the message is processed it can be marked read, moved or deleted so that
 * it will not be processed again.
 * <p />
 *
 * Supports both POP3 and IMAP protocols.
 * <p />
 *
 * Note: the implementation of protocol is down to the vendors of the mail server
 * and seem to vary quite a bit. This is particularly relevant to the message flags.
 * We have tested this component using MS-Exchange (IMAP) and also Java Email Server
 * (POP3).
 * <p />
 *
 * Besides deleting messages, the POP protocol does not support setting flags on messages.
 * So this means that there is no point marking messages as read after they are processed.
 * A warning will be issued if this is attempted.
 * <p />
 *
 * Given the flexibility of the properties it is very easy to get the adaptor to poll
 * a single message continuously. This is generally a bad thing and the adaptor will
 * warn you about it - but won't stop you from doing it. The following table tries to
 * highlight the effects:
 * <p />
 *
 * <pre>
 *     ProcessReadMessages     MarkRead        Effects
 *             true              true          1. All messages are to be processed
 *                                                regardless of read state so the first
 *                                                one will be continuously processed.
 *                                             2. Read messages are left read.
 *                                             3. Unread messages are marked read.
 *                                             Warning issued
 *
 *             true              false         1. All messages are to be processed
 *                                                regardless of read state so the first
 *                                                one will be continuously processed.
 *                                             2. Read messages are left read.
 *                                             3. Unread messages are left unread.
 *                                             Warning issued
 *
 *             false*            true*         1. Only unread messages are processed.
 *                                                As the MarkRead property is true, each
 *                                                unread message is processed in order.
 *                                             2. Read messages are left read.
 *                                             3. Unread messages are marked read.
 *
 *             false             false         1. Only unread messages are processed but
 *                                                they are not marked as read so the first
 *                                                unread message will be continuously
 *                                                processed.
 *                                             2. Read messages are left read.
 *                                             3. Unread messages are left unread.
 *                                             Warning issued
 *
 *      * default values
 *
 *      Note: This only applies when the <i>DeleteWhenRead</i> property is set to false.
 * </pre>
 *
 * There is one vulnerability to this connector and that is where deleted messages are
 * not expunged from the mailbox until the folder is closed. We open/close the folder
 * on each poll but there is always a chance that the close fails and thus any processed
 * messages marked for deletion will be put into a limbo state. If you start seeing
 * "Error closing inbox" messages in the logs then you will need to manually check the
 * folder and potentially clear out these messages.
 * <p />
 *
 * You can also run into trouble if you delete messages from the inbox while the adaptor
 * is running. Because we need to set flags on the message after we have processed it
 * there is a window of opportunity that the message will "disappear" after the data is
 * read and the flags are set. In this case, the adaptor will log a message and continue
 * on to the next message ignoring the deleted one. In this way we achieve a kind of a
 * transactional process.
 * <p />
 *
 * Based on the MailSource from openadaptor 1.x by Pinaki Poddar, Russ Fennell,
 * Roopinder Singh, Andrew Shire
 *
 * @see org.openadaptor.auxil.connector.smtp.SMTPConnection
 *
 * @author Russ Fennell
 */
public class MailReadConnector extends Component implements IReadConnector {
  private static final Log log = LogFactory.getLog(MailReadConnector.class);

  private MailConnection conn = null;

  private boolean originalMsgReadState;

  private Folder inbox;

  private boolean isDry = false;

  private String folder = "INBOX";

  private int numPolls = 0;

  private boolean processReadMessages = false;

  private boolean deleteWhenRead = false;

  private boolean markRead = true;

  private String moveToFolder = null;

  private boolean createFolder = false;

  private boolean msgBodyAsString = false;

  private int maxPolls = -1;

  /**
   * Stores the name of the folder on the mail server that is to be used as a source
   * of the data for the read connector. Defaults to "INBOX".
   *
   * @param folder name of the folder. May be case sensitive depending on your mail
   * server vendor.
   */
  public void setFolder(String folder) {
    this.folder = folder;
  }

  /**
   * @param processReadMessages if true then ALL messages will be processed
   * regardless of the read flag setting (default = false)
   */
  public void setProcessReadMessages(boolean processReadMessages) {
    this.processReadMessages = processReadMessages;
  }

  /**
   * @param deleteWhenRead if true then the message will be deleted once it has
   * been processed (default = false)
   */
  public void setDeleteWhenRead(boolean deleteWhenRead) {
    this.deleteWhenRead = deleteWhenRead;
  }

  /**
   * @param markRead if true then the message will be marked as read after it
   * has been processed (default = true)
   */
  public void setMarkRead(boolean markRead) {
    this.markRead = markRead;
  }

  /**
   * @param moveToFolder if supplied then the processed messages will be moved
   * to this folder.
   */
  public void setMoveToFolder(String moveToFolder) {
    this.moveToFolder = moveToFolder;
  }

  /**
   * @param createFolder if true then the "moveTo" folder will be created if
   * necessary (default = false)
   */
  public void setCreateFolder(boolean createFolder) {
    this.createFolder = createFolder;
  }

  /**
   * @param maxPolls the number of polls to execute after which the adaptor
   * will be terminated. A value of -1 indicates an infinite number of polls
   * (default = -1)
   */
  public void setMaxPolls(int maxPolls) {
    this.maxPolls = maxPolls;
  }

  /**
   * @param conn mail connection details
   *
   * @see MailConnection
   */
  public void setMailConnection(MailConnection conn) {
    this.conn = conn;
  }

  /**
   * @param msgBodyAsString if true then the body of the message will be converted
   * to a string (default = false)
   */
  public void setMsgBodyAsString(boolean msgBodyAsString) {
    this.msgBodyAsString = msgBodyAsString;
  }

  /**
   * @return true if the "moveToFolder" property has been set
   */
  public boolean moveWhenRead() {
    return moveToFolder != null;
  }

  // getters
  public boolean processReadMessages() {
    return processReadMessages;
  }

  public boolean deleteWhenRead() {
    return deleteWhenRead;
  }

  public boolean markRead() {
    return markRead;
  }

  public boolean createFolder() {
    return createFolder;
  }

  public String getMoveToFolder() {
    return moveToFolder;
  }

  public int getMaxPolls() {
    return maxPolls;
  }

  /**
   * Creates a connection to the remote mail server. Dumps out message reading
   * properties to the log.
   *
   * @throws ConnectionException if there was a problem or if the MailConnection
   * properties have not been set
   */
  public void connect() {
    log.info("Connecting to mail server");

    if (conn == null)
      throw new ConnectionException("No connection properties set", this);

    try {
      conn.connect();
    } catch (MessagingException e) {
      throw new ConnectionException("Failed to connect to [" + conn.getHost() + "]: " + e.getMessage(), e, this);
    }
  }

  /**
   * Disconnects from the mail server
   *
   * @throws ConnectionException if there was a problem
   */
  public void disconnect() {
    log.info("Disconnecting from mail server");

    try {
      if (conn != null)
        conn.disconnect();
    } catch (MessagingException e) {
      throw new ConnectionException("Failed to disconnect from [" + conn.getHost() + "]: " + e.getMessage(), e, this);
    }
  }

  /**
   * @return true if there are no more messages. Normally this is false (ie. the
   * connector will wait for messages to be received) however if the max number of
   * polls is breached then will return true.
   */
  public boolean isDry() {
    return isDry;
  }

  /**
   * Called on each poll of the adaptor. Connects to the mailbox defined and checks for
   * any new messages.
   * <p />
   *
   * In order to mimic the behaviour of the openadaptor 1.x component, we only ever process
   * one message per poll. This may need to be changed so that it returns all eligible
   * messages available.
   * <p />
   *
   * Checks to see if the max number of polls (as defined in the properties) have completed
   * and if so the adaptor is terminated.
   * <p />
   *
   * @param timeoutMs ignored
   *
   * @return  an array containing a single records corresponding to the data contained in
   * body of the first "new" message.
   *
   * @throws ConnectionException if there is a comms error when opening the mailbox or the
   * data could not be processed
   */
  public Object[] next(long timeoutMs) {
    log.info("Checking for new mail");

    Object[] data = new Object[0];

    try {
      // retrieve the next new message. If none then close the inbox and wait for next
      // poll
      Message msg = getNextMessage();
      if (msg != null) {
        // store whether the message has been read or not so that we can reset it if
        // needs be
        originalMsgReadState = conn.isReadFlagSet(msg);
        data = new Object[] { processMailMessage(msg) };
      }
    } catch (Exception e) {
      throw new ConnectionException("Failed to process message: " + e.getMessage(), e, this);
    } finally {
      // we have to close the inbox after each message is processed so that the message
      // can be moved/deleted. This could be expensive so it might be a good idea to add
      // a flag to allow the user to control this behaviour with an option to only close
      // the folder when the adaptor is stopped.
      // Also, if we get an error when processing the message then we need to close the
      // inbox so that previously deleted messages are expunged
      closeInbox();
    }

    // check to see if we have any limit to the number of polls to execute and if so then
    // checked to see if that limit has been breached.
    if (maxPolls > 0 && ++numPolls == maxPolls) {
      log.warn("MaxPolls [" + maxPolls + "] reached");
      isDry = true;
    }

    return data;
  }

  /**
   * Dumps properties to log and performs some sanity checks on them. Any exceptions
   * encountered are added to the supplied list.
   */
  public void validate(List exceptions) {
    log.debug("Message Actions:");
    log.debug("  Mark Read:             " + markRead);
    log.debug("  Process Read Messages: " + processReadMessages);
    log.debug("  Delete When Read:      " + deleteWhenRead);
    log.debug("  Move When Read:        " + moveWhenRead());
    if (moveWhenRead()) {
      log.debug("  Move to folder:        " + moveToFolder);
      log.debug("  Create Folder:         " + createFolder);
    }

    // do some sanity checks and warn of any potential loops
    if (deleteWhenRead) {
      // hmmm ... move and delete. Which one do you really want to do?
      if (moveWhenRead())
        exceptions.add(new ValidationException(
            "Both [DeleteWhenRead] AND [MoveWhenRead] flags have been set. Which one do you want to do?", this));

      // no problem with looping but MarkRead means nothing
      if (markRead)
        log.warn("[DeleteWhenRead] is set so will ignore [MarkRead]");
    } else {
      // we're not deleting messages and if we don't move them then there is a
      // chance that the adaptor will loop continuously processing the first
      // message
      if (!moveWhenRead()) {
        // we are processing both read and unread messages which will loop
        if (processReadMessages)
          log
              .warn("The adaptor will loop continuously processing the first "
                  + maxPolls
                  + " message(s) it encounters"
                  + "\n - either use the [DeleteWhenRead] property or set [ProcessReadMessages] to false and [MarkRead] to true");

        // even if we are not processing read messages then there is still a
        // chance to loop!
        else if (!markRead)
          log.warn("The adaptor will loop continuously if processing unread messages"
              + "\n - either use the [DeleteWhenRead] property or set [MarkRead] to true");
      }
    }

    // POP3 doesn't have multiple folders so we can't move messages after they've
    // been processed. Also it doesn't support setting flags (other than delete)
    // so warn users if they try
    if (conn != null && "pop3".equalsIgnoreCase(conn.getProtocol())) {
      if (moveWhenRead())
        exceptions.add(new ValidationException(
            "POP3 does not support multiple folders so the [MoveToFolder] option is confusing", this));

      if (markRead)
        log.warn("POP3 does not support setting message flags so setting [MarkRead] will have no effect"
            + "\n - use the [DeleteWhenRead] property instead");
    }
  }

  /**
   * @return null
   */
  public Object getReaderContext() {
    return null;
  }
  
  public void setReaderContext(Object context) {
  }

  /**
   * Retrieves any "new" (ie. any eligible to be processed) messages from the InBox. We
   * only process the first message in the folder. The rest will be picked up on
   * subsequent polls. If required we also try to set the message status as READ/SEEN.
   * <p />
   *
   * If the message is deleted while we are processing it then we just log the problem
   * and return null.
   *
   * @return the first mail message to precess or null if none present.
   *
   * @throws MessagingException if the inbox can not be opened, if there was a comms
   * error or if we failed to retrieve the message details.
   */
  private Message getNextMessage() throws MessagingException {
    if (inbox == null || !inbox.isOpen())
      openInbox();

    Message[] messages = inbox.getMessages();
    log.debug(messages.length + " msssage(s) found");

    if (messages.length == 0)
      return null;

    int index = 0;
    try {
      while (index < messages.length) {
        // we only process the first unread message in the folder. The rest will be
        // picked up on subsequent polls
        Message msg = messages[index++];

        if (isToBeProcessed(msg)) {
          log.info("Processing message [" + msg.getSubject() + "] from " + msg.getFrom()[0]);
          return msg;
        }
      }
    } catch (NullPointerException e) {
      // ok, so this is a little dodgy as different version of the JavaMail API might
      // exhibit different behaviour but works for 1.3 and 1.4
      log.warn("Message [" + index + "] deleted by external party. "
          + "Will skip this iteration and continue on the next poll.");
    } catch (Exception e) {
      log.warn("Failed to retrieve message [" + index + "]: " + e.getMessage() + ". "
          + "Will skip this iteration and continue on the next poll.");
    }

    return null;
  }

  /**
   * Reads the body of the main message. Will try to delete the message if required
   * <p />
   *
   * In the case where MarkRead=false, we actually have to mark it as being unread as
   * most servers will automatically mark it as read when we obtain the data.
   * <p />
   *
   * If the msgBodyAsString flag is set then we try to convert the message body into
   * a string. This will not always work so it is far better to make sure that your
   * message contents are strings in the first place!
   *
   * @param msg a mail message that contains the data
   *
   * @return  the body of the message
   *
   * @throws MessagingException
   * @throws IOException if the message body could not be retrieved
   */
  private Object processMailMessage(Message msg) throws MessagingException, IOException {
    // process the message if it is a valid one
    if (!isToBeProcessed(msg))
      return null;

    Object body = conn.getBody(msg);

    if (!(body instanceof String) && msgBodyAsString)
      body = body.toString();

    // mark the message as read or not. In the case where we are not marking the
    // message as being read, we actually have to mark it as being unread as most
    // servers will automatically mark it as read when we obtained the data :-)
    if (markRead) {
      conn.markMsgRead(msg);
      log.info("Message marked as READ");
    } else {
      conn.resetMsgReadFlag(msg, originalMsgReadState);
    }

    // delete the message if necessary
    if (deleteWhenRead) {
      conn.deleteMsg(msg);
      log.info("Message Deleted");
    }

    // move the message if required - the MailProperties class will take care
    // of conflicts between move and delete :-)
    if (moveWhenRead()) {
      conn.copyMsgToFolder(msg, moveToFolder, createFolder);
      conn.deleteMsg(msg);
      log.info("Message moved to [" + moveToFolder + "]");
    }

    return body;
  }

  /**
   * @return true if the supplied message is to be processed - ie. if it has
   * not been expunged, marked for deletion or is null. Also takes into account
   * the [ProcessReadMessages] property which, if set to false, means that read
   * messages should not be processed.
   *
   * @throws MessagingException if there was a comms error
   */
  protected boolean isToBeProcessed(Message msg) throws MessagingException {
    // Note: isExpunged() is the only allowable call on an expunged message
    // or we get an exception so we need to check this first before checking
    // if it has been read.
    if (msg == null || msg.isExpunged() || msg.isSet(Flags.Flag.DELETED))
      return false;

    if (!processReadMessages && msg.isSet(Flags.Flag.SEEN))
      return false;

    return true;
  }

  /**
   * Opens the inbox as defined by the Folder property
   *
   * @throws ConnectionException if there was a problem which is just a wrapper around
   * the underlying MessagingException
   */
  private void openInbox() {
    try {
      inbox = conn.openFolder(folder, createFolder);
    } catch (MessagingException e) {
      throw new ConnectionException("Failed to open the inbox:" + e.getMessage(), e, this);
    }
  }

  /**
   * Closes the inbox. Will expunge messages if either the [deleteWhenRead] or
   * [moveToFolder] properties are set.
   *
   * @throws ConnectionException if there was a problem which is just a wrapper around
   * the underlying MessagingException
   */
  private void closeInbox() {
    try {
      boolean expunge = (deleteWhenRead || moveWhenRead());
      conn.closeFolder(inbox, expunge);
    } catch (MessagingException e) {
      throw new ConnectionException("Failed to close the inbox:" + e.getMessage(), e, this);
    }
  }
}
