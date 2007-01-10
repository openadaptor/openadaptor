package org.openadaptor.thirdparty.velocity;

import java.io.File;
import java.io.FileWriter;
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
  
  public void setTemplate(String s) {
    try {
      File temp = File.createTempFile("xyz", ".vm", new File(System.getProperty("user.dir")));
      temp.deleteOnExit();
      FileWriter writer = new FileWriter(temp);
      writer.write(s);
      writer.close();
      template = engine.getTemplate(temp.getName());
    } catch (Exception e) {
      throw new ComponentException("failed to create Template", e, this);
    }
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
