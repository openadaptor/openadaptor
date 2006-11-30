package org.oa3;

public interface ILifecycleListener {
	void stateChanged(ILifecycleComponent component, State newState);
}
