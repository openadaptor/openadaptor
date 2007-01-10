package org.oa3.core.lifecycle;


public interface ILifecycleListener {
	void stateChanged(ILifecycleComponent component, State newState);
}
