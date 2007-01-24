package example.adaptor;

import java.util.Arrays;

import org.openadaptor.auxil.connector.iostream.reader.FileReader;
import org.openadaptor.auxil.connector.iostream.reader.StreamReadConnector;
import org.openadaptor.auxil.connector.iostream.reader.StringRecordReader;
import org.openadaptor.auxil.connector.iostream.writer.FileWriter;
import org.openadaptor.auxil.connector.iostream.writer.StreamWriteConnector;
import org.openadaptor.auxil.convertor.delimited.DelimitedStringToOrderedMapConvertor;
import org.openadaptor.auxil.convertor.xml.OrderedMapToXmlConvertor;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.experimental.PipelineRouter;

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
    
    StreamReadConnector reader = new StreamReadConnector("Reader");
    reader.setStreamReader(new FileReader());
    reader.setRecordReader(new StringRecordReader());
    
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
    
    StreamWriteConnector writer = new StreamWriteConnector("Writer");
    writer.setStreamWriter(new FileWriter());
    
    // we then instantiate an Adaptor and set it's pipeline to be the
    // array of ordered components
    
    Adaptor adaptor = new Adaptor();
    PipelineRouter router=new PipelineRouter();
    router.setProcessors(Arrays.asList(new Object[] {reader, mapConverter, xmlConverter, writer}));
    adaptor.setMessageProcessor(router);
    
    // this starts the adaptor
    adaptor.run();
  }

}
