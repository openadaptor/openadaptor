package org.openadaptor.core.lifecycle;


public interface ILifecycleListener {
	void stateChanged(ILifecycleComponent component, State newState);
}
