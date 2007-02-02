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

/*
 * File: $Header$ Rev: $Revision$
 */

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.openadaptor.core.exception.ComponentException;

/**
 * Defines the actions a FTP library must be able to perform
 * 
 * @author Russ Fennnell
 */
public interface IFTPLibrary {

  /**
   * Takes the supplied hostname and port of the target machine and attempts to connect to it. If successful the
   * isConnected() flag is set.
   * 
   * @throws ComponentException
   */
  public void connect();

  /**
   * @return boolean to indicate if the FTPClient object has successfully connected to the remote server
   */
  public boolean isConnected();

  /**
   * @return boolean to indicate if the FTPClient object has successfully logged into the remote server
   */
  public boolean isLoggedIn();

  /**
   * Attempt to log into the remote server using the supplied credentials. This method checks to make saure that the
   * client has been successfully connected to the remote server before attempting to log in.
   * 
   * @throws ComponentException
   */
  public void logon();

  /**
   * This method creates a FTP input stream in the shape of an InputStreamReader that the caller can use to perform the
   * GET function. The file is retrieved from the current working directory. Assumes that the FTP client has already
   * been successfully connected to and authenticated. Although, you really shouldn't be able to get this far without
   * this being the case anyway!
   * 
   * @param fileName -
   *          The name of the file to retrieve
   * 
   * @return An InputStreamReader object containing the file contents
   * 
   * @throws ComponentException
   */
  public InputStreamReader get(String fileName);

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
  public OutputStreamWriter put(String fileName);

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
  public OutputStreamWriter append(String fileName);

  /**
   * Close the connection to the remote server
   */
  public void close() throws ComponentException;

  /**
   * Check for directory on remote server. Assumes that the client is already connected and logged into the remote
   * server
   * 
   * @param dirName -
   *          the directory to check for
   * 
   * @return boolean to indicate the presence of the directory
   * 
   * @throws ComponentException -
   *           if the client is not connected and logged into the remote server
   */
  public boolean directoryExists(String dirName);

  /**
   * Deletes supplied file from the remote server
   * 
   * @param fileName -
   *          the file to delete
   * 
   * @throws ComponentException -
   *           if the client is not logged into the remote server or there was a problem with the deletion
   */
  public void delete(String fileName);

  /**
   * Retrieves a list of file names on the remote server that match the supplied pattern.
   * 
   * @param filePattern -
   *          the pattern the match file names against
   * 
   * @return - array of the file names or null if none found
   * 
   * @throws ComponentException -
   *           if there was an communications error
   */
  public String[] fileList(String filePattern);

  /**
   * Changes the current working directory
   * 
   * @param directoryName -
   *          the new directory
   */
  public void cd(String directoryName);

  /**
   * @return the current working directory
   */
  public String getCurrentWorkingDirectory();

  /**
   * Sets the file transfer mode toi BINARY.
   * 
   * @param b -
   *          If false then will be set to ASCII
   */
  public void setBinaryTransfer(boolean b);

  /**
   * There are a few FTPClient methods that do not complete the entire sequence of FTP commands to complete a
   * transaction. These commands require some action by the programmer after the reception of a positive intermediate
   * command. After the programmer's code completes its actions, it must call this method to receive the completion
   * reply from the server and verify the success of the entire transaction.
   * 
   * @return true if the transfer has been successful
   * 
   * @throws ComponentException
   */
  public boolean verifyFileTransfer();
}
