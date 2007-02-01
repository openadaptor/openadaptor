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

package org.openadaptor.auxil.connector.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.util.NetUtil;

/**
 * exposes IDataProcessor interface as remote interface. Allowing rmi clients to send data for
 * processing.
 * 
 * @author perryj
 *
 */
public class RMIReadConnector extends QueuingReadConnector implements IReadConnector {

  private static final Log log = LogFactory.getLog(RMIReadConnector.class);

  public static final int DEFAULT_PORT = 1099;

  public static final String DEFAULT_NAME = "RMIReadConnector";

  private IRemoteDataProcessor rmiServer;

  private String registryHost;

  private int registryPort = DEFAULT_PORT;

  private String serviceName = DEFAULT_NAME;

  private boolean createRegistry = false;

  public RMIReadConnector() {
    super();
  }
  
  public RMIReadConnector(String id) {
    super(id);
  }
  
  public void setRegistryHost(String registryHost) {
    this.registryHost = registryHost;
  }

  public void setServiceName(String name) {
    this.serviceName = name;
  }

  public void setRegistryPort(int registryPort) {
    this.registryPort = registryPort;
  }

  public void setCreateRegistry(boolean createRegistry) {
    this.createRegistry  = createRegistry;
  }
  
  public void validate(List exceptions) {
    if (serviceName == null) {
      exceptions.add(new ValidationException("rmiName is not set", this));
    }
  }
  
  public void connect() {
    
    if (createRegistry) {
      try {
        log.info("starting rmi registry on port " + registryPort);
        LocateRegistry.createRegistry(registryPort);
        registryHost = NetUtil.getLocalHostname();
      } catch (RemoteException e) {
        throw new ConnectionException("failed to create rmi registry", e, this);
      }
    }

    try {
      rmiServer = new RMIDataProcessor();
    } catch (RemoteException e) {
      throw new ConnectionException("failed to create rmi server", e, this);
    }

    try {
      log.info("binding rmi server " + serviceName + " on " + registryHost + ":" + registryPort);
      LocateRegistry.getRegistry(registryHost, registryPort).rebind(serviceName, rmiServer);
    } catch (Exception e) {
      rmiServer = null;
      throw new ConnectionException("failed to bind ", e, this);
    }
  }

  public void disconnect() {
    try {
      LocateRegistry.getRegistry(registryHost, registryPort).unbind(serviceName);
    } catch (Exception e) {
      throw new ConnectionException("failed to unbind", e, this);
    } finally {
      rmiServer = null;
    }
  }

  public Object getReaderContext() {
    return null;
  }

  public boolean isDry() {
    return false;
  }

  class RMIDataProcessor extends UnicastRemoteObject implements IRemoteDataProcessor {

    private static final long serialVersionUID = 1L;

    protected RMIDataProcessor() throws RemoteException {
      super();
    }

    public void process(Serializable data) {
      enqueue(data);
    }
  }
}
