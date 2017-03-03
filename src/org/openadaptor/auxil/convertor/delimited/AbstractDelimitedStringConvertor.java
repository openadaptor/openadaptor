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

package org.openadaptor.auxil.convertor.delimited;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Base Converter for delimited string records
 * 
 * @author Eddy Higgins, Kris Lachor
 */
public abstract class AbstractDelimitedStringConvertor extends AbstractConvertor {

  private final static Log log = LogFactory.getLog(AbstractDelimitedStringConvertor.class);

  public static final String DEFAULT_DELIMITER = ",";
  
  public static final char DEFAULT_QUOTE_ESCAPE_CHARACTER ='\\';
  
  // Internal state:
  protected boolean nextRecordContainsFieldNames = false;

  // Bean properties exposed only in appropriate subclasses:
  protected boolean stripEnclosingQuotes = false;

  protected boolean addNeededEnclosingQuotes = false;
  
  protected boolean forceEnclosingQuotes = false;

  protected boolean protectQuotedFields = false;
  
  // Bean properties:
  protected String[] fieldNames;

  protected String delimiter = AbstractDelimitedStringConvertor.DEFAULT_DELIMITER;

  protected char quoteChar = '\"'; // actually used to hold a char.
  
  private char quoteEscapeChar = DEFAULT_QUOTE_ESCAPE_CHARACTER;

  protected boolean firstRecordContainsFieldNames = false;
  
  private boolean delimiterAlwaysRegExp = false;
  
  private boolean delimiterAlwaysLiteralString = false;
  
  private boolean escapeQuoteCharacters = false;
  
  private boolean smartEscapeQuoteCharacters = false;

  /**
   * Default constructor.
   */
  protected AbstractDelimitedStringConvertor() {
  }

  /**
   * Constructor. 
   */
  protected AbstractDelimitedStringConvertor(String id) {
    super(id);
  }
  
  // BEGIN Bean getters/setters
  // Todo: Mutability of _fieldNames array is not managed.

  /**
   * @return list of the field names that
   */
  public String[] getFieldNames() {
    return fieldNames;
  }

  /**
   * Set the list of field names to use when converting delimited strings into OrderedMaps
   * 
   * @param fieldNames
   *          array of field names
   */
  public void setFieldNames(String[] fieldNames) {
    this.fieldNames = fieldNames;
  }

  /**
   * Assign fieldNames from a List - allows trivial Spring Bean construction.
   * 
   * @param fieldNameList
   *          list of field names
   */
  public void setFieldNames(List fieldNameList) {
    setFieldNames((String[]) fieldNameList.toArray(new String[fieldNameList.size()]));
  }

  /**
   * @return the character that will be used as a delimiter between fields
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * Optional: set the field delimiter character (defaults to comma).
   * 
   * If you want tab delimited then your Spring XML config would look like: <verbatim> <property name="delimiter"
   * value="&#x09;"/> <!-- Tab separated --> </verbatim>
   * 
   * Note that without quotes handling this value is actually interpreted as a regular expression, so for a "|" character
   * you will need to use a <code>value="[|]"</code> to avoid it being interpreted as a logical-OR.
   * Whereas when it is used with quote handling it is interpreted as a literal string.
   * This looks like a bug that should be fixed (i.e. they should both behave the same way).
   * 
   * @param delimiter
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * @param quoteChar
   *          set the character that will be used when wrapping fields in quotes
   */
  public void setQuoteChar(char quoteChar) {
    this.quoteChar = quoteChar;
  }

  /**
   * @return the character that will be used when wrapping fields in quotes
   */
  public char getQuoteChar() {
    return quoteChar;
  }

  /**
   * controls whether delmiters that are within quotes are
   * @param protectQuotedFields
   */
  public void setProtectQuotedFields(boolean protectQuotedFields) {
    this.protectQuotedFields = protectQuotedFields;
  }

  /**
   * 
   * @param firstRecordContainsFieldNames
   */
  public void setFirstRecordContainsFieldNames(boolean firstRecordContainsFieldNames) {
    this.firstRecordContainsFieldNames = firstRecordContainsFieldNames;
    nextRecordContainsFieldNames = firstRecordContainsFieldNames;
  }

  /**
   * @return true if the first record is to be processed as a header record
   */
  public boolean getFirstRecordContainsFieldNames() {
    return firstRecordContainsFieldNames;
  }

  /**
   * @return flag indicating that the delimiter is a literal string
   */
  public boolean isDelimiterAlwaysLiteralString() {
    return delimiterAlwaysLiteralString;
  }

  /**
   * If set to true the delimiter will always be treated as a literal string.
   * Either this flag or delimiterAlwaysRegExp can be set to true at the same time.
   */
  public void setDelimiterAlwaysLiteralString(boolean delimiterAlwaysLiteralString) {
    this.delimiterAlwaysLiteralString = delimiterAlwaysLiteralString;
  }

  /**
   * @return true is delimiter is to be always treated as a regular expression, false otherwise
   */
  public boolean isDelimiterAlwaysRegExp() {
    return delimiterAlwaysRegExp;
  }

  /**
   * If set to true the delimiter will always be treated as a regular expression.
   * Either this flag or delimiterAlwaysLiteralString can be set to true at the same time.
   */
  public void setDelimiterAlwaysRegExp(boolean delimiterAlwaysRegExp) {
    this.delimiterAlwaysRegExp = delimiterAlwaysRegExp;
  }
  
  /**
   * Set flag to remove enclosing quotes from fields where necessary. This is applied to all fields
   * 
   * @param stripQuotes
   *          true to remove quotes
   */
  public void setStripEnclosingQuotes(boolean stripQuotes) {
    stripEnclosingQuotes = stripQuotes;
  }

  /**
   * @return true if enclosing quotes are to be removed from all fields
   */
  public boolean getStripEnclosingQuotes() {
    return stripEnclosingQuotes;
  }

  /**
   * If set to true every quote character will be checked for being preceded with an escape
   * character. If it is escaped, it won't be treated as a boundary of a quoted block.
   * If set to false (default), quote characters will never be escaped.
   * The flag takes effect only when <code>protectQuotedFields</code> flat is enabled.
   * Escaping quoted chars only works when the delimiter is a literal string. 
   * 
   * ToDo: implement escaping quoted chars when the delimiter is a regular expression
   * @see AbstractDelimitedStringConvertor#setQuoteEscapeChar(char)
   */
  public void setEscapeQuoteCharacters(boolean escapeQuoteCharacters) {
    this.escapeQuoteCharacters = escapeQuoteCharacters;
  }
  
  /**
   * Setter that allows of overriding of the default quote escaping character 
   * {@link AbstractDelimitedStringConvertor#DEFAULT_QUOTE_ESCAPE_CHARACTER}.
   * 
   * @param quoteEscapeChar new quote escaping character.
   */
  public void setQuoteEscapeChar(char quoteEscapeChar) {
    this.quoteEscapeChar = quoteEscapeChar;
  }
  
  /**
   * If set to true, the converter will attempt to auto-escape certain quote characters,
   * namely those that occur between two other quote characters but there's no delimiter
   * occurring between the quote char in question and either of the quotes on its sides.
   * 
   * For example (, -the delimiter  ' -the quote):
   * 'abc','de'f','ghj'
   * 
   * The quote char before the letter f will be auto-escaped. 
   * This setting will only take effect if the protectQuotedFields flag is set to true.
   * The flat is self-exclusive with the escapeQuoteCharacters flag.
   * Smart-escaping quoted chars only works when the delimiter is a literal string. 
   * 
   * @see #setProtectQuotedFields(boolean)
   */
  public void setSmartEscapeQuoteCharacters(boolean smartEscapeQuoteCharacters) {
    this.smartEscapeQuoteCharacters = smartEscapeQuoteCharacters;
  }
  
  // END Bean getters/setters

  // BEGIN implementation IRecordProcessor interface

  /**
   * if the firstRecordContainsFieldNames flag is set then we also set the nextRecordContainsFieldNames flag
   */
  public void reset() {
    if (firstRecordContainsFieldNames)
      nextRecordContainsFieldNames = true;
  }

  // END implementation of IRecordProcessor interface

  /**
   * Validate the parameters of this bean.
   * @param exceptions the list of exceptions to append to if validation
   * of this bean fails due to invalid or inconsistent parameters. 
   * E.g. both <code>delimiterAlwaysRegExp</code> and 
   * <code>delimiterAlwaysLiteralString</code> are true.
   */
  public void validate(List exceptions) {
    super.validate(exceptions);
	  String delimiter = this.getDelimiter();
	  String quoteChar = String.valueOf(this.getQuoteChar());
	  
	  if (delimiter == null || delimiter.length() == 0) {
		  exceptions.add(new ValidationException("The delimiter must be set", this));
	  }
	  if (this.isDelimiterAlwaysRegExp() && this.isDelimiterAlwaysLiteralString()) {
		  exceptions.add(new ValidationException("Cannot set both delimiterAlwaysRegExp and delimiterAlwaysLiteralString to true", this));
	  }
	  if (this.treatDelimiterAsRegExp()) {
		  // interpret the delimiter as a regular expression
		  try {
			  Pattern.compile(delimiter);
			  if (Pattern.matches(delimiter, quoteChar)) {
				  exceptions.add(new ValidationException("Quote character cannot match delimiter pattern", this));
			  }
		  } catch (PatternSyntaxException e) {
			  exceptions.add(new ValidationException("Invalid regular expression delimiter: " + delimiter, e, this));
		  }
	  } else {
		  // interpret the delimiter as a string literal
		  if (quoteChar.equals(delimiter)) {
			  exceptions.add(new ValidationException("Quote character cannot be the same as the delimiter", this));
		  }
	  }
      if (this.escapeQuoteCharacters && this.smartEscapeQuoteCharacters) {
          exceptions.add(new ValidationException("Cannot set both escapeQuoteCharacters and smartEscapeQuoteCharacters to true", this));
      }
      if (this.escapeQuoteCharacters && !this.protectQuotedFields) {
        exceptions.add(new ValidationException("Cannot set escapeQuoteCharacters to true when protectQuotedFields is set to false", this));
      }
      if (this.smartEscapeQuoteCharacters && !this.protectQuotedFields) {
        exceptions.add(new ValidationException("Cannot set smartEscapeQuoteCharacters to true when protectQuotedFields is set to false", this));
      }
  }
  
  /**
   * Takes the supplied delimited string and chops it via the <code>delimiter</code> character. Each field is then
   * added as an attribute to a map. The attribute name are generated either from the <code>fieldNames</code> list or
   * if this is not defined then automatically generated.
   * 
   * @param delimitedString
   *          the delimited string to process
   * 
   * @return a map containing an attribute for each field in the supplied string
   * 
   * @throws NullRecordException
   *           if the string passed is null
   * @throws RecordException
   *           if the incoming record does not match up with the structure expected
   */
  protected IOrderedMap convertDelimitedStringToOrderedMap(String delimitedString) {
    IOrderedMap map;
    if (delimitedString == null) {
      throw new NullRecordException("Null values not permitted");
    }

    String[] values = extractValues(delimitedString);
    int received = values.length;
    // If the delimited String has field names specified, then use them.
    // If insufficient fields are provided to match the names - throw an exception.
    // If too many fields are supplied, keep them, but give them auto-generated names.
    if (fieldNames != null) {
      int count = fieldNames.length;
      AbstractDelimitedStringConvertor.validateStructure(count, received);
      map = new OrderedHashMap();
      for (int i = 0; i < count; i++) {// Add the named ones
        map.put(fieldNames[i], values[i]);
      }
      for (int j = count; j < received; j++) { // Now add the nameless poor souls.
        map.add(values[j]);
      }
    } else { // don't care about names. Ain't got none.
      map = new OrderedHashMap(Arrays.asList(values));
    }
    return map;
  }

  /**
   * Takes the supplied delimited string and chops it via the <code>delimiter</code> character. Will strip quotes if
   * the <code>stripEnclosingQuotes</code> flag is set.
   * 
   * @param delimitedString
   *          the delimited string to process
   * 
   * @return an array of strings corresponding to the fields in the string supplied
   */
  protected String[] extractValues(String delimitedString) {
    String[] values = null;
    
    if( this.treatDelimiterAsRegExp() ){ 
       values = splitByRegularExpression(delimitedString);
    } else {
       values = splitByLiteralString(delimitedString);
    }
    
    if (stripEnclosingQuotes) {
      stripEnclosingQuotes(values);
    }
    return values;
  }
  
  /**
   * Determines whether to treat the <code>delimiter</code> as a regular
   * expression (rather than a literal string)
   * @return true if the delimiter should be interpreted as a regular expression
   * @see <code>java.util.regex.Pattern</code>
   */
  protected boolean treatDelimiterAsRegExp() {
	  return this.isDelimiterAlwaysRegExp() || 
	  (!this.isDelimiterAlwaysLiteralString() && this.getDelimiter().length() > 1);
  }

  private String [] splitByLiteralString(String delimitedString){
    String[] values = null;
    if (!protectQuotedFields || delimitedString.indexOf(quoteChar) == -1) {
      values = extractValuesLiteralString(delimitedString, delimiter);
    }else {
      values = extractQuotedValuesLiteralString(delimitedString, this.delimiter, this.quoteChar);
    }
    return values;
  }
  
  private String [] splitByRegularExpression(String delimitedString){
    String[] values = null;
    if (!protectQuotedFields || delimitedString.indexOf(quoteChar) == -1) {
      values = extractValuesRegExp(delimitedString, delimiter);
    } else {
      values = extractQuotedValuesRegExp(delimitedString, delimiter, quoteChar);
    }
    return values;
  }
  
  /**
   * Splits a string using a regular expression. Does not preserve blocks of characters
   * between quoteChars.
   * 
   * @param delimitedString
   * @param regexp
   * @return Array of Strings resulting from the regular explession split operation
   */
  protected String[] extractValuesRegExp(String delimitedString, String regexp) {
    return delimitedString.split(regexp, -1);
  }
  
  /**
   * Splits a string using a regular expression. Preserves blocks of characters
   * between quoteChars.
   * 
   * @param delimitedString the delimited string
   * @param regexp a regular expression delimiter
   * @param quoteChar quote character
   * @return an array of strings corresponding to the fields in the string supplied
   * ToDo: add escaping of quote characters
   */
  protected String[] extractQuotedValuesRegExp(String delimitedString, String regexp, char quoteChar) {
    char[] chars = delimitedString.toCharArray();
    boolean inQuotes = false;
    StringBuffer buffer = new StringBuffer();
    StringBuffer quoteBuffer = new StringBuffer();
    ArrayList quotes = new ArrayList();
    String escapeSeq = new String(new char[]{0, 0xff, 0, 0xff});

    /* replace quoted blocks with the escapeSeq */
    for (int i = 0; i < chars.length; i++) { 
      if (inQuotes) {
        inQuotes = chars[i] != quoteChar;
        quoteBuffer.append(chars[i]);
        if(!inQuotes){
          /* finished parsing a quote */
          buffer.append(escapeSeq);
          quotes.add(quoteBuffer.toString());
          quoteBuffer.setLength(0);
        }
      } else if (chars[i] == quoteChar) {
        inQuotes = delimitedString.indexOf(quoteChar, i+1) != -1;
        if(inQuotes){
          /* started quote parsing */
          quoteBuffer.append(chars[i]);
        }else{
          buffer.append(chars[i]);
        }
      } else {
        buffer.append(chars[i]);
      }
    }
  
    /* split the delimitedString and put back the quoted blocks */
    String [] result = buffer.toString().split(regexp, -1);
    java.util.Iterator it = quotes.iterator();
    for(int i=0; i<result.length; i++){
      if(result[i].indexOf(escapeSeq) != -1){
        result[i] = result[i].replaceAll(escapeSeq, (String)it.next());
      }
    }
    return result;
  }
  
  /**
   * Splits a string using a literal char delimiter. Preserves blocks of characters
   * between quoteChars.
   * 
   * This method originally had an algorithm parallel and almost identical to 
   * AbstractDelimitedStringConvertor#extractQuotedValuesLiteralString(String, String, char)
   * but operating on chars rather than Strings/StringBuffers. This seemed 
   * unnecessary and was replaced by a simple char->String conversion and forward
   * to the method taking a String. Code is cleaner & easier to test
   * at a certain performance cost (approx. 50% longer to execute).
   */
  protected String[] extractQuotedValuesLiteralString(String delimitedString, char d, char quoteChar) {
    return extractQuotedValuesLiteralString(delimitedString, new Character(d).toString(), quoteChar);
  }
  
  /**
   * Splits a string using a literal string delimiter. Does not preserve blocks of characters
   * between quoteChars.
   * 
   * @param delimitedString
   * @param delimiter
   * @return extracted tokens resulting from split operation.
   */
  protected String[] extractValuesLiteralString(String delimitedString, String delimiter) {
    char[] chars = delimitedString.toCharArray();
    List strings = new ArrayList();
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < chars.length; i++) {
      buffer.append(chars[i]);
      if (buffer.toString().endsWith(delimiter)) {
        strings.add(buffer.substring(0, buffer.length() - delimiter.length()));
        buffer.setLength(0);
      }
    }
    strings.add(buffer.toString());
    return (String[]) strings.toArray(new String[strings.size()]);
  }
  
  /**
   * Splits a string using a literal string delimiter. Preserves blocks of characters
   * between quoteChars.
   * <p>
   * This method is quite forgiving of poorly formatted data and will allow a mixture
   * of quoted and unquoted values and <i>partially</i> quoted values, such as:
   * <p>
   * <ul>
   * <li>one,two"two",three,four<br>
   * 	will return an array containing<ul>
   * 		<li>one</li>
   * 		<li>two"two"</li>
   * 		<li>three</li>
   * 		<li>four</li>
   * 	</ul>
   * </li>
   * <li>"one,one"one,two,three,four<br>
   * 	will return an array containing<ul>
   * 		<li>"one,one"one</li>
   * 		<li>two</li>
   * 		<li>three</li>
   * 		<li>four</li>
   * 	</ul>
   * </li>
   * <li>"one,one"one,"two,three",four<br>
   * 	will return an array containing<ul>
   * 		<li>"one,one"one</li>
   * 		<li>"two,three"</li>
   * 		<li>four</li>
   * 	</ul>
   * </li>
   * <li>"one,"two,"three",four<br>
   * 	will return an array containing<ul>
   * 		<li>"one,"two</li>
   * 		<li>"three"</li>
   * 		<li>four</li>
   * 	</ul>
   * </li>
   * <li>"one,"two"three","four"<br>
   * 	will return an array containing<ul>
   * 		<li>"one,"two"three"</li>
   * 		<li>"four"</li>
   * 	</ul>
   * </li>
   * <li>one,"two,three,four<br>
   * 	will return an array containing<ul>
   * 		<li>one</li>
   * 		<li>"two</li>
   * 		<li>three</li>
   * 		<li>four</li>
   * 	</ul>
   * </li>
   * </ul> 
   * Note there is currently no means of escaping quote characters.
   * @param str the string to split
   * @return a string array containing the (optionally) quoted values delimited by the
   * given delimiter string.
   * @see #setEscapeQuoteCharacters(boolean)
   * @see #setSmartEscapeQuoteCharacters(boolean)
   */
  protected String[] extractQuotedValuesLiteralString(String str, String delimiter, char quoteChar) {
    char[] chars = str.toCharArray();
    List strings = new ArrayList();
    
    /* Tracks whether the currently parsed string is inside a quote */
    boolean inQuotes = false;
    
    String parsed = "";
    for (int i = 0; i < chars.length; i++) {
      if(!escapeQuoteCharacters || chars[i]!= quoteEscapeChar){
        parsed += chars[i];
      }
      if (inQuotes) {
        
    	  /* 
           * deal with quote escaping if necessary.
           * we are (still) in quotes unless the current character is the quote character, 
           * and not an escaped quote character at that
           */
          if(escapeQuoteCharacters && (chars[i-1] == quoteEscapeChar)){
            continue;
          }
    	  inQuotes = chars[i] != quoteChar;
          
          /* deal with smart quote escaping if necessary */
          if(smartEscapeQuoteCharacters && chars[i]==quoteChar){
            String remainder = "";
            for(int j=i+1; j<chars.length; j++){
              remainder += chars[j];
              if(chars[j]==quoteChar && remainder.indexOf(delimiter)==-1){
                
                /* 
                 * We're dealing with a quote char that is in between two other quote chars
                 * but the succeeding quote char occurs *before* the delimiter.. we're remaining
                 * inQuotes, basically treating the quote in question as a normal character.
                 */ 
                inQuotes = true;
                break;
              }
            }
          }
      } else if (chars[i] == quoteChar) {
    	
          /*
    	   * we are entering a quoted block there are more quote characters to parse, 
           * and if this quote character is not escaped
           */
    	  inQuotes = str.indexOf(quoteChar, i+1) != -1;
          if(i !=0 && escapeQuoteCharacters){
            inQuotes = inQuotes && !(chars[i-1] == quoteEscapeChar);
          }       
      } else if (parsed.endsWith(delimiter)) {
    	 
          /*
           * we are not in quotes and we've parsed a delimiter
           * so add the parsed string
    	   */
          strings.add(parsed.substring(0, parsed.length() - delimiter.length()));
          parsed = "";
      }
    }
    
    /* add whatever's left at the end */
    strings.add(parsed);
    return (String[]) strings.toArray(new String[strings.size()]);
  }

  
  /**
   * Convert an ordered map into a delimited String. <p/>
   * 
   * If the <code>fieldNames</code> have been provided then use them to get the attribute values from the map. 
   * In this case the order that the fields are output is based on the order of the <code>fieldNames</code>. <p/>
   * 
   * Otherwise, we output all the attributes from the map in the order that they are encountered by the map.get()
   * call.
   * <p/>
   * 
   * Fields will be quoted if necessary (ie. if addNeededEnclosingQuotes is set).
   * 
   * @param map
   *          the record source
   * 
   * @return DelimitedString representing the ordered map
   * 
   * @throws NullRecordException
   *           if the map is null
   * @throws RecordFormatException
   *           if the field being quoted is not a CharSequence
   */
  protected String convertOrderedMapToDelimitedString(IOrderedMap map) throws RecordException {
    if (map == null)
      throw new NullRecordException("Null values not permitted");

    StringBuffer sb = new StringBuffer();
    int count = map.size();
    if (fieldNames != null) { // Then just output those fields.
      for (int i = 0; i < fieldNames.length; i++) {
        String fieldName = fieldNames[i];
        if (map.containsKey(fieldName)){
          sb.append(addEnclosingQuotes(map.get(fieldName)));
        }
        else{
          log.warn("map does not contain expected key \'" + fieldName + "\'. value will be empty");
        }

        if (i < fieldNames.length - 1){
          sb.append(delimiter);
        }
      }
    } else { // Output every field found.
      for (int i = 0; i < count; i++) {
        sb.append(addEnclosingQuotes(map.get(i)));
        if (i < count - 1){
          sb.append(delimiter);
        }
      }
    }
    return sb.toString();
  }

  /**
   * Convert an ordered map into a delimited String header (containing the names of the attributes rather than their
   * values). <p/>
   * 
   * If <code>fieldNames</code> has been set then we simply use them. Otherwise we take each attribute in the supplied
   * map and add its name to the list <p/>
   * 
   * Fields will be quoted if necessary (ie. if addNeededEnclosingQuotes is set).
   * 
   * @param map
   * 
   * @return DelimitedString representing the ordered map header
   * 
   * @throws NullRecordException
   *           if the map is null
   * @throws RecordFormatException
   *           if the field being quoted is not a CharSequence
   */
  protected String convertOrderedMapToDelimitedStringHeader(IOrderedMap map) throws RecordException {
    if (map == null)
      throw new NullRecordException("Null values not permitted");

    StringBuffer sb = new StringBuffer();
    if (fieldNames != null) { // Then just output those fields.
      for (int i = 0; i < fieldNames.length; i++) {
        if (i > 0)
          sb.append(delimiter);
        sb.append(addEnclosingQuotes(fieldNames[i]));
      }
    } else { // Output every field found.
      List keys = map.keys();
      for (int i = 0; i < keys.size(); i++) {
        if (i > 0)
          sb.append(delimiter);
        sb.append(addEnclosingQuotes(keys.get(i)));
      }
    }
    return sb.toString();
  }

  /**
   * Checks that the incoming record structure is as expected. If the number of fields received is greater than the
   * number expected then we write a warning to the logs saying that the extra fields will have auto-generated field
   * names.
   * 
   * @param expected
   *          number of fields expected
   * @param received
   *          number of fields received
   * 
   * @throws RecordFormatException
   *           if the number of expected fields is greater than the number received.
   */
  private static void validateStructure(int expected, int received) throws RecordException {
    if (expected > received) {
      throw new RecordFormatException("Expected " + expected + " fields, but received only " + received);
    }

    else if (received > expected)
      log.warn(received + " fields, received, but " + expected + " expected. Remainder will have automatic names");
  }

  /**
   * Loops through the array of strings and strips any enclosing quote chars from each element.
   * 
   * @param strings
   */
  private void stripEnclosingQuotes(String[] strings) {
    if (strings == null)
      return;

    for (int i = 0; i < strings.length; i++)
      strings[i] = stripEnclosingQuotes(strings[i]);
  }

  /**
   * If the supplied string starts AND ends with a <code>quoteChar</code> then they will be removed.
   * 
   * @param possiblyQuotedString
   * 
   * @return the "dequoted" string
   */
  private String stripEnclosingQuotes(String possiblyQuotedString) {
    String s = possiblyQuotedString;
    if (s == null)
      return s;

    int lastIndex = s.length() - 1;
    if ((lastIndex > 0) && (s.charAt(0) == quoteChar) && (s.charAt(lastIndex) == quoteChar))
    	s = s.substring(1, lastIndex);

    return s;
  }

  /**
   * Wraps the field in <code>quoteChar</code> if:
   * 
   * <code>addNeededEnclosingQuotes</code> is set to true and the field contains the delimiter string. 
   * 
   * or
   * 
   * <code>forceEnclosingQuotes</code> is set to true.
   * 
   * @param field
   * 
   * @return the quoted string if necessary
   */
  private Object addEnclosingQuotes(Object field) throws RecordFormatException {
    // #SC11 - No longer check that field is a charSequence (hence no longer throws RecordFormatException if not)
    Object result = field;
    if (field != null && (addNeededEnclosingQuotes || forceEnclosingQuotes)) {
        String s = (field instanceof String) ? (String) field : field.toString();

        if (s.indexOf(delimiter) >= 0 || forceEnclosingQuotes) {
          // It contains the delimiter and this bean has been told to insert quotes.
          if (s.indexOf(quoteChar) >= 0) {
            // Houston, we have a problem -- already contains embedded quote char
            log.warn("Field value already contains an embedded quote character, so not been quoted: " + s);
          } else {
            // Quote the field value
            StringBuffer sb = new StringBuffer(2 + s.length());
            sb.append(quoteChar).append(s).append(quoteChar);
            result = sb.toString();
          }
        }
    }
    return result;
  }

}
