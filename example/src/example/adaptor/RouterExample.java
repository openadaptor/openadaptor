package example.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.oa3.auxil.connector.iostream.reader.FileReader;
import org.oa3.auxil.connector.iostream.reader.StreamReadConnector;
import org.oa3.auxil.connector.iostream.reader.StringRecordReader;
import org.oa3.auxil.connector.iostream.writer.FileWriter;
import org.oa3.auxil.connector.iostream.writer.StreamWriteConnector;
import org.oa3.auxil.convertor.delimited.DelimitedStringToOrderedMapConvertor;
import org.oa3.auxil.convertor.xml.OrderedMapToXmlConvertor;
import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.router.RoutingMap;
import org.oa3.core.router.Router;

/**
 * This example code is equivalent to the router.xml spring example
 * 
 * It shows how to construct an adaptor with a Router rather than setting
 * the Adaptor "pipeline". In this example the configuration of the router
 * is identical to that of the pipeline in PipelineExample.java.
 * 
 * Typically the reasons for using a Router are that you need some kind
 * of branched linkage between the adaptor component and / or exception
 * management for specific exceptions.
 * 
 * @author perryj
 *
 */
public class RouterExample {

  public static void main(String[] args) {
    
    // see Simple.java
    
    StreamReadConnector reader = new StreamReadConnector("Reader");
    reader.setStreamReader(new FileReader());
    reader.setRecordReader(new StringRecordReader());
    
    // see Simple.java
    
    DelimitedStringToOrderedMapConvertor mapConverter;
    mapConverter = new DelimitedStringToOrderedMapConvertor("MapConverter");
    mapConverter.setFieldNames(new String[] {"field"});
    
    // see Simple.java
    
    OrderedMapToXmlConvertor xmlConverter;
    xmlConverter = new OrderedMapToXmlConvertor("XmlConverter");
    xmlConverter.setRootElementTag("data");
    
    // see Simple.java
    
    StreamWriteConnector writer = new StreamWriteConnector("Writer");
    writer.setStreamWriter(new FileWriter());
    
    // create a RoutingMap and Router
    // see class comment for RoutingMap
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(reader, mapConverter);
    processMap.put(mapConverter, xmlConverter);
    processMap.put(xmlConverter, writer);
    routingMap.setProcessMap(processMap);
    Router router = new Router();
    router.setRoutingMap(routingMap);
    
    // create Adaptor and set 
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    
    // this starts the adaptor
    adaptor.run();
  }

}
