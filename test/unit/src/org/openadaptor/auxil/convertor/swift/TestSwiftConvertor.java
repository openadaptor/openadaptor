package org.openadaptor.auxil.convertor.swift;

import java.util.ArrayList;

import net.sourceforge.wife.services.ConversionService;
import net.sourceforge.wife.services.IConversionService;
import net.sourceforge.wife.swift.model.SwiftMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.delimited.AbstractDelimitedStringConvertor;
import org.openadaptor.auxil.processor.script.ScriptProcessorTestCase;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
/**
 * Common unit tests for {@link AbstractDelimitedStringConvertor}.
 */
public  class TestSwiftConvertor extends AbstractTestIDataProcessor {
  private static final Log log =LogFactory.getLog(TestSwiftConvertor.class);
  private SwiftConvertor swiftConvertor;
  private IConversionService conversionService=new ConversionService();

  public void setup() throws Exception {
    super.setUp();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }


  protected IDataProcessor createProcessor() {
    swiftConvertor=new SwiftConvertor();
    return swiftConvertor;
  }

  public void testProcessRaw() {
    log.debug("--- BEGIN testProcessRaw ---");
    process(Msg.SWIFT_RAW,Msg.SWIFT_RAW,SwiftConvertor.FORMAT_SWIFT_RAW);
    process(Msg.SWIFT_RAW,Msg.SWIFT_XML,SwiftConvertor.FORMAT_SWIFT_XML);
    process(Msg.SWIFT_RAW,Msg.SWIFT_OBJECT,SwiftConvertor.FORMAT_SWIFT_OBJECT);
    log.debug("--- END testProcessRaw ---");
  }
  public void testProcessXml() {
    log.debug("--- BEGIN testProcessXml ---");
    process(Msg.SWIFT_XML,Msg.SWIFT_RAW,SwiftConvertor.FORMAT_SWIFT_RAW);
    process(Msg.SWIFT_XML,Msg.SWIFT_XML,SwiftConvertor.FORMAT_SWIFT_XML);
    process(Msg.SWIFT_XML,Msg.SWIFT_OBJECT,SwiftConvertor.FORMAT_SWIFT_OBJECT);
    log.debug("--- END testProcessXml ---");
  }
  
  public void testProcessSwiftMessageObject() {
    log.debug("--- BEGIN testProcessSwiftMessageObject ---");
    process(Msg.SWIFT_OBJECT,Msg.SWIFT_RAW,SwiftConvertor.FORMAT_SWIFT_RAW);
    process(Msg.SWIFT_OBJECT,Msg.SWIFT_XML,SwiftConvertor.FORMAT_SWIFT_XML);
    process(Msg.SWIFT_OBJECT,Msg.SWIFT_OBJECT,SwiftConvertor.FORMAT_SWIFT_OBJECT);
    log.debug("--- END testProcessSwiftMessageObject ---");
  }

  private void process(Object input,Object expected, int outputFormat) {
    swiftConvertor.setOutputFormat(outputFormat);
    Object[] result=swiftConvertor.process(input);
    assertNotNull(result);
    assertTrue("Expected one output record, got "+result.length,result.length==1);
    Object output=result[0];
    assertTrue("Output class is "+output.getClass()+" but expected is "+expected.getClass(),expected.getClass().isAssignableFrom(output.getClass()));
    if (output instanceof String) { //Then compare output with expected, 
      String outputString=((String)output).replaceAll("\r\n", "\n");   
      String expectedString=((String)expected).replaceAll("\r\n", "\n"); 
//      System.out.println("---- actual output (cr fudged) ---");
//      System.out.println(output);
//      System.out.println("---- expected output (cr fudged)---");
//      System.out.println(expected);
//      System.out.println("-----------------------------------");
//      for (int i=0;i<outputString.length();i++) {
//          if (outputString.charAt(i) != expectedString.charAt(i)) {
//            int range=20;
//            int from=Math.max(0,i-range);
//            int to=Math.min(i+range,outputString.length());
//            System.err.println("Mismatch at offset "+i);
//            System.err.println("Out ("+outputString.length()+"):\n"+outputString.substring(from,to));
//            System.err.println("Exp ("+expectedString.length()+"):\n"+expectedString.substring(from,to));
//            break;
//          }
//      }
      assertEquals("Expected (String)" +expectedString +"; but got: "+outputString,expectedString,outputString);
    }
    else  if (output instanceof SwiftMessage) {
      assertEquals("SwiftMesssage (as FIN) doesn't match expected",conversionService.getFIN((SwiftMessage)expected),conversionService.getFIN((SwiftMessage)output));
    }
    else {
      assertEquals("Expected "+expected+"; got "+output,expected,output);
    }
  }
  public void testProcessRecord() {}

  private static final class Msg {
    private static final String MT103_RAW=
      "{1:F01LOGITERMXXXX2222123456}{2:I103DESTADDRXXXXN3003}{3:{113:XXXX}}{4:\n"+
      ":20:PAYREF-TB54302\n"+
      ":32A:010103EUR1000000\n"+
      ":50:CUSTOMER NAME\n"+
      "AND ADDRESS\n"+
      ":59:/123-456-789\n"+
      "BENEFICIARY NAME\n"+
      "AND ADDRESS\n"+
      "-}";
    private static final String MT103_XML=
      "<message>\n"+
      "<block1>\n"+
      "\t<applicationId>F</applicationId>\n"+
      "\t<serviceId>01</serviceId>\n"+
      "\t<logicalTerminal>LOGITERMXXXX</logicalTerminal>\n"+
      "\t<sessionNumber>2222</sessionNumber>\n"+
      "\t<sequenceNumber>123456</sequenceNumber>\n"+
      "</block1>\n"+
      "<block2 type=\"input\">\n"+
      "\t<messageType>103</messageType>\n"+
      "\t<receiverAddress>DESTADDRXXXX</receiverAddress>\n"+
      "\t<messagePriority>N</messagePriority>\n"+
      "\t<deliveryMonitoring>3</deliveryMonitoring>\n"+
      "\t<obsolescencePeriod>003</obsolescencePeriod>\n"+
      "</block2>\n"+
      "<block3>\n"+
      "\t<tag>\n"+
      "\t\t<name>113</name>\n"+
      "\t\t<value>XXXX</value>\n"+
      "\t</tag>\n"+
      "</block3>\n"+
      "<block4>\n"+
      "\t<tag>\n"+
      "\t\t<name>20</name>\n"+
      "\t\t<value>PAYREF-TB54302</value>\n"+
      "\t</tag>\n"+
      "\t<tag>\n"+
      "\t\t<name>32A</name>\n"+
      "\t\t<value>010103EUR1000000</value>\n"+
      "\t</tag>\n"+
      "\t<tag>\n"+
      "\t\t<name>50</name>\n"+
      "\t\t<value>CUSTOMER NAME\n"+
      "AND ADDRESS</value>\n"+
      "\t</tag>\n"+
      "\t<tag>\n"+
      "\t\t<name>59</name>\n"+
      "\t\t<value>/123-456-789\n"+
      "BENEFICIARY NAME\n"+
      "AND ADDRESS</value>\n"+
      "\t</tag>\n"+
      "</block4>\n"+
      "</message>";
    private static final String SWIFT_RAW=MT103_RAW;
    private static final String SWIFT_XML=MT103_XML;
    
    private static final SwiftMessage SWIFT_OBJECT =new ConversionService().getMessageFromFIN(SWIFT_RAW);
  }
}
