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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.util.DefaultNameGenerator;
import org.openadaptor.util.INameGenerator;

/**
 * General purpose convertor for converting Object arrays into OrderedMaps.
 * It will do this by one of the following methods:
 * <pre>
 * <ul>
 * <li> Apply a configured list of names to each field in turn of a supplied array
 * <li> Use the first array as the list of names to use for subsequent arrays
 * <li> Automatically generate names for values which do not have them configured
 * </ul>
 * 
 * @author Eddy Higgins
 */
public abstract class AbstractMapGenerator extends AbstractConvertor {
  private final static Log log = LogFactory.getLog(AbstractMapGenerator.class);

  // Internal state:
  // Flag indicating if the next record is to be used to supply field names.
  protected boolean nextRecordContainsFieldNames = false;
  // Automatic name generator implentation, with default.
  protected INameGenerator nameGenerator=new DefaultNameGenerator("Col_");

  // Bean properties:
  //List of field names to apply to incoming arrays
  protected String[] fieldNames=null;
  //Flag to limit warnings.
  protected boolean insufficientNamesWarningIssued=false;
  //Flag to indicate whether missing fields may be substituted with null values
  protected boolean padMissingFields=false;

  //Flag which indicates if the first record supplied will contain field names.
  protected boolean firstRecordContainsFieldNames = false;

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
    nextRecordContainsFieldNames=false;
  }

  /**
   * Wrapper around {@link setFieldNames(String[] fieldNames}.
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
   * Converts list to Array and calls {@link setFieldNames(Object[] fieldNames}
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
  
 /**
  * Override default name generator with an alternative implementation
  * @param nameGenerator Any INameGenerator implementation
  */ 
  public void setNameGenerator(INameGenerator nameGenerator) {
    this.nameGenerator=nameGenerator;
  }

  /**
   * Flag to indicate that the first field supplied will in fact contain
   * the field names to apply to subsequent records.
   * Note - in doing so the convertor will consume the records and return an empty
   * result.
   * @param firstRecordContainsFieldNames true if first record will contain field Names
   */
  public void setFirstRecordContainsFieldNames(boolean firstRecordContainsFieldNames) {
    this.firstRecordContainsFieldNames = firstRecordContainsFieldNames;
    nextRecordContainsFieldNames = firstRecordContainsFieldNames;
  }

  /**
   * Flag which indicates if the next record is expected to contain fieldNames.
   * @return true if nextRecord is expectd to contain fieldNames; false otherwse.
   */
  public boolean nextRecordContainsFieldNames() {
    return nextRecordContainsFieldNames;
  }

  /**
   * Flag indicating if convertor is configured to treat the first records as
   * containing field names to apply to subsequent records.
   * @return true if the first record is to be processed as a header record
   */
  
  public boolean getFirstRecordContainsFieldNames() {
    return firstRecordContainsFieldNames;
  }

  // END Bean getters/setters

  public void initialise() {
    reset(null);
  }
  /**
   * if the firstRecordContainsFieldNames flag is set then we also set the nextRecordContainsFieldNames flag
   */
  public void reset(Object context) {
    super.reset(context);
    if (firstRecordContainsFieldNames) {
      nextRecordContainsFieldNames = true;
    }
    insufficientNamesWarningIssued=false;
  }
}
