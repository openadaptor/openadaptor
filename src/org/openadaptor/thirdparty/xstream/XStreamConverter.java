/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.thirdparty.xstream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ValidationException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Base bean for using XStream, does XStream initialisation.
 * @author perryj
 *
 */
public abstract class XStreamConverter extends Component implements IDataProcessor {

  private XStream xstream;
  private boolean useXPP3 = false;
  private Map aliasMap = new HashMap();

  protected XStreamConverter() {
  }

  protected XStreamConverter(String id) {
    super(id);
  }

  /**
   * tells XStream to use XPP3 parser, default is not to use this
   * @param useXPP3
   */
  public void setUseXPP3(final boolean useXPP3) {
    this.useXPP3 = useXPP3;
  }

  /**
   * sets map entries between xml elements and java classes that XStream will use.
   * The keys are the xml element names, the values can either be Class objects
   * or class names.
   */
  public void setAliasMap(final Map aliasMap) {
    this.aliasMap.clear();
    for (Iterator iter = aliasMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      if (entry.getValue() instanceof Class) {
        addAlias(entry.getKey().toString(), (Class) entry.getValue());
      } else {
        addAlias(entry.getKey().toString(), entry.getValue().toString());
      }
    }
  }

  /**
   * adds a map entry between xml elements and java classes that XStream will use.
   */
  public void addAlias(final String element, final String className) {
    try {
      Class klass = Class.forName(className);
      addAlias(element, klass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * adds a map entry between xml elements and java classes that XStream will use.
   */
  public void addAlias(final String element, final Class klass) {
    this.aliasMap.put(element, klass);
  }
  
  public void reset(Object context) {
  }

  /**
   * initialises XStream, expects at least one entry in the aliasMap
   */
  public void validate(List exceptions) {
    if (aliasMap.isEmpty()) {
      exceptions.add(new ValidationException("aliasMap property is empty", this));
    }
    if (useXPP3) {
      xstream = new XStream();
    } else {
      xstream = new XStream(new DomDriver());
    }
    for (Iterator iter = aliasMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      xstream.alias((String) entry.getKey(), (Class) entry.getValue());
    }
  }

  protected Object toObject(String xml) {
    return xstream.fromXML(xml);
  }
  
  protected String toXml(Object data) {
    return xstream.toXML(data);
  }
}
