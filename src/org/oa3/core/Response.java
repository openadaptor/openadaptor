/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */

package org.oa3.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.oa3.core.exception.MessageException;

public class Response {

  public static final Response EMPTY = new Response();

  private List currentBatch = null;
  private List batches = new ArrayList();

  private Set types=new HashSet();

  /**
   * Effectively a convenience wrapper around addData(Object data,OutputBatch.class)
   * @param data
   */
  public void addData(List data) {
    addData(data,OutputBatch.class,true); //bypass unnecessary method call
  }
  public void addData(List data,Class type) {
    addData(data,type,true);
  }

  /**
   * Effectively a convenience wrapper around addDatum(Object datum,OutputBatch.class).
   * @param data
   */
  public void addDatum(Object datum) {
    addData(datum,OutputBatch.class,false); //bypass unnecessary method call
  }

  public void addDatum(Object datum,Class type) {
    addData(datum,type,false);
  }

  public boolean containsType(Class type) {
    return types.contains(type);
  }

  public List getBatches() {
    return Collections.unmodifiableList(batches);
  }

  public Object[] getCollatedBatches(Class c) {
    ArrayList output = new ArrayList();
    for (Iterator iter = batches.iterator(); iter.hasNext();) {
      List batch = (List) iter.next();
      if (batch.getClass() == c) {
        output.addAll(batch);
      }
    }
    return output.toArray(new Object[output.size()]);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (Iterator iter = batches.iterator(); iter.hasNext();) {
      List batch = (List) iter.next();
      buffer.append(buffer.length() > 0 ? "," : "");
      buffer.append(batch.toString());
    }
    return buffer.toString();
  }

  public boolean isEmpty() {
    return batches.size() == 0;
  }

  private void addData(Object data,Class type,boolean isMultiple) {
    if (type==null) {
      type=OutputBatch.class;
    }
    if (currentBatch==null || (currentBatch.getClass()!=type)) {
      if (OutputBatch.class==type) {
        currentBatch=new OutputBatch();
      }
      else if (DiscardBatch.class==type){
        currentBatch=new DiscardBatch();
      } 
      else if (ExceptionBatch.class==type) {
        currentBatch=new ExceptionBatch();
      } 
      else {
        throw new RuntimeException("Unable to handle data of type"+type.toString());
      }
      batches.add(currentBatch);
    }
    if (isMultiple && (data instanceof List)) {
      currentBatch.addAll((List)data);
    }
    else {
      currentBatch.add(data);
    }
    if (!types.contains(type)) {
      types.add(type);
    }
  }

  /**
   * @deprecated - use addDatum(datum,OutputBatch.class) instead
   */
  public void addOutput(Object datum) {
    addDatum(datum);
  }

  /**
   * @deprecated - use addDatum(datum,DiscardBatch.class) instead
   */
  public void addDiscardedInput(Object datum) {
    addDatum(datum,DiscardBatch.class);
  }

  /**
   * @deprecated - use addDatum(datum,ExceptionBatch.class) instead
   */
  public void addException(MessageException exception) {
    addDatum(exception,ExceptionBatch.class);
  }

  /**
   * @deprecated - use addData(batch,OutputBatch.class) instead
   */
  public void addoutputs(List batch) {
    addData(batch);
  }

  /**
   * @deprecated - use addData(batch,DiscardBatch.class) instead
   */
  public void addDiscardedInputs(List batch) {
    addData(batch,DiscardBatch.class);
  }

  /**
   * @deprecated - use addData(batch,ExceptionBatch.class) instead
   */
  public void addExceptions(List batch) {
    addData(batch,ExceptionBatch.class);
  }


  /**
   * @deprecated - use getCollatedBatches(OutputBatch.class) instead
   */
  public Object[] getCollatedOutput() {
    return getCollatedBatches(OutputBatch.class);
  }

  /**
   * @deprecated - use getCollatedBatches(DiscardBatch.class) instead
   */
  public Object[] getCollatedDiscards() {
    return getCollatedBatches(DiscardBatch.class);
  }

  /**
   * @deprecated - use getCollatedBatches(ExceptionBatch.class) instead
   */
  public Object[] getCollatedExceptions() {
    return getCollatedBatches(ExceptionBatch.class);
  }

  /**
   * @deprecated - use containsType(DiscardBatch.class) instead
   */
  public boolean containsDiscards() {
    return containsType(DiscardBatch.class);
  }

  /**
   * @deprecated - use containsType(ExceptionBatch.class) instead
   */
  public boolean containsExceptions() {
    return types.contains(ExceptionBatch.class);
  }

  public class DataBatch extends ArrayList {
    protected static final long serialVersionUID= 0x01;

    public Object[] getData() {
      return toArray(new Object[size()]);
    }

    public String toString() {
      String type=getClass().getName();
      
      int i=type.lastIndexOf('.');
      if (i>=0) {
        type=type.substring(i+1);
      }
      i=type.lastIndexOf('$');
      if (i>=0) {
        type=type.substring(i+1);
      }
    
      if (type.endsWith("Batch")) {
        type=type.substring(0,type.length()-"Batch".length());
        if (type.startsWith("Response$")){
          type=type.substring("Response".length());
        }
      }
      return size() + " "+type+"(s)";
    }
  }

  public class OutputBatch extends DataBatch {
    private static final long serialVersionUID= DataBatch.serialVersionUID;
  }
  public  class DiscardBatch extends DataBatch {
    private static final long serialVersionUID= DataBatch.serialVersionUID;
  }
  public  class ExceptionBatch extends DataBatch {
    private static final long serialVersionUID= DataBatch.serialVersionUID;
  }
}