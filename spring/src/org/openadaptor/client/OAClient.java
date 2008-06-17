/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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
package org.openadaptor.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IWriteConnector;

/** 
 * A utility class that may be used to send data from a Java program 
 * into a running Openadaptor.
 * 
 * @author Kris Lachor
 */
public class OAClient {
  
  private static final WriterBuilder DEFAULT_WRITER_BUILDER = new WebServiceWriterBuilder();
  
  private static final Log log = LogFactory.getLog(OAClient.class);
  
  private IWriteConnector writer;
  
  private boolean connected;
  
  private WriterBuilder writerBuilder = DEFAULT_WRITER_BUILDER;
  
  /**
   * Constructor.
   * Construct a default write connector and connects it.
   */
  public OAClient() {
    writer = writerBuilder.getWriter();
    connect();
  }

  /**
   * Constructor.
   * 
   * Gets IWriteConnector fromt the provided WriterBuilder.
   */
  public OAClient(WriterBuilder writerBuilder){
    this.writerBuilder = writerBuilder;
    writer = writerBuilder.getWriter();
    connect();
  }
    
  /**
   * Constructor.
   * Constructs a write connector based on a Spring config and connects it.
   */
  public OAClient(String writerConfigURI){
    writerBuilder = new SpringWriterBuilder(writerConfigURI);
    connect();
  }

  /**
   * Sends data to Openadaptor.
   */
  public void send(Object data){
    if(!connected){
      connect();
    }
    writer.deliver(new Object[]{data});
    log.info(writer + " sent data.");
    writer.disconnect();
  }
  
  private void connect(){
    if(writer == null){
      writer = writerBuilder.getWriter();
    }
    if(!connected){
      writer.connect();
      connected = true;
    }
    else{
      log.warn("WriteConnector is already connected.");
    } 
  }
  
  /**
   * Disconnects the write connector.
   */
  public void disconnect(){
    writer.disconnect();
  }
  
  /**
   * Sets an IWriteConnector that this client will use to send data to 
   * Openadaptor.
   * 
   * @param writer the write connector.
   */
  public void setWriteConnector(IWriteConnector writer) {
    this.writer = writer;
  }

  /**
   * Creates an instance of <code>OAClient</code> with a WebServiceWriteConnector 
   * as a default writer.
   */
  public static void main(String [] args){
    
    /* 
     * Creates a client with a web services writer definition - connects 
     * to an Openadaptor instance with a web service reader.
     */
//    WriterBuilder writerBuilder = new WebServiceWriterBuilder("..some ws endpoint?wsdl");
//    OAClient client = new OAClient(writerBuilder);
    
    
    /* 
     * Creates a client with writer definition based on Spring config. 
     * Connects to an Openadaptor instance with a read connector equivalent 
     * to the write connector defined in Spring config.
     */ 
//    OAClient client = new OAClient("ws-client.xml");
    OAClient client = new OAClient("http-client.xml");
   

    /*
     * Sends data to Openadaptor.
     */
    client.send("foo");
    client.send(new Integer(111));
    client.send("bar");    
    
  }
}
