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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.openadaptor.auxil.connector.soap.ReadConnectorWebService;
import org.openadaptor.auxil.connector.soap.WebServiceWriteConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.adaptor.AdaptorInpoint;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.transaction.ITransaction;

/**
 * An adaptor component which allows adaptor to use a centralised ExceptionManagement
 * service. This component can be used to route exception to the remote web service
 * and it can also expose a web service so that it can receive data to "retry".
 * 
 * As such it can act as both an AdaptorInpoint and AdadptorOutpoint.
 * 
 * @author perryj
 *
 */
public class ExceptionInOutpoint extends AdaptorInpoint {

  private static final Log log = LogFactory.getLog(ExceptionInOutpoint.class);

  private ReadConnectorWebService localWebService;

  private WebServiceWriteConnector webServiceWriter;

  private boolean logExceptions = true;

  private String from;

  private String replyTo = "";

  public ExceptionInOutpoint() {
    super();
  }

  public ExceptionInOutpoint(String id) {
    this();
    setId(id);
  }

  private WebServiceWriteConnector getWriter() {
    if (webServiceWriter == null) {
      webServiceWriter = new WebServiceWriteConnector();
    }
    return webServiceWriter;
  }

  private ReadConnectorWebService getReader() {
    if (localWebService == null) {
      localWebService = new ReadConnectorWebService();
      setConnector(localWebService);
      localWebService.setServiceName("ExceptionProcessor");
    }
    return localWebService;
  }

  public void setEndpoint(final String endpoint) {
    getWriter().setEndpoint(endpoint);
  }

  public void setRemoteMethodName(final String methodName) {
    getWriter().setMethodName(methodName);
  }

  public void setJettyServer(final Server server) {
    getReader().setJettyServer(server);
  }

  public void setLocalJettyPort(final int port) {
    getReader().setLocalJettyPort(port);
  }

  public void setServiceName(final String name) {
    getReader().setServiceName(name);
  }
  
  public void setLogExceptions(final boolean log) {
    logExceptions = log;
  }

  public void setFrom(final String from) {
    this.from = from;
  }
  
  //
  // override cus read connector is optional
  //
  
  public void validate(List exceptions) {
    if (localWebService != null) {
      super.validate(exceptions);
    }
    if (from == null) {
      exceptions.add(new ComponentException("from must be set", this));
    }
  }
  
  //
  // override cus read connector is optional
  //
  
  public void run() {
    if (localWebService != null) {
      super.run();
    } else {
      setState(State.STOPPED);
    }
  }
  
  //
  // override start to catch any exceptions and connect webServiceWriter
  // 

  public void start() {
    try {
      if (webServiceWriter != null) {
        webServiceWriter.connect();
      }
    } catch (RuntimeException ex) {
      log.error(getId() + " failed to start, continuing anyway", ex);
    }
    try {
      if (localWebService != null) {
        super.start();
        replyTo = localWebService.getEndpoint();
       }
    } catch (RuntimeException ex) {
      log.error(getId() + " failed to start, continuing anyway", ex);
    }
  }

  //
  // override
  //
  // called by the inpoint thread, this reads retry data that has been queued
  // by local web service, The message has a flag set so that nothing upstream
  // should catch exceptions. This impl catches exceptions sets ErrorOrException
  // this triggers a rollback without stopping the inpoint.
  //

  protected boolean getDataAndProcess(ITransaction transaction) {
    Object[] data = getNext();
    if (data != null) {
      Message msg = new Message(data, this, transaction);
      msg.setDisableExceptionCapture(true);
      try {
        callChainedMessageProcessor(msg);
      } catch (Throwable t) {
        log.error(getId() + "uncaught exception", t);
        transaction.setErrorOrException(t);
      }
    }
    return data != null;
  }

  //
  // route data to the remote web service
  //

  public Response process(Message msg) {
    long time = (new Date()).getTime();
    Object[] data = msg.getData();
    Object[] exceptionData = new Object[data.length];
    for (int i = 0; i < data.length; i++) {
      MessageException exception = (MessageException) data[i];
      exceptionData[i] = MessageExceptionXmlConverter.toXml(exception, from, replyTo, time);
      if (logExceptions) {
        log.error(exception.getMessage(), exception.getException());
      }
    }
    webServiceWriter.deliver(exceptionData);
    log.info(getId() + " sent " + msg.getData().length + " MessageException(s) to remote webservice");
    return Response.EMPTY;
  }

  public void setId(String id) {
    super.setId(id);
    if (localWebService != null) {
      localWebService.setId(id);
    }
    if (webServiceWriter != null) {
      webServiceWriter.setId(id);
    }
  }
}
