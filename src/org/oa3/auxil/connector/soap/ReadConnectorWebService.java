package org.oa3.auxil.connector.soap;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.transport.http.XFireServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.oa3.auxil.connector.QueuingReadConnector;
import org.oa3.core.exception.ComponentException;
import org.oa3.util.ResourceUtils;

public class ReadConnectorWebService extends QueuingReadConnector implements IStringDataProcessor {

  private static final Log log = LogFactory.getLog(ReadConnectorWebService.class);

  private static final String CONTEXT = "soap";

  private String serviceName = "openadaptorws";

  private int localJettyPort = 8080;
  private Server server;
  
  public ReadConnectorWebService() {
  }

  public ReadConnectorWebService(String id) {
    super(id);
  }
  
  public void setServiceName(final String name) {
    serviceName = name;
  }
  
  public void setJettyServer(final Server server) {
    this.server = server;
    localJettyPort = 0;
  }
  
  public void setLocalJettyPort(final int port) {
    if (server != null) {
      throw new ComponentException("attempt to configure jettyServer and localJettyPort", this);
    }
    localJettyPort = port;
  }
  
  public void connect() {
    if (localJettyPort > 0 && server == null) {
      server = new Server(localJettyPort);
      try {
        server.start();
      } catch (Exception e) {
        log.error("failed to start jetty server", e);
        throw new ComponentException("failed to start local jetty server", this);
      }
    }
    Context root = new Context(server,"/" + CONTEXT,Context.SESSIONS);
    root.addServlet(new ServletHolder(new StringDataProcessorServlet()), "/*");
  }

  public void disconnect() {
    if (localJettyPort > 0 && server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        log.error("failed to stop jetty server", e);
      }
    }
  }

  public void process(String s) {
    if (log.isDebugEnabled()) {
      log.debug(getId() + " received data [" + s + "]"); 
    }
    enqueue(s);
  }
  
  public String getEndpoint() {
    return "http://" + ResourceUtils.getLocalHostname() + ":" + localJettyPort + "/" + CONTEXT + "/" + serviceName + "?wsdl";
  }
  
  class StringDataProcessorServlet extends XFireServlet {
    public void init() throws ServletException
    {
      super.init();
      ObjectServiceFactory factory = new ObjectServiceFactory(getXFire().getTransportManager(), null);
      Service service = factory.create(IStringDataProcessor.class, serviceName, null, null);
      log.info(getId() + " created webservice " + getEndpoint());
      service.setInvoker(new BeanInvoker(ReadConnectorWebService.this));
      getController().getServiceRegistry().register(service);
    }
  }

}
