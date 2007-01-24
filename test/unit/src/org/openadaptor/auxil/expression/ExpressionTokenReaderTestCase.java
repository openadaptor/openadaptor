package org.openadaptor.auxil.expression;

import java.util.Iterator;

import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.expression.ExpressionTokenReader;

import junit.framework.TestCase;

public class ExpressionTokenReaderTestCase extends TestCase {

  private String process(String line) throws ExpressionException {
    StringBuffer buffer = new StringBuffer();
    Iterator it = ExpressionTokenReader.getTokenList(line, true).iterator();
    while (it.hasNext()) {
      buffer.append("^").append(it.next());
    }
    buffer.append("^");
    return buffer.toString();
  }

  public void test() {
    assertTrue(process("(5+5)*5-7").equals("^(^5^+^5^)^*^5^-^7^"));
    assertTrue(process("3+4").equals("^3^+^4^"));
  }
}
