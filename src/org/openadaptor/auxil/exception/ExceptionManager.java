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

package org.openadaptor.auxil.exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.xfire.client.Client;
import org.mortbay.jetty.Server;
import org.openadaptor.auxil.connector.http.ServletContainer;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;
import org.openadaptor.util.ResourceUtil;
import org.openadaptor.util.ResourceUtils;

public class ExceptionManager {

  private static final Log log = LogFactory.getLog(ExceptionManager.class);

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM");

  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

  private ExceptionStore exceptionStore;

  private ReadConnectorWebService webService;

  private Server server;

  private VelocityEngine engine;

  private static final Comparator COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      ExceptionSummary s1 = (ExceptionSummary) o1;
      ExceptionSummary s2 = (ExceptionSummary) o2;
      return s2.getOrderKey().compareTo(s1.getOrderKey());
    }
  };

  public ExceptionManager() {
    engine = new VelocityEngine();
    Properties p = new Properties();
    try {
      InputStream inStream = this.getClass().getResourceAsStream("velocity.properties");
      p.load(inStream);
      inStream.close();
      engine.init(p);
    } catch (Exception e) {
      throw new RuntimeException("unable to initialise velocity engine, " + e.getMessage(), e);
    }
  }

  public void setExceptionStore(final ExceptionStore exceptionStore) {
    this.exceptionStore = exceptionStore;
  }

  public void setJettyServer(final Server server) {
    this.server = server;
  }

  public static void main(String[] args) {
    String dir = null;
    int port = 8080;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-dir")) {
        dir = args[++i];
      } else if (args[i].equalsIgnoreCase("-port")) {
        port = Integer.parseInt(args[++i]);
      }
    }

    ExceptionManager mgr = new ExceptionManager();
    mgr.setExceptionStore(new ExceptionFileStore(dir));
    mgr.setJettyServer(new Server(port));
    log.info("admin interface at http://" + ResourceUtils.getLocalHostname() + ":" + port + "/admin");
    mgr.run();
  }

  public void run() {

    try {
      server.start();
    } catch (Exception e) {
      log.error("failed to start jetty server", e);
      throw new RuntimeException("failed to start local jetty server", e);
    }

    // create web service to receive exceptions and store
    ServletContainer servletContainer = new ServletContainer(server);
    webService = new ReadConnectorWebService("WebService");
    webService.setServletContainer(servletContainer);
    webService.setServiceName("ExceptionManager");
    webService.setPath("/soap/*");
    webService.setTransacted(false);
    webService.connect();

    // create servlet for browsing exceptions
    webService.addServlet(new MyServlet(), "/admin/*");

    while (!webService.isDry()) {
      Object[] data = webService.next(1000);
      if (data != null) {
        storeData(data);
      }
    }

  }

  private void storeData(Object[] data) {
    for (int i = 0; i < data.length; i++) {
      String id = exceptionStore.store(data[i].toString());
      log.debug("stored new exception " + id);
    }
  }

  public class MyServlet extends HttpServlet {
    private static final String ENCODING = "UTF-8";
    private static final String BROWSE_TEMPLATE = "browse.html";
    private static final String LIST_TEMPLATE = "list.html";
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      response.setStatus(HttpServletResponse.SC_OK);
      String action = request.getParameter("action");
      if (action == null || action.equals("list")) {
        list(request, response);
      } else if (action.equals("browse")) {
        browse(request, response);
      } else if (action.equals("delete")) {
        delete(request, response);
      } else if (action.equals("deleteall")) {
        deleteall(request, response);
      } else if (action.equals("retry")) {
        retry(request, response);
      } else {
        throw new RuntimeException("action " + action + " not recognized");
      }
    }
    
    private void retry(HttpServletRequest request, HttpServletResponse response) {
      String id = request.getParameter("id");
      HttpSession session = request.getSession();
      if (id != null) {
        ExceptionSummary summary = exceptionStore.getExceptionSummary(id);
        String data = exceptionStore.getDataForId(id);
        try {
          Client client = new Client(new URL(summary.getRetryAddress()));
          client.invoke("retry", new Object[] { summary.getComponentId(), data });
          delete(request, response);
        } catch (Exception e) {
          exceptionStore.incrementRetry(id);
          id = exceptionStore.store(MessageExceptionXmlConverter.toXml(e, id));
          session.putValue("lastid", id);
        }
      }
      redirect(response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) {
      String id = request.getParameter("id");
      if (id != null) {
        exceptionStore.delete(id);
      } else {
        exceptionStore.deleteAll();
      }
      redirect(response);
    }

    private void redirect(HttpServletResponse response) {
      try {
        String url = response.encodeRedirectUrl("/admin/?action=list");
        response.sendRedirect(url);
      } catch (IOException e) {
      }
    }

    private void deleteall(HttpServletRequest request, HttpServletResponse response) {
      String[] ids = request.getParameter("ids").split(",");
      for (int i = 0; i < ids.length; i++) {
        if (ids[i] != null && ids[i].length() > 0) {
          exceptionStore.delete(ids[i]);
        }
      }
      redirect(response);
    }

    private void browse(HttpServletRequest request, HttpServletResponse response) {
      String id = request.getParameter("id");
      if (id != null) {
        ExceptionSummary summary = exceptionStore.getExceptionSummary(id);
        VelocityContext context = new VelocityContext();
        context.put("summary", summary);
        if (summary.getParentId() != null && summary.getParentId().length() > 0) {
          ExceptionSummary parentSummary = exceptionStore.getExceptionSummary(summary.getParentId());
          context.put("parentSummary", parentSummary);
          context.put("data", exceptionStore.getDataForId(summary.getParentId()));
        } else {
          context.put("data", exceptionStore.getDataForId(id));
        }
        try {
          PrintWriter writer = response.getWriter();
          engine.mergeTemplate(ResourceUtil.getResourcePath(this, "", BROWSE_TEMPLATE), ENCODING, context, writer);
          writer.close();
        } catch (Exception e) {
          throw new RuntimeException("exception, " + e.getMessage(), e);
        }
      }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) {
      HttpSession session = request.getSession();
      int max;
      if (request.getParameter("show") != null) {
        max = Integer.parseInt(request.getParameter("show"));
        session.putValue("show", String.valueOf(max));
      } else if (session.getValue("show") != null) {
        max = Integer.parseInt((String)session.getValue("show"));
      } else {
        max = 50;
      }
      String lastId = (String) session.getValue("lastid");
      session.putValue("lastid", "");
      String[] ids = exceptionStore.getAllIds();
      List summaries = new ArrayList();
      for (int i = 0; i < ids.length && (max == 0 || i < max); i++) {
        ExceptionSummary summary = exceptionStore.getExceptionSummary(ids[i]);
        summaries.add(summary);
      }
      Collections.sort(summaries, COMPARATOR);
      VelocityContext context = new VelocityContext();
      context.put("id", lastId);
      context.put("summaries", summaries);
      context.put("totalCount", new Integer(ids.length));
      context.put("show", new Integer(max));
      context.put("DATE_FORMATTER", DATE_FORMATTER);
      context.put("TIME_FORMATTER", TIME_FORMATTER);
      try {
        PrintWriter writer = response.getWriter();
        engine.mergeTemplate(ResourceUtil.getResourcePath(this, "", LIST_TEMPLATE), ENCODING, context, writer);
        writer.close();
      } catch (Exception e) {
        throw new RuntimeException("exception, " + e.getMessage(), e);
      }
    }
  }
}
