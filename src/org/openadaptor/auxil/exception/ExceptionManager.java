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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.openadaptor.auxil.connector.http.ServletContainer;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;
import org.openadaptor.util.NetUtil;

public class ExceptionManager {

  private static final Log log = LogFactory.getLog(ExceptionManager.class);

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
    log.info("admin interface at http://" + NetUtil.getLocalHostname() + ":" + port + "/admin");
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
    webService.addServlet(new ExceptionManagerServlet(exceptionStore), "/admin/*");

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

}
