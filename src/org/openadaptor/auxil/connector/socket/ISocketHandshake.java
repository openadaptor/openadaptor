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

import java.io.*;
import java.util.Properties;


/**
*** Interface which defines how Socket client/server should implement
*** handshaking with another socket server/client
***
*** @author Dealbus Development
**/

public interface ISocketHandshake
{
	/**
	*** Properties are initialised via Spring and then validated here.
	*** @exception RuntimeException	
	*** Thrown if: 
	*** Validation fails (mandatory properties are missing or properties cannot be parsed.
	**/

	public void validate()
		throws RuntimeException;
					 
	
	/**
	*** This will get called after Socket component has initiated connection
	**/
	
	public void offerHandshake(OutputStream socket_writer, BufferedReader socket_reader)
		throws IOException, RuntimeException;

	/**
	*** This will get called after Socket component has accepted a connection
	**/
	
	public void acceptHandshake(OutputStream socket_writer, BufferedReader socket_reader)
		throws IOException, RuntimeException;
	
	/**
	*** This will get called when Socket component is exiting cleanly, should not block
	*** for long period of time
	**/
	
	public void sayGoodbye(OutputStream socket_writer, BufferedReader socket_reader, boolean isPseudoTransaction)
		throws IOException, RuntimeException;
		
}
