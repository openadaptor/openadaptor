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

package org.openadaptor.auxil.connector.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ReadConnector that registers a servlet to accept HttpRequests for processing 
 * 
 * @author perryj
 * 
 */
public class ReadConnectorServlet extends ServletServingReadConnector {

  private static final Log log = LogFactory.getLog(ReadConnectorServlet.class);

  private boolean acceptGet = false;

  private String[] parameterNames = new String[0];

  /**
   * If this is set then HTTP GET is accepted, default is false
   * 
   * @param accept
   */
  public void setAcceptGet(boolean accept) {
    acceptGet = accept;
  }

  /**
   * set the expected parameters to process, If a single parameter is specified then this single 
   * value is queued for processing. Otherwise a map of the parameters and values is queued.
   * 
   * @param params
   */
  public void setParameterNames(String[] params) {
    parameterNames = new String[params.length];
    for (int i = 0; i < params.length; i++) {
      parameterNames[i] = params[i];
    }
  }

  /**
   * @see #setParameterNames
   * @param param
   */
  public void setParameterName(String param) {
    parameterNames = new String[1];
    parameterNames[0] = param;
  }

  /**
   * registers servlet
   */
  public void connect() {
    log.debug("Creating HttpServlet");
    Servlet servlet = new HttpServlet() {
      private static final long serialVersionUID = 1L;

      protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (acceptGet) {
          process(request, response);
        } else {
          log.debug("httpGet ignored");
        }
      }

      protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        process(request, response);
      }
    };
    setServlet(servlet);
    super.connect();
    log.info(getId() + " added servlet " + getServletUrl());
  }

  public String getServletUrl() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("http://");
    buffer.append(getServletContainer().getHost());
    buffer.append(":");
    buffer.append(getServletContainer().getPort());
    buffer.append(getServletContainer().getContext().equals("/") ? "" : getServletContainer().getContext());
    buffer.append(getPath());
    return buffer.toString();
  }

  private void process(HttpServletRequest request, HttpServletResponse response) {

    if (log.isDebugEnabled()) {
      Enumeration e = request.getParameterNames();
      while (e.hasMoreElements()) {
        String name = (String) e.nextElement();
        String value = request.getParameter(name);
        log.debug(name + "=" + value);
      }
    }

    // single param configuration - queues string value
    if (parameterNames.length == 1) {
      String data = request.getParameter(parameterNames[0]);
      if (data != null) {
        enqueue(data);
      } else {
        String msg = "request did not contain expected parameter " + parameterNames[0];
        log.error(getId() + " " + msg);
        writeErrorNoThrow(response, msg);
      }
    }

    // multiple param configuration - queues map
    else if (parameterNames.length > 1){
      Map map = new HashMap();
      for (int i = 0; i < parameterNames.length; i++) {
        String data = request.getParameter(parameterNames[i]);
        map.put(parameterNames[i], data);
      }
      if (!map.isEmpty()) {
        enqueue(map);
      } else {
        String msg = "request did not contain expected parameters ";
        for (int i = 0; i < parameterNames.length; i++) {
          msg += (i > 0 ? "," : "") + parameterNames[i];
        }
        log.error(getId() + " " + msg);
        writeErrorNoThrow(response, msg);
      }
    } 

    // no param configuration - queues map of all request parameters
    else {
      Map map = new HashMap();
      Enumeration e = request.getParameterNames();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        String data = request.getParameter(key);
        map.put(key, data);
      }
      if (!map.isEmpty()) {
        enqueue(map);
      } else {
        String msg = "request did not contain any parameters ";
        log.error(getId() + " " + msg);
        writeErrorNoThrow(response, msg);
      }
    }
  }

  private void writeErrorNoThrow(HttpServletResponse response, String msg) {
    try {
      PrintWriter writer = response.getWriter();
      writer.write(msg);
    } catch (IOException e) {
    }
  }

  public void validate(List exceptions) {
  }
}
