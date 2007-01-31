package org.openadaptor.auxil.processor.javascript;
/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1998.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Norris Boyd
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordFormatException;

public class ScriptableSimpleRecord extends ScriptableObject  {
  public static final long serialVersionUID = 0x00;
  private static final Log log = LogFactory.getLog(ScriptableSimpleRecord.class);
  public static final String CLASSNAME=ScriptableSimpleRecord.class.getName();
  private ISimpleRecord simpleRecord;
  private boolean modified=false;
  
  // The zero-argument constructor used by Rhino runtime to create instances
  public ScriptableSimpleRecord() {}

  // Method jsConstructor defines the JavaScript constructor
  public void jsConstructor(Object simpleRecord) { 
    if (simpleRecord instanceof ISimpleRecord){
      this.simpleRecord=(ISimpleRecord)simpleRecord;
    }
    else {
      log.warn("Supplied record is not an ISimpleRecord instance, but "+simpleRecord.getClass().getName());
      throw new RecordFormatException("Only ISimpleRecord instances may be used within scripts");
    }
  }

  // The class name is defined by the getClassName method
  public String getClassName() { 
    return CLASSNAME; 
    }

  // Exposed methods are be defined using the jsFunction_ prefix. Here we define
  //  get for JavaScript.
  public Object jsFunction_get(Object key) {
    log.debug("get("+key+") invoked");
    Object value=simpleRecord.get(key);

    return value;
  }

  //Note: Always forces modify flag, even if new value = old value.
  public void jsFunction_put(Object key,Object value) {
    log.debug("put("+key+","+value+") invoked");
    modify();
    simpleRecord.put(key, value);
  }

  //Note: Always forces modify flag, even if it didn't contain the key.
    public Object jsFunction_remove(Object key) {
    log.debug("remove("+key+") invoked");
    modify();
    return simpleRecord.remove(key);
  }

  public String jsFunction_toString() {
    return simpleRecord.toString();
  }

  public boolean js_Function_containsKey(Object key) {
    log.debug("containsKey("+key+") invoked");
    return simpleRecord.containsKey(key);
  }
  
  public ISimpleRecord getSimpleRecord() {
    return simpleRecord;
  }
  
  private void modify() {
    if (!modified) {
      log.info("Modifying a simpleRecord - cloning original");
      simpleRecord=(ISimpleRecord)simpleRecord.clone();
      modified=true;
    }
  }
}
