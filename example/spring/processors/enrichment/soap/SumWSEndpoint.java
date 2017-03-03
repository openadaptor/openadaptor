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

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.server.http.XFireHttpServer;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;

/** 
 * Starts a web service (http://localhost:9998/ISumWS?wsdl) that adds two numbers.
 * Written for the example\spring\processors\enrichment\soap\enrich-processor.xml example.
 * 
 * @author Kris Lachor
 */
public class SumWSEndpoint {

  public static final int PORT_NR = 9998;
  
  /**
   * Constructor, starts a web service.
   */
  public SumWSEndpoint() throws Exception{
    ServiceStarter service = new ServiceStarter();
    service.start();
  }
  
  /**
   * WS interface.
   */
  public interface ISumWS{
    /**
     * Both arguments expected to be integer numbers.
     * 
     * @param arg1 an integer
     * @param arg2 an integer
     * @return sum of arguments
     */
    public String sum(String arg1, String arg2);
  }
  
  /**
   * WS implementation.
   */
  public class SumWS implements ISumWS{
    public String sum(String arg1, String arg2){
      int arg1Int = 0;
      int arg2Int = 0;
      try {
        arg1Int = Integer.parseInt(arg1);
        System.out.println("First argument: " + arg1Int);
        arg2Int = Integer.parseInt(arg2);
        System.out.println("Second argument: " + arg2Int);
      } catch (NumberFormatException nfe) {
        String msg = "Arguments must be numbers.";
        System.out.println(msg);
        return msg;
      }    
      return new Integer(arg1Int + arg2Int).toString();
    }
  }
   
 /**
  * Exposes the WS endpoint with XFire.
  */
 class ServiceStarter{
   
   public void start() throws Exception{
     XFire xfire = XFireFactory.newInstance().getXFire();
   
     /* Create XFire Services and register them in ServiceRegistry */
     ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
     Service service = serviceFactory.create(ISumWS.class);
     service.setInvoker(new BeanInvoker(new SumWS()));
     xfire.getServiceRegistry().register(service);
     
     /* Start the HTTP server */
     XFireHttpServer server = new XFireHttpServer();
     server.setPort(PORT_NR);
     server.start();
   }
   
 }
 
 /**
  * See  example\spring\processors\enrichment\soap\enrich-processor.xml for 
  * instructions. 
  */
 public static void main(String[] args) throws Exception {
   SumWSEndpoint service = new SumWSEndpoint();
 }
}

