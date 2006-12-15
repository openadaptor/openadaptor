package org.oa3.auxil.connector.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
public class ReadConnectorServlet extends JettyReadConnector {

  private static final Log log = LogFactory.getLog(ReadConnectorServlet.class);

  private boolean acceptGet = false;

  private String[] parameterNames;

  /**
   * If this is set then HTTP GET is accepted, default is false
   * 
   * @param map
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
   * @see setParameterNames
   * @param param
   */
  public void setParameterName(String param) {
    parameterNames = new String[1];
    parameterNames[0] = param;
  }

  /**
   * triggers jetty to be started if required registers servlet
   */
  public void connect() {
    if (getServlet() == null) {
      Servlet servlet = new HttpServlet() {

        private static final long serialVersionUID = 1L;

        protected void doGet(HttpServletRequest request, HttpServletResponse response) {
          if (acceptGet) {
            process(request, response);
          } else {
            log.info("httpGet ignored");
          }
        }

        protected void doPost(HttpServletRequest request, HttpServletResponse response) {
          process(request, response);
        }
      };
      setServlet(servlet);
    }
    super.connect();
    log.info(getId() + " added servlet " + getServletUrl());
  }

  public String getServletUrl() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("http://");
    buffer.append(getJettyHost());
    buffer.append(":");
    buffer.append(getJettyPort());
    buffer.append(getContext().equals("/") ? "" : getContext());
    buffer.append(getPath());
    return buffer.toString();
  }

  private void process(HttpServletRequest request, HttpServletResponse response) {

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
    else {
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
  }

  private void writeErrorNoThrow(HttpServletResponse response, String msg) {
    try {
      PrintWriter writer = response.getWriter();
      writer.write(msg);
    } catch (IOException e) {
    }
  }
}
