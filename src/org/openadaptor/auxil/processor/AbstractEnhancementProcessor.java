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
package org.openadaptor.auxil.processor;

import java.util.List;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.Component;
import org.openadaptor.core.IEnhancementProcessor;
import org.openadaptor.core.IEnhancementReadConnector;
import org.openadaptor.core.exception.ValidationException;

/**
 * Abstract implementation of {@link IEnhancementProcessor}.
 * 
 * @author Kris Lachor
 * @TODO javadocs
 */
public abstract class AbstractEnhancementProcessor extends Component implements IEnhancementProcessor {

  IEnhancementReadConnector readConnector;
  
  private String commaSeparatedIndexes;
  
  private String commaSeparatedFieldNames;
  
  /**
   * @see org.openadaptor.core.IEnhancementProcessor#getReadConnector()
   */
  public IEnhancementReadConnector getReadConnector() {
    return readConnector;
  }

  public void setReadConnector(IEnhancementReadConnector readConnector) {
    this.readConnector = readConnector;
  }

  /**
   * Validates: 
   * - is read connector set
   * - does read connector itself validates
   * 
   * @see org.openadaptor.core.IDataProcessor#validate(java.util.List)
   */
  public void validate(List exceptions) {
    if(readConnector==null){
      throw new ValidationException("Set an IReadConnector", this);
    }
    readConnector.validate(exceptions);
  }
  
  /**
   * @see org.openadaptor.core.IDataProcessor#reset(java.lang.Object)
   */
  public void reset(Object context) {
  }
  
  /**
   * @see org.openadaptor.core.IEnhancementProcessor#prepareParameters(java.lang.Object)
   */
  public abstract IOrderedMap prepareParameters(Object input);


  /**
   * @see org.openadaptor.core.IEnhancementProcessor#enhance(java.lang.Object, java.lang.Object[])
   */
  public abstract Object[] enhance(Object input, Object[] additionalData);
  
  /**
   * Allows user to specify indexes of the input record (ordered map) that hold parameters
   * to be passed to the reader.
   */
  public void setParameterIndexes(String commaSeparatedIndexes){
    this.commaSeparatedIndexes = commaSeparatedIndexes;
    //TODO
  }
  
  /**
   * Allows user to specify names of fields in the input record that hold parameters
   * to be passed to the reader.
   */
  public void setParameterNames(String commaSeparatedFieldNames){
    this.commaSeparatedFieldNames = commaSeparatedFieldNames;
    //TODO
  }

  public String getCommaSeparatedFieldNames() {
    return commaSeparatedFieldNames;
  }
  
}
