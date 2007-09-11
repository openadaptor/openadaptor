package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class LineReaderTestCase extends TestCase {

  protected LineReader reader;
  protected String[] inputData;
  protected ByteArrayInputStream inputStream;
  
  protected void setUp() {
    reader = new LineReader();
	  inputData = new String[] {
	 	  	"# delimited test file",
		    "BUY,1000000,BT.L,MR. SMITH,325",
		    "SELL,500000,BT.L,MR. SMITH,324",
	 	  	"# another comment",
		    "BUY,1000000,BT.L,MR. SMITH,325",
		    "SELL,25000,BT.L,MR. JONES,321"
		  };
		  
	  StringBuffer sb = new StringBuffer();
		for (int i=0; i<inputData.length; i++) {
			if (i!=0) { sb.append('\n'); }
			sb.append(inputData[i]);
		}

		inputStream = new ByteArrayInputStream(sb.toString().getBytes());
	}
	
  public void testPlainRead() throws IOException {
	  reader.setInputStream(inputStream);
    for (int i=0; i<inputData.length; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testExcludeRegexCommentsCarat() throws IOException {
    reader.setExcludeRegex("^#.*");
  	reader.setInputStream(inputStream);
  	for (int i=0; i<inputData.length; i++) {
    	if (i==0 || i==3) { continue; }  // skip the two comments
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testExcludeRegexCommentsNoCarat() throws IOException {
    reader.setExcludeRegex("#.*");
  	reader.setInputStream(inputStream);
  	for (int i=0; i<inputData.length; i++) {
    	if (i==0 || i==3) { continue; }  // skip the two comments
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testExcludeRegexsBuyComment() throws IOException {
    reader.setExcludeRegexs(new String[] { "BUY.*", "#.*" });
  	reader.setInputStream(inputStream);
  	for (int i=0; i<inputData.length; i++) {
    	if (i==0 || i==1 || i==3 || i==4) { continue; }  // skip the two comments and the two BUY
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testIncludeRegexBuy() throws IOException {
  	reader.setIncludeRegex("BUY.*");
	  reader.setInputStream(inputStream);
  	for (int i=0; i<inputData.length; i++) {
    	if (i==0 || i==2 || i==3 || i==5) { continue; }  // skip the two comments and the two SELL
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testIncludeRegexsSellBuy() throws IOException {
  	reader.setIncludeRegexs(new String[] { "SELL.*", "BUY.*" });
	  reader.setInputStream(inputStream);
  	for (int i=0; i<inputData.length; i++) {
    	if (i==0 || i==3) { continue; }  // skip the two comments and the two SELL
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartBuyExclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
  	reader.setInputStream(inputStream);
  	for (int i=2; i<inputData.length; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartBuyInclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setIncludeBlockOfRecordsDelimiters(true);
  	reader.setInputStream(inputStream);
  	for (int i=1; i<inputData.length; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartBuyExclusiveExcludeBuy() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setExcludeRegex("BUY.*");
  	reader.setInputStream(inputStream);
  	for (int i=2; i<inputData.length; i++) {
  		if (i==4) { continue; } // excludeRegex excludes this Buy inside block of records
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartBuyInclusiveExcludeBuy() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setExcludeRegex("BUY.*");
    reader.setIncludeBlockOfRecordsDelimiters(true);
  	reader.setInputStream(inputStream);
  	for (int i=1; i<inputData.length; i++) {
  		if (i==1 || i==4) { continue; } // excludeRegex excludes this Buy inside block of records
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartBuyExclusiveExcludeSell() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setExcludeRegex("SELL.*");
  	reader.setInputStream(inputStream);
  	for (int i=2; i<inputData.length; i++) {
  		if (i==2 || i==5) { continue; } // excludeRegex excludes this Sell inside block of records
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartBuyInclusiveExcludeSell() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setExcludeRegex("SELL.*");
    reader.setIncludeBlockOfRecordsDelimiters(true);
  	reader.setInputStream(inputStream);
  	for (int i=1; i<inputData.length; i++) {
  		if (i==2 || i==5) { continue; } // excludeRegex excludes this Sell inside block of records
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartEndBuyExclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setEndBlockOfRecordsRegex("BUY.*");
  	reader.setInputStream(inputStream);
  	for (int i=2; i<inputData.length-2; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartEndBuyInclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("BUY.*");
    reader.setEndBlockOfRecordsRegex("BUY.*");
    reader.setIncludeBlockOfRecordsDelimiters(true);
  	reader.setInputStream(inputStream);
  	for (int i=1; i<inputData.length-1; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartEndCommentExclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("#.*");
    reader.setEndBlockOfRecordsRegex("#.*");
  	reader.setInputStream(inputStream);
  	for (int i=1; i<3; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartEndCommentInclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("#.*");
    reader.setEndBlockOfRecordsRegex("#.*");
    reader.setIncludeBlockOfRecordsDelimiters(true);
  	reader.setInputStream(inputStream);
  	for (int i=0; i<4; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartCommentEndSellExclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("#.*");
    reader.setEndBlockOfRecordsRegex("SELL.*");
  	reader.setInputStream(inputStream);
  	assertEquals(inputData[1], reader.read());
  	assertEquals(inputData[4], reader.read());
    assertEquals(null, reader.read());
  }
	
  public void testBlockOfRecordsRegexStartCommentEndSellInclusive() throws IOException {
    reader.setStartBlockOfRecordsRegex("#.*");
    reader.setEndBlockOfRecordsRegex("SELL.*");
    reader.setIncludeBlockOfRecordsDelimiters(true);
  	reader.setInputStream(inputStream);
  	for (int i=0; i<inputData.length; i++) {
    	assertEquals(inputData[i], reader.read());
    }
    assertEquals(null, reader.read());
  }
	
}
