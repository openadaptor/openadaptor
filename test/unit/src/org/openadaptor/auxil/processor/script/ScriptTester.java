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

package org.openadaptor.auxil.processor.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;

/**
 * Trivial debug class to allow testing of MapScriptProcessor
 * @author higginse
 * @since after 3.3
 */
public class ScriptTester  {

  /**
   * Convenience mechanism to allow ad-hoc execution of javascript (for debugging only).
   * 
   * Note: It is very inefficient - instantiating a new processor for each executed script.
   * 
   * @param argv argumens of the form name=value for pre-populating the test map.
   * @throws IOException on I/O problems.
   */
  public static void main(String[] argv) throws IOException{

    IOrderedMap map=new OrderedHashMap(); 

//  Object[][] multiValues= new Object[][] {{"One",1},{"Two",2},{"Three",3}};


//  Map nvPairs=new HashMap();
//  Object[] arr=new Object[multiValues.length];
//  for (int i=0;i<multiValues.length;i++) {
//  arr[i]=nvPairs.put(multiValues[i][0],multiValues[i][1]);
//  }
//    map.put("nv",arr);
    IOrderedMap sub11=new OrderedHashMap();
    sub11.put("sub11", "sub11val");

    IOrderedMap sub1=new OrderedHashMap();
    sub1.put("sub1", sub11);

    IOrderedMap sub2=new OrderedHashMap();
    sub2.put("sub2", "sub2val");

    map.put("top1", sub1);
    map.put("top2", sub2);

    for (int i=0;i<argv.length;i++){
      try {
        String[] pair=argv[i].split("=");
        String name=pair[0];
        String value=pair[1];
        map.put(name, value);
        System.out.println("Primed input record with "+name+"->"+value);
      }
      catch (Exception e) {
        System.err.println("Failed to process arg: "+argv[i]);
      }
    }
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    String line;
    StringBuffer script=new StringBuffer();
    System.out.println("Enter javascript to be executed.");
    System.out.println("Two empty lines signfies end of input. ");
    System.out.println();
    System.out.println("The input map may be populated (top level) from command line arguments of the form 'name=value'");
    System.out.println();
    do  {
      System.out.print("javascript > "); 
      script.setLength(0);
      while ((line=br.readLine()).length()>0) {
        script.append(line);
        System.out.print(" ctd. > ");
      }  
      System.out.println("Input:");
      System.out.println(map);
      System.out.println("Result: ");
      MapScriptProcessor msp=new MapScriptProcessor();
      msp.setScript(script.toString());
      List exceptions=new ArrayList();
      msp.validate(exceptions);
      Object[] results=msp.process(map);
      Object output=results[0];
      System.out.println("Output:");
      System.out.println(output);
      map=(IOrderedMap)output; //Ready for next loop;
    }
    while (script.length()>0);
    System.out.println("Exiting.");
  }


}
