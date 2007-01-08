package org.oa3.auxil.exception;

import java.util.Date;

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
