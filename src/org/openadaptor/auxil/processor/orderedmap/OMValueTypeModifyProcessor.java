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

package org.openadaptor.auxil.processor.orderedmap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Do a type conversion of a value in an OrderedMap. This processor can only convert to or from Strings.
 * todo replace with a SimpleRecord based version.
 */
public class OMValueTypeModifyProcessor extends OrderedMapModifyProcessor {

  //private static final Log log = LogFactory.getLog(OMValueTypeModifyProcessor.class);

  /**
   * Target type for conversion
   */
  protected String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  protected IOrderedMap applyToMap(IOrderedMap map) throws RecordException {

    Object existingValue = map.get(getAttribute());
    if (existingValue == null) {
      throw new RecordFormatException("Null value for attribute [" + getAttribute()
          + "] during OrderedMap Type Transform");
    }

    try {
      Class newTypeClass = Class.forName(getType());
      Class oldTypeClass = existingValue.getClass();
      Object newValue;

      if (oldTypeClass.getName().equals("java.lang.String")) {
        Class[] argTypes = { oldTypeClass };
        Object[] argValues = { existingValue };

        Constructor constructor = newTypeClass.getConstructor(argTypes);
        newValue = constructor.newInstance(argValues);
      } else if (getType().equals("java.lang.String")) {
        newValue = existingValue.toString();
      } else {
        // Can't handle this
        throw new ProcessingException("Unsupported OrderedMap Type Transform.", this);
      }

      map.put(getAttribute(), newValue);
    } catch (ClassNotFoundException e) {
      throw new RecordFormatException("ClassNotFoundException during OrderedMap Type Transform.", e);
    } catch (NoSuchMethodException e) {
      throw new RecordFormatException("NoSuchMethodException during OrderedMap Type Transform.", e);
    } catch (InstantiationException e) {
      throw new RecordFormatException("InstantiationException during OrderedMap Type Transform.", e);
    } catch (IllegalAccessException e) {
      throw new RecordFormatException("IllegalAccessException during OrderedMap Type Transform.", e);
    } catch (InvocationTargetException e) {
      throw new RecordFormatException("InvocationTargetException during OrderedMap Type Transform.", e);
    }
    return map;
  }

}
