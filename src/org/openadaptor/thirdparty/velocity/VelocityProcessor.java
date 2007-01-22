package org.openadaptor.thirdparty.velocity;

import java.io.StringWriter;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ComponentException;

public class VelocityProcessor extends Component implements IDataProcessor {

  private VelocityEngine engine = new VelocityEngine();
  
  private String templateString;
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
  
  public void setTemplateString(String s) {
    templateString = s;
  }
  
  public void setTemplateFile(String fileName) {
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
      if (template != null) {
        template.merge(context, sw);
      } else if (templateString != null) {
        engine.evaluate(context, sw, "", templateString);
      } else {
        throw new Exception("neither template nor templateString have been set");
      }
    } catch (Exception e) {
      throw new ComponentException("process exception", e, this);
    }
    return new Object[] {sw.toString()};
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
    if (template == null && templateString == null) {
      exceptions.add(new ComponentException("neither template nor templateString have been set", this));
    }
  }

}
