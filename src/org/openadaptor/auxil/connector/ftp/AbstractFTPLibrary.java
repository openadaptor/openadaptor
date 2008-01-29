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

package org.openadaptor.auxil.connector.ftp;

import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ConnectionException;

/**
 * Abstract class that will set the connection properties necessary to
 * create an FTP session with a remote server
 * 
 * @author Russ Fennell
 */
public abstract class AbstractFTPLibrary extends Component implements IFTPConnection {

  protected String hostName;

  protected int port = 21;

  protected String userName = "anonymous";

  protected String password = "openadaptor";

  protected boolean binaryTransfer = true;

  public void setHostName(String s) {
    this.hostName = s;
  }

  public void setPort(int i) {
    this.port = i;
  }

  public void setUserName(String s) {
    this.userName = s;
  }

  public void setPassword(String s) {
    this.password = s;
  }

  /**
   * If true then the files will be transferred via BINARY mode. Defaults to true
   */
  public void setBinaryTransfer(boolean b) {
    this.binaryTransfer = b;
  }

  public String getHostName() {
    return hostName;
  }

  public int getPort() {
    return port;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  /**
   * @return true if the files will be transferred via BINARY mode
   */
  public boolean isBinaryTransfer() {
    return binaryTransfer;
  }

  /**
   * checks to see if the FTP client is connected and logged in. If not then
   * disconnects the client to free up the resources and throws an
   * ConnectionException
   */
  protected void checkLoggedIn() throws ConnectionException {
    if (!isLoggedIn()) {
      close();
      throw new ConnectionException("The client is NOT logged into a remote server", this);
    }
  }
}
