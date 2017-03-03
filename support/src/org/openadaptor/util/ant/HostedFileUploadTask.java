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

package org.openadaptor.util.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openadaptor.util.hosting.HostedConnection;
import org.openadaptor.util.hosting.HostedFile;
import org.openadaptor.util.hosting.HostedFolder;

/**
 * Upload a file to a hosted server.
 * <br>
 * Intended to be used for uploading nightly builds of openadaptor.
 * @author higginse
 */
public class HostedFileUploadTask extends Task {
  private String projectUrl;
  private String proxyHost;
  private int proxyPort;
  private String proxyUsername=null;
  private String proxyPassword=null;
  private String username;
  private String password;

  private String targetFolder;
  private String filePath;
  private String description;
  private String status=HostedFile.STABLE;
  private boolean overwrite=false;

  private HostedConnection connection;
  private File file;
  private HostedFile existingFile;

  public void setProjectURL(String projectUrl) {this.projectUrl=projectUrl;}
  public void setProxyHost(String proxyHost){this.proxyHost=realValue(proxyHost);}
  private void setProxyPort(int proxyPort){this.proxyPort=proxyPort;} //Will cause an NFE if called with null - hence string variant below
  public void setProxyPort(String proxyPort) { //Convenience - mostly in case it's called with null
    if (proxyPort!=null) {
      try {
        setProxyPort(Integer.parseInt(realValue(proxyPort)));
      }
      catch (NumberFormatException nfe) {} //Just ignore it.
    }
  }
  public void setProxyUsername(String proxyUsername){this.proxyUsername=realValue(proxyUsername);}
  public void setProxyPassword(String proxyPassword){this.proxyPassword=realValue(proxyPassword);}

  public void setUsername(String username){this.username=username;}
  public void setPassword(String password){this.password=password;}

  public void setTargetFolder(String targetFolder){this.targetFolder=targetFolder;}
  public void setFilePath(String filePath){this.filePath=filePath;}
  public void setDescription(String description){this.description=description;}
  public void setStatus(String status){this.status=status;}
  public void setOverwrite(boolean overwrite){this.overwrite=overwrite;}

  /**
   * Check if a property is really an unresolved placeholder.
   * <br>
   * If so, return null instead.
   * @param propValue candidate value
   * @return The value, or null if it looks like ${...}
   */
  private static String realValue(String propValue) {
    String realValue=propValue;
    if ((propValue!=null) && 
        propValue.startsWith("${") &&
        propValue.endsWith("}")) {
      realValue=null;
    }
    return realValue;
  }

  private void validate() {
    if (projectUrl==null) {
      fail("projectUrl is a mandatory property");
    }
    if (filePath==null) {
      fail("filePath is a mandatory property");
    }
    file=new File(filePath);
    if (!file.exists()) {
      fail("Unable to locate "+file);
    }
  }

  private HostedFolder initialiseConnection() {
    HostedFolder folder=null;
    connection=new HostedConnection(projectUrl);
    if (proxyHost!=null) {
      connection.setProxy(proxyHost, proxyPort,proxyUsername,proxyPassword);
    }
    folder=connection.login(username, password);
    if(targetFolder!=null) {
      folder=folder.getSubFolder(targetFolder);
    }
    String filename=file.getName();
    existingFile=folder.getFile(filename);
    if ((!overwrite) && (existingFile!=null)) {
      throw new RuntimeException("File "+filename+" already exists, and overwrite=false");
    }
    return folder;
  }

  /**
   * This will attempt to upload a file to a hosted service using
   * provided properties.
   */
  public void execute() throws BuildException {
    validate();
    try {
      HostedFolder folder=initialiseConnection();
      if (overwrite && (existingFile!=null)){
        connection.deleteFile(existingFile);
      }
      folder.addFile(filePath,null,status, description);
    }
    catch (RuntimeException re) {
      fail("Upload failed - "+re.getMessage(),re);
    }
  }

  private void fail(String msg) {
    System.err.println(msg);
    throw new BuildException(msg);
  }
  private void fail(String msg,Throwable t) {
    System.err.println(msg);
    throw new BuildException(msg,t);
  }

  /**
   * Main task is purely for testing and should not normally be used.
   * @param argv String arguments to main
   */
  public static void main(String argv[]) {
    HostedFileUploadTask task=new HostedFileUploadTask();
    task.setProjectURL("https://some-url-here");
    task.setProxyHost("proxy");
    task.setProxyPort(8080);
    task.setUsername("username");
    task.setPassword("password");

    task.setFilePath("uploadfilepath");
    task.setDescription("description");
    task.setOverwrite(true); 
    task.execute();
  }

}
