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

package org.oa3.auxil.connector.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.IReadConnector;
import org.oa3.core.connector.QueuingReadConnector;
import org.oa3.core.exception.ComponentException;
import org.oa3.util.ResourceUtils;

/**
 * exposes IDataProcessor interface as remote interface. Allowing rmi clients to send data for
 * processing.
 * 
 * @author perryj
 *
 */
public class RMIReadConnector extends QueuingReadConnector implements IReadConnector {

  private static final Log log = LogFactory.getLog(RMIReadConnector.class);

  private IRemoteDataProcessor rmiServer;

  private String rmiHost;

  private int rmiPort = 1099;

  private String rmiName;

  private String url;
  
  protected Registry registry;

  private boolean createRegistry = false;

  public void setRmiHost(String rmiHost) {
    this.rmiHost = rmiHost;
  }

  public void setRmiName(String rmiName) {
    this.rmiName = rmiName;
  }

  public void setRmiPort(int rmiPort) {
    this.rmiPort = rmiPort;
  }

  public void setCreateRegistry(boolean createRegistry) {
    this.createRegistry  = createRegistry;
  }
  
  public void validate(List exceptions) {
    if (rmiName == null) {
      exceptions.add(new ComponentException("rmiName is not set", this));
    }
  }
  
  public void connect() {
    
    if (rmiHost != null) {
      url = "rmi://" + rmiHost + ":" + rmiPort + "/" + rmiName;
    } else {
      url = "rmi://" + ResourceUtils.getLocalHostname() + ":" + rmiPort + "/" + rmiName;
    }

    if (createRegistry) {
      try {
        log.info("starting rmi registry");
        registry = LocateRegistry.createRegistry(rmiPort);
      } catch (RemoteException e) {
        throw new ComponentException("failed to create rmi registry", e, this);
      }
    }

    try {
      rmiServer = new RMIDataProcessor();
    } catch (RemoteException e) {
      throw new ComponentException("failed to create rmi server", e, this);
    }

    try {
      log.info("binding rmi server to " + url);
      Naming.bind(url, rmiServer);
    } catch (Exception e) {
      throw new ComponentException("failed to bind " + url, e, this);
    }
  }

  public void disconnect() {
    try {
      Naming.unbind(url);
    } catch (Exception e) {
      throw new ComponentException("failed to unbind " + url, e, this);
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

    public Object[] process(Object data) {
      enqueue(data);
      return null;
    }

    public void reset(Object context) {
    }

    public void validate(List exceptions) {
    }
    
  }
}
