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

package org.openadaptor.auxil.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.thirdparty.dom4j.Dom4jUtils;

/**
 * This class extracts values from records (<code>ISimpleRecord</code> instances) and uses them to evaluate an
 * expression.
 * <p>
 * Expressions are pre-compiled to speed up execution.
 * <p>
 * Only valid simple expressions are supported.
 * <p>
 * Expressions not supported include:
 * <ul>
 * <li> <b>Missing trailing bracket</b> e.g. <code>"(round(9.3)"</code>.
 * <p>
 * Workaround: make sure you always have matching brackets. </li>
 * <li> <b>Expressions as arguments to nested functions</b> when they rely on sub-expression being treated as a full
 * expression e.g. <code>"format(dateparse('22.10.2006','dd.MM.yyyy') - 24*60*60*1000,'dd.MM.yyyy')"</code>.
 * <p>
 * Workaround: break into multiple expressions. </li>
 * </ul>
 */
public class Expression implements IExpression {

  private static Log log = LogFactory.getLog(Expression.class);

  private Stack postfixStack;

  private Stack operatorStack;

  private String expression;

  private Map typeConversionMap;

  /**
   * Temporary flag to enable/disable shortCircuitEvaluation. This should disappear once we're happy that the
   * short-circuit stuff works ok!
   */
  private boolean shortCircuitEvaluation = true;

  /**
   * If true, then throw an exception whenever an attribute is referenced, but doesn't exist within an ISimpleRecord
   * instance. default is false - it will just return null instead.
   */
  private boolean throwExceptionOnMissingAttribute = false;

  private ExpressionToken[] compiledExpression;

  // BEGIN Implementation of IExpression

  // Temporary flag to allow/prevent functions.
  // At the time of writing, function support is
  // very simplistic, and has the side effect of allowing many types
  // of non-sensical expressions to be specified.
  private boolean tmpFunctionSupport = true;

  // BEGIN Bean Accessors

  public void setExpression(String expression) throws ExpressionException {
    this.expression = expression;
    // compile the expression if it isn't null.
    compiledExpression = expression == null ? null : compile();
    /**
     * Process the compiled expression for possible short-circuiting of boolean (AND/OR) ops.
     */
    if (shortCircuitEvaluation && (compiledExpression != null)) {
      log.debug("Investigating evaluation short circuit possiblities.");
      applyShortCircuitEvaluation(compiledExpression);
    }

  }

  public String getExpression() {
    return expression;
  }

  /**
   * This map may be used to provide type 'hints' for arguments to the expression Currently supported types are
   * Double,Long,Date,String.
   * 
   * @return Map of attribute name to type
   */

  public Map getAttributeTypeMap() {
    return typeConversionMap;
  }

  /**
   * This map may be used to provide type 'hints' for arguments to the expression Currently supported types are
   * Double,Long,Date,String.
   * 
   * @param attributeTypeMap
   */
  public void setAttributeTypeMap(Map attributeTypeMap) {
    typeConversionMap = attributeTypeMap;
  }

  /**
   * Flag to indicate if short-circuit expression evaluation should be applied.
   * <p>
   * The default value is <tt>true</tt>
   * 
   * @param enabled
   *          short circuit evaluation is enabled if this is <tt>true</tt>
   */
  public void setShortCircuitEvaluation(boolean enabled) {
    this.shortCircuitEvaluation = enabled;
  }

  /**
   * Flag to indicate if short-circuit expression evaluation should be applied.
   * <p>
   * The default value is <tt>true</tt>
   * 
   * @return <tt>true</tt> if short circuit evaluation is enabled.
   */
  public boolean getShortCircuitEvaluation() {
    return shortCircuitEvaluation;
  }

  public boolean getThrowExceptionOnMissingAttribute() {
    return throwExceptionOnMissingAttribute;
  }

  public void setThrowExceptionOnMissingAttribute(boolean throwExceptionOnMissingAttribute) {
    this.throwExceptionOnMissingAttribute = throwExceptionOnMissingAttribute;
  }

  // END Bean Accessors

  /**
   * Evaluate the expression. This takes each token in turn and processes it against the postfix & operator stacks. When
   * all tokens are processed, the postfix stack should contain a single value which is the result of the expression.
   * 
   * @return Object containing the result of the expression.
   */
  public Object evaluate(ISimpleRecord record) throws RecordException {
    if (compiledExpression == null) {
      return null;
    }
    postfixStack.clear();
    for (int i = 0; i < compiledExpression.length; i++) {
      ExpressionToken token = compiledExpression[i];
      // Check for short circuit evaluation of boolean ops.
      if (token.shortCircuitNextIndex > 0) {
        Object stackVal = postfixStack.peek(); // Top of stack must contain a boolean
        if (stackVal instanceof Boolean) {
          if (token.shortCircuitExpectedStackValue == ((Boolean) stackVal).booleanValue()) { // Result is already
                                                                                              // known.
            log.debug("Shortcircuiting expression from step " + i + "to step " + token.shortCircuitNextIndex);
            i = token.shortCircuitNextIndex;
            token = compiledExpression[i];

            // For now we just push another of the same value so the boolean op will return the same. Ugh!
            // ToDo: Fix this - there's actually no need to execute the boolean op any more.
            postfixStack.push(new Boolean(token.shortCircuitExpectedStackValue)); // Push another of the same.
          }
        } else {
          log.warn("Short circuit evaluation expected a boolean on stack, but found " + stackVal);
        }
      }

      if (token.type == ExpressionToken.TYPE_VALUE) {
        Object value = token.resolved ? token.value : getValue(record, token.value);
        log.debug(token.value + " resolves to " + value);
        postfixStack.push(value);
      } else {
        token.function.execute(postfixStack);
        log.debug("Executing " + token.toString() + " yields " + postfixStack.peek());
      }
    }
    if (postfixStack.size() != 1) {
      throw new ExpressionException("Stack should have exactly one element, the result");
    }
    return postfixStack.pop();
  }

  // END Implementation of IExpression

  public Expression() {
    postfixStack = new Stack();
    operatorStack = new Stack();
  }

  // ToDo: Could optimise - put actual expected boolean into shortcircuitop (faster evaluate)

  /**
   * Add short circuit indicators for boolean operators AND and OR.
   * <p>
   * Basically it works out where the second operand of an operation begins, and adds a skip step which is followed if
   * the top of stack contains the appropriate value after evaluating the first operand.
   * <p>
   * 
   * @param compiledExpression
   *          The expression to be evaluated (fully until now)
   */
  private void applyShortCircuitEvaluation(ExpressionToken[] compiledExpression) {
    for (int i = 0; i < compiledExpression.length; i++) {
      ExpressionToken token = compiledExpression[i];
      if ((ExpressionToken.AND == token) || (ExpressionToken.OR == token)) {
        log.debug("Found " + token + " at index " + i);
        int steps = 1;
        int argIndex = i;
        while (steps > 0) {
          argIndex--;
          steps--;
          IPostfixFunction fn = compiledExpression[argIndex].function;
          if (fn != null) {
            steps += fn.getArgCount();
          }
        }
        ExpressionToken scToken = compiledExpression[argIndex];
        scToken.shortCircuitExpectedStackValue = (ExpressionToken.AND != token);
        scToken.shortCircuitNextIndex = i;
        log.debug("Second arg starts at: " + argIndex);
      }
    }
  }

  /**
   * Compile the expression.
   * 
   * @return the expression compiled into an <code>ExpressionToken[]</code>.
   * @throws ExpressionException
   */
  private ExpressionToken[] compile() throws ExpressionException {
    postfixStack.clear(); // Not strictly necessary
    operatorStack.clear();
    operatorStack.push(ExpressionToken.EOF);
    List steps = new ArrayList();
    Expression.log.debug("Compiling expression (function support=" + tmpFunctionSupport + ")");
    List tokenList = ExpressionTokenReader.getTokenList(expression, tmpFunctionSupport);
    Iterator it = tokenList.iterator();
    while (it.hasNext()) {
      ExpressionToken current = (ExpressionToken) it.next();
      processToken(steps, current);
    }
    processToken(steps, ExpressionToken.EOF);

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < steps.size(); i++) {
      sb.append(steps.get(i)).append("|");
    }
    log.info("Compiled steps: |" + sb.toString());

    log.debug("Expression compiled (" + steps.size() + " steps)");
    return (ExpressionToken[]) steps.toArray(new ExpressionToken[steps.size()]);
  }

  private void processToken(List steps, ExpressionToken token) throws ExpressionException {
    Expression.log.debug("Processing token " + token);
    ExpressionToken stackOp = (ExpressionToken) operatorStack.peek();
    // Check flag to see if functionSupport is enabled.
    // ToDo: More permanent solution to this.
    if (tmpFunctionSupport) {
      // If the last token was a function, the next token MUST be an open bracket.
      // This is a hack which uses the resolved flag to mean 'args read'
      if (stackOp.type == ExpressionToken.TYPE_FN) {
        if (!stackOp.resolved && (ExpressionToken.L_BRACKET != token)) {
          throw new ExpressionException("Expected " + ExpressionToken.L_BRACKET + " after function " + stackOp.value);
        } else {
          stackOp.resolved = true;
        }
      }
    }

    switch (token.type) {
    case ExpressionToken.TYPE_VALUE: // It's a value. Push it
      steps.add(token);
      break;
    case ExpressionToken.OP_L_BRACKET:
      operatorStack.push(token);
      break;
    // Functions handling is very iffy thus far
    // It's only included now because we really need substring functionality.
    case ExpressionToken.OP_COMMA: // Arguments
      // NB:
      // Function support is flaky at the moment. Thus it is being con
      if (tmpFunctionSupport) {// Behaviour is controlled by flag.
        while ((stackOp != ExpressionToken.L_BRACKET) && (stackOp != ExpressionToken.COMMA)
            && (stackOp != ExpressionToken.EOF)) {
          steps.add(stackOp);
          operatorStack.pop();// throw away the used operator.
          stackOp = (ExpressionToken) operatorStack.peek();
        }
        if (stackOp == ExpressionToken.COMMA) {
          operatorStack.pop();
        }
      } else {
        throw new ExpressionException((char) ExpressionToken.OP_COMMA + " is not allowed without function support");
      }
      break;

    case ExpressionToken.OP_R_BRACKET:
      while ((stackOp != ExpressionToken.L_BRACKET) && (stackOp != ExpressionToken.EOF)) {
        steps.add(stackOp);
        operatorStack.pop();// throw away the used operator.
        stackOp = (ExpressionToken) operatorStack.peek();
      }
      if (stackOp == ExpressionToken.L_BRACKET) {
        operatorStack.pop();
        stackOp = (ExpressionToken) operatorStack.peek();
      } else {
        throw new ExpressionException("Missing " + (char) ExpressionToken.OP_L_BRACKET);
      }
      break;
    default: // Regular operator
      while ((operatorStack.size() > 1) && stackOp.inputPrecedence <= token.inputPrecedence) {
        steps.add(stackOp);
        operatorStack.pop(); // Pop off the consumed operator
        stackOp = (ExpressionToken) operatorStack.peek();
      }
      if (token != ExpressionToken.EOF) {
        operatorStack.push(token);
      }
      break;
    }
  }

  /**
   * get the value of this token. If the value is a String and is surrounded by '{' '}' then it is an attribute
   * reference that needs further resolution, handled by getValue(String attributeReference)
   * 
   * @param tokenValue
   * @return the value of this variable.
   */
  private Object getValue(ISimpleRecord record, Object tokenValue) throws RecordException {
    Object result = tokenValue;
    if (tokenValue instanceof String) {
      String stringVal = (String) tokenValue;
      int len = stringVal.length();
      if ((len > 2) && (ExpressionToken.OP_L_BRACE == stringVal.charAt(0))
          && (ExpressionToken.OP_R_BRACE == stringVal.charAt(len - 1))) {
        result = getValue(record, stringVal.substring(1, len - 1));
      }

    }
    return result;
  }

  /**
   * Resolve the value of an attributeReference While there are embedded {} in the reference, then recursively resolve
   * them until the value has been retrieved. //ToDo: Improve javadoc here...
   * 
   * @param attributeReference
   * @return the value of the attribute that is referred to
   * @throws org.openadaptor.core.processor.RecordException
   */
  private Object getValue(ISimpleRecord record, String attributeReference) throws RecordException {
    // Expression.log.debug("Resolving attributeReference:"+attributeReference);
    if (record == null) {
      throw new ExpressionException("Cannot reference an attribute in a <null> record");
    }
    Object result;
    int open = attributeReference.indexOf(ExpressionToken.OP_L_BRACE);
    if (open < 0) { // Just return the value
      // First check if we need to guarantee existance of attribute.
      if (throwExceptionOnMissingAttribute) {
        // Need to verify that the record exists, or throw an exception
        if (!record.containsKey(attributeReference)) {
          throw new RecordException("Attribute " + attributeReference + " is expected, but cannot be accessed");
        }
      }
      // Ok. Just get the value.
      result = record.get(attributeReference);
      // Now check if we need to post-convert the type.
      if ((result != null) && (typeConversionMap != null) && (typeConversionMap.containsKey(attributeReference))) {
        // Need to attempt type conversion.
        result = Dom4jUtils.asTypedValue(result.toString(), (String) typeConversionMap.get(attributeReference));

      }
    } else {// Further dereferencing required.
      // Expression.log.debug("Still contains a reference");
      int close = attributeReference.lastIndexOf(ExpressionToken.OP_R_BRACE);
      String start = attributeReference.substring(0, open);
      String ref = attributeReference.substring(open + 1, close);
      String end = attributeReference.substring(close + 1);
      Expression.log.debug("|" + start + "|" + ref + "|" + end);
      return getValue(record, attributeReference.substring(0, open)
          + getValue(record, attributeReference.substring(open + 1, close)) + attributeReference.substring(close + 1));
    }
    return result;
  }

  public String toString() {
    return expression;
  }

  /**
   * Convenience method to create an expression from a String.
   * 
   * @param expressionString
   * @return IExpression which had been generated from the supplied Expression <code>String</code>
   * @throws ExpressionException
   */
  public static IExpression createExpressionFromString(String expressionString) throws ExpressionException {
    IExpression expression = new Expression();
    expression.setExpression(expressionString);
    return expression;
  }

  // ////////
  // Temporary development-testing-only methods

  /*
   * private static void process(IOrderedMap map, String line) { System.out.println("Processing : " + line); try {
   * Expression e = new Expression(); e.setExpression(line); Object result = e.evaluate(map); System.out.println(line + " = " +
   * result); } catch (RecordException ee) { System.out.println("Failed: " + ee.toString()); } catch
   * (NullPointerException npe) { System.out.println("NullPointerException: " + npe.toString()); } }
   */

  /*
   * public static void main(String args[]) throws IOException { RunAdaptor adaptorGenerator = new RunAdaptor(); try {
   * adaptorGenerator.initialise("dummy"); } catch (Exception e) { log.info("Ignoring adaptor exception " + e); }
   *  // command args ignored IOrderedMap map = new OrderedHashMap(); map.put("alpha", new Integer(4)); map.put("beta",
   * new Double(6.0)); map.put("stringOne", "alpha"); map.put("stringDate", "01/Jul/2006"); map.put("dateObject", new
   * Date());
   * 
   * map.put("market", "liffe"); map.put("id_liffe", "LI_04539"); map.put("nully", null); map.put("long1003", new
   * Long(1003)); map.put("long1002", new Long(1002)); BufferedReader br = new BufferedReader(new
   * InputStreamReader(System.in)); String line; // NewOperatorPrecedenceParser.process(map,"{stringOne}"); process(map,
   * "3/(4*5)"); process(map, "{alpha}/2"); process(map, "{id_{market}}+' is the id'"); process(map,
   * "dateparse({stringDate},'dd/MMM/yyyy') < (dateparse('31/Oct/2006','dd/MMM/yyyy'))"); process(map,
   * "dateparse({stringDate},'dd/MMM/yyyy') < dateparse('31/Oct/2006','dd/MMM/yyyy')"); process(map, "(3=4) & (4=5)");
   * process(map, "({nonExistent} eq null) or (dateparse({nonExistent},'dd.MM.yyyy') lt adaptorstart())"); process(map,
   * "({nonExistent} eq null) or ((dateparse({nonExistent},'dd.MM.yyyy') lt adaptorstart()))"); process(map, "not
   * ({nonExistent} ne null) and (not( dateparse({nonExistent},'dd.MM.yyyy') lt adaptorstart() ))"); // de morgan's law
   * applied to previous line process(map, "({stringDate} eq null) or (dateparse({nonExistent},'dd.MM.yyyy') lt
   * adaptorstart())"); // should fail process(map, "({stringDate} eq null) or ((dateparse({nonExistent},'dd.MM.yyyy')
   * lt adaptorstart()))"); // should fail process(map, "(null eq null) or (dateparse({nonExistent},'dd.MM.yyyy') lt
   * adaptorstart())"); process(map, "format(dateparse('22.10.2006','dd.MM.yyyy') - 1000,'dd.MM.yyyy')"); // should
   * fail: expressions as args not supported process(map,
   * "(dateparse(format(dateparse('22.10.2006','dd.MM.yyyy'),'dd.MM.yyyy'),'dd.MM.yyyy') lt adaptorstart())");
   * process(map, "(dateparse(format(dateparse('22.10.2006','dd.MM.yyyy') - 24*60*60*1000,'dd.MM.yyyy'),'dd.MM.yyyy') lt
   * adaptorstart())"); // should fail: expressions as args not supported process(map,
   * "dateparse(format(dateparse('22.10.2006','dd.MM.yyyy') - 24*60*60*1000,'dd.MM.yyyy'),'dd.MM.yyyy') lt
   * adaptorstart()"); // should fail: expressions as args not supported process(map,
   * "(dateparse('22.10.2006','dd.MM.yyyy') lt adaptorstart()"); // missing final ")" process(map, "(round(9.3)"); //
   * missing final ")" process(map, "format(dateparse('22.10.2006','dd.MM.yyyy') - 24*60*60*1000,'dd.MM.yyyy')"); //
   * fails: expression as argument to nested function while ((line = br.readLine()) != null) { process(map, line); } }
   */

}
