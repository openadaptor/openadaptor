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

package org.openadaptor.auxil.connector.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Session;
import org.openadaptor.auxil.connector.jms.IMessageGenerator;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordFormatException;
import org.dom4j.*;
import java.util.*;

/*
 * File: $Header: $ 
 * Rev: $Revision: $ 
 * Created Sep 22, 2008 by Martin Mooney
 */

/**
 * This class will set properties in the header of a JMS message using the XML
 * message itself as the source for these messages. It will use xpath to get the
 * value of the message.<br>
 * </p>
 * Example bean:<br>
 * <br>
 * 
 * <pre>
 *    &lt;bean id=&quot;JMSPropertySetter&quot; class=&quot;utils.openadaptor.JMSPropertiesMessageGenerator&quot;&gt;
 *      &lt;property name=&quot;properties&quot;&gt;
 *          &lt;map&gt;
 *              &lt;entry key=&quot;Client&quot; value=&quot;//Fields/Client&quot;/&gt;
 *              &lt;entry key=&quot;ActivityType&quot; value=&quot;//Fields/ActivityType&quot;/&gt;
 *              &lt;entry key=&quot;FundOrCostCentreID&quot; value=&quot;//Fields/FundOrCostCentreID&quot;/&gt;
 *              &lt;entry key=&quot;InstructionsRequired&quot; value=&quot;//Fields/InstructionsRequired&quot;/&gt;
 *              &lt;entry key=&quot;AutoSettInd&quot; value=&quot;//Fields/AutoSettInd&quot;/&gt;
 *              &lt;entry key=&quot;CptyInternalRef&quot; value=&quot;//Fields/CptyInternalRef&quot;/&gt;
 *              &lt;entry key=&quot;CptyMajor&quot; value=&quot;//Fields/CptyMajor&quot;/&gt;
 *          &lt;/map&gt;
 *      &lt;/property&gt;
 *    &lt;/bean&gt;
 * </pre>
 * 
 * <p>
 * As you can see you may have as many entries as necessary. The key becomes the
 * name of of the property on the JMS message and the value is used via xpath to
 * get the value from the XML message being passed in.
 * </p>
 * <p>
 * If the value does not begin with // then it is treated as is and added to the properties.
 * </p>
 * <p>
 * The 'isXml' property is optional and defaults to true if not supplied. When set to false
 * the input is assumed not to be XML so no parsing is done to prevent XML errors from
 * being thrown. This also causes all map entries to be treated literally
 * </p>
 * <p> 
 * The input to this Message Generator is expected to be either a String 
 * containing valid XML or a Dom4j Document if 'isXml' is true or a String otherwise.
 * </p>
 * 
 * Contributed by: Martin Mooney<br>
 * Modified by: Kevin Scully
 */
public class JMSPropertiesMessageGenerator implements IMessageGenerator {
  private Map _properties = new HashMap();
  private boolean isXml = true;

  public Message createMessage(Object messageRecord, Session session)
      throws JMSException {
    String key = null;
    String value = null;
    String path = null;

    try {
      Document document = null;
      String mr;
      
      if (getIsXml()) { // Will cope with either a string with valid xml or a Dom4j Document.
        if (messageRecord instanceof Document) {
          document = (Document) messageRecord;
          mr = document.asXML();
        } else if ((messageRecord instanceof String)) {
          mr = (String) messageRecord;
          document = DocumentHelper.parseText(mr);
        } else { // Since we are creating a TextMessage
          throw new RecordFormatException(
              "Unsupported record type ["
                  + messageRecord.getClass().getName()
                  + "]. Must be either XML or a Dom4j Document.");
        }
      } else {
        try {
          mr = (String)messageRecord;
        } catch (ClassCastException cce) {
          throw new RecordFormatException(
          "Unsupported record type ["
          + messageRecord.getClass().getName()
          + "]. With isXML false this must be a String as we wish to create a TextMessage.");
        }
      
        
      }
      

      // Now convert to TextMessage and return
      TextMessage tm = session.createTextMessage();

      for (Iterator keys = _properties.keySet().iterator(); keys.hasNext();) {
        key = (String) keys.next();
        path = (String) _properties.get(key);
        if( path.startsWith("//") && this.getIsXml() && document != null ) { // we have an XPath
        // use xpath to get the value from the message
        Node node = document.selectSingleNode(path);
        if (node != null) {
          value = node.getText();
          tm.setStringProperty(key, value);
        } else {
          throw new ProcessingException("XPATH with value [" + path + "] does not exist in XML");
        }
        } else { // we have a literal
          tm.setStringProperty(key, path) ;
        }
      }

      tm.setText(mr);

      return tm;
    } catch (DocumentException e) {
      throw new RecordFormatException(
          "Unable to generate Document from record [" + messageRecord + "]");
    }
  }

  public void setProperties(Map properties) {
    this._properties = properties;
  }

  public Map getProperties() {
    return this._properties;
  }

  public boolean getIsXml() {
    return isXml;
  }

  public void setIsXml(boolean isXml) {
    this.isXml = isXml;
  }
  
  
}
