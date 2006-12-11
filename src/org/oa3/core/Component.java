package org.oa3.core;

public class Component implements IComponent {

	private String id;

	public Component() {
	}
	
	public Component(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public final void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return id != null ? id : super.toString();
	}

}
