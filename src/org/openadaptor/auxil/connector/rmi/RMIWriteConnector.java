/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.connector.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.lifecycle.LifecycleComponent;

public class RMIWriteConnector extends LifecycleComponent implements IWriteConnector {

  private static final Log log = LogFactory.getLog(RMIWriteConnector.class);
  
  private IRemoteDataProcessor rmiServer;
  
  private String registryHost;
  private int registryPort = RMIReadConnector.DEFAULT_PORT;
  private String serviceName = RMIReadConnector.DEFAULT_NAME;

  /**
   * Default Constructor
   */
  public RMIWriteConnector() {
    super();
  }
  
  public RMIWriteConnector(String id) {
    super(id);
  }

  /**
   * Set registry host name
   *
   * @param registryHost
   */
  public void setRegistryHost(String registryHost) {
    this.registryHost = registryHost;
  }

  /**
   * Set Service name
   *
   * @param name Service name
   */
  public void setServiceName(String name) {
    this.serviceName = name;
  }

  /**
   * Set Registry port number
   *
   * @param registryPort Registry port
   */
  public void setRegistryPort(int registryPort) {
    this.registryPort = registryPort;
  }

  /**
   * Look up service on Registry Host and attempt to connect to service
   */
  public void connect() {
    try {
      log.info(getId() + " looking up " + serviceName + " on " + registryHost + ":" + registryPort);
      rmiServer = (IRemoteDataProcessor) LocateRegistry.getRegistry(registryHost, registryPort).lookup(serviceName);
    } catch (Exception e) {
      throw new ConnectionException("failed to lookup rmi server", e, this);
    }
  }

  /**
   * 
   *
   * @param data
   * @return Object data
   */
  public Object deliver(Object[] data) {
    try {
      for (int i = 0; i < data.length; i++) {
        rmiServer.process((Serializable)data[i]);
      }
    } catch (RemoteException e) {
      throw new ConnectionException("remote exception", e, this);
    }
    return null;
  }

  public void disconnect() {
    rmiServer = null;
  }
}
