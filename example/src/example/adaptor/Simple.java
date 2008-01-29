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

package example.adaptor;

import java.util.Arrays;

import org.openadaptor.auxil.connector.iostream.reader.FileReadConnector;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.auxil.connector.iostream.writer.FileWriteConnector;
import org.openadaptor.auxil.convertor.delimited.DelimitedStringToOrderedMapConvertor;
import org.openadaptor.auxil.convertor.xml.OrderedMapToXmlConvertor;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;

/**
 * This code example corresponds to the basic spring example in simple.xml
 * 
 * It assembles a adaptor which read fromm stdin, converts the delimited
 * fields into a map, converts that map into xml and writes to stdout.
 * 
 * @author perryj
 *
 */
public class Simple {

  public static void main(String[] args) {
    
    // stream read connectors delegate the stream reading to an IStreamReader
    // and the record reading to an IRecordReader. This example uses a
    // FileReader and because this doesn't specify a file to read from it
    // defaults to stdin.
    
    FileReadConnector reader = new FileReadConnector("Reader");
    reader.setDataReader(new LineReader());
    
    // this convertor converts a delimited string to a map
    // the configuration specifies a single key
    
    DelimitedStringToOrderedMapConvertor mapConverter;
    mapConverter = new DelimitedStringToOrderedMapConvertor("MapConverter");
    mapConverter.setFieldNames(new String[] {"field"});
    
    // this converter converts a map into an xml string
    
    OrderedMapToXmlConvertor xmlConverter;
    xmlConverter = new OrderedMapToXmlConvertor("XmlConverter");
    xmlConverter.setRootElementTag("data");
    
    // stream writer connectors delegate the stream writing to an IStreamWriter
    // and the record writing to an IRecordWriter. This example uses a
    // FileWriter and because it doesn't specify a file to write to it
    // defaults to stdin. Because no record writer is configured it defaults
    // to a StringRecordWriter (which writes a string followed by a newline)
    
    FileWriteConnector writer = new FileWriteConnector("Writer");
    
    // we then instantiate an Adaptor and set it's pipeline to be the
    // array of ordered components
    
    Adaptor adaptor = new Adaptor();
    Router router=new Router();
    router.setProcessors(Arrays.asList(new Object[] {reader, mapConverter, xmlConverter, writer}));
    adaptor.setMessageProcessor(router);
    
    // this starts the adaptor
    adaptor.run();
  }

}
