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

package org.openadaptor.auxil.convertor.fixedwidth;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Defines the common elements of a fixed width converter. <p/>
 * 
 * There are multiple attributes that define a field in the fixed width world (ie. name, width, to be trimmed, etc.) and
 * we have a specific class to represent this. The field details are defined in the properties file and Swing will
 * instantiate them for us. <p/>
 * 
 * These field details are common to both conversion to and from fixed width string so we have an abstract
 * implementation of the IRecordProcessor which maintains code that manipulates them.
 * 
 * @author Russ Fennell
 * 
 * @see org.openadaptor.processor.IRecordProcessor
 * @see FixedWidthFieldDetail
 * @see AbstractFixedWidthStringConvertor
 * @see FixedWidthStringToOrderedMapConvertor
 */
public abstract class AbstractFixedWidthStringConvertor extends AbstractConvertor {
  private static final Log log = LogFactory.getLog(AbstractFixedWidthStringConvertor.class);

  // List of the field names/width mappings.
  protected FixedWidthFieldDetail[] fieldDetails;

  // Array of the field widths. Used instead of the FixedWidthFieldDetail's when you
  // just want to define the widths and don't care about the other attributes
  protected Integer[] fieldWidths;

  /**
   * @return a list of the field details mappings
   * 
   * @see FixedWidthFieldDetail
   */
  public FixedWidthFieldDetail[] getFieldDetails() {
    return fieldDetails;
  }

  /**
   * @return array of the field widths
   */
  public Integer[] getFieldWidths() {

    if (fieldWidths != null)
      return fieldWidths;

    if (fieldDetails != null) {
      Integer[] widths = new Integer[fieldDetails.length];

      for (int i = 0; i < fieldDetails.length; i++)
        widths[i] = new Integer(fieldDetails[i].getFieldWidth());

      return widths;
    }

    return new Integer[0];
  }

  /**
   * Sets the list of the field details mappings
   * 
   * @param details
   *          list of field details mappings
   * 
   * @throws ComponentException
   *           if the fieldWidths have already been set
   * 
   * @see FixedWidthFieldDetail
   */
  public void setFieldDetails(FixedWidthFieldDetail[] details) {
    if (fieldWidths != null)
      throw new ValidationException("You cannot define both fieldDetails and fieldWidths", this);

    this.fieldDetails = details;
  }

  /**
   * Sets the field widths only. Actually, just creates field details with default values for name, trim, etc. <p/>
   * 
   * There are two occasions when this method is called:
   * 
   * <ol>
   * <li>as part of the Spring properties bootstrap process - we need to create a new fieldDetail for each width</li>
   * 
   * <li>when being updated by the properties editor - we need to update the appropriate fieldDetail as well</li>
   * 
   * </ol>
   * 
   * @param widths
   *          list of the field widths
   * 
   * @throws ComponentException
   *           if the field details have already been set or if you define both field details and widths
   */
  public void setFieldWidths(Integer[] widths) {
    if (widths == null)
      return;

    // 1. boostrap sequence
    if (fieldWidths == null) {
      // if the fieldDetails have been set then we have a problem
      if (fieldDetails != null)
        throw new ValidationException("You cannot define both fieldDetails and fieldWidths", this);

      // create new fieldDetails based on the widths
      fieldDetails = new FixedWidthFieldDetail[widths.length];
      for (int i = 0; i < widths.length; i++) {
        Integer wdth = widths[i];
        FixedWidthFieldDetail fd = new FixedWidthFieldDetail();
        fd.setFieldWidth(wdth.intValue());

        fieldDetails[i] = fd;
      }
    }

    // 2. props editor update
    else {
      if (widths.length != fieldDetails.length)
        throw new ValidationException("Error updated field widths: the length does not match the existing field details length", this);

      for (int i = 0; i < widths.length; i++) {
        Integer width = widths[i];

        if (width == null) {
          AbstractFixedWidthStringConvertor.log.warn("Null value for width " + i + ": will reset to 0");
          width = new Integer(0);
        }

        fieldDetails[i].setFieldWidth(width.intValue());
      }
    }

    // not sure why but we might need to reference the list directly. Besides
    // we use this to indicate that we've been through the boostrap process
    fieldWidths = widths;
  }

  /**
   * @return a list of the field names defined
   */
  public ArrayList getFieldNames() {
    ArrayList names = new ArrayList();

    if (fieldDetails != null)
      for (int i = 0; i < fieldDetails.length; i++) {
        String name = fieldDetails[i].getFieldName();
        if (name != null)
          names.add(name);
      }

    return names;
  }

  /**
   * @return true if any of the field details have their names set
   */
  public boolean hasFieldNames() {
    return (getFieldNames().size() > 0);
  }

  /**
   * @return the size of the record which is the sum of the individual field widths
   */
  public int getTotalFieldWidth() {
    int recSize = 0;

    if (fieldDetails != null)
      for (int i = 0; i < fieldDetails.length; i++)
        recSize += fieldDetails[i].getFieldWidth();

    return recSize;
  }

  /**
   * Returns the minimum size that a record must be before it can be chopped or -1 if there are no field details
   * defined. <p/>
   * 
   * The minimum size is calculated as the total width of all fields bar the last one if it's to be trimmed.
   */
  public int getMinimumRecordSize() {
    if (fieldDetails == null || fieldDetails.length == 0)
      return -1;

    int min = getTotalFieldWidth();

    FixedWidthFieldDetail last = fieldDetails[fieldDetails.length - 1];
    if (last.isTrim())
      min -= last.getFieldWidth();

    return min;
  }

  /**
   * @return false if the field name supplied is null or an empty string
   * 
   * @throws ComponentException
   *           if there are multiple fields defined with the same name
   */
  public boolean isValidFieldName(String name) {
    ArrayList names = getFieldNames();
    int a = names.indexOf(name);
    int b = names.lastIndexOf(name);
    if (a != b)
      throw new ValidationException("Multiple field names defined", this);

    return (name != null && !name.equals(""));
  }
}
