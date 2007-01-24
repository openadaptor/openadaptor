package org.openadaptor.core;

import org.openadaptor.core.jmx.Administrable;

public class Component implements IComponent, Administrable {

	private String id;

	public Component() {
	}
	
	public Component(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return id != null ? id : super.toString();
	}

  public Object getAdmin() {
    return new Admin();
  }
  
  public interface AdminMBean {
    String getId();
  }
  
  public class Admin implements AdminMBean {

    public String getId() {
      return Component.this.getId();
    }
    
  }
}
