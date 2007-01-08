package org.oa3.auxil.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.oa3.auxil.connector.soap.ReadConnectorWebService;

public class ExceptionManager {

  private static final Log log = LogFactory.getLog(ExceptionManager.class);

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy");

  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

  private ExceptionStore exceptionStore;

  private ReadConnectorWebService webService;

  private Server server;

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
    webService = new ReadConnectorWebService("WebService");
    webService.setJettyServer(server);
    webService.setServiceName("ExceptionManager");
    webService.setPath("/soap/*");
    webService.setTransacted(false);
    webService.connect();

    // create servlet for browsing exceptions
    webService.getRootContext().addServlet(new ServletHolder(new MyServlet()), "/*");

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
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      response.setStatus(HttpServletResponse.SC_OK);
      String action = request.getParameter("action");
      if (action == null || action.equals("list")) {
        list(request, response);
      } else if (action.equals("browse")) {
        browse(request, response);
      } else if (action.startsWith("delete")) {
        delete(request, response);
      }else if (action.startsWith("resend")) {
        resend(request, response);
      }
    }

    private void resend(HttpServletRequest request, HttpServletResponse response) {
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) {
      String id = request.getParameter("id");
      PrintWriter writer = null;
      try {
        writer = response.getWriter();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (id != null) {
        exceptionStore.delete(id);
      } else {
        exceptionStore.deleteAll();
      }
      response.setContentType("text/html");
      writer.write("<a href=\"?action=list\">back</a>");
      writer.close();
    }

    private void browse(HttpServletRequest request, HttpServletResponse response) {
      String id = request.getParameter("id");
      PrintWriter writer = null;
      try {
        writer = response.getWriter();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (id != null) {
        String exception = exceptionStore.getExceptionForId(id);
        response.setContentType("text/xml");
        writer.write(exception);
      } else {
        writer.write("no id specified");
      }
      writer.close();
    }

    private void list(HttpServletRequest request, HttpServletResponse response) {
      response.setContentType("text/html");
      int max = getIntParameter(request, "show", 50);
      String[] ids = exceptionStore.getAllIds(max);
      try {
        PrintWriter writer = response.getWriter();
        if (ids.length == 0) {
          writer.write("no exceptions");
        } else {
          String prevDate = null;
          writer.write("<table>");
          for (int i = 0; i < ids.length; i++) {
            try {
              ExceptionSummary summary = exceptionStore.getExceptionSummary(ids[i]);
              String date = DATE_FORMATTER.format(summary.getDate());
              if (!date.equals(prevDate)) {
                writer.write("</table>");
                writer.write("<p><b>" + date + "</b></d>");
                writer.write("<table width=\"80%\">");
                writer.write("<tr><td><u>ID</u></td><td><u>FROM</u></td><td><u>TIME</u></td><td><u>MESSAGE</u></td>");
                writer.write("<td><a href=\"?action=resendall\">RESEND ALL</a></td>");
                writer.write("<td><a href=\"?action=deleteall\">DELETE ALL</a></td>");
              }
              writer.write("<tr>");
              writer.write("<td><a href=\"?action=browse&id=" + ids[i] + "\">" + ids[i] + "</a></td>");
              writer.write("<td>" + summary.getFrom() + "</td>");
              writer.write("<td>" + TIME_FORMATTER.format(summary.getDate()) + "</td>");
              writer.write("<td>" + summary.getMessage() + "</td>");
              writer.write("<td><a href=\"?action=resend&id=" + ids[i] + "\">resend</a></td>");
              writer.write("<td><a href=\"?action=delete&id=" + ids[i] + "\">delete</a></td></tr>");
              prevDate = date;
            } catch (RuntimeException e) {
              e.printStackTrace();
            }
          }
          writer.write("</table>");
        }
        writer.close();
      } catch (IOException e) {
        log.error(e);
      }
    }

    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
      String s = request.getParameter(name);
      try {
        return s != null ? Integer.parseInt(s) : defaultValue;
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
  }
}
