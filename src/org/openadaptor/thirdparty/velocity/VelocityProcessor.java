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

  private VelocityEngine engine;

  private String templateString;

  private String templateFile;

  private Template template;

  public VelocityProcessor() {
    super();
  }

  public VelocityProcessor(String id) {
    super();
  }

  private void init() {
    if (engine == null) {
      try {
        engine = new VelocityEngine();
        engine.init();
        template = templateFile != null ? engine.getTemplate(templateFile) : null;
      } catch (Exception e) {
        throw new RuntimeException("failed to init velocity engine", e);
      }
    }
  }

  public void setTemplateString(String s) {
    templateString = s;
  }

  public void setTemplateFile(String fileName) {
    templateFile = fileName;
  }

  public Object[] process(Object data) {
    init();
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
    return new Object[] { sw.toString() };
  }

  public void reset(Object context) {
  }

  public void validate(List exceptions) {
    init();
    if (template == null && templateString == null) {
      exceptions.add(new ComponentException("neither template nor templateString have been set", this));
    }
  }

}
