/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */

package org.openadaptor.core.adaptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.ILifecycleComponentContainer;
import org.openadaptor.core.lifecycle.ILifecycleComponentManager;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.router.Pipeline;
import org.openadaptor.core.transaction.ITransactionManager;
import org.openadaptor.core.transaction.TransactionManager;
import org.openadaptor.util.Application;

public class Adaptor extends Application implements IMessageProcessor, ILifecycleComponentManager, Runnable,
    AdaptorMBean {

  private static final Log log = LogFactory.getLog(Adaptor.class);

  private IMessageProcessor processor;

  private List inpoints;

  private List components;

  private boolean runInpointsInCallingThread = false;

  private Thread[] inpointThreads = new Thread[0];

  private State state = State.CREATED;

  private ITransactionManager transactionManager;

  private boolean started = false;
  
  private int exitCode = 0;

  private IMessageProcessor exceptionProcessor;
  
  private AdaptorRunConfiguration runConfiguration;

  public Adaptor() {
    super();
    transactionManager = new TransactionManager();
    inpoints = new ArrayList();
    components = new ArrayList();
  }

  public void setMessageProcessor(final IMessageProcessor processor) {
    if (this.processor != null) {
      throw new RuntimeException("message processor has already been set");
    }
    this.processor = processor;
    if (processor instanceof ILifecycleComponentContainer) {
      log.debug("MessageProcessor is also a component container. Registering with processor.");
      ((ILifecycleComponentContainer) processor).setComponentManager(this);
    }
  }

  public void setRunInpointsInCallingThread(final boolean runInpointsInCallingThread) {
    this.runInpointsInCallingThread = runInpointsInCallingThread;
  }

  public void setTransactionManager(final ITransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void setRunConfiguration(AdaptorRunConfiguration config) {
    runConfiguration = config;
  }
  
  public ITransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void register(ILifecycleComponent component) {
    if (started) {
      throw new RuntimeException("Cannot register component with running adaptor");
    }
    components.add(component);
    if (component instanceof IAdaptorInpoint) {
      log.debug("inpoint " + component.getId() + " registered with adaptor");
      inpoints.add(component);
      ((IAdaptorInpoint) component).setAdaptor(this);
    } else {
      log.debug("component " + component.getId() + " registered with adaptor");
    }
  }

  public ILifecycleComponent unregister(ILifecycleComponent component) {
    ILifecycleComponent match = null;
    if (started) {
      throw new RuntimeException("Cannot unregister component from running adaptor");
    }
    if (components.remove(component)) {
      match = component;
      inpoints.remove(component);
    }
    return match;
  }

  public Response process(Message msg) {
    return processor.process(msg);
  }

  public void start() {
    exitCode = 0;
    
    if (started) {
      throw new RuntimeException("adaptor is already started");
    }
    
    try {
      validate();
      started = true;
      startNonInpoints();
      startInpoints();
  
      if (runInpointsInCallingThread) {
        runInpoint();
      } else {
        runInpointThreads();
      }
  
      log.info("waiting for inpoints to stop");
      waitForInpointsToStop();
      log.info("all inpoints are stopped");
      stopNonInpoints();
    } catch (Throwable ex) {
      log.error("failed to start adaptor", ex);
      exitCode = 1;
    } finally {
      started = false;
    }
    
    if (getExitCode() != 0) {
      log.fatal("adaptor exited with " + getExitCode());
    }
  }

  public void stop() {
    stopInpoints();
    stopNonInpoints();
    for (Iterator iter = components.iterator(); iter.hasNext();) {
      ILifecycleComponent component = (ILifecycleComponent) iter.next();
      component.waitForState(State.STOPPED);
    }
  }

  public void stopNoWait() {
    stopInpoints();
    stopNonInpoints();
  }

  public void interrupt() {
    for (int i = 0; i < inpointThreads.length; i++) {
      inpointThreads[i].interrupt();
    }
  }

  public void validate(List exceptions) {

    if (inpoints.isEmpty()) {
      exceptions.add(new Exception("no inpoints"));
    }

    if (runInpointsInCallingThread && inpoints.size() > 1) {
      exceptions.add(new Exception("runInpointsInCallingThread == true but multiple inpoints"));
    }

    for (Iterator iter = components.iterator(); iter.hasNext();) {
      IMessageProcessor processor = (IMessageProcessor) iter.next();
      if (processor instanceof ILifecycleComponent) {
        try {
          ((ILifecycleComponent) processor).validate(exceptions);
        } catch (Exception e) {
          log.error("validation exception for " + processor.toString() + " : ", e);
          exceptions.add(e);
        }
      }
    }
  }

  public void run() {
    if (runConfiguration != null) {
      runConfiguration.run(this);
    } else {
      start();
    }
  }

  public State getState() {
    return state;
  }

  public int getExitCode() {
    int exitCode = this.exitCode;
    for (Iterator iter = inpoints.iterator(); iter.hasNext();) {
      IAdaptorInpoint inpoint = (IAdaptorInpoint) iter.next();
      exitCode += inpoint.getExitCode();
    }
    return exitCode;
  }

  private void validate() {
    List exceptions = new ArrayList();
    validate(exceptions);
    if (exceptions.size() > 0) {
      for (Iterator iter = exceptions.iterator(); iter.hasNext();) {
        Exception exception = (Exception) iter.next();
        log.error("validation exception", exception);
      }
      throw new RuntimeException("adaptor validation failed");
    }
  }

  private void runInpoint() {
    if (inpoints.size() != 1) {
      throw new RuntimeException("cannot run inpoint directly as there are " + inpoints.size() + " inpoints");
    }
    IAdaptorInpoint inpoint = (IAdaptorInpoint) inpoints.get(0);
    setState(State.RUNNING);
    inpoint.run();
  }

  private void setState(final State state) {
    this.state = state;
  }

  private void waitForInpointsToStop() {
    for (Iterator iter = inpoints.iterator(); iter.hasNext();) {
      IAdaptorInpoint inpoint = (IAdaptorInpoint) iter.next();
      inpoint.waitForState(State.STOPPED);
    }
  }

  private void runInpointThreads() {
    inpointThreads = new Thread[inpoints.size()];
    int i = 0;
    for (Iterator iter = inpoints.iterator(); iter.hasNext(); i++) {
      IAdaptorInpoint inpoint = (IAdaptorInpoint) iter.next();
      inpointThreads[i] = new Thread(inpoint);
      if (inpoint.getId() != null) {
        inpointThreads[i].setName(inpoint.toString());
      }
    }
    setState(State.RUNNING);
    for (int j = 0; j < inpointThreads.length; j++) {
      inpointThreads[j].start();
    }
  }

  private void startInpoints() {
    for (Iterator iter = inpoints.iterator(); iter.hasNext();) {
      IAdaptorInpoint inpoint = (IAdaptorInpoint) iter.next();
      inpoint.start();
    }
  }

  private void stopInpoints() {
    for (Iterator iter = inpoints.iterator(); iter.hasNext();) {
      IAdaptorInpoint inpoint = (IAdaptorInpoint) iter.next();
      inpoint.stop();
    }
  }

  private void startNonInpoints() {
    for (Iterator iter = components.iterator(); iter.hasNext();) {
      ILifecycleComponent component = (ILifecycleComponent) iter.next();
      if (!inpoints.contains(component)) {
        component.start();
      }
    }
  }

  private void stopNonInpoints() {
    for (Iterator iter = components.iterator(); iter.hasNext();) {
      ILifecycleComponent component = (ILifecycleComponent) iter.next();
      if (!inpoints.contains(component) && component.isState(State.RUNNING)) {
        component.stop();
      }
    }
  }

  public void setExceptionWriteConnector(final IWriteConnector exceptionProcessor) {
    this.exceptionProcessor = new AdaptorOutpoint("exceptionProcessor", exceptionProcessor);
  }
  
  public void setExceptionProcessor(final IMessageProcessor exceptionProcessor) {
    this.exceptionProcessor = exceptionProcessor;
  }
  
  public void setPipeline(final Object[] pipeline) {
    if (exceptionProcessor != null) {
      setMessageProcessor(new Pipeline(pipeline, exceptionProcessor));
    } else {
      setMessageProcessor(new Pipeline(pipeline));
    }
  }
  
  public void exit() {
    if (runConfiguration != null) {
      runConfiguration.setExitFlag();
    }
    stop();
  }
}
