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

package org.openadaptor.thirdparty.tibco;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordFormatException;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * decodes Tibco Rendezvous messages into Maps
 * @author Eddy Higgins
 */
public class MapTibrvMessageEncoder implements ITibrvMessageEncoder {
  private List fields=null;

  /**
   * Specify the list of fields to be encoded in the message.
   * If a named field does not exist, it will
   * be assigned a null value in the message.
   * Note that it is an optional property - if left unspecified,
   * then all fields from the map will be used to populate
   * the outgoing message.
   * @param fields List of fields to encode into the message.
   */
  public void setFields(List fields) {
    this.fields=fields;
  }
  /**
   * Encode a supplied Object as a Tibco Rendezvous Message (TibrvMsg).
   * @param Object containing Map of data to be encoded
   * @return TibrvMsg instance containing the encoded fields.
   * @throws RecordFormatException if Object does not contain a map.
   */
  public TibrvMsg encode(Object data) throws TibrvException {
    if (data instanceof Map) {
      return encode((Map)data);
    }
    else {
      throw new RecordFormatException("Expected a Map instance. Got "+data.getClass().getName());
    }
  }

  /**
   * Encode a supplied Map as a Tibco Rendezvous Message (TibrvMsg).
   * @param Map of data to be encoded
   * @return TibrvMsg instance containing the encoded fields.
   */
  public TibrvMsg encode(Map dataMap) throws TibrvException {
    TibrvMsg msg;
    msg=new TibrvMsg();
    //If fields specified, use only them; otherwise use all fields
    Iterator it=(fields==null)?dataMap.keySet().iterator():fields.iterator(); 
    while (it.hasNext()) {
      Object key=it.next();
      Object value=dataMap.get(key);
      msg.update(key.toString(),value);
    }
    return msg;   
  }

}
