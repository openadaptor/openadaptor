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

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.transport.http.XFireServlet;
import org.openadaptor.auxil.connector.http.ServletContainer;
import org.openadaptor.auxil.connector.soap.WebServiceWriteConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionInitiator;
import org.openadaptor.core.transaction.ITransactionManager;

/**
 * An component which allows adaptor to use a centralised ExceptionManagement
 * service. This component can be used to route exception to the remote web service
 * and it can also expose a web service so that it can receive data to "retry".
 * 
 * @author perryj
 *
 */
public class ExceptionMessageProcessor extends WriteNode implements ITransactionInitiator, IStringRetryProcessor {

  private static final Log log = LogFactory.getLog(ExceptionMessageProcessor.class);

  private WebServiceWriteConnector webServiceWriter = new WebServiceWriteConnector();

  private boolean logExceptions = true;

  private String from;

  private String retryServiceName;
  
  private ServletContainer servletContainer = new ServletContainer();
  
  private String path = "/*";

  private ITransactionManager transactionManager;
  
  private Router router;

  public void setRouter(Router router) {
    this.router = router;
  }

  public ExceptionMessageProcessor() {
    super();
    setConnector(webServiceWriter);
  }

  public ExceptionMessageProcessor(String id) {
    this();
    setId(id);
  }

  public void setEndpoint(final String endpoint) {
    webServiceWriter.setEndpoint(endpoint);
  }

  public void setRemoteMethodName(final String methodName) {
    webServiceWriter.setMethodName(methodName);
  }

  public void setRetryServiceName(final String serviceName) {
    this.retryServiceName = serviceName;
  }

  public void setRetryPort(final int port) {
    servletContainer.setPort(port);
  }

  public void setRetryPath(final String path) {
    this.path = path;
  }
  
  public void setLogExceptions(final boolean log) {
    logExceptions = log;
  }

  public void setFrom(final String from) {
    this.from = from;
  }
  
  public void setServletContainer(final ServletContainer servletContainer) {
    this.servletContainer = servletContainer;
  }

  public ServletContainer getServletContainer() {
    return servletContainer;
  }
  
  public void validate(List exceptions) {
    if (from == null) {
      exceptions.add(new ComponentException("from must be set", this));
    }
    super.validate(exceptions);
  }
  
  public void start() {
    super.start();
    if (retryServiceName != null) {
      servletContainer.addServlet(new StringDataProcessorServlet(), path);
      servletContainer.start();
    }
  }
  
  public void stop() {
    super.stop();
    if (retryServiceName != null) {
      servletContainer.stop();
    }
  }
  
  public Response process(Message msg) {
    long time = (new Date()).getTime();
    Object[] data = msg.getData();
    Object[] exceptionData = new Object[data.length];
    for (int i = 0; i < data.length; i++) {
      MessageException exception = (MessageException) data[i];
      exceptionData[i] = XMLUtil.toXml(exception, from, getRetryEndpoint(), time);
      if (logExceptions) {
        log.error(exception.getMessage(), exception.getException());
      }
    }
    webServiceWriter.deliver(exceptionData);
    log.info(getId() + " sent " + msg.getData().length + " MessageException(s) to remote webservice");
    return Response.EMPTY;
  }

  public void setTransactionManager(ITransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void retry(String componentId, String data) {
    ITransaction transaction = null;
    if (transactionManager != null) {
      transaction = transactionManager.getTransaction();
    }
    Message msg = new Message(data, this, transaction);
    try {
      router.process(msg, componentId);
    } catch (MessageException e) {
      throw new RuntimeException("retry failed again, " + e.getException().getMessage(), e.getException());
    }
  }
  
  public String getRetryEndpoint() {
    if (retryServiceName != null) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("http://");
      buffer.append(servletContainer.getHost());
      buffer.append(":");
      buffer.append(servletContainer.getPort());
      buffer.append(servletContainer.getContext().equals("/") ? "" : servletContainer.getContext());
      buffer.append(path.replaceAll("\\*", ""));
      buffer.append(retryServiceName);
      buffer.append("?wsdl");
      return buffer.toString();
    } else {
      return "";
    }
  }

  class StringDataProcessorServlet extends XFireServlet {

    private static final long serialVersionUID = 1L;

    public void init() throws ServletException
    {
      super.init();
      ObjectServiceFactory factory = new ObjectServiceFactory(getXFire().getTransportManager(), null);
      Service service = factory.create(IStringRetryProcessor.class, retryServiceName, null, null);
      log.info(getId() + " created webservice " + getRetryEndpoint());
      service.setInvoker(new BeanInvoker(ExceptionMessageProcessor.this));
      getController().getServiceRegistry().register(service);
    }
  }

}
