/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.auxil.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.openadaptor.auxil.connector.http.ReadConnectorServlet;
import org.openadaptor.auxil.connector.iostream.RFC2279;


public class ReadConnectorServletTestCase extends TestCase {

  public void testPost() {
    
    // create connector and connect (this starts jetty)
    ReadConnectorServlet servlet = new ReadConnectorServlet();
    servlet.setParameterName("data");
    servlet.setLocalJettyPort(9999);
    servlet.setTransacted(false);
    servlet.connect();
    
    // to http get
    postData(servlet.getServletUrl(), "data", "foo");
    postData(servlet.getServletUrl(), "data", "bar");

    // poll service and check results
    Object[] data = servlet.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("foo"));

    data = servlet.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("bar"));

    servlet.setParameterNames(new String[] {"field1", "field2", "field3"});
    Map map = new HashMap();
    map.put("field1", "foo");
    map.put("field2", "bar");
    map.put("field3", "foobar");
    postData(servlet.getServletUrl(), map);
    
    data = servlet.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals(map));
   
    servlet.disconnect();
  }

  public void testGet() {
    
    // create connector and connect (this starts jetty)
    ReadConnectorServlet servlet = new ReadConnectorServlet();
    servlet.setParameterName("data");
    servlet.setAcceptGet(true);
    servlet.setLocalJettyPort(9999);
    servlet.setTransacted(false);
    servlet.connect();
    
    // to http get
    getData(servlet.getServletUrl(), "data", "foo");
    getData(servlet.getServletUrl(), "data", "bar");

    // poll service and check results
    Object[] data = servlet.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("foo"));

    data = servlet.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("bar"));

    servlet.setParameterNames(new String[] {"field1", "field2", "field3"});
    Map map = new HashMap();
    map.put("field1", "foo");
    map.put("field2", "bar");
    map.put("field3", "foobar");
    getData(servlet.getServletUrl(), map);
    
    data = servlet.next(0);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals(map));
   
    servlet.disconnect();
  }

  private void postData(String urlString, String paramName, String data) {
    try {
      URL url = new URL(urlString.replaceAll("\\*", ""));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(paramName + "=" + data);
      writer.flush();
      int responseCode = connection.getResponseCode();
      if (HttpURLConnection.HTTP_OK != responseCode) {
        fail("post failed");
      }
      writer.close();
      readResponse(connection);
    } catch (Exception e) {
    }
  }
  
  private void getData(String urlString, String paramName, String data) {
    try {
      String s = urlString.replaceAll("\\*", "?" + paramName + "=" + data);
      URL url = new URL(s);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      readResponse(connection);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
  
  private void postData(String urlString, Map data) {
    try {
      URL url = new URL(urlString.replaceAll("\\*", ""));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(createtParamString(data));
      writer.flush();
      int responseCode = connection.getResponseCode();
      if (HttpURLConnection.HTTP_OK != responseCode) {
        fail("post failed");
      }
      writer.close();
      readResponse(connection);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private void readResponse(HttpURLConnection connection) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    StringBuffer buffer = new StringBuffer();
    while ((line = br.readLine()) != null) {
      buffer.append(line);
    }
    br.close();
    if (buffer.length() > 0) {
      throw new RuntimeException(buffer.toString());
    }
  }

  private String createtParamString(Map data) throws UnsupportedEncodingException {
    StringBuffer buffer = new StringBuffer();
    for (Iterator iter = data.entrySet().iterator(); iter.hasNext();) {
      buffer.append(buffer.length() > 0 ? "&" : "");
      Map.Entry entry = (Map.Entry) iter.next();
      buffer.append(URLEncoder.encode(entry.getKey().toString(), RFC2279.UTF_8));
      buffer.append("=");
      buffer.append(URLEncoder.encode(entry.getValue().toString(), RFC2279.UTF_8));
    }
    return buffer.toString();
  }
  
  private void getData(String urlString, Map data) {
    try {
      URL url = new URL(urlString.replaceAll("\\*", "?" + createtParamString(data)));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      readResponse(connection);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
