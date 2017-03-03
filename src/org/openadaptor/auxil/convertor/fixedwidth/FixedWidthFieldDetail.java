/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.convertor.fixedwidth;

/**
 * Defines a fixed width field. Allows you to set the various attributes required
 * to manipulate a field:
 * <p/>
 * <p/>
 * <pre>
 *      fieldWidth      the number of characters the field will be made up of. Default = 0
 *      fieldName       the name used to reference the field. Default = null
 *      trim            wether the field is to have whitespace trimmed. Default = false
 *      rightAlign      if true then align the text to the right hand side of the field by
 *                      padding from the left. Default is false.
 * </pre>
 *
 * @author Russ Fennell
 */
public class FixedWidthFieldDetail {
  private int fieldWidth = 0;

  private String fieldName = null;

  private boolean trim = false;

  private boolean rightAlign = false;

  /**
   * @return the field width value. Defaults to 0
   */
  public int getFieldWidth() {
    return fieldWidth;
  }

  /**
   * @return the field name value. Defaults to null
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @return true if the field is to be trimmed. Defaults to false
   */
  public boolean isTrim() {
    return trim;
  }

  /**
   * Sets the field width
   *
   * @param fieldWidth
   */
  public void setFieldWidth(int fieldWidth) {
    this.fieldWidth = fieldWidth;
  }

  /**
   * Sets the field name
   *
   * @param fieldName
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Sets the flag to indicate if the field should be trimmed orr not
   *
   * @param trim
   */
  public void setTrim(boolean trim) {
    this.trim = trim;
  }

  /**
   * @return true if the text in the field is to be aligned to the right
   * hand side.
   */
  public boolean isRightAlign() {
    return rightAlign;
  }

  /**
   * Sets the flag to turn right align on
   *
   * @param rightAlign true/false
   */
  public void setRightAlign(boolean rightAlign) {
    this.rightAlign = rightAlign;
  }
}
