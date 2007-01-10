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

package org.oa3.auxil.exception;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.oa3.core.exception.ComponentException;
import org.oa3.core.exception.MessageException;
import org.oa3.util.ResourceUtils;

public class MessageExceptionXmlConverter {

  public static final String LINE = "Line";
  public static final String STACK_TRACE = "StackTrace";
  public static final String DATA = "Data";
  public static final String COMPONENT = "Component";
  public static final String MESSAGE = "Message";
  public static final String CLASS = "class";
  public static final String EXCEPTION = "Exception";
  public static final String MESSAGE_EXCEPTION = "MessageException";
  public static final String FROM = "From";
  public static final String REPLY_TO = "ReplyTo";
  public static final String TIME = "Time";
  private static final String HOST = "HostName";

  public static String toXml(MessageException exception, String from, String replyTo, long time) {
    Exception ebeddedException  = exception.getException();
    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement(MESSAGE_EXCEPTION);
    Element exceptionElement = root.addElement(EXCEPTION);
    exceptionElement.addAttribute(CLASS, ebeddedException.getClass().getName());
    exceptionElement.addElement(MESSAGE).setText(ebeddedException.getMessage());
    addStackTrace(exceptionElement, ebeddedException);
    if (ebeddedException instanceof ComponentException) {
      exceptionElement.addElement(COMPONENT).setText(((ComponentException)ebeddedException).getComponent().getId());
    }
    root.addElement(DATA).add(DocumentHelper.createCDATA(exception.getData().toString()));
    root.addElement(FROM).setText(from);
    root.addElement(REPLY_TO).setText(replyTo);
    root.addElement(TIME).setText(String.valueOf(time));
    root.addElement(HOST).setText(ResourceUtils.getLocalHostname());
    return doc.asXML();
  }
  
  private static void addStackTrace(Element root, Exception exception) {
    Element trace = root.addElement(STACK_TRACE);
    StackTraceElement[] elements  = exception.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
      trace.addElement(LINE).setText(elements[i].toString());
    }
  }

}
