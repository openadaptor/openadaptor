/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;

import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * This component will provide basic File Transfer Protocol (FTP) connunication to allow the adaptor to GET a file from
 * or PUT a file onto a remote machine. <p/>
 * 
 * The methods return FTP input/output streams to the caller which can then be used to either read a file from or write
 * a file to the remote server. <p/>
 * 
 * Uses the standard sun.net.ftp.FtpClient and sun.net.TelnetInputStream classes to perform the actual file transfer.
 * <p/>
 * 
 * NB: the Sun libraries are subject to change between JDK releases. A case in point is if you specify a wildcard in the
 * filename. Under JDK 1.4 the source will retrieve each file that matches the file pattern on subsequent polls pretty
 * much as you would expect. However, if you use JDK 1.3 then it will only retrieve the first file that matches and then
 * empty files for the rest.
 * 
 * @author Russ Fennell
 */
public class SunFTPConnection extends AbstractFTPLibrary {
  
  private static final Log log = LogFactory.getLog(SunFTPConnection.class);

  // SunFTP client object that will perform the actual file transfer
  private FtpClient _ftpClient;

  // flag to indicate that the FTPClient has successfully connected to the remote server
  private boolean _connected = false;

  // flag to indicate that the FTPClient has successfully logged into the remote server
  private boolean _loggedIn = false;

  /**
   * Takes the supplied hostname and port of the target machine and attempts to connect to it. If successful the
   * isConnected() flag is set.
   * 
   * @throws ComponentException
   *           if we failed to create the client
   */
  public void connect() {
    log.debug("Connecting to " + hostName + " on port " + port);

    try {
      _ftpClient = new FtpClient(hostName, port);
      _connected = true;
    } catch (IOException e) {
      throw new ConnectionException("Failed to create SunFTP Client: " + e.getMessage(), this);
    }

    log.debug("Connected to " + hostName);
  }

  /**
   * @return boolean to indicate if the FTPClient object has successfully connected to the remote server
   */
  public boolean isConnected() {
    return _connected;
  }

  /**
   * @return boolean to indicate if the FTPClient object has successfully logged into the remote server
   */
  public boolean isLoggedIn() {
    return _loggedIn;
  }

  /**
   * Attempt to log into the remote server using the supplied credentials. This method checks to make saure that the
   * client has been successfully connected to the remote server before attempting to log in.
   * 
   * @throws ComponentException
   *           if we failed to log into the remote serevr or if we fail to set the transfer mode
   */
  public void logon() {
    log.debug("Logging in " + userName);

    try {
      _ftpClient.login(userName, password);

      _loggedIn = true;
      log.debug(userName + " logged in");
    } catch (IOException e) {
      close();
      throw new ConnectionException("Failed to login to SunFTP Server: " + e.getMessage(), this);
    }

    // set the file transfer mode
    try {
      if (binaryTransfer) {
        _ftpClient.binary();
        log.debug("Binary transfer mode set");
      } else {
        _ftpClient.ascii();
        log.debug("ASCII transfer mode set");
      }
    } catch (IOException e) {
      close();
      throw new ConnectionException("Failed to set " + (binaryTransfer ? "binary" : "ascii") + " transfer mode: "
          + e.getMessage(), this);
    }
  }

  /**
   * This method creates a FTP input stream in the shape of an InputStreamReader that the caller can use to perform the
   * GET function. The file is retrieved from the current workign directory. Assumes that the FTP client has already
   * been successfully connected to and authenticated. Although, you really shouldn't be able to get this far without
   * this being the case anyway!
   * 
   * @param fileName -
   *          The name of the file to retrieve
   * 
   * @return An InputStreamReader object containing the file contents
   * 
   * @throws ComponentException
   *           if we cannot open the transfer stream with the remote server
   */
  public InputStream get(String fileName) {
    InputStream in;

    log.debug("get() called: directory=" + getCurrentWorkingDirectory() + "; file=" + fileName);

    checkLoggedIn();

    try {
      in = _ftpClient.get(fileName);
      if (in == null) {
        log.warn("Empty file retrieved");
        return null;
      }
      log.debug("SunFTP input transfer stream created for " + fileName);
    } catch (IOException e) {
      close();
      throw new ConnectionException("Cannot open SunFTP stream:" + e.getMessage(), this);
    }

    return in;
  }

  /**
   * This method creates a SunFTP output stream in the shape of an OutputStreamWriter that the caller can use to perform
   * the PUT function. Assumes that the SunFTP client has already been successfully connected to and authenticated.
   * Although, you really shouldn't be able to get this far without this being the case anyway! <p/>
   * 
   * If the createDirectory flag is set then the upload directory will be created if it is not present on the remote
   * server
   * 
   * @param fileName -
   *          The name of the file to transfer
   * 
   * @return OutputStreamWriter that the caller can use to write the file
   * 
   * @throws ComponentException -
   *           if the client is not conected and logged into the remote server or the SunFTP output stream cannot be
   *           created (eg. does not have permission)
   */
  public OutputStream put(String fileName) {
    OutputStream out;

    log.debug("put() called: directory=" + getCurrentWorkingDirectory() + "; file=" + fileName);

    checkLoggedIn();

    try {
      out = _ftpClient.put(fileName);
      if (out == null) {
        log.warn("Error creating file");
        return null;
      }
      log.debug("SunFTP output transfer stream created for " + fileName);
    } catch (IOException e) {
      close();
      throw new ConnectionException("Cannot open SunFTP stream:" + e.getMessage(), this);
    }

    return out;
  }

  /**
   * Transfers a file to the remote server but appends the output stream the remote file rather than overwriting it.
   * <p/>
   * 
   * If the createDirectory flag is set then the upload directory will be created if it is not present on the remote
   * server
   * 
   * @param fileName -
   *          The name of the file to transfer
   * 
   * @return OutputStreamWriter that the caller can use to write the file
   * 
   * @throws ComponentException -
   *           if the client is not conected and logged into the remote server or the SunFTP output stream cannot be
   *           created (eg. does not have permission)
   */
  public OutputStream append(String fileName) {
    OutputStream _out;

    log.debug("append() called: directory=" + getCurrentWorkingDirectory() + "; file=" + fileName);

    checkLoggedIn();

    try {
      _out = _ftpClient.append(fileName);
      log.debug("SunFTP output transfer stream created for " + fileName);
    } catch (IOException e) {
      close();
      throw new ConnectionException("Cannot open SunFTP stream:" + e.getMessage(), this);
    }

    return _out;
  }

  /**
   * Close the connection to the remote server
   * 
   * @throws ComponentException
   *           if we failed to close to connection
   */
  public void close() throws ComponentException {
    try {
      _ftpClient.closeServer();
      _connected = false;
      log.debug("SunFTP connection closed");
    } catch (IOException e) {
      throw new ConnectionException("Failed to close SunFTP Server " + e.toString(), this);
    }
  }

  /**
   * Check for directory on remote server. Assumes that the client is already connected and logged into the remote
   * server
   * 
   * @param dirName -
   *          the directory to check for
   * 
   * @return boolean to indicate the presence of the directory
   */
  public boolean directoryExists(String dirName) {
    boolean _isPresent = false;

    checkLoggedIn();

    // attempt to change to the supplied directory. Throws an exception if the
    // directory does not exist
    try {
      _ftpClient.cd(dirName);
      _isPresent = true;
    } catch (Exception e) {
      // do nothing as this means that the directory does not exist and the _isPresent
      // flag is left initialised to false
    }

    return _isPresent;
  }

  /**
   * Deletes supplied file from the remote server. After the file is deleted we read the response from the listener
   * 
   * @param fileName -
   *          the file to delete
   * 
   * @throws ComponentException -
   *           if the client is not logged into the remote server or there was a problem with the deletion
   */
  public void delete(String fileName) {
    checkLoggedIn();

    // attempt to delete the supplied file from the remote server
    try {
      _ftpClient.sendServer("del " + fileName + "\r\n");

      // hmmm ... if we don't read the response then the listener bugs out
      // after retrieving and deleting the first file and then trying to
      // get the second file in the list. Don't know why this fixes it.
      _ftpClient.readServerResponse();
    } catch (Exception e) {
      close();
      throw new ConnectionException("Failed to delete " + fileName + ": " + e.getMessage(), this);
    }
  }

  /**
   * Retrieves a list of file names on the remote server that match the supplied pattern. We use the nameList() call on
   * the FTP client which returns a string of the format: <p/>
   * 
   * <blockquote>
   * 
   * <pre>
   *       -rw-r--r--   1 restrict restrict      3787 Nov 10 06:07 Georgina.html
   * </pre>
   * 
   * </blockquote>
   * 
   * We strip out all the dross up to the filename. This will fail if the output from the list call changes!
   * 
   * @param filePattern -
   *          the pattern the match file names against
   * 
   * @return array of the file names or null if none found
   * 
   * @throws ComponentException -
   *           if there was an communications error
   */
  public String[] fileList(String filePattern) {
    Vector v = new Vector();

    checkLoggedIn();

    try {
      TelnetInputStream in = _ftpClient.nameList(filePattern);

      BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
      String fileName;

      while ((fileName = rdr.readLine()) != null) {
        // list returns file details in the following format:
        // -rw-r--r-- 1 restrict restrict 3787 Nov 10 06:07 Georgina.html
        // we have to strip out the extra details and leave the filename
        fileName = fileName.substring(fileName.lastIndexOf(" ") + 1);
        v.add(fileName);
      }
    } catch (Exception e) {
      close();
      throw new ConnectionException("Error retrieving file list: " + e.getMessage(), this);
    }

    // no files - well return null then
    if (v.size() == 0)
      return null;

    // take the list of file names and convert them into an array
    String[] s = new String[v.size()];
    v.copyInto(s);

    return s;
  }

  /**
   * Changes the current working directory to that suppplied. Checks to see if the directory exists first
   * 
   * @param directoryName -
   *          the new directory
   * 
   * @throws ComponentException
   *           if we fail to change directory or it does not exist
   */
  public void cd(String directoryName) {
    try {
      // check that the directory exists
      if (!directoryExists(directoryName)) {
        close();
        throw new ConnectionException("The directory (" + directoryName + ") does NOT exist", this);
      }

      // set up the working directory
      _ftpClient.cd(directoryName);
      log.debug("Working directory set to: " + directoryName);

    } catch (IOException e) {
      close();
      throw new ConnectionException("Failed to change direcotries to [" + directoryName + "]: " + e.getMessage(), this);
    }

  }

  /**
   * @return the current working directory or null if there was an error
   */
  public String getCurrentWorkingDirectory() {
    try {
      return _ftpClient.pwd();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * @return True. There is no way to check that all is ok so we return true anyway!!
   */
  public boolean verifyFileTransfer() {
    return true;
  }
}
