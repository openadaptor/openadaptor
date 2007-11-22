/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.dataobjects.DataObject;
import org.openadaptor.doconverter.XMLFormatter;

/**
 * Convert Data Objects into DOXML (using legacy openadaptor functionality).
 * <BR>
 * <B>Note</B>: Usage of this class depends on the availability of a legacy openadaptor jar 
 * to do the conversions, as openadaptor 3 doesn't directly support dataobjects, or
 * DOXML.
 * 
 * @author Eddy Higgins
 */
public class DataObjectToDOXmlConvertor extends AbstractDataObjectConvertor {

  private static final Log log = LogFactory.getLog(DataObjectToDOXmlConvertor.class);

  //Legacy attribute name for forcing use of useUniqueID;
  public static final String USE_UNIQUE_ID_ATTR = "useUniqueID";

  /**
   * This is the class which does the work.
   * <br>
   * Attributes may be set via setAttributes().
   */
  protected XMLFormatter formatter;

  
  /**
   * Assign attributes for the legacy convertor compenent.
   * <br>
   * Consult legacy openadaptor documentation for details on
   * possible attributes.
   * <br>
   * Note: this defaults the attribute {@link #USE_UNIQUE_ID_ATTR } to true
   * which is in keeping with legacy adaptor behaviour.
   * By default the XMLFormatter does not issue a uniqueid.
   * Unfortunately, the legacy sink that it's associated with
   * sets it to true, so pretty much all actual usage does have it set.
   * It's forced here, unless a configured Map explicitly overrides it.
   * 
   * @param attributeMap
   */
  public void setAttributes(Map attributeMap) {
    if (attributeMap==null) { //Make sure we have a map.
      attributeMap=new HashMap();
    }
    if (!attributeMap.containsKey(USE_UNIQUE_ID_ATTR)) {
      log.debug("Defaulting "+USE_UNIQUE_ID_ATTR+" to true - configure explicitly if this is not desired");
      attributeMap.put(USE_UNIQUE_ID_ATTR, String.valueOf(true));
    }
    super.setAttributes(attributeMap);
  }
  
  public DataObjectToDOXmlConvertor() {
    formatter = new XMLFormatter();
    //Allow the base class to set attributes on it (where possible)
    super.legacyConvertorComponent=formatter;
    setAttributes(null); 
  }

  /**
   * This converts a supplied DataObject[] into a DOXML String
   * <br>
   * <B>Note</B>: Usage of this method depends on the
   * availability of a legacy openadaptor jar to do the conversions, as openadaptor 3 doesn't directly support dataobjects, or
   * DOXML.
   * 
   * @param dobs contains an Array of DataObjects
   * @return XMl representation of the data
   * @throws RecordException if conversion fails
   */
  protected Object convert(DataObject[] dobs) throws RecordException {
    try {
      return formatter.toString(dobs);
    } 
    catch (Exception e) {
      String reason = "Failed to convert " + dobs == null ? "<null>" : dobs + ". Exception - " + e;
      log.warn(reason);
      throw new RecordException(reason, e);
    }
  }
  
}
