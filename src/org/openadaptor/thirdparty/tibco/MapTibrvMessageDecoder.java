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

import org.openadaptor.auxil.orderedmap.OrderedHashMap;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * decodes Tibco Rendezvous messages into Maps
 * @author Eddy Higgins
 */
public class MapTibrvMessageDecoder implements ITibrvMessageDecoder {
  public static final String DEFAULT_SUBJECT_FIELD_NAME="subject";
  private String subjectFieldName=DEFAULT_SUBJECT_FIELD_NAME;
  private boolean includeSubject=true;
  private List fields=null;

  /**
   * If set, then subject will also be included in the decoded map.
   * This is enabled by default, with DEFAULT_SUBJECT_FIELD_NAME as
   * the name of the subject field
   * @param includeSubject
   */
  public void setIncludeSubject(boolean includeSubject) {
    this.includeSubject=includeSubject;
  }
  /**
   * Useful to override the default field name {@link DEFAULT_SUBJECT_FIELDNAME}
   * Note: Behaviour is undefined if a subject field name is chosen which clashes
   * with the actual retreived message field name(s).
   * If includeSubject property is false, this has no effect.
   * @param subjectFieldName a name for the subject field.
   */
  public void setSubjectFieldName(String subjectFieldName) {
    this.subjectFieldName=subjectFieldName;
  }
  
  /**
   * Specify the list of fields to be included intthe decoded map.
   * If a named field does not existin the tibrv message, it will
   * be assigned a null value in the decoded map.
   * Note that it is an optional property - if left unspecified,
   * then all fields from the message will be used to populate
   * the outgoing map.
   * @param fields List of fields to extract from the message.
   */
  public void setFields(List fields) {
    this.fields=fields;
  }
  /**
   * Decode a supplied Tibco Rendezvous Message (TibrvMsg).
   * @param message TibrvMsg instance
   * @return Map containing the result of decoding
   */
  public Object decode(TibrvMsg msg) throws TibrvException {
    OrderedHashMap result=null;
    if (msg!=null) { //Something to process!
      result=new OrderedHashMap();
      if (includeSubject){
        result.put(subjectFieldName,msg.getSendSubject());
      }     
      if (fields==null) { //Get 'em all
        int numFields=msg.getNumFields();
        for (int i=0;i<numFields;i++) {
          TibrvMsgField field=msg.getFieldByIndex(i);
          result.put(field.name, field.data);          
        }
      }
      else  { //Get selected fields only.
        Iterator it=fields.iterator();
        while (it.hasNext()) {
          String name=it.next().toString();
          TibrvMsgField field=msg.getField(name);
          result.put(name, (field==null)?null:field.data);
        }
      }
    }
    return result;
  }
}
