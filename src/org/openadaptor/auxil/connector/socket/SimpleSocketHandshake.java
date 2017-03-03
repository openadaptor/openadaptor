/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.socket;

import org.apache.log4j.Logger;
import org.openadaptor.core.Constants;

import java.io.*;
//import java.util.Properties;


/**
 * Implements default handshaking behaviour for openadaptor socket components.
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><b>HandshakeGreet</b>	What to say if you are the client, and what you expect to be said if are being greeted
 * <li><b>HandshakeReply</b>	What you expect the reply to be if you sent a greeting, and what to reply if you are greeted
 * <li><b>SayGoodbye</b>		What you want to write to the socket just before you disconnect	
 * </ul>
 * </p>
 */

public class SimpleSocketHandshake
	implements ISocketHandshake
{
	static Logger log = Logger.getLogger(SimpleSocketHandshake.class.getName());
	
	//public static String newline = System.getProperty("line.separator");
	
	/**
	 * handshakeGreet is what is sent when the handshake is initiated
	 */
	private String handshakeGreet = null; 
	/**
	 * handshakeReply is what we expect the reply to be
	 */
	private String handshakeReply = null;
	/**
	 * handshakeGreetCheck, when a greeting is offered, check that it is what you wanted before replying.
	 * Default to N (no check) 
	 */
	private String handshakeGreetCheck = "N";
	/**
	 * handshakeProgress records whether a handshake is expected (Y/N) 
	 */
	private String handshakeProgress = "N";
	/**
	 * sayGoodbye, if set, will write to the socket just before close(ing) it.
	 */
	private String sayGoodbye = null;

	/**
	*** Option of specifying expected handshake conversation
	**/

	public void validate() 
		throws RuntimeException
	{
		log.debug("SimpleSocketHandshake.validate() called");
		if (handshakeGreet != null && handshakeGreet.length() > 0) {
			log.debug("handshakeGreet set to: " + handshakeGreet);
			if (handshakeReply == null) {
				log.error("handshakeReply was not set");
				throw new RuntimeException("Validation error in handshake delegate. If you start a handshake you must specfiy the expected greeting/reply");
			}
			log.info("Handshake will be executed: " + handshakeGreet + "/" + handshakeReply);
		}
		if (handshakeReply != null) {
			log.debug("handshakeReply set to: " + handshakeReply);
		}
		if (handshakeGreet != null || handshakeReply != null) {
			handshakeProgress = "Y";			
		}
		if (handshakeGreetCheck != null) { 
			if (!"Y".equals(handshakeGreetCheck) && !"N".equals(handshakeGreetCheck)) {
				log.error("handshakeGreetCheck must be N or Y, found " + handshakeGreetCheck);
				throw new RuntimeException("Validation error in handshake delegate. handshakeGreetCheck must be N or Y, found " + handshakeGreetCheck);		
			}
		}
		else {
			log.error("handshakeReply was not set");
			throw new RuntimeException("Validation error in handshake delegate. handshakeGreetCheck must be N or Y, got null!");
		}
		return;
	}
	
	
	/**
	*** this will get called after this Socket component has initiated connection
	**/
	
	public void offerHandshake(OutputStream socket_writer, BufferedReader socket_reader)
		throws IOException, RuntimeException {
		if ("Y".equals(handshakeProgress)) {
			String  replied;
			String  say = handshakeGreet + Constants.NEW_LINE;
			log.debug("SimpleSocketHandshake.offerHandshake() about to say " + say);
	        for (int i = 0; i < say.length(); i++) {
	        	socket_writer.write(say.charAt(i));
	        }
	        socket_writer.flush();
			log.debug("SimpleSocketHandshake.offerHandshake() said " + say);
			
			replied = socket_reader.readLine();
			log.debug("SimpleSocketHandshake.offerHandshake() got back " + replied);
			
			if ( replied == null || !replied.equals(handshakeReply) ) {
				throw new RuntimeException("Invalid response, expected " + handshakeReply + " got, " + replied);
			}
		}
	}


	/**
	*** this will get called after the other Socket component has accepted a connection
	**/
		
	public void acceptHandshake(OutputStream socket_writer, BufferedReader socket_reader)
		throws IOException, RuntimeException {

		if ("Y".equals(handshakeProgress)) {
			String theySaid = socket_reader.readLine();
			log.debug("SimpleSocketHandshake.acceptHandshake() they said " + theySaid);
			if ("Y".equals(handshakeGreetCheck)) {
				if ( theySaid == null || !theySaid.equals(handshakeGreet) ) {
					throw new RuntimeException("Invalid greeting, expected " + handshakeGreet + " got, " + theySaid);
				}
			}
			//If we got what we expected then we will reply
			String  reply = handshakeReply + Constants.NEW_LINE;
	        for (int i = 0; i < reply.length(); i++) {
	        	socket_writer.write(reply.charAt(i));
	        }
	        socket_writer.flush();
	        log.debug("SimpleSocketHandshake.acceptHandshake() we replied " + handshakeReply);
	    }
	}
	
	/**
	*** Writes a final line to the socket before closing 
	**/
	
	public void sayGoodbye(OutputStream socket_writer, BufferedReader socket_reader, boolean isPseudoTransaction)
		throws IOException, RuntimeException {
		if (sayGoodbye != null && sayGoodbye.length() > 0) {
			String goodBye = sayGoodbye + Constants.NEW_LINE;
			for (int i = 0; i < goodBye.length(); i++) {
	        	socket_writer.write(goodBye.charAt(i));
	        }
	        socket_writer.flush();
	        if (isPseudoTransaction) {
	        	String dontCare = socket_reader.readLine();
	        }
		}
	}

	public void setHandshakeGreet(String handshakeGreet) {
		this.handshakeGreet = handshakeGreet;
	}


	public void setHandshakeReply(String handshakeReply) {
		this.handshakeReply = handshakeReply;
	}


	public void setHandshakeGreetCheck(String handshakeGreetCheck) {
		this.handshakeGreetCheck = handshakeGreetCheck;
	}


	public void setSayGoodbye(String sayGoodbye) {
		this.sayGoodbye = sayGoodbye;
	}
}
