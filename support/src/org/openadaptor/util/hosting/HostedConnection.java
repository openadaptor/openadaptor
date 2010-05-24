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

package org.openadaptor.util.hosting;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;

import com.meterware.httpunit.UploadFileSpec;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class HostedConnection {
  private static final Log log = LogFactory.getLog(HostedConnection.class);

  private static final String[][] MIME_TYPES= {
    {"zip","application/x-zip-compressed"},
    {"txt","text/plain"},
    {"","text/plain"}
  };

  /**
   * Trust Manager which trusts anything.
   */
  private final TrustManager[] trustManagers= new TrustManager [] {
      new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] certs, String AuthType) {}
        public void checkServerTrusted(X509Certificate[] arg0, String arg1){}
        public X509Certificate[] getAcceptedIssuers() {return null;}    
      }
  };

  public static final String PROP_PROJECT_URL="project.url";
  public static final String PROP_PROXY_HOST="proxy.host";
  public static final String PROP_PROXY_PORT="proxy.port";
  public static final String PROP_PROXY_USERNAME="proxy.username"; //For authenticating proxy
  public static final String PROP_PROXY_PASSWORD="proxy.password"; //For authenticating proxy
  public static final String PROP_USERNAME="username";
  public static final String PROP_PASSWORD="password";
  public static final String PROP_UPLOAD_TARGET_FOLDER="upload.target.folder";
  public static final String PROP_UPLOAD_FILE_PATH="upload.file.path";
  public static final String PROP_UPLOAD_FILE_DESCRIPTION="upload.file.description";
  public static final String PROP_UPLOAD_FILE_STATUS="upload.file.status";

  public static final String REL_LOGIN_PATH="/servlets/TLogin";
  private static final String LOGIN_FORM_ID="loginform";
  private static final String LOGIN_USER_FIELD="loginID";
  private static final String LOGIN_PASSWORD_FIELD="password";

  private static final String REL_FOLDER_ADD_PATH="/servlets/ProjectFolderAdd?folderID=";
  private static final String REL_DOCUMENT_ADD_PATH="/servlets/ProjectDocumentAdd?folderID=";
  private static final String REL_DOC_DELETE_PATH="/servlets/ProjectDocumentDelete?documentID=";

  private String connectURL;
  private String projectName;

  private WebConversation wc = null;

  private Properties properties=null;

  public String getProjectName() { return projectName; }

  protected HostedConnection() {
    log.debug("Installing all-trusting Security manager");

    SSLContext sc;
    try {
      sc = SSLContext.getInstance("SSL");
      sc.init(null, trustManagers, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      new URL("https://not.real");
    } catch (NoSuchAlgorithmException e) {
      fail("Failed to establish SSL context",e);

    } catch (KeyManagementException e) {
      fail(e.getMessage(),e);

    } catch (MalformedURLException e) {
      fail("Failed to setup security manager "+e);
    }
  }

  public HostedConnection(String urlString) throws RuntimeException {
    this();
    log.debug("ProjectUrl: "+urlString);
    connectURL=urlString;
    projectName=deriveProjectName(connectURL);
    wc=new WebConversation();
  }

  public HostedConnection(Properties properties) {
    this(properties.getProperty(PROP_PROJECT_URL));
    this.properties=properties;
    if (properties.containsKey(PROP_PROXY_HOST)) {
      String proxyHost=properties.getProperty(PROP_PROXY_HOST);
      String proxyPort=properties.getProperty(PROP_PROXY_PORT);
      String proxyUsername=properties.getProperty(PROP_PROXY_USERNAME);
      String proxyPassword=properties.getProperty(PROP_PROXY_PASSWORD);
      setProxy(proxyHost,proxyPort,proxyUsername,proxyPassword);
    }
  }

  public void setProxy(String proxyHost, int proxyPort,String proxyUsername,String proxyPassword) {
    log.info("Configuring proxy of "+proxyHost+":"+proxyPort);
    System.setProperty("https.proxyHost", proxyHost);
    System.setProperty("https.proxyPort", String.valueOf(proxyPort));
    if (proxyUsername!=null) {
      log.info("Configuring proxy user of "+proxyUsername);
      System.setProperty("https.proxyUser",proxyUsername);
      System.setProperty("https.proxyPassword",proxyPassword);           
      wc.setProxyServer(proxyHost, proxyPort,proxyUsername,proxyPassword);
    }
    else {
      wc.setProxyServer(proxyHost, proxyPort);
    }
  }

  public void setProxy(String proxyHost,String proxyPortString,String proxyUsername,String proxyPassword) {
    setProxy(proxyHost,Integer.parseInt(proxyPortString),proxyUsername,proxyPassword);
  }

  public HostedFolder login() {
    HostedFolder result=null;
    if (properties!=null) {
      if (properties.containsKey(PROP_USERNAME)) {
        result= login(properties.getProperty(PROP_USERNAME),properties.getProperty(PROP_PASSWORD));
      }
      else {
        fail("Property "+PROP_USERNAME+" must be configured to use parameterless login");
      }
    }
    return result;
  }

  public HostedFolder login(String userName,String password) {
    return login(REL_LOGIN_PATH,userName,password);
  }

  protected HostedFolder login(String loginRelativeURL,String userName, String password) {
    WebResponse r = fetchRelative(loginRelativeURL);
    log.debug("Retrieving login form");
    WebForm form = getForm(r,LOGIN_FORM_ID);

    form.setParameter(LOGIN_USER_FIELD, userName);
    form.setParameter(LOGIN_PASSWORD_FIELD, password);
    log.debug("Submitting login for "+userName);
    r=submit(form);

    //Success if we're not still on the login pae.
    boolean success=wc.getCurrentPage().getURL().toExternalForm().indexOf("TLogin") == -1;
    if (success) {
      log.info("Login successful");
      log.debug("Current URL now: "+wc.getCurrentPage().getURL().toString());
    }
    else {
      fail("Login failed.");
    }
    return new HostedFolder(this);
  }


  public WebResponse fetchRelative(String relativeUrl) {
    return fetch(connectURL+relativeUrl);
  }  

  private WebResponse fetch(String url) {
    WebResponse response=null;
    synchronized (wc) {
      try {
        log.debug("Fetching: "+url);
        response=wc.getResponse(url);
      } 
      catch (Throwable t) {
        fail("Failed to fetch "+url,t);
      }
    }
    int responseCode=response.getResponseCode();
    if ( responseCode != 200) {
      log.warn("Response code was: "+responseCode);
    }
    else {
      log.debug("Response code: "+responseCode);
    }
    return response;
  }

  public Document fetchDocument(String relativePath) {
    WebResponse response = fetchRelative(relativePath);
    Document document = null;
    try {
      document = new DOMReader().read(response.getDOM());
      log.debug("Fetched "+relativePath);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("Failed to fetch Document using url "+relativePath,e);
    }
    return document;
  }


  public WebForm getForm(WebResponse response,String id){
    WebForm form=null;
    try {
      form = response.getFormWithID(id);
    }
    catch (org.xml.sax.SAXException e) {
      fail("Failed to retrieve form "+id,e);
    }
    return form;
  }

  public WebResponse submit(WebForm form) {
    WebResponse response=null;
    try {
      response=form.submit();
    }
    catch (Throwable t) {
      fail("Failed to submit form "+form,t);
    }
    return response;

  }

  private String deriveProjectName(String urlString) {
    String projectName=null;
    try {
      URL url=new URL(urlString);
      String host=url.getHost();
      projectName=host;
      int ofs=host.indexOf('.');
      if (ofs !=-1) {
        projectName=host.substring(0,ofs);
      }
    } catch (MalformedURLException e) {
      fail("Failed to derive project name from: "+urlString,e);
    } 
    if (projectName==null) {
      fail("Failed to derive project name from: "+urlString);
    }
    log.debug("Derived project name is: "+projectName);
    return projectName;
  }

  public void addSubFolder(int parentId,String name, String description)  {
    try {
      WebResponse webResponse=fetchRelative(REL_FOLDER_ADD_PATH+parentId);
      WebForm[] forms=webResponse.getForms();
      if (forms!=null && (forms.length > 1)) {
        WebForm createForm=forms[1]; //Form 0 is a search form
        createForm.setParameter("name", name);
        createForm.setParameter("description",description);
        WebRequest webRequest=createForm.getRequest(createForm.getSubmitButton("Button"));
        webResponse=wc.getResponse(webRequest);
      }
    }
    catch (Exception e) {
      fail("Failed to add folder",e);
    } 
  }

  private static String getContentType(String path) {
    String suffix="";
    String mimeType="text/plain";
    int ofs=path.lastIndexOf('.');
    if (ofs != -1) {
      suffix=path.substring(ofs+1);
    }
    log.debug("Checking for matching mime type for suffix: "+suffix);
    for (int i=0;i<MIME_TYPES.length;i++) {
      String key=MIME_TYPES[i][0];
      if (key.equalsIgnoreCase(suffix)) {
        mimeType=MIME_TYPES[i][1];
        break;
      }
    }
    log.info("Mime type for "+path +" is "+mimeType);
    return mimeType;
  }

  public void addFile(int parentId,String name,String status,String description,String filePath)  {
    try {
      WebResponse webResponse=fetchRelative(REL_DOCUMENT_ADD_PATH+parentId);
      WebForm[] forms=webResponse.getForms();
      if (forms!=null && (forms.length > 1)) {
        WebForm createForm=forms[1]; //Form 0 is a search form
        File file = new File(filePath);
        if (!file.exists()){
          fail("Unable to locate file: "+filePath);
        }
        String uploadFileName=(name!=null)?name:file.getName();
        createForm.setParameter("name", uploadFileName);
        createForm.setParameter("status", status);
        createForm.setParameter("description", description);
        createForm.setParameter("type", "file");
        UploadFileSpec ufs=new UploadFileSpec(uploadFileName, new FileInputStream(file), getContentType(filePath));
        createForm.setParameter("file", new UploadFileSpec[]{ufs});

        log.info("Uploading "+uploadFileName+" ... ");
        WebRequest webRequest=createForm.getRequest(createForm.getSubmitButton("Button"));
        webResponse=wc.getResponse(webRequest);
        int responseCode=webResponse.getResponseCode();
        if ( responseCode != 200) {
          fail("Upload of "+uploadFileName+" failed - HTTP response code was "+responseCode);
        }     
        else {
          log.info("Upload of "+uploadFileName+" was successful");
        }
      }
      else {
        fail("Failed to get add form");
      }
    }
    catch (Exception e) {
      fail("Failed to add file: "+filePath,e);
    } 
  }

  public void deleteFile(HostedFile hostedFile) {
    if (hostedFile!=null) {
      int id=hostedFile.getId();
      log.debug("Deleting hosted file "+hostedFile.getName());
      deleteFile(id);
    }
    else {
      fail("Cannot delete - hosted file is <null>");
    }
  }

  public void deleteFile(int fileId) {
    try {
      log.debug("Deleting file with id: "+fileId);
      WebResponse webResponse=fetchRelative(REL_DOC_DELETE_PATH+fileId);
      WebForm[] forms = webResponse.getForms();
      if (forms!=null && (forms.length > 1)) {
        WebForm confirmForm = forms[1];  
        WebRequest webRequest=confirmForm.getRequest(confirmForm.getSubmitButtons()[0]);
        webResponse=wc.getResponse(webRequest);
      }
    }
    catch (Exception e) {
      fail("Failed to delete file with id "+fileId);
    } 
  }

  public static void fail(String msg) {
    fail(msg,null);
  }
  public static void fail(String msg,Throwable t) {
    log.warn(msg);
    log.warn("Cause: "+t);
    if (t!=null){
      throw new RuntimeException(msg,t);
    }
    else {
      throw new RuntimeException(msg);
    }
  }
}
