package org.openadaptor.doxml;

import org.openadaptor.StubException;
import org.openadaptor.dataobjects.DataObject;
import org.openadaptor.dataobjects.DataObjectException;

/**
 * Stub for legacy openadaptor code so that legacy components can be compiled and distributed
 */

public class GenericXMLReader {

  public DataObject[] fromString(String xml) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public void setAttributeValue(String name, String value) throws DataObjectException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }
}
