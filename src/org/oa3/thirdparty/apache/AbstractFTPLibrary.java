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
package org.oa3.thirdparty.apache;

/*
 * File: $Header$ Rev: $Revision$
 */

import org.oa3.auxil.connector.ftp.IFTPLibrary;
import org.oa3.core.exception.ComponentException;
import org.oa3.core.Component;


/**
 * Simple abstract class that will set the connection properties necessary to create an FTP session with a remote server
 * 
 * @author Russ Fennell
 */
public abstract class AbstractFTPLibrary extends Component implements IFTPLibrary {

  protected String hostName;

  protected int port = 21;

  protected String userName;

  protected String password;

  protected boolean changeDir = true;

  protected boolean binaryTransfer = true;

  protected String textEncoding = null;

  // setters
  /**
   * Sets the name of the remote server
   */
  public void setHostName(String s) {
    this.hostName = s;
  }

  /**
   * Sets the communications port to use. Defaults to 21
   */
  public void setPort(int i) {
    this.port = i;
  }

  /**
   * Sets the user name to use to authenticate
   */
  public void setUserName(String s) {
    this.userName = s;
  }

  /**
   * Sets the password to use to authenticate
   */
  public void setPassword(String s) {
    this.password = s;
  }

  /**
   * If false then the listener will NOT try to change directory regardless of the SourceDir property. Defaults to true
   */
  public void setChangeDir(boolean b) {
    this.changeDir = b;
  }

  /**
   * If true then the files will be transferred via BINARY mode. Defaults to true
   */
  public void setBinaryTransfer(boolean b) {
    this.binaryTransfer = b;
  }

  /**
   * Sets the file encoding to use when reading from the input stream
   */
  public void setTextEncoding(String s) {
    this.textEncoding = s;
  }

  /**
   * @return The name of the remote server
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * @return The communications port to use
   */
  public int getPort() {
    return port;
  }

  /**
   * @return The user name to use to authenticate
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @return The password to use to authenticate
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return false if the listener will NOT try to change directory regardless of the SourceDir property
   */
  public boolean isChangeDir() {
    return changeDir;
  }

  /**
   * @return true if the files will be transferred via BINARY mode
   */
  public boolean isBinaryTransfer() {
    return binaryTransfer;
  }

  /**
   * @return the file encoding to use when reading from the input stream
   */
  public String getTextEncoding() {
    return textEncoding;
  }

  /**
   * checks to see if the FTP client is connected and logged in. If not then disconnects the client to free up the
   * resources and throws an ComponentException
   */
  protected void checkLoggedIn() throws ComponentException {
    if (!isLoggedIn()) {
      close();
      throw new ComponentException("The client is NOT logged into a remote server", this);
    }
  }
}
