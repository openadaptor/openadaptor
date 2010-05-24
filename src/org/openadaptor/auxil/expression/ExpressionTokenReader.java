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

package org.openadaptor.auxil.expression;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class splits input expressions into tokens for evaluation.
 * <p>
 * It roughly operates as follows:
 * <UL>
 * <LI>Read an expression in the form of a String.</LI>
 * <LI>Preprocess the expression by substituting multi-char operators (e.g. "<=") with a single char equivalent (e.g.
 * '\u2264') </LI>
 * <LI>It will then generate tokens for each literal,variable,number or operator it encounters until the end of the
 * expression.</LI>
 * <LI>During parsing, operator aliases (such as LE) are also converted to their single-char equivalent. The valid set
 * of aliases is defined in <code>NewToken</code>.
 * <LI>The resulting list of tokens is returned as a <code>List</code></LI>
 * </UL>
 * The returned tokens are each an instance of <code>NewToken</code> <BR>
 * The Various types are parsed as follows:
 * <UL>
 * <LI>Numbers are parsed as <code>Double</code> or <code>Long</code> depending on the presence or absence of a
 * decimal point/LI>
 * <LI>Operators are parsed as operator tokens (post substitution mentioned above)</LI>
 * <LI>Any token enclosed in curly braces {} are treated as variables</LI>
 * <LI>Any token enclosed in quotes ('' or "") are treated as literal strings (quotes may be embedded by using the
 * single within double or vice versa). Currently no escape sequences are recognised.</LI>
 * </UL>
 * 
 * Some sample (legal) expressions are: <BR>
 * Expression: "3+4*(7-5)" <BR Preprocessed output: "3+4*(7-5)" <BR>
 * Parsed tokens: |3|+|4|*|(|7|-|5|)| <BR>
 * //ToDo: Add more examples.
 * 
 * @see Expression for more detail on valid expressions.
 * @deprecated ScriptProcessor or ScriptFilterProcessor may be used in place of Expressions
 */
public class ExpressionTokenReader {

  private static final Log log = LogFactory.getLog(ExpressionTokenReader.class);

  StreamTokenizer st;

  List tokens;

  Reader reader;

  int ch = ' ';

  StringBuffer sb;

  private boolean allowFunctions = true;

  private static String[][] opSubstitutions = ExpressionTokenReader.getOperatorSubstitutions();

  /**
   * Lookup table for whitespace characters limited to (0 <= ch <= 255)
   */
  private boolean[] whitespaceCharTable = new boolean[256];

  // Unused
  // private boolean[] wordCharTable=new boolean[256];
  /**
   * Lookup table for numeric characters limited to (0 <= ch <= 255)
   */
  private boolean[] numberCharTable = new boolean[256];

  /**
   * Lookup table for quote characters limited to (0 <= ch <= 255)
   */
  private boolean[] quoteCharTable = new boolean[256];

  /**
   * Return a list of recognised tokens from the supplied expression string
   * 
   * @param expression
   * @return a List containing NewToken entries.
   * @throws ExpressionException
   *           if a problem is encountered.
   */
  public static List getTokenList(String expression, boolean allowFunctions) throws ExpressionException {
    expression = ExpressionTokenReader.preProcess(expression);
    log.debug("Pre-processed expression now reads: " + expression);
    return ExpressionTokenReader.getTokenList(new StringReader(expression), allowFunctions);
  }

  /**
   * Return a list of recognised tokens from the supplied reader. Note that the reader is closed when reading is
   * complete.
   * 
   * @param in
   *          a reader which should have the (pre-processed) expression.
   * @return list of tokens parsed from the supplied <code>Reader</code>
   * @throws org.openadaptor.expression.ExpressionException
   */
  private static List getTokenList(Reader in, boolean allowFunctions) throws ExpressionException {
    List tokens = null;
    try {
      tokens = new ExpressionTokenReader(in, allowFunctions).generateTokens();
      in.close();
    } catch (IOException ioe) {
      throw new ExpressionException(ioe.getMessage(), ioe);
    }
    return tokens;
  }

  private static String[][] getOperatorSubstitutions() {
    return new String[][] { { "<=", String.valueOf((char) ExpressionToken.OP_LE) },
        { ">=", String.valueOf((char) ExpressionToken.OP_GE) }, { "!=", String.valueOf((char) ExpressionToken.OP_NE) } };
  }

  private static String preProcess(String expression) {
    String result = expression.trim();
    for (int i = 0; i < ExpressionTokenReader.opSubstitutions.length; i++) {
      result = result.replaceAll(ExpressionTokenReader.opSubstitutions[i][0],
          ExpressionTokenReader.opSubstitutions[i][1]);
    }
    return result;
  }

  /**
   * Create the tokenGenerator given a reader. private - users should use the static method.
   * 
   * @param in
   *          A reader which should contain the expression
   */
  private ExpressionTokenReader(Reader in, boolean allowFunctions) {
    reader = in;
    sb = new StringBuffer();
    this.allowFunctions = allowFunctions;
    initialise();
  }

  /**
   * Initialise the lookup tables for the token generator.
   */
  private void initialise() {
    clearTable(whitespaceCharTable);
    clearTable(numberCharTable);
    clearTable(quoteCharTable);
    setTableChars(whitespaceCharTable, '\u0000', '\u0020');

    /*
     * Unused clearTable(wordCharTable); setTableChars(wordCharTable,'A','Z'); //Uppercase chars
     * setTableChars(wordCharTable,'a','z'); //Lowercase chars setTableChars(wordCharTable,new int[] {'_','#','$'});
     * //Miscellaneous setTableChars(wordCharTable,'\u00A0','\u00FF');//Accented chars, etc.
     */
    setTableChars(numberCharTable, '0', '9');
    // Note that we exclude '-' from the numeric set. This is part
    // of our unary '-' handling.
    // setTableChars(numberCharTable,new int[] {'-','.'});
    setTableChars(numberCharTable, new int[] { '.' });
    setTableChars(quoteCharTable, new int[] { '\'', '"' });
  }

  /**
   * Fetch the next token from the reader. Logic is crudely as follows: <code>
   *   skip whitespace
   *   if not end of data try these in order:
   *   (1) See if it is a number. If it is, check if it had a decimal point.
   *       Return a value token with a Double or Long accordingly.
   *
   *   (2) If current char is '{' Then read a variable (possibly with
   *       nested variables enclosed in '{' '}') to a corresponding '}'
   *       Return a value token with a Variable.
   *
   *   (3)Check if it is an operator. If it is, return it.
   *
   *   (4)See is it a literal, enclosed in quotes. If it is, read until a
   *      quote which matches the opening quote. Return value token with literal.
   *
   *   (5)Must be an operator alias. Read it, and see if it is. Throw exception if not.
   *      Return corresponding operator token.
   *
   * </code> Step (5) may be relaxed to allow other values which might correspond to, say, user supplied function
   * implementation or simiar.
   * 
   * @return The next parsed <code>ExpressionToken</code>
   * @throws ExpressionException
   */
  private ExpressionToken nextToken() throws ExpressionException {
    // log.debug("nextToken() ch is ["+ch+"] "+(char)ch);
    ExpressionToken token = ExpressionToken.EOF;
    // Skip whitespace
    try {
      while (isWhitespace(ch)) {
        ch = reader.read();
      }
      if (ch >= 0) { // Not end of input
        sb.setLength(0);
        // Check if it is a number first.
        if (isNumeric(ch)) {// Get thee a number
          // log.debug("Reading a number");
          boolean decimal = false;
          while (isNumeric(ch)) {
            if (ch == '.') {
              if (decimal) { // Already have a decimal point.
                break;
              } else {
                decimal = true;
              }
            }
            sb.append((char) ch);
            ch = reader.read();
          }
          if (decimal) {
            return new ExpressionToken(new Double(sb.toString()));
          } else {
            return new ExpressionToken(new Long(sb.toString()));
          }
          // log.debug("Number is: "+token.value);
        }
        // It's not numeric if we get this far.

        if (ExpressionToken.OP_L_BRACE == ch) { // It's a variable.
          log.debug("Reading a variable");
          int braceCount = 1;
          StringBuffer sb = new StringBuffer();
          sb.append((char) ch); // Add the opening brace.
          while (braceCount > 0) {
            ch = reader.read();
            switch (ch) {
            case -1:
              throw new ExpressionException("Variable has no closing " + ((char) ExpressionToken.OP_R_BRACE));
              // break;
            case ExpressionToken.OP_L_BRACE:
              braceCount++;
              break;
            case ExpressionToken.OP_R_BRACE:
              braceCount--;
              break;
            default:
              break;
            }
            sb.append((char) ch);
          }
          log.debug("Variable name is " + sb.toString());
          ch = reader.read(); // Move on.
          return new ExpressionToken(sb.toString(), false);
        }

        // Ok, so it's not a variable either. Try for an operator next.
        token = ExpressionToken.getOperator(ch);
        if (token != null) { // Bingo.
          ch = reader.read();
          return token;
        }
        // Now we're getting desperate. Get a word.
        // log.debug("reading a word");
        if (isQuote(ch)) { // Need to read quoted word
          log.debug("Reading quoted word");
          int quoteCh = ch; // This is the opening quote.
          StringBuffer sb = new StringBuffer();
          while (((ch = reader.read()) >= 0) && (quoteCh != ch)) {
            sb.append((char) ch);
          }
          if (quoteCh != ch) {
            throw new ExpressionException("Literal value has no closing quote - " + ((char) quoteCh));
          }
          log.debug("Quoted literal value is " + sb.toString());
          ch = reader.read(); // Skip the closing brace.
          return new ExpressionToken(sb.toString());
        }
        // Not a quoted literal - try for a bog standard literal.
        while ((ch >= 0) && (ch != ExpressionToken.OP_L_BRACE) && (!isNumeric(ch) && (!isWhitespace(ch)))
            && (ExpressionToken.getOperator(ch) == null)) {
          sb.append((char) ch);
          ch = reader.read();
        }
        String word = sb.toString();
        // Look up any operator aliases - eg GE LE etc.
        token = ExpressionToken.getOperator(word);
        if (token == null) { // See if there's a registered function.
          if (allowFunctions) {
            token = ExpressionToken.getFunction(word);
          }
          if (token == null) {
            throw new ExpressionException("Unrecognised token: " + word);
          }
        }
      }
    } catch (IOException ioe) {
      ExpressionTokenReader.log.error("Failed to read a token - " + ioe.toString());
      throw new ExpressionException("Failed to read a token - " + ioe.toString(), ioe);
    }

    return token;
  }

  /*
   * Unused. private boolean isWord(int ch) { return ((ch>=0) && (ch<256) && !isNumeric(ch) && !isWhitespace(ch) &&
   * (NewToken.getOperator(ch)==null) ); }
   */

  /**
   * Test if supplied value is numeric. This means it's between 0 and 255 and is in the numberCharTable.
   * 
   * @param ch
   *          candidate value
   * @return true ch is numeric, false otherwise.
   */
  private boolean isNumeric(int ch) {
    return ((ch >= 0) && (ch < 256) && numberCharTable[ch]);
  }

  /**
   * Test if supplied value is whitespace. This means it's between 0 and 255 and is in the whitespaceCharTable.
   * 
   * @param ch
   *          candidate value
   * @return true ch is whitespace, false otherwise.
   */
  private boolean isWhitespace(int ch) {
    return ((ch >= 0) && (ch < 256) && whitespaceCharTable[ch]);
  }

  /**
   * Test if supplied value is a quote. This means it's between 0 and 255 and is in the quoteCharTable.
   * 
   * @param ch
   *          candidate value
   * @return true ch is a quote, false otherwise.
   */
  private boolean isQuote(int ch) {
    return ((ch >= 0) && (ch < 256) && quoteCharTable[ch]);
  }

  /**
   * Read all of the tokens in the expression. Notes:
   * <UL>
   * <LI>Double and single quoted strings are stripped</LI>
   * <LI>Unary operators + and - are detected here. //ToDo: Find a better way of handling unary stuff.
   * 
   * @return List containing all of the tokens from the Tokenizer.
   * @throws ExpressionException
   *           if it cannot read a token
   */
  public List generateTokens() throws ExpressionException {
    ArrayList result = new ArrayList();
    // Extend to include other unary ops.
    boolean possibleUnaryPlusMinus = true;
    ExpressionToken token = nextToken();
    while (token != ExpressionToken.EOF) {
      if (possibleUnaryPlusMinus) {
        if (ExpressionToken.MINUS == token) {
          token = ExpressionToken.UNARY_MINUS;
        }
      }
      result.add(token);
      possibleUnaryPlusMinus = ExpressionToken.L_BRACKET == token; // Could have unary op next.
      token = nextToken();
    }
    return result;
  }

  /**
   * Utility method to set flags within a table of boolean values. Any out of range indices provided in the entries
   * array are ignored.
   * 
   * @param table
   *          the table of booleans to use
   * @param entries
   *          an int[] which contains the incices within the table which should be set
   */
  private static void setTableChars(boolean[] table, int[] entries) {
    setTableChars(table, entries, true);
  }

  /**
   * Utility to set or reset flags within a table of boolean values Any out of range indices provided in the entries
   * array are ignored.
   * 
   * @param table
   *          a table of boolean values
   * @param entries
   *          an array containing the indices to be set or reset
   * @param value
   *          the value to set the affected entries within the table.
   */
  private static void setTableChars(boolean[] table, int[] entries, boolean value) {
    for (int i = 0; i < entries.length; i++) {
      int entry = entries[i];
      if ((entry >= 0) & (entry < table.length)) {
        table[entry] = value;
      } else {
        log.warn("Ignoring entry - " + entry + " as is out of range");
      }
    }
  }

  /**
   * Set a flag on a consecutive set of entries within a boolean table. Out of range indices provided in the entries
   * array are clipped appropriately.
   * 
   * @param table
   *          a table of boolean values
   * @param low
   *          starting index
   * @param high
   *          ending index
   */
  private static void setTableChars(boolean[] table, int low, int high) {
    setTableChars(table, low, high, true);
  }

  /**
   * Set or reset a flag on a consecutive set of entries within a boolean table Out of range indices provided in the
   * entries array are clipped appropriately.
   * 
   * @param table
   *          a table of boolean values
   * @param low
   *          starting index
   * @param high
   *          ending index
   * @param value
   *          true or false depending on whether flag is being set or reset
   */
  private static void setTableChars(boolean[] table, int low, int high, boolean value) {
    int max = table.length - 1;
    if (low < 0)
      low = 0;
    if (high > max)
      high = max;
    for (int i = low; i <= high; i++) {
      table[i] = value;
    }
  }

  /**
   * Convenience method to clear all the flags in a table.
   * 
   * @param table
   *          boolean table.
   */
  private static void clearTable(boolean[] table) {
    setTableChars(table, 0, table.length - 1, false);
  }


}
