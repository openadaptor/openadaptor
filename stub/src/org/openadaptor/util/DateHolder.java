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

/*
 ** File: $Header: /cvs/openadaptor/src/org/openadaptor/util/DateHolder.java,v 1.25 2005/03/03 16:28:44 tim Exp $
 **  Rev: $Revision: 1.25 $
 **
 */

package org.openadaptor.util;

//IMPORTS

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.openadaptor.StubException;

//CLASS

public class DateHolder implements java.io.Serializable, Comparable {
  static final long serialVersionUID = 8719547390575463199L;
  /**
   * Initialises a new DateHolder to the current date in the default timezone.
   */
  public DateHolder() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public DateHolder(Date date, TimeZone tz){
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public DateHolder(Calendar cal){
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public DateHolder(int year, int month, int date, TimeZone tz){
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public Date asDate(){
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public int compareTo(Object o) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }
}
