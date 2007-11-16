/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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
package org.openadaptor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.router.Router;
import org.openadaptor.spring.SpringAdaptor;
import org.springframework.core.io.UrlResource;

/**
 * Utility methods for OA system tests. 
 * 
 * @author Kris Lachor
 */
public class SystemTestUtil {
  
  /**
   * Runs adaptor using a given Spring config file.
   * 
   * @param caller calling object
   * @param resourceLocation path to the config file.
   * @param configFile adaptor's Spring config file
   * @return an instance of adaptor that was executed
   * @throws Exception
   */
  public static SpringAdaptor runAdaptor(Object caller, String resourceLocation, 
      String configFile) throws Exception{
    SpringAdaptor adaptor = new SpringAdaptor();
    UrlResource urlResource = new UrlResource("file:" + ResourceUtil.getResourcePath(
        caller, resourceLocation, configFile));
    String configPath = urlResource.getFile().getAbsolutePath();
    adaptor.addConfigUrl(configPath);
    adaptor.run();
    return adaptor;
  }
  
  /**
   * Finds references to write connectors in a Spring adaptor that has been run.
   * If passed a reference to SpringAdaptor that has not been run, the result
   * will be an empty set.
   * 
   * @param springAdaptor an executed SpringAdaptor
   * @return a set with write connectors
   */
  public static List getWriteConnectors(SpringAdaptor springAdaptor){
	List writeConnectors = new ArrayList();
    Adaptor adaptor = springAdaptor.getAdaptor();
    IMessageProcessor messageProcessor = adaptor.getMessageProcessor();
    Router router = (Router) messageProcessor;
    Collection messageProcessors = router.getMessageProcessors();
    Iterator it = messageProcessors.iterator();
    while(it.hasNext()){
	  Object el = it.next();
	  if(el instanceof WriteNode){
		IWriteConnector writer = ((WriteNode) el).getConnector();
		writeConnectors.add(writer);
	  }
    }
	return writeConnectors;
  }

}
