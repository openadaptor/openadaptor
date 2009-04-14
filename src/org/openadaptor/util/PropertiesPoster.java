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

package org.openadaptor.util;

/*
 * File: $Headers$ Rev: $Revision: 1.4 $ Created Feb 19, 2006 by Eddy Higgins
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.client.OAClient;
import org.openadaptor.auxil.client.WebServiceWriterBuilder;
import org.openadaptor.auxil.client.WriterBuilder;

/**
 * Adaptor registration is a process for collecting active adaptor information in a central location. If the property
 * <code>registrationUrl</code> is set within an adaptor bean, data regarding the adaptor and its configuration are
 * sent to the registration primaryUrl using HTTP POST each time the adaptor is started.
 * <p>
 * The server side of the registration process is a JBoss application server backed by a MySQL database which stores all
 * of the data received in a table
 * 
 * @author Eddy Higgins
 */
public class PropertiesPoster {
  protected static Log log = LogFactory.getLog(PropertiesPoster.class.getName());

  /**
   * W3C state that UTF-8 should be used (not doing so may introduce compatibilities)
   * 
   * @see URLEncoder
   */
  private static final String DEFAULT_ENCODING = EncodingAwareObject.UTF_8;

  private PropertiesPoster() {
  } // No instantiation allowed.


  public static Thread asyncPost(String registrationPrimaryURL, String registrationFailoverURL,
      Properties properties, IRegistrationCallbackListener caller) {
    AsyncPoster poster=new AsyncPoster(caller,registrationPrimaryURL,registrationFailoverURL,properties);
    Thread posterThread=new Thread(poster,"Registration-Thread");
    log.debug("Launching new Thread to register asynchronously");
    posterThread.setDaemon(true);
    posterThread.start();
    return posterThread;
  }

  public static void syncPost(String registrationURL, Properties properties) throws Exception {
    if(registrationURL!=null && registrationURL.indexOf("wsdl")!=-1){
      syncPostWS(registrationURL,properties);
    }
    else{
      syncPostHttp(registrationURL,properties);
    }
  }

  /**
   * Sends registration data to a web service.
   * 
   * @param endpoint - Webservice endpoint.
   * @param properties - data to be sent.
   * @throws Exception
   */
  protected static void syncPostWS(String endpoint, Properties properties) throws Exception {
    log.info("Attempt to register via a web service");
    WriterBuilder wsWriterBuilder = new WebServiceWriterBuilder(endpoint, "OA3RegistrationWSCaller");
    OAClient client = null;
    try{
       client = new OAClient(wsWriterBuilder);
    }
    catch(java.lang.NoClassDefFoundError e){
      log.error("Error while connecting to registration web service. Make sure activation.jar is on the classpath. Class not found: " + e.getMessage());
      log.info("Registration failed.");
      return;
    }
    client.send(properties);
    log.info("Registration complete.");
  }
  
  /**
   * Utility method which will attempt to POST the supplied properties information to the supplied URL.
   * 
   * This method currently contains an all trusting trust manager for use with https. This will be replaced with a more
   * secure trust manager which will use a cert store.
   * 
   * @param registrationURL
   * @param properties
   * @throws Exception
   */
  protected static void syncPostHttp(String registrationURL, Properties properties) throws Exception {

    URL url = new URL(registrationURL);
    String postData = generatePOSTData(properties);
    log.debug("Protocol: " + url.getProtocol());
    if (url.getProtocol().equals("https")) {

      // https connection

      // TODO: Replace this all trusting manager with one that uses a cert store
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
      } };

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());

      HttpsURLConnection secureConnection = null;
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      secureConnection = (HttpsURLConnection) url.openConnection();
      secureConnection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(secureConnection.getOutputStream());
      writer.write(postData);
      writer.flush();
      int responseCode = secureConnection.getResponseCode();
      if (HttpsURLConnection.HTTP_OK != responseCode) {
        log.error("\nFailed to register. Response Code " + responseCode + "\nResponse message:"
            + secureConnection.getResponseMessage() + "\nRegistration URL: " + registrationURL + "\nData: "
            + generateString(properties));
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(secureConnection.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        log.debug("Returned data: " + line);
      }
      writer.close();
      br.close();
    } else {

      // Normal http connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(postData);
      writer.flush();
      int responseCode = connection.getResponseCode();
      if (HttpURLConnection.HTTP_OK != responseCode) {
        log.error("\nFailed to register. Response Code " + responseCode + "\nResponse message:"
            + connection.getResponseMessage() + "\nRegistration URL: " + registrationURL + "\nData: "
            + generateString(properties));
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        log.debug("Returned data: " + line);
      }
      writer.close();
      br.close();
    }
  }

  /**
   * Simple utility method to convert a set of properties into a HTTP POST string.
   * 
   * @param properties
   * @return String containing appropriately encoded name/value pairs
   * @throws UnsupportedEncodingException
   */
  public static String generatePOSTData(Properties properties) throws UnsupportedEncodingException {
    StringBuffer sb = new StringBuffer();
    if (properties != null) {
      for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
        String key = (String) keys.nextElement();
        String value = properties.getProperty(key);
        if (sb.length() > 0) {// Need separator between <name>=<value> pairs
          sb.append("&");
        }
        sb.append(URLEncoder.encode(key, DEFAULT_ENCODING));
        sb.append("=");
        sb.append(URLEncoder.encode(value, DEFAULT_ENCODING));
      }
    }
    log.debug("Generated POST data:" + sb.toString());
    return sb.toString();
  }

  /**
   * Simple utility method to convert a set of properties into a string to send as part of email when there is a failure
   * to register.
   * 
   * @param properties
   * @return String containing appropriately encoded name/value pairs
   */
  public static String generateString(Properties properties) {
    StringBuffer sb = new StringBuffer();
    if (properties != null) {
      for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
        String key = (String) keys.nextElement();
        String value = properties.getProperty(key);
        if (sb.length() > 0) {// Need separator between <name>=<value> pairs
          sb.append("\n");
        }
        sb.append(key);
        sb.append("=");
        sb.append(value);
      }
    }
    // log.debug("Generated String:"+sb.toString());
    return sb.toString();
  }
}

class AsyncPoster implements Runnable {
  private IRegistrationCallbackListener listener;
  private String primaryUrl;
  private String failoverUrl;
  private Properties props;

  public AsyncPoster(IRegistrationCallbackListener listener,String primaryUrl,String failoverUrl, Properties props) {
    this.listener=listener;
    this.primaryUrl=primaryUrl;
    this.failoverUrl=failoverUrl;
    this.props=props;
  }

  public void run() {
    Object result=null;
    boolean success = false;
    /* First try primary URL */
    try {
      PropertiesPoster.syncPost(primaryUrl,props);
      result = "Complete.";
      success = true;
    }
    catch (Exception e) {
      result=e;
    }
    /* If primary didn't work try failover*/
    if(!success && failoverUrl!=null && failoverUrl.length()>0){
      try {
        PropertiesPoster.syncPost(failoverUrl,props);
        result = "Complete (failover).";
        success = true;
      }
      catch (Exception e) {
        result=e;
      }
    }
    listener.registrationCallbackEvent(this, result);
    PropertiesPoster.log.debug("Registration thread is done");
  }
}

