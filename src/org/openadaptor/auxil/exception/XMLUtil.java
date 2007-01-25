/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.exception;

import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.MessageException;
import org.openadaptor.util.NetUtil;

public class XMLUtil {

  private static final String LINE = "Line";
  private static final String STACK_TRACE = "StackTrace";
  private static final String DATA = "Data";
  private static final String COMPONENT = "Component";
  private static final String MESSAGE = "Message";
  private static final String CLASS = "Class";
  private static final String EXCEPTION = "Exception";
  private static final String MESSAGE_EXCEPTION = "MessageException";
  private static final String FROM = "From";
  private static final String RETRY_ADDRESS = "RetryAddress";
  private static final String TIME = "Time";
  private static final String HOST = "HostName";
  private static final String RETRIES = "Retries";
  private static final String PARENT_ID = "ParentId";
  
  private static final String EXCEPTION_PATH = "//" + MESSAGE_EXCEPTION  + "/" + EXCEPTION;
  private static final String MESSAGE_PATH =   EXCEPTION_PATH + "/" + MESSAGE;
  private static final String CLASS_PATH =   EXCEPTION_PATH + "/" + CLASS;
  private static final String FROM_PATH = "//" + MESSAGE_EXCEPTION + "/" + FROM;
  private static final String HOST_PATH = "//" + MESSAGE_EXCEPTION + "/" + HOST;
  private static final String COMPONENT_PATH = EXCEPTION_PATH + "/" + COMPONENT;
  private static final String TIME_PATH = "//" + MESSAGE_EXCEPTION + "/" + TIME;
  private static final String RETRIES_PATH = "//" + MESSAGE_EXCEPTION + "/" + RETRIES;
  private static final String RETRY_ADDRESS_PATH = "//" + MESSAGE_EXCEPTION + "/" + RETRY_ADDRESS;
  private static final String PARENT_ID_PATH = "//" + MESSAGE_EXCEPTION + "/" + PARENT_ID;
  private static final String DATA_PATH = "//" + MESSAGE_EXCEPTION  + "/" + DATA;
  private static final String TRACE_PATH = EXCEPTION_PATH + "/" + STACK_TRACE;

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

  private static String getText(Document doc, String path, String defaultValue) {
    try {
      Node n = doc.selectSingleNode(path);
      return n.getText();
    } catch (RuntimeException e) {
      return defaultValue;
    }
  }
  
  private static long getLong(Document doc, String path, long defaultValue) {
    try {
      Node n = doc.selectSingleNode(path);
      return Long.parseLong(n.getText());
    } catch (RuntimeException e) {
      return defaultValue;
    }
  }
  
  private static int getInt(Document doc, String path, int defaultValue) {
    try {
      Node n = doc.selectSingleNode(path);
      return Integer.parseInt(n.getText());
    } catch (RuntimeException e) {
      return defaultValue;
    }
  }
  
  public static void populateSummary(Document doc, ExceptionSummary summary) {
    summary.setMessage(getText(doc, XMLUtil.MESSAGE_PATH, ""));
    summary.setFrom(getText(doc, XMLUtil.FROM_PATH, ""));
    summary.setDate(new Date(getLong(doc, XMLUtil.TIME_PATH, 0)));
    summary.setRetryAddress(getText(doc, XMLUtil.RETRY_ADDRESS_PATH, ""));
    summary.setComponentId(getText(doc, XMLUtil.COMPONENT_PATH, ""));
    summary.setRetries(getInt(doc, XMLUtil.RETRIES_PATH, 0));
    summary.setParentId(getText(doc, XMLUtil.PARENT_ID_PATH, ""));
    summary.setHost(getText(doc, XMLUtil.HOST_PATH, ""));
    summary.setException(getText(doc, XMLUtil.CLASS_PATH, ""));
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

  public static String getData(Document doc) {
   return getText(doc, XMLUtil.DATA_PATH, "");
  }

  public static String[] getStackTrace(Document doc) {
    List nodes = doc.selectNodes(XMLUtil.TRACE_PATH + "/" + "Line");
    String[] lines = new String[nodes.size()];
    for (int i = 0; i < lines.length; i++) {
      lines[i] = ((Node)nodes.get(i)).getText();
    }
    return lines;
  }

  public static void incrementRetryCount(Document doc) {
    Node node = doc.selectSingleNode(XMLUtil.RETRIES_PATH);
    int retries = Integer.parseInt(node.getText());
    node.setText(String.valueOf(retries+1));
  }
}
