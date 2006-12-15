package org.oa3.auxil.connector.soap;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.transport.http.XFireServlet;
import org.oa3.auxil.connector.http.JettyReadConnector;

/**
 * ReadConnector that exposes a webservice which allows external clients to send it data
 * @author perryj
 *
 */
public class ReadConnectorWebService extends JettyReadConnector implements IStringDataProcessor {

  private static final Log log = LogFactory.getLog(ReadConnectorWebService.class);

  private String serviceName = "openadaptorws";

  public ReadConnectorWebService() {
    super();
  }

  public ReadConnectorWebService(String id) {
    super(id);
  }
  
  public void setServiceName(final String name) {
    serviceName = name;
  }
  
  public void connect() {
    if (getServlet() == null) {
      setServlet(new StringDataProcessorServlet());
    }
    super.connect();
  }

  public void process(String s) {
    enqueue(s);
  }
  
  public String getEndpoint() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("http://");
    buffer.append(getJettyHost());
    buffer.append(":");
    buffer.append(getJettyPort());
    buffer.append(getContext().equals("/") ? "" : getContext());
    buffer.append("/");
    buffer.append(serviceName);
    buffer.append("?wsdl");
    return buffer.toString();
  }

  class StringDataProcessorServlet extends XFireServlet {

    private static final long serialVersionUID = 1L;

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
