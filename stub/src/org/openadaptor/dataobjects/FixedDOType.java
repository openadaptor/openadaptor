/*
**
** Copyright (C) 2001 The Software Conservancy as Trustee. All rights
** reserved.
**
** Permission is hereby granted, free of charge, to any person obtaining a
** copy of this software and associated documentation files (the
** "Software"), to deal in the Software without restriction, including
** without limitation the rights to use, copy, modify, merge, publish,
** distribute, sublicense, and/or sell copies of the Software, and to
** permit persons to whom the Software is furnished to do so, subject to
** the following conditions:
**
** The above copyright notice and this permission notice shall be included
** in all copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
** OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
** MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
** NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
** LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
** OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
** WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**
** Nothing in this notice shall be deemed to grant any rights to
** trademarks, copyrights, patents, trade secrets or any other intellectual
** property of the licensor or any contributor except as expressly stated
** herein. No patent license is granted separate from the Software, for
** code that you delete from the Software, or for combinations of the
** Software with other software or hardware.
**
*/



/*
** File: $Header: /cvs/openadaptor/src/org/openadaptor/dataobjects/FixedDOType.java,v 1.21 2005/06/17 09:44:05 tim Exp $
**  Rev: $Revision: 1.21 $
**
*/

package org.openadaptor.dataobjects;

import java.io.Serializable;
import java.util.Hashtable;

import org.openadaptor.StubException;

public class FixedDOType implements DOType, Serializable {
  static final long serialVersionUID = -4585101188412970017L;

  public boolean equals(DOType other) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public boolean equals(DOType other, Hashtable checkedTypes) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public DOAttribute getAttribute(String name) throws InvalidParameterException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public int getAttributeCount() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public String[] getAttributeNames() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public DOAttribute[] getAttributes() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public String getName() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public long getVersion() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public boolean isCollection() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public boolean isEnumeration() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public boolean isPrimitive() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

}
