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

package org.openadaptor.auxil.expression;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.expression.function.AdaptorStartFn;
import org.openadaptor.auxil.expression.function.DateFn;
import org.openadaptor.auxil.expression.function.DateParseFn;
import org.openadaptor.auxil.expression.function.EndsWithFn;
import org.openadaptor.auxil.expression.function.FloorFn;
import org.openadaptor.auxil.expression.function.FormatFn;
import org.openadaptor.auxil.expression.function.IsNullFn;
import org.openadaptor.auxil.expression.function.LengthFn;
import org.openadaptor.auxil.expression.function.LowerFn;
import org.openadaptor.auxil.expression.function.MatchesFn;
import org.openadaptor.auxil.expression.function.NowFn;
import org.openadaptor.auxil.expression.function.RoundFn;
import org.openadaptor.auxil.expression.function.StartsWithFn;
import org.openadaptor.auxil.expression.function.SubstringFn;
import org.openadaptor.auxil.expression.function.SystemPropertyFn;
import org.openadaptor.auxil.expression.function.TrimFn;
import org.openadaptor.auxil.expression.function.UpperFn;

/**
 * Expression support class to represent parsed tokens from expressions.
 * <p>
 * The tokens specify the following:
 * <UL>
 * <LI>The type of token, or the operator associated with it</LI>
 * <LI>The value of the token as an Object (for numbers, variables and literals) </LI>
 * <LI>The input precedence of the token for postfix (RPN) processing</LI>
 * <LI>A flag (resolved)indicating whether or not a variable requires de-referencing</LI>
 * <LI>An optional postfix function which is associated with an operator.
 * </UL>
 * //ToDo: Check if resolved flag can be removed by using a leading '{' as an indicator
 * @deprecated ScriptProcessor or ScriptFilterProcessor may be used in place of Expressions
 */
public class ExpressionToken {

  private static final Log log = LogFactory.getLog(ExpressionToken.class);

  // Allowable Types
  public static final int CH_EOF = -1; // Sentinel value

  public static final int TYPE_VALUE = 0; // Values - identifiers,numbers.

  public static final int TYPE_FN = '\u2231';// Function. Arbitrary (otherwise unused) value

  // token values for operators, as seen by the StreamReader
  public static final int OP_PLUS = '+';

  public static final int OP_MINUS = '-';

  public static final int OP_MUL = '*';

  public static final int OP_DIV = '/';

  public static final int OP_L_BRACKET = '(';

  public static final int OP_R_BRACKET = ')';

  public static final int OP_GT = '>';

  public static final int OP_LT = '<';

  public static final int OP_EQ = '=';

  public static final int OP_NE = '\u2260'; // !=

  public static final int OP_LE = '\u2264'; // <=

  public static final int OP_GE = '\u2265'; // >=

  public static final int OP_AND = '&';

  public static final int OP_OR = '|';

  public static final int OP_NOT = '!';

  // public static final int OP_EXISTS = '\u2203'; // There exists symbol (linkbackwards E)
  public static final int OP_NULL = '\u2205'; // Empty set (zero with slash through)

  public static final int OP_L_BRACE = '{';

  public static final int OP_R_BRACE = '}';

  public static final int OP_COMMA = ','; // Will be needed for function args.

  /*
   * No Longer used - replaced by functions upper() and lower() //Not too sure about these guys, but they get us out of
   * a hole. //They represent String.toUpperCase() and String.toLowerCase //as unary operators. public static final int
   * OP_UPPER = '\u22C0'; //Arbitrary - looks like upside down V public static final int OP_LOWER = '\u22C1';
   * //Arbitrary - looks like v //public static final int OP_SIZE = '\u2240'; //Arbitrary character (box with X)
   */
  /*
   * These are, as yet, unused. public static final int OP_DOLLAR = '$';
   */

  // Tokens and their precedences.
  // Lower numbers mean higher precedence.
  private static final int PREC_EOF = 100;

  private static final int PREC_UNARY = 30;

  private static final int PREC_L_BRACKET = 80;

  private static final int PREC_R_BRACKET = 0;

  private static final int PREC_COMMA = PREC_R_BRACKET;

  private static final int PREC_MULOP = 40;

  private static final int PREC_ADDOP = 60;

  private static final int PREC_BOOLOP = 20;

  private static final int PREC_FUNC = 10;

  private static final int PREC_VALUE = 0;

  public static final ExpressionToken EOF = new ExpressionToken(ExpressionToken.CH_EOF, PREC_EOF, "<EOF>", null);

  public static final ExpressionToken UNARY_MINUS = new ExpressionToken(ExpressionToken.OP_MINUS, PREC_UNARY,
      new UnaryOperation(OP_MINUS));

  public static final ExpressionToken UNARY_PLUS = new ExpressionToken(ExpressionToken.OP_PLUS, PREC_UNARY,
      new UnaryOperation(OP_PLUS));

  public static final ExpressionToken NOT = new ExpressionToken(ExpressionToken.OP_NOT, PREC_UNARY, new UnaryOperation(
      OP_NOT));

  /*
   * No Longer used - replaced by functions upper() and lower() public static final ExpressionToken LOWER= new
   * ExpressionToken(ExpressionToken.OP_LOWER,PREC_UNARY+1,"lower",new UnaryOperation(OP_LOWER)); public static final
   * ExpressionToken UPPER= new ExpressionToken(ExpressionToken.OP_UPPER,PREC_UNARY+1,"upper",new
   * UnaryOperation(OP_UPPER));
   */
  public static final ExpressionToken L_BRACKET = new ExpressionToken(ExpressionToken.OP_L_BRACKET, PREC_L_BRACKET);

  public static final ExpressionToken R_BRACKET = new ExpressionToken(ExpressionToken.OP_R_BRACKET, PREC_R_BRACKET);

  public static final ExpressionToken COMMA = new ExpressionToken(ExpressionToken.OP_COMMA, PREC_COMMA);

  public static final ExpressionToken MUL = new ExpressionToken(OP_MUL, PREC_MULOP, new BinaryOp(OP_MUL));

  public static final ExpressionToken DIV = new ExpressionToken(OP_DIV, PREC_ADDOP, new BinaryOp(OP_DIV));

  public static final ExpressionToken PLUS = new ExpressionToken(OP_PLUS, PREC_ADDOP, new BinaryOp(OP_PLUS));

  public static final ExpressionToken MINUS = new ExpressionToken(OP_MINUS, PREC_ADDOP, new BinaryOp(OP_MINUS));

  public static final ExpressionToken EQ = new ExpressionToken(OP_EQ, PREC_BOOLOP, new BinaryOp(OP_EQ));

  public static final ExpressionToken NE = new ExpressionToken(OP_NE, PREC_BOOLOP, new BinaryOp(OP_NE));

  public static final ExpressionToken GT = new ExpressionToken(OP_GT, PREC_BOOLOP, new BinaryOp(OP_GT));

  public static final ExpressionToken LT = new ExpressionToken(OP_LT, PREC_BOOLOP, new BinaryOp(OP_LT));

  public static final ExpressionToken LE = new ExpressionToken(OP_LE, PREC_BOOLOP, new BinaryOp(OP_LE));

  public static final ExpressionToken GE = new ExpressionToken(OP_GE, PREC_BOOLOP, new BinaryOp(OP_GE));

  public static final ExpressionToken AND = new ExpressionToken(OP_AND, PREC_BOOLOP, new BinaryOp(OP_AND));

  public static final ExpressionToken OR = new ExpressionToken(OP_OR, PREC_BOOLOP, new BinaryOp(OP_OR));

  public static final ExpressionToken ZERO = new ExpressionToken(new Long(0));

  public static final ExpressionToken NULL = new ExpressionToken(null);

  /**
   * Ordered list of operator types. This is used for ExpressionToken operator lookups using the type alone. Note: This
   * is used in conjunction with OPERATOR_TOKENS and thus must be kept in sync
   */
  private static final int[] OPERATORS = { OP_PLUS, OP_MINUS, OP_MUL, OP_DIV, OP_L_BRACKET, OP_R_BRACKET, OP_EQ, OP_NE,
      OP_GT, OP_LT, OP_LE, OP_GE, OP_AND, OP_OR, OP_NOT, OP_NULL, OP_COMMA };

  /**
   * Expression token lookup table (may be looked up by their type). Note: This is used in conjunction with OPERATORS
   * and thus must be kept in sync
   */
  private static final ExpressionToken[] OPERATOR_TOKENS = { PLUS, MINUS, MUL, DIV, L_BRACKET, R_BRACKET, EQ, NE, GT,
      LT, LE, GE, AND, OR, NOT, NULL, COMMA };

  /**
   * Internal lookup table to resolve operatoraliases into ExpressionTokens.
   */
  private static final Map operatorAliases = getOperatorAliases();

  private static final Map functionMap = getDefaultFunctionMap();

  /**
   * This holds the value (e.g. String, number), if any, for this token.
   */
  public Object value;

  /**
   * This holds the type of the token. It also doubles as the operator symbol for operators.
   */
  public final int type;

  /**
   * This specifies the precedence of this token for postfix (RPN) processing Lower numbers indicate higher precedences.
   */
  public final int inputPrecedence;

  /**
   * Flag to indicate whether a value has been fully resolved. Note: It's generally true for everything except variable
   * tokens
   */
  public boolean resolved = true;

  /**
   * This holds the postfix function which will be executed by operators. Only operators need specify this (and already
   * it's done above)
   */
  public final IPostfixFunction function;

  /**
   * This holds the reqired top-of-stack value to allow short circuit of boolean operation.
   * <p>
   * Unused if shortCircuitNextIndex is zero.
   */
  public boolean shortCircuitExpectedStackValue = false;// Only used if shortCircuitNextIndex>0

  /**
   * Execution step to jump to if short-circuiting, and top of stack value matches shortCircuitExpectedStackValue.
   * <p>
   * Zero (default) means no short circuit.
   */
  public int shortCircuitNextIndex = 0;

  /**
   * Primes operator aliases.
   * 
   * @return Map of alias-to-ExpressionToken entries.
   */
  private static final Map getOperatorAliases() {
    Map map = new TreeMap();
    map.put("eq", EQ);
    map.put("ne", NE);

    map.put("gt", GT);
    map.put("lt", LT);

    map.put("ge", GE);
    map.put("le", LE);

    map.put("and", AND);
    map.put("or", OR);
    map.put("not", NOT);

    // map.put("exists",EXISTS);
    map.put("null", NULL);

    /*
     * No Longer used - replaced by functions upper() and lower() map.put("lower",LOWER); map.put("upper",UPPER);
     */
    return map;
  }

  // private static final Map getDefaultFunctionMap() {
  // Map map=new TreeMap();
  // addFunction(map,SubstringFn.NAME,new SubstringFn());
  // addFunction(map,RoundFn.NAME,new RoundFn());
  // addFunction(map,EndsWithFn.NAME,new EndsWithFn());
  // addFunction(map,FloorFn.NAME,new FloorFn());
  // addFunction(map,ToUpperCaseFn.NAME,new ToUpperCaseFn());
  // addFunction(map,ToLowerCaseFn.NAME,new ToLowerCaseFn());
  // addFunction(map,StartsWithFn.NAME,new StartsWithFn());
  // addFunction(map,LengthFn.NAME,new LengthFn());
  // addFunction(map,MatchesFn.NAME,new MatchesFn());
  // addFunction(map,NowFn.NAME,new NowFn());
  // addFunction(map,FormatFn.NAME,new FormatFn());
  // addFunction(map,DateParseFn.NAME,new DateParseFn());
  // addFunction(map,SystemPropertyFn.NAME,new SystemPropertyFn());
  // addFunction(map,AdaptorStartFn.NAME,new AdaptorStartFn());
  // addFunction(map,DateFn.NAME,new DateFn());
  // return map;
  // }
  //
  // /**
  // * Add a function definition.
  // * @param map
  // * @param functionName
  // * @param function
  // */
  // private static void addFunction(Map map,String functionName,IPostfixFunction function){
  // ExpressionToken functionToken=new ExpressionToken(TYPE_FN,0,functionName+"()",function);
  //
  // //Hack!
  // //The resolved flag is currently used to represent 'arguments read' by the tokeniser.
  // //Once the args are in, the flag is set to true.
  // functionToken.resolved=false;
  // map.put(functionName,functionToken);
  // }
  private static final Map getDefaultFunctionMap() {
    Map map = new TreeMap();
    register(map, new SubstringFn());
    register(map, new RoundFn());
    register(map, new EndsWithFn());
    register(map, new FloorFn());
    register(map, new UpperFn());
    register(map, new LowerFn());
    register(map, new StartsWithFn());
    register(map, new LengthFn());
    register(map, new MatchesFn());
    register(map, new NowFn());
    register(map, new FormatFn());
    register(map, new DateParseFn());
    register(map, new SystemPropertyFn());
    register(map, new AdaptorStartFn());
    register(map, new DateFn());
    register(map, new IsNullFn());
    register(map, new TrimFn());
    return map;
  }

  private static void register(Map map, IPostfixFunction function) {
    if ((map != null) && (function != null)) {
      String functionName = function.getName();
      if (functionName != null) {
        if (!map.containsKey(functionName)) { // Time to add it then.
          ExpressionToken functionToken = new ExpressionToken(TYPE_FN, PREC_FUNC, functionName + "()", function);
          // Hack!
          // The resolved flag is currently used to represent 'arguments read' by the tokeniser.
          // Once the args are in, the flag is set to true.
          functionToken.resolved = false;
          map.put(functionName, functionToken);
        } else {
          log.warn("Ignoring attempt to register duplicate function with name " + functionName);
        }
      } else {
        log.warn("Ignoring attempt to register function with <null> name ");
      }
    }
  }

  public static void register(IPostfixFunction function) {
    register(functionMap, function);
  }

  /**
   * General purpose constructor for Initialising the Operator Tokens. See variants for more detail.
   * 
   * @param type
   *          the type of this token.
   * @param inputPrecedence
   * @param value
   * @param function
   */
  private ExpressionToken(int type, int inputPrecedence, Object value, IPostfixFunction function) {
    this.type = type;
    this.inputPrecedence = inputPrecedence;
    this.value = value;
    this.function = function;
  }

  /**
   * Constructor for literal tokens. A value token has type of TYPE_VALUE, zero precedence and no associated function
   * //ToDo: Investigate hooking in a function here to get at Record values perhaps.
   * 
   * @param value
   */
  public ExpressionToken(Object value) {
    this(ExpressionToken.TYPE_VALUE, PREC_VALUE, value, null);
  }

  /**
   * Constructor for user types (functions).
   * 
   * @param type
   * @param value
   */
  public ExpressionToken(int type, Object value) {
    this(type, 0, value, null);
  }

  /**
   * Constructor for literal or variable tokens. Effect is same as ExpressionToken(Object value), but with the resolved
   * flag set appropriately (generally false for variables).
   * 
   * @param value
   * @param resolved
   */
  public ExpressionToken(Object value, boolean resolved) {
    this(value);
    this.resolved = resolved;
  }

  /**
   * Constructor for Tokens with no associated value or function (such as brackets).
   * 
   * @param type
   * @param inputPrecedence
   */
  private ExpressionToken(int type, int inputPrecedence) {
    this(type, inputPrecedence, null, null);
  }

  /**
   * Constructor for operators with associated functions.
   * 
   * @param type
   * @param inputPrecedence
   * @param function
   */
  private ExpressionToken(int type, int inputPrecedence, IPostfixFunction function) {
    this(type, inputPrecedence, null, function);
  }

  /**
   * Return a verbose String representation of the token. Doesn't currently show the precedence, or the function, if
   * any.
   * 
   * @return String with the type and value, if any of the Token.
   */
  public String toStringVerbose() {
    return ("Token[" + type + "] " + (value == null ? (String.valueOf((char) type)) : value));
  }

  /**
   * Returns a String representation of this token. For an operator it will be operator's character. For values it will
   * be the value.
   * 
   * @return String corresponding to the token.
   */
  public String toString() {
    return (value == null ? (String.valueOf((char) type)) : value.toString());
  }

  /**
   * Return the Token associated with the supplied token type. Returns null if there is no matching operator.
   * 
   * @param tokenType
   * @return The operator Token, or null of no operator matches.
   */
  public static ExpressionToken getOperator(int tokenType) {
    ExpressionToken result = null;
    for (int i = 0; i < OPERATORS.length; i++) {
      if (OPERATORS[i] == tokenType) {
        result = OPERATOR_TOKENS[i];
        break;
      }
    }
    return result;
  }

  /**
   * Return the operator Token mapped to the supplied alias. Returns null if no such alias exists. Note: the alias is
   * converted to lower case before the lookup is performed.
   * 
   * @param operatorAlias
   * @return The Token for the operator, or null.
   */
  public static ExpressionToken getOperator(String operatorAlias) {
    return (ExpressionToken) operatorAliases.get(operatorAlias.toLowerCase());
  }

  /**
   * Return the function (if any) mapped to the supplied function name. Returns null if no such alias exists. Note: the
   * alias is converted to lower case before the lookup is performed.
   * 
   * @param functionName
   * @return The Token for the operator, or null.
   */
  public static ExpressionToken getFunction(String functionName) {
    return (ExpressionToken) functionMap.get(functionName.toLowerCase());
  }

}
