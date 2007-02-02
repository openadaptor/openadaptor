/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.exception;

import java.util.Date;

/**
 * Holds exception details (but not the actual data that relates to the exception)
 * Used to communicate with the ExceptionStore.
 * 
 * @author perryj
 *
 */

public class ExceptionSummary {
  
  private String id;
  private String componentId;
  private String from;
  private Date date;
  private String message;
  private String retryAddress;
  private int retries;
  private String parentId;
  private String host;
  private String exception;
  
  public String getException() {
    return exception;
  }
  
  public void setException(String exception) {
    this.exception = exception;
  }
  
  public String getHost() {
    return host;
  }
  
  public void setHost(String host) {
    this.host = host;
  }
  
  public String getParentId() {
    return parentId;
  }
  
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  public Date getDate() {
    return date;
  }
  
  public long getTime() {
    return date.getTime();
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
  
  public String getRetryAddress() {
    return retryAddress;
  }
  
  public void setRetryAddress(String replyTo) {
    this.retryAddress = replyTo;
  }
  public String getComponentId() {
    return componentId;
  }
  
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }
  
  public int getRetries() {
    return retries;
  }
  
  public void setRetries(int retries) {
    this.retries = retries;
  }
  
  public double getOrderKey() {
    double n = Integer.parseInt(id);
    if (parentId != null && parentId.length() > 0) {
      n = Integer.parseInt(parentId) - (1 / n);
    }
    return n;
  }
}