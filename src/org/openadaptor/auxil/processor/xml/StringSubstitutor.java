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

package org.openadaptor.auxil.processor.xml;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Perform string substitution upon incoming java.lang.String payload.
 * <br/>
 * All occurrences will be substituted (not just the first one).
 * 
 * <verbatim>
 * <bean id="XmlStripControlCodes" class="com.drkw.gds.oa3.StringSubstitutor">
 *   <description>
 *     Strip illegal control codes in incoming XML payload (publisher should have XML-entified them, and in
 *     fact really they need cleaning up in source database).
 *  
 *     http://www.w3.org/TR/REC-xml/#NT-Char
 *     
 *     We only encounter this issue in a handful of records, so we set checkfirst to true.
 *   </description>
 *   <property name="pattern" value="[\x00-\x08\x0b\x0c\x0e-\x1f]" />
 *   <property name="replacement" value="" />
 *   <property name="replaceall" value="true" />
 *   <property name="checkfirst" value="true" />
 * </bean>
 *</verbatim>
 * @author shirea
 * 
 */
public class StringSubstitutor extends Component implements IDataProcessor {

  private String pattern = null;
  private String replacement = "";
  private boolean replaceall = true;
  private boolean checkfirst = false;
  
  private Pattern compiledPattern = null;

  // BEGIN Bean getters/setters
  /**
   * Set the pattern that is to be searched for (and replaced).
   * <br>
   * This must be set explicitly.
   * 
   * @param regexp a regular expression defining the pattern to search for
   */
  public void setPattern(String regexp) {
    if ((regexp == null) || (regexp.trim().length() == 0)) {
      throw new RuntimeException(
          "Null or empty value for pattern regexp is not permitted");
    }
    pattern = regexp;
  }

  /**
   * Get the pattern that is to be searched for (and replaced).
   *
   * @return the regexp to be searched for in the string payload.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Set the string that will be substituted (in place of the pattern). <br/>
   * The default value is empty string (which deletes all occurrences of the
   * pattern).
   * 
   * @param value to replace matches with
   */
  public void setReplacement(String value) {
    replacement = value;
  }

  /**
   * Get the replacement value.
   * 
   * @return the value to be substituted into the string payload.
   */
  public String getReplacement() {
    return replacement;
  }

  /**
   * Set whether all occurrences are to be replaced, or just the first one.
   * The default value is true (i.e. replace all).
   * 
   * @param value true if all occurrences are to be replaced, false if only first one is to be replaced.
   */
  public void setReplaceall(boolean value) {
    replaceall = value;
  }

  /**
   * Check whether all occurrences are to be replaced, or just the first one.
   * 
   * @return true if all occurrences of the pattern are to be replaced.
   */
  public boolean getReplaceall() {
    return replaceall;
  }

  /**
   * Set whether we should check whether the payload will be changed before accepting the copying burden of an actual substitution.
   * <br/>
   * The default value is false (i.e. do the substitution and associated copying without first checking if it is needed).
   * <ul>
   * <li>Set it to false if you expect most/all records to be modified.</li>
   * <li>Set it to true if you expect, say, 1 in 1000 records each of which is 100k in size to need modification.</li>
   * </ul>
   * @param value to replace matches with
   */
  public void setCheckfirst(boolean value) {
    checkfirst = value;
  }

  /**
   * Get the replacement value.
   * 
   * @return the value to be substituted into the string payload.
   */
  public boolean getCheckfirst() {
    return checkfirst;
  }

  // END Bean getters/setters

  
  /**
   * Perform the string substitution.
   * 
   * @return Object[] containing the substituted string.
   */
  public Object[] process(Object input) {
    if (!(input instanceof java.lang.CharSequence)) {
      throw new RecordFormatException("Expected input to be a java.lang.CharSequence (e.g. java.lang.String) but instead it was "+input.getClass().getName());
    }

    CharSequence result = (CharSequence)input;

    Matcher matcher = compiledPattern.matcher(result);
        
    if (!checkfirst || matcher.reset().find()) {
      result = replaceall ? matcher.replaceAll(replacement) : matcher.replaceFirst(replacement);
    }
    
    return new Object[] { result };
  }

  
  /**
   * Checks that the mandatory properties have been set ("pattern" is a mandatory property).
   * 
   * @param exceptions
   *          list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if (pattern == null) {
      exceptions.add(new ValidationException("No pattern configured. Fatal error", this));
    }
    try {
      compiledPattern = Pattern.compile(pattern);
    } catch (PatternSyntaxException e) {
      exceptions.add(new ValidationException("Unable to compile the configured pattern \""+pattern+"\". Fatal error", this));
    }
  }

  public void reset(Object context) {
  }

}
