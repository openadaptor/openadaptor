/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.*;
import org.openadaptor.core.*;
import org.openadaptor.core.exception.*;

public class CustomBuySellValidator extends Component implements IDataProcessor {

  private String fieldName="buySell";
  private List legalValues=new ArrayList();
  private boolean discardBadValues=true;

  public void setFieldName(String fieldName) {
    this.fieldName=fieldName;
  }
  public String getFieldName() {
    return fieldName;
  }
  public void setLegalValues(List legalValues) {
    this.legalValues=legalValues;
  }
  public void setDiscardBadValues(boolean discard) {
    this.discardBadValues=discard;
  }

  public Object[] process(Object data) {
    Object[] output=new Object[]{};
    if (data instanceof Map) {
      Map map = (Map) data;
      Object value = map.get(fieldName);
      if (legalValues.contains(value)){
        output=new Object[]{data};
      }
      else { //Not legal - discard or exception!
        if (!discardBadValues) {
          throw new ProcessingException("Error: Illegal value for "+fieldName, this);
        }
      }
    }
    else {
      throw new RecordFormatException("Expected Map data, but got: "+data.getClass().getName());
    }
    return output;
  }

  public void reset(Object context) {}

  public void validate(List exceptions) {
    if (legalValues.isEmpty()) {
      exceptions.add(new ValidationException("legalValues property may not be empty", this));
    }
  }

}
