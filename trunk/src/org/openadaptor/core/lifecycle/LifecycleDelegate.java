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

package org.openadaptor.core.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LifecycleDelegate {

	private static Log log = LogFactory.getLog(LifecycleDelegate.class);
	
	private ILifecycleComponent component;
	private State currentState;
	private List listeners = new ArrayList();
	private Object LOCK = new Object();
	private Object WAIT = new Object();
	
	public LifecycleDelegate(final ILifecycleComponent component, final State state) {
		this.component = component;
		this.currentState = state;
	}
	
	public void addListener(ILifecycleListener listener) {
		synchronized (LOCK) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void removeListener(ILifecycleListener listener) {
		synchronized (LOCK) {
			if (listeners.contains(listener)) {
				listeners.remove(listener);
			}
		}
	}

	public void notifyListeners(State newState) {
    // copy listeners to avoid concurrent modification during notification!!!
    ILifecycleListener[] listeners = getListeners();
    for (int i = 0; i < listeners.length; i++) {
      try {
        listeners[i].stateChanged(component, newState);
      } catch (RuntimeException e) {
        log.error("unexpected exception", e);
      }
    }
  }
	
  private ILifecycleListener[] getListeners() {
    synchronized (LOCK) {
      return (ILifecycleListener[]) listeners.toArray(new ILifecycleListener[listeners.size()]);
    }
  }
  
	public boolean isState(State state) {
		return currentState.equals(state);
	}

	public void setState(State state) {
		synchronized (WAIT) {
			if (currentState != state) {
			  currentState = state;
        notifyListeners(currentState);
      }
			WAIT.notifyAll();
		}
	}
	
	public State getState() {
		return currentState;
	}
	
	public void waitForState(State state) {
		while (currentState != state) {
			synchronized (WAIT) {
				try {
					WAIT.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
