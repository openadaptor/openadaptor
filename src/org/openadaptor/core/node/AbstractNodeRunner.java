/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.lifecycle.LifecycleComponent;
import org.openadaptor.core.lifecycle.State;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 4, 2007 by oa3 Core Team
 */

/**
 * Abstract Superclass for IRunnables that can embed any IMessageProcessor.
 * NodeRunners themselves implement IMessageProcessor and can be used participate
 * in Adaptor Routing just like any other IMessageProcessor.
 *
 * NodeRunners exist to allow the seperation of the IRunnable role from the
 * IMessageProcessor role. This is often desirable when embedding Adaptors
 * in a larger context or when handing off transaction management to an
 * enclosing container.
 */
public abstract class AbstractNodeRunner extends LifecycleComponent implements IMessageProcessor, IRunnable {

  private static final Log log = LogFactory.getLog(AbstractNodeRunner.class);

  protected IMessageProcessor messageProcessor;
  protected ILifecycleComponent managedComponent;

  protected int exitCode;
  protected IMessageProcessor target;
  protected Throwable exitThrowable;

  public Response process(Message msg) {
    return target.process(msg);
  }

  /**
   * For an {@link IRunnable} the MessageProcessor is used to "plug in" the rest of the
   * adaptor or other downstream component.
   *
   * @param processor
   */
  public void setMessageProcessor(IMessageProcessor processor) {
    messageProcessor = processor;
  }

  public void setTarget(IMessageProcessor processor) {
   target = processor;   
   if (target instanceof ILifecycleComponent  && managedComponent == null) {
     managedComponent = (ILifecycleComponent)target;
   }
  }

  public IMessageProcessor getTarget() {
    return target;
  }

  public int getExitCode() {
    return exitCode;
  }

  /**
   * @return an instance of a Throwable if this runnable exits with an unhandled
   *         error. null if the runnable exits correctly.
   */
  public Throwable getExitError() {
    return exitThrowable;
  }

  public abstract void run();

  protected boolean isStillRunning() {
    if (managedComponent != null) {
      return ((isState(State.STARTED)) && managedComponent.isState(State.STARTED));
    }
    else {
      return isState(State.STARTED);
    }
  }

  public void start() {
    // Force the delegate to have the same ID as this.
    if(target instanceof IComponent) {
      ((IComponent)target).setId(getId());
    }
    if ((messageProcessor != null) && (target instanceof Node)) {
      // If the target is a Node force it to use the same message processor as this (If one has been set)
      ((Node)target).setMessageProcessor(messageProcessor); 
    }
    if (managedComponent != null) { managedComponent.start();}
    super.start();
  }

  protected void stopping() {
    if (isState(State.STARTED)) {
      log.info(getId() + " is stopping");
      setState(State.STOPPING);
    }
  }

  public void stop() {
    if (!isState(State.STOPPING)) {
      stopping();
    }
    if (managedComponent != null) { managedComponent.stop();}
    super.stop();
  }

  public ILifecycleComponent getManagedComponent() {
    return managedComponent;
  }

  public void setManagedComponent(ILifecycleComponent managedComponent) {
    this.managedComponent = managedComponent;
  }
  
}
