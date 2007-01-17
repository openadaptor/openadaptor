/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.auxil.exception;

import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.util.NetUtil;

public class MessageExceptionXmlConverter {

  public static final String LINE = "Line";
  public static final String STACK_TRACE = "StackTrace";
  public static final String DATA = "Data";
  public static final String COMPONENT = "Component";
  public static final String MESSAGE = "Message";
  public static final String CLASS = "Class";
  public static final String EXCEPTION = "Exception";
  public static final String MESSAGE_EXCEPTION = "MessageException";
  public static final String FROM = "From";
  public static final String RETRY_ADDRESS = "RetryAddress";
  public static final String TIME = "Time";
  public static final String HOST = "HostName";
  public static final String RETRIES = "Retries";
  public static final String PARENT_ID = "ParentId";
  
  public static final String EXCEPTION_PATH = "//" + MESSAGE_EXCEPTION  + "/" + EXCEPTION;
  public static final String MESSAGE_PATH =   EXCEPTION_PATH + "/" + MESSAGE;
  public static final String CLASS_PATH =   EXCEPTION_PATH + "/" + CLASS;
  public static final String FROM_PATH = "//" + MESSAGE_EXCEPTION + "/" + FROM;
  public static final String HOST_PATH = "//" + MESSAGE_EXCEPTION + "/" + HOST;
  public static final String COMPONENT_PATH = EXCEPTION_PATH + "/" + COMPONENT;
  public static final String TIME_PATH = "//" + MESSAGE_EXCEPTION + "/" + TIME;
  public static final String RETRIES_PATH = "//" + MESSAGE_EXCEPTION + "/" + RETRIES;
  public static final String RETRY_ADDRESS_PATH = "//" + MESSAGE_EXCEPTION + "/" + RETRY_ADDRESS;
  public static final String PARENT_ID_PATH = "//" + MESSAGE_EXCEPTION + "/" + PARENT_ID;
  public static final String DATA_PATH = "//" + MESSAGE_EXCEPTION  + "/" + DATA;
  public static final String TRACE_PATH = EXCEPTION_PATH + "/" + STACK_TRACE;

  public static String toXml(MessageException exception, String from, String replyTo, long time) {
    Exception embeddedException  = exception.getException();
    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement(MESSAGE_EXCEPTION);
    Element exceptionElement = root.addElement(EXCEPTION);
    exceptionElement.addElement(CLASS).setText(embeddedException.getClass().getName());
    exceptionElement.addElement(MESSAGE).setText(embeddedException.getMessage());
    addStackTrace(exceptionElement, embeddedException);
    String componentId = "";
    if (embeddedException instanceof ComponentException) {
      componentId = ((ComponentException)embeddedException).getComponent().getId();
    }
    exceptionElement.addElement(COMPONENT).setText(componentId);
    root.addElement(DATA).add(DocumentHelper.createCDATA(exception.getData().toString()));
    root.addElement(FROM).setText(from);
    root.addElement(RETRY_ADDRESS).setText(replyTo);
    root.addElement(TIME).setText(String.valueOf(time));
    root.addElement(RETRIES).setText("0");
    root.addElement(HOST).setText(NetUtil.getLocalHostname());
    return doc.asXML();
  }
  
  private static void addStackTrace(Element root, Exception exception) {
    Element trace = root.addElement(STACK_TRACE);
    StackTraceElement[] elements  = exception.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
      trace.addElement(LINE).setText(elements[i].toString());
    }
  }

  public static String toXml(Exception e, String parentId) {
    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement(MESSAGE_EXCEPTION);
    Element exceptionElement = root.addElement(EXCEPTION);
    root.addElement(FROM).setText("retry");
    exceptionElement.addElement(CLASS).setText(e.getClass().getName());
    exceptionElement.addElement(MESSAGE).setText(e.getMessage());
    addStackTrace(exceptionElement, e);
    root.addElement(TIME).setText(String.valueOf((new Date()).getTime()));
    root.addElement(PARENT_ID).setText(parentId);
    return doc.asXML();
  }
}
