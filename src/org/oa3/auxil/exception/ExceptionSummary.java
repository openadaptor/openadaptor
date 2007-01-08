package org.oa3.auxil.exception;

import java.util.Date;

public class ExceptionSummary {
  private String id;
  private String from;
  private Date date;
  private String message;
  
  public Date getDate() {
    return date;
  }
  public String getFrom() {
    return from;
  }
  public String getId() {
    return id;
  }
  public String getMessage() {
    return message;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setMessage(String message) {
    this.message = message;
  }
}
