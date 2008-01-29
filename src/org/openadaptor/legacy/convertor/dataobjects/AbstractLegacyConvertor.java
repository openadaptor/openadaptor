/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.legacy.convertor.dataobjects;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;

/**
 * Abstract Convertor which converts from DataObjects into other formats
 * 
 * @author higginse
 * 
 */
public abstract class AbstractLegacyConvertor extends AbstractConvertor {
  private static final Log log = LogFactory.getLog(AbstractLegacyConvertor.class);

  /**
   * This holds the appropriate legacy convertor component, if any.
   */
  protected Object legacyConvertorComponent;
  /**
   * Assign attributes for the legacy convertor compenent.
   * Consult legacy openadaptor documentation for details on
   * possible attributes.
   * @param attributeMap
   */
  public void setAttributes(Map attributeMap) {
    if (legacyConvertorComponent!=null) {
      log.debug("Setting "+attributeMap.size()+" attributes on legacy convertor component");
      LegacyUtils.setAttributes(legacyConvertorComponent, attributeMap);
    }
    else {
      throw new RuntimeException("Legacy attribute configuration is not supported");   
    }
  }
 
}

