package org.oa3.thirdparty.velocity;

import java.io.StringWriter;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.oa3.core.Component;
import org.oa3.core.IDataProcessor;
import org.oa3.core.exception.ComponentException;

public class VelocityProcessor extends Component implements IDataProcessor {

  private VelocityEngine engine = new VelocityEngine();
  
  private Template template;
  
  public VelocityProcessor() {
    super();
    init();
  }

  public VelocityProcessor(String id) {
    super();
    init();
  }

  private void init() {
    try {
      engine.init();
    } catch (Exception e) {
      throw new RuntimeException("failed to init velocity engine", e);
    }
  }
  
  public void setTemplate(String fileName) {
    try {
      template = engine.getTemplate(fileName);
    } catch (Exception e) {
      throw new ComponentException("failed to create Template", e, this);
    }
  }
  
  public Object[] process(Object data) {
    VelocityContext context = new VelocityContext();
    context.put("SystemProperties", System.getProperties());
    context.put("data", data);
    StringWriter sw = new StringWriter();
    try {
      template.merge(context, sw);
    } catch (Exception e) {
      throw new ComponentException("process exception", e, this);
    }
    return new Object[] {sw.toString()};
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
  }

}
