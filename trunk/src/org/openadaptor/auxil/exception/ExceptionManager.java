/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.exception;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.SessionHandler;
import org.openadaptor.auxil.connector.http.ServletContainer;
import org.openadaptor.auxil.connector.smtp.SMTPConnection;
import org.openadaptor.auxil.connector.smtp.SMTPWriteConnector;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;
import org.openadaptor.core.transaction.ITransactionalResource;
import org.openadaptor.util.NetUtil;
import org.quartz.CronTrigger;

public class ExceptionManager {

  private static final String DEFAULT_MAIL_CRON = "0 0,15,30,45 7-17 ? * MON-FRI";

  private static final String DEFAULT_STATUS_CRON = "0 0 8 ? * MON-FRI";

  private static final Log log = LogFactory.getLog(ExceptionManager.class);

  private Server server;
  
  private ExceptionStore exceptionStore;

  private ReadConnectorWebService webService;
  
  private SMTPWriteConnector mailer;
  
  private Date lastMailTime;
  
  private CronTrigger mailCronTrigger = new CronTrigger();
  
  private StringBuffer mailBody = new StringBuffer();
  
  private Timer timer = new Timer();
  
  private TimerTask nextScheduledTask;
  
  private CronTrigger statusCronTrigger = new CronTrigger();
  
  private int purgeLimitDays = 7;
  
  public ExceptionManager() {
    setCronExpression(mailCronTrigger, DEFAULT_MAIL_CRON);
    setCronExpression(statusCronTrigger, DEFAULT_STATUS_CRON);
  }

  private void setCronExpression(CronTrigger trigger, String expression) {
    try {
      trigger.setCronExpression(expression);
    } catch (ParseException e) {
      throw new RuntimeException("cron expression " + expression + " is illegal, " + e.getMessage(), e);
    }
  }
  
  protected String[] processArgs(String[] args) {

    int port = 8080;
    String realmFile = null;
    SMTPConnection mailConnection = new SMTPConnection();
    mailConnection.setMailHost("mailhost");
    mailConnection.setBodyPreface("DO NOT REPLY TO THIS MESSAGE");
    
    boolean unsecured = false;
    
    HashUserRealm realm = createDefaultRealm();
    
    ArrayList unprocessedArgs = new ArrayList();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-port")) {
        port = Integer.parseInt(args[++i]);
      } else if (args[i].equalsIgnoreCase("-unsecured")) {
        unsecured = true;
      } else if (args[i].equalsIgnoreCase("-realm")) {
        realmFile = args[++i];
      } else if (args[i].equalsIgnoreCase("-purge")) {
        setPurgeLimitDays(Integer.parseInt(args[++i]));
      } else if (args[i].equalsIgnoreCase("-mail.to")) {
        mailConnection.setTo(args[++i]);
      } else if (args[i].equalsIgnoreCase("-mail.from")) {
        mailConnection.setFrom(args[++i]);
      } else if (args[i].equalsIgnoreCase("-mail.host")) {
        mailConnection.setMailHost(args[++i]);
      } else if (args[i].equalsIgnoreCase("-mail.port")) {
        mailConnection.setMailHostPort(args[++i]);
      } else if (args[i].equalsIgnoreCase("-mail.subject")) {
        mailConnection.setSubject(args[++i]);
      } else if (args[i].equalsIgnoreCase("-mail.cc")) {
        mailConnection.setCc(args[++i]);
      } else if (args[i].equalsIgnoreCase("-mail.cron")) {
        setCronExpression(mailCronTrigger, args[++i]);
      } else if (args[i].equalsIgnoreCase("-status.cron")) {
        setCronExpression(statusCronTrigger, args[++i]);
      } else {
        unprocessedArgs.add(args[i]);
      }
    }

    if (!unsecured) {
      if (realmFile == null) {
        realm = createDefaultRealm();
      } else {
        try {
          realm = new HashUserRealm("Realm", realmFile);
        } catch (IOException e) {
          throw new RuntimeException("failed to create user realm, " + e.getMessage(), e);
        }
      }
    }
    setServer(createDefaultServer(port, realm));
    
    if (mailConnection.getTo() != null || mailConnection.getCc() != null) {
      SMTPWriteConnector mailer = new SMTPWriteConnector();
      mailer.setSmtpConnection(mailConnection);
      setMailer(mailer);
    }
    
    return (String[]) unprocessedArgs.toArray(new String[unprocessedArgs.size()]);
  }

  protected static String getUsageString() {
    String s = "";
    s += "  [-port <num>]       http port number (defaults to 8080)\n";
    s += "  [-purge <num days>] number of days to store exceptions, defaults to 7, set zero for no purge\n";
    s += "  [-realm <file>]     jetty realm file (defaults to test: password,view & testadmin: password,view,admin)\n";
    s += "  [-unsecured]        no security\n";
    s += "  [-mail.to]          to list (comma separated smtp addresses) for mail notification\n";
    s += "  [-mail.subject]     subject for mail notification\n";
    s += "  [-mail.host]        mail host for mail notification, defaults to mailhost\n";
    s += "  [-mail.cc]          cc list (comma separated smtp addresses) for mail notification\n";
    s += "  [-mail.port]        mail host port for mail notification\n";
    s += "  [-mail.frequency]   cron expression that controls frquency of mail notification\n";
    s += "                      defaults to every 15 mins, 7am - 6pm weekdays";
    return s;
  }

  public void setExceptionStore(final ExceptionStore exceptionStore) {
    this.exceptionStore = exceptionStore;
  }

  public void setServer(final Server server) {
    this.server = server;
  }
  
  protected static HashUserRealm createDefaultRealm() {
    HashUserRealm realm = new HashUserRealm();
    realm.put("test", "password");
    realm.addUserToRole("test", "view");
    realm.put("testadmin", "password");
    realm.addUserToRole("testadmin", "view");
    realm.addUserToRole("testadmin", "admin");
    return realm;
  }
  
  protected static Server createDefaultServer(int port, HashUserRealm realm) {
    SecurityHandler securityHandler = null;
    
    if (realm != null) {
      Constraint viewConstraint = new Constraint();
      viewConstraint.setName(Constraint.__BASIC_AUTH);
      viewConstraint.setRoles(new String[] {"view"});
      viewConstraint.setAuthenticate(true);
      
      ConstraintMapping viewMapping = new ConstraintMapping();
      viewMapping.setConstraint(viewConstraint);
      viewMapping.setPathSpec("/admin/*");
      
      securityHandler = new SecurityHandler();
      securityHandler.setUserRealm(realm);
      securityHandler.setConstraintMappings(new ConstraintMapping[] {viewMapping});
    }
    
    Context context = new Context();
    context.setContextPath("/");
    if (securityHandler != null) {
      context.addHandler(new SessionHandler());
    }
    context.addHandler(securityHandler);
    
    Server server = new Server(port);
    server.setHandlers(new Handler[] {context, new DefaultHandler()});
    
    return server;
  }
  
  public void run() {

    if (mailer != null) {
      resetMailBody();
      mailer.connect();
      housekeeping();
    }
    
    // start jetty
    boolean started = server.isStarted() || server.isStarting();
    if (!started) {
      try {
        server.start();
      } catch (Exception e) {
        log.error("failed to start jetty server", e);
        throw new RuntimeException("failed to start local jetty server", e);
      }
    }

    // create web service to receive exceptions and store
    ServletContainer servletContainer = new ServletContainer(server);
    webService = new ReadConnectorWebService("WebService");
    webService.setServletContainer(servletContainer);
    webService.setServiceName("ExceptionManager");
    webService.setPath("/soap/*");
    webService.connect();

    // create servlet for browsing and managing exceptions
    webService.addServlet(new ExceptionManagerServlet(exceptionStore), "/admin/*");
    log.info("admin interface at " + getAdminUrl());

    // receive incoming exceptions and store them
    while (!webService.isDry()) {
      ITransactionalResource txnResource = (ITransactionalResource) webService.getResource();
      try {
        Object[] data = webService.next(1000);
        for (int i = 0; data != null && i < data.length; i++) {
          String id = exceptionStore.store(data[i].toString());
          if (mailer != null) {
            mailNotification(id, data[i]);
          }
          log.debug("stored new exception");
        }
        txnResource.commit();
      } catch (RuntimeException e) {
        txnResource.rollback(e);
      }
    }

    if (started) {
      try {
        server.stop();
      } catch (Exception e) {
      }
    }
  }

  private void mailNotification(String id, Object data) {
    synchronized (mailBody) {
      mailBody.append(getAdminUrl() + "?action=browse&id=" + id + "\n");
    }
    
    if (nextScheduledTask == null) {
      nextScheduledTask = new TimerTask() {
        public void run() {
          synchronized (mailBody) {
            mailer.deliver(new Object[] {mailBody.toString()});
            lastMailTime = new Date();
            nextScheduledTask = null;
            resetMailBody();
          }
        }
      };
      timer.schedule(nextScheduledTask, getNextMailTime());
    }
  }

  private void resetMailBody() {
    synchronized (mailBody) {
      mailBody.setLength(0);
      mailBody.append("To see all exceptions, goto " + getAdminUrl() + "\n");
      mailBody.append("To see individual exceptions, use the links below...\n");
    }
  }

  private Date getNextMailTime() {
    return lastMailTime != null ? mailCronTrigger.getFireTimeAfter(lastMailTime) : new Date();
  }

  private String getAdminUrl() {
    String url = "http://" + NetUtil.getLocalHostAddress() + ":" + getServerPort(server) + "/admin";
    return url;
  }

  private int getServerPort(Server server) {
    Connector[] connectors = server.getConnectors();
    for (int i = 0; i < connectors.length; i++) {
      if (connectors[i] instanceof SocketConnector) {
        return ((SocketConnector)connectors[i]).getPort();
      }
    }
    return 0;
  }

  public void setMailCronTrigger(CronTrigger mailCronTrigger) {
    this.mailCronTrigger = mailCronTrigger;
  }

  public void setMailer(SMTPWriteConnector mailer) {
    this.mailer = mailer;
  }
  
  public void housekeeping() {
    if (purgeLimitDays > 0) {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR, -1 * purgeLimitDays);
      log.info("purging exceptions older than " + cal.getTime());
      int count = exceptionStore.purge(cal.getTime());
      if (count > 0) {
        log.info("purged " + count + " exceptions");
        mailer.deliver(new Object[] {"purged " + count + " exceptions in the exception manager, " + getAdminUrl()});
      }
    }
    List ids = exceptionStore.getIds(new ExceptionSummary());
    if (ids.size() > 0) {
      mailer.deliver(new Object[] {"There are " + ids.size() + " exceptions in the exception manager, " + getAdminUrl()});
    }
    Date nextTime = statusCronTrigger.getFireTimeAfter(new Date());
    timer.schedule(new TimerTask() {
      public void run() {
        housekeeping();
      }
    }, nextTime);
  }

  public void setPurgeLimitDays(int purgeLimitDays) {
    this.purgeLimitDays = purgeLimitDays;
  }
}
