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
package org.openadaptor.auxil.connector.iostream.writer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openadaptor.auxil.convertor.simplerecord.ToSimpleRecordConvertor;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.thirdparty.dom4j.Dom4jSimpleRecordAccessor;
import org.openadaptor.util.ResourceUtil;

import junit.framework.TestCase;

/**
 * System tests for {@link DynamicFileWriteConnector};
 * 
 * @author Kris Lachor
 */
public class DynamicFileWriteConnectorTestCase extends TestCase {
  
  private static String DIR = "test/unit/output";
  
  private static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  
  private static String TEST_MSG = "<test><record1>file1</record1><record2>file2</record2></test>";
  
  private static String SCRIPT1 = "oa_data=\"" + DIR + "/\"+oa_data.get('test/record1')+\".txt\";";

  private File dir = null;
  
  protected void setUp() throws Exception {
    super.setUp();
    dir = new File(DIR);
    if (!dir.exists()) {
        dir.mkdir();
    }
  }
  
  /**
   * Creates simple pipeline: reader -> accessor -> writer.
   * Runs adaptor. Ensures a file with name based on msg payload was created
   * and has the right content.
   */
  public void test1() throws IOException {
    /* Create reader, writer and accessor components */
    TestReadConnector reader = new TestReadConnector("reader");
    reader.setDataString(TEST_MSG);
    
    DynamicFileWriteConnector writer = new DynamicFileWriteConnector("writer");    
    writer.setScript(SCRIPT1);
    System.out.println(SCRIPT1);
    File tempFile = File.createTempFile("xyz", ".txt", dir);
    writer.setFilename(tempFile.getAbsolutePath());
    
    ToSimpleRecordConvertor accessor = new ToSimpleRecordConvertor();
    accessor.setSimpleRecordAccessor(new Dom4jSimpleRecordAccessor());
    
    /* Run adaptor*/
    List pipeline = Arrays.asList(new Object[]{reader, accessor, writer});
    Adaptor adaptor = Adaptor.run(pipeline);
    assertEquals(adaptor.getExitCode(), 0);

    /* Check current file name in the writer */
    String dynamicFileName = writer.getFilename();
    assertNotNull(dynamicFileName);
    assertEquals(dynamicFileName, DIR + "/file1.txt");

    /* Verify content and delete both files. */
    verifyFileContent(dynamicFileName, XML_HEADER + TEST_MSG + "\n");    
    tempFile.delete();
    File dynamicFile = new File(dynamicFileName);
    assertTrue(dynamicFile.delete());
  }
    
  private void verifyFileContent(String filename, String expectedContent){
    String fileContents = ResourceUtil.readFileContents(filename);
    fileContents = ResourceUtil.removeCarriageReturns(fileContents);
    System.out.println("!" + fileContents + "!");
    assertEquals(fileContents, expectedContent);
  }
}
