package org.oa3.auxil.exception;

public interface ExceptionStore {
  String store(String exception);
  String[] getAllIds(int limit);
  String getExceptionForId(String id);
  ExceptionSummary getExceptionSummary(String id);
  void delete(String id);
  void deleteAll();
}
