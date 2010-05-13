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

package org.openadaptor.auxil.convertor;

import java.util.List;

/**
 * Abstract base class for eneral purpose Map conversion.
 * If an (optional) list of names is configured, it may be used
 * when converting to and from Maps. In this case, missing fields
 * may be null-padded if the padMissingFields property is set.
 * 
 * @author Eddy Higgins
 */
public abstract class AbstractMapConvertor extends AbstractConvertor {
  
  // Internal state:
  // Bean properties:
  //List of field names to apply to incoming arrays
  protected String[] fieldNames=null;

  //Flag to indicate whether missing fields may be substituted with null values
  protected boolean padMissingFields=false;


  /**
   * Return configured Array of field names, if any.
   * @return String[] containing field names; possibly null.
   */
  public String[] getFieldNames() { 
    return fieldNames;
  }
  /**
   * Configures the names to be used for fields.
   * @param fieldNames String[] containing a list of field Names.
   */
  public void setFieldNames(String[] fieldNames) {
    this.fieldNames = fieldNames;
  }

  /**
   * Wrapper around {@link #setFieldNames(String[] fieldNames)}.
   * It will convert non-strings to Strings, and call
   * setFieldNames(String[]).
   * @param fieldNames
   */
  public void setFieldNames(Object[] fieldNames) {
    if (fieldNames==null) {
      this.fieldNames=null;
    }
    else {
      String[] nameStrings=new String[fieldNames.length];
      for (int i=0;i<nameStrings.length;i++) {
        Object name=fieldNames[i];
        nameStrings[i]= (name instanceof String)?(String)name:name.toString();
      }
      setFieldNames(nameStrings);     
    }
  }

  /**
   * Set fieldNames from a list. 
   * Converts list to Array and calls {@link #setFieldNames(Object[] fieldNames)}
   * @param fieldNameList
   */
  public void setFieldNames(List fieldNameList) {
    if (fieldNameList==null) {
      setFieldNames((String[])null);
    }
    else {
      setFieldNames(fieldNameList.toArray(new Object[fieldNameList.size()]));
    }
  }

  /**
   * Flag to indicate if missing fields can safely be substituted with 
   * null values.
   * Default is false for backward compatibility reasons.
   * @param padMissingFields true if padding is to be permitted; false otherwise
   */
  public void setPadMissingFields(boolean padMissingFields){
    this.padMissingFields=padMissingFields;
  }
  // END Bean getters/setters

}
