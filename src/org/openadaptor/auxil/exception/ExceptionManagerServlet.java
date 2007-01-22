package org.openadaptor.auxil.exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.xfire.client.Client;
import org.openadaptor.util.ResourceUtil;

public class ExceptionManagerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM yy");

  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

  private ExceptionStore exceptionStore;

  /**
   * velocioty template engine, used to render pages based on templates
   */
  private VelocityEngine engine;

  private static final String ENCODING = "UTF-8";
  private static final String BROWSE_TEMPLATE = "browse.html";
  private static final String LIST_TEMPLATE = "list.html";

  /**
   * comparator to sort exceptions for display
   */
  private static final Comparator COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      double key1 = ((ExceptionSummary) o1).getOrderKey();
      double key2 = ((ExceptionSummary) o2).getOrderKey();
      
      if (key1 > key2) {
        return -1;
      } else if (key1 < key2) {
        return +1;
      } else {
        return 0;
      }
    }
  };

  public ExceptionManagerServlet(ExceptionStore exceptionStore) {
    this.exceptionStore = exceptionStore;
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

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_OK);
    
    String action = request.getParameter("action");
    if (action == null || action.equals("list")) {
      checkRole(request, "view");
      list(request, response);
    } else if (action.equals("browse")) {
      checkRole(request, "view");
      browse(request, response);
    } else if (action.equals("delete")) {
      checkRole(request, "admin");
      delete(request, response);
    } else if (action.equals("deleteall")) {
      checkRole(request, "admin");
      deleteall(request, response);
    } else if (action.equals("retry")) {
      checkRole(request, "admin");
      retry(request, response);
    } else {
      throw new RuntimeException("action " + action + " not recognized");
    }
  }
  
  private void checkRole(HttpServletRequest request, String role) {
    if (!request.isUserInRole(role)) {
      throw new RuntimeException("user does not have " + role + " role");
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
        exceptionStore.incrementRetryCount(id);
        id = exceptionStore.store(XMLUtil.toXml(e, id));
        session.setAttribute("lastid", id);
      }
    }
    redirect(response);
  }

  private void delete(HttpServletRequest request, HttpServletResponse response) {
    exceptionStore.delete(request.getParameter("id"));
    redirect(response);
  }

  private void redirect(HttpServletResponse response) {
    try {
      String url = response.encodeRedirectURL("/admin/?action=list");
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
      context.put("trace", exceptionStore.getStackTrace(id));
      if (summary.getParentId() != null && summary.getParentId().length() > 0) {
        ExceptionSummary parentSummary = exceptionStore.getExceptionSummary(summary.getParentId());
        context.put("parentSummary", parentSummary);
        context.put("parentTrace", exceptionStore.getStackTrace(summary.getParentId()));
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
    
    StringBuffer messages = new StringBuffer();
    HttpSession session = request.getSession();
    int max;
    
    // resolve maximum number of excepton to show a page
    String show = resolveRequestParameterOrSessionAttribute(request, "show");
    if (show != null) {
      max = Integer.parseInt(show);
    } else {
      max = 50;
    }
    
    // resolve maximum number of excepton to show a page
    resolveRequestParameterOrSessionAttribute(request, "refreshRate");
    
    // resolve filters
    ExceptionSummary exceptionFilter = new ExceptionSummary();

    String idFilter = resolveRequestParameterOrSessionAttribute(request, "idFilter");
    exceptionFilter.setId(idFilter);
    
    String dateFilter = resolveRequestParameterOrSessionAttribute(request, "dateFilter");
    Date date = null;
    if (dateFilter != null) {
      try {
        date = DATE_FORMATTER.parse(dateFilter);
      } catch (ParseException e) {
        messages.append("unable to parse date filter");
      }
    }
    exceptionFilter.setDate(date);
    
    String fromFilter = resolveRequestParameterOrSessionAttribute(request, "fromFilter");
    exceptionFilter.setFrom(fromFilter);
    
    // get and reset last id, so we can highlight data that corresponds
    // to the previous operation
    String lastId = null;
    if (session != null) {
      lastId = (String) session.getAttribute("lastid");
      session.setAttribute("lastid", "");
    }
    
    // get all the exception summaries
    List allIds = exceptionStore.getIds(exceptionFilter);
    List summaries = new ArrayList();
    int count = 0;
    for (Iterator iter = allIds.iterator(); count++ < max && iter.hasNext();) {
      String id = (String) iter.next();
      summaries.add(exceptionStore.getExceptionSummary(id));
    }
    Collections.sort(summaries, COMPARATOR);

    // populate velocity context
    VelocityContext context = new VelocityContext();
    context.put("id", lastId);
    context.put("summaries", summaries);
    context.put("allIds", allIds);
    context.put("messages", messages.toString());
    context.put("DATE_FORMATTER", DATE_FORMATTER);
    context.put("TIME_FORMATTER", TIME_FORMATTER);
    context.put("SESSION", session);
    
    // create page from template
    try {
      PrintWriter writer = response.getWriter();
      engine.mergeTemplate(ResourceUtil.getResourcePath(this, "", LIST_TEMPLATE), ENCODING, context, writer);
      writer.close();
    } catch (Exception e) {
      throw new RuntimeException("exception, " + e.getMessage(), e);
    }
  }
  
  private String resolveRequestParameterOrSessionAttribute(HttpServletRequest request, String name) {
    HttpSession session = request.getSession();
    String value = null;
    if (request.getParameter(name) != null) {
      value = request.getParameter(name);
      if (value.length() > 0) {
        if (session != null) {
          session.setAttribute(name, value);
        }
      } else {
        if (session != null) {
          session.removeAttribute(name);
        }
        value = null;
      }
    } else if (session != null && session.getAttribute(name) != null) {
      value = (String)session.getAttribute(name);
    }
    return value;
  }
}
