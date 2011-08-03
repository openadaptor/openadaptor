package org.openadaptor.auxil.connector.iostream.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.auxil.connector.iostream.reader.string.StringReader;
import org.openadaptor.util.ResourceUtil;

public class DirectoryReadConnectorTestCase extends MockObjectTestCase {

	public void testAppendEOFMessageAndStoreFileName() throws IOException {

		DirectoryReadConnector connector = createConnector("test/unit/src/",
				"test2.txt", ".*\\.txt");
		connector.setAppendEOFMessage(true);
		connector.setStoreFileName(true);
		connector.connect();

		// Read file and verify that the metadata contain the current processed
		// filename
		StringBuffer buffer = new StringBuffer();
		while (!connector.isDry()) {
			Object[] data = connector.next(1);
			for (int i = 0; data != null && i < data.length; i++) {
				buffer.append(data[i] + "\n");
				if (data[i] instanceof Map) {
					Map dataMap = (Map) data[i];
					Map metadata = (Map) dataMap.get("readNodeMetadata");
					assertTrue(
							"Metadata does not contain the filename as expected!",
							metadata.get("name") != null);
				}
			}
		}

		// Verify the appended EOFMessage exists
		String readedData = buffer.toString();
		assertTrue("Returned metadata does not contain EOF flag! ",
				readedData.indexOf("EOF=true") > 0);

		// Verify that the appended EOFMessage was appended for every processed
		// file
		// First get number of files in directory
		String f = ResourceUtil.getResourcePath(this, "test/unit/src/",
				"test2.txt");
		File file = new File(f);

		final Matcher matcher = Pattern.compile(".*\\.txt").matcher("");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return matcher.reset(name).matches();
			}
		};

		String[] list = file.getParentFile().list(filter);
		String[] eofs = readedData.split("EOF=true");

		assertTrue(list.length == eofs.length - 1);

	}

	public void testNoExceptionWhenNoFiles() throws IOException {

		// Default configuration
		DirectoryReadConnector connector = createConnector("test/unit/src/",
				"test1.txt", ".this_files_not_exists*\\.txt");
		connector.connect();
		readData(connector);

		DirectoryReadConnector connector2 = createConnector("test/unit/src/",
				"test2.txt", ".this_files_also_not_exists*\\.txt");
		connector2.setAppendEOFMessage(true);
		connector2.setStoreFileName(true);
		connector2.connect();
		readData(connector2);

		DirectoryReadConnector connector3 = createConnector("test/unit/src/",
				"test3.txt", ".this_files_not_exists*\\.txt");
		connector3.setAppendEOFMessage(false);
		connector3.setStoreFileName(true);
		connector3.connect();
		readData(connector3);

		DirectoryReadConnector connector4 = createConnector("test/unit/src/",
				"test4.txt", ".this_files_not_exists*\\.txt");
		connector4.setAppendEOFMessage(true);
		connector4.setStoreFileName(false);
		connector4.connect();
		readData(connector4);

	}

	public void testExceptionWhenFileWasDeleted() throws IOException {

		String path = "test/unit/src/";
		String filename = "test1.txt";
		String regex = ".*\\.txt";

		// Create File
		String f = ResourceUtil.getResourcePath(this, path, filename);
		File file = new File(f);
		file.createNewFile();
		Assert.assertTrue(file.exists());

		// Create Directory Reader
		DirectoryReadConnector connector = createConnector(path, filename, regex);
		connector.connect();

		// Delete File
		file.delete();
		Assert.assertFalse(file.exists());

		// Read Data
		try {
			readData(connector);
			fail();
		} catch (Exception ex) {
			Assert.assertTrue(ex.getCause() instanceof FileNotFoundException);
		}
	}

	public void testNoExceptionWhenFileWasDeleted() throws IOException {

		String path = "test/unit/src/";
		String filename = "test1.txt";
		String regex = ".*\\.txt";

		// Create File
		String f = ResourceUtil.getResourcePath(this, path, filename);
		File file = new File(f);
		file.createNewFile();
		Assert.assertTrue(file.exists());

		// Create Directory Reader
		DirectoryReadConnector connector = createConnector(file, regex);
		connector.connect();

		connector.setContinueIfFileNotFound(true);

		// Delete File
		file.delete();
		Assert.assertFalse(file.exists());

		// Read Data
		try {
			readData(connector);
		} catch (Exception ex) {
			fail();
		}
	}

	public DirectoryReadConnector createConnector(File file,
			String filenameRegex) {

		DirectoryReadConnector connector = new DirectoryReadConnector("reader_"
				+ System.currentTimeMillis());

		// String f = ResourceUtil.getResourcePath(this, path, filename);
		// File file = new File(f);
		connector.setDirname(file.getParent());
		connector.setFilenameRegex(filenameRegex);
		connector.setDataReader(new LineReader());

		List exceptions = new ArrayList();
		connector.validate(exceptions);
		assertTrue(
				"There should be no validation exceptions (exception size == "
						+ exceptions.size() + ")", exceptions.size() == 0);

		return connector;
	}

	public DirectoryReadConnector createConnector(String path, String filename,
			String filenameRegex) {

		DirectoryReadConnector connector = new DirectoryReadConnector("reader_"
				+ System.currentTimeMillis());

		String f = ResourceUtil.getResourcePath(this, path, filename);
		File file = new File(f);

		return createConnector(file, filenameRegex);
	}

	public StringBuffer readData(DirectoryReadConnector connector) {

		StringBuffer buffer = new StringBuffer();

		while (!connector.isDry()) {
			Object[] data = connector.next(1);
			for (int i = 0; data != null && i < data.length; i++) {
				buffer.append(data[i] + "\n");
			}
		}
		return buffer;
	}

	public void testLineReader() throws IOException {

		for (int i = 1; i == 10; i++) {

			// Default connector configuration
			DirectoryReadConnector connector = createConnector(
					"test/unit/src/", "test1.txt", ".*\\.txt");

			// Set batch size
			connector.setBatchSize(i);
			connector.connect();
			StringBuffer buffer = readData(connector);

			String s = ResourceUtil.readFileContents(this, "test.txt")
					+ ResourceUtil.readFileContents(this, "test2.txt");
			s = ResourceUtil.removeCarriageReturns(s);
			assertEquals(buffer.toString(), s);
		}

	}

	public void testStringReader() throws IOException {

		for (int b = 1; b == 10; b++) {

			List messageList = new ArrayList();
			List expectedMessageList = new ArrayList();

			// Default connector configuration
			DirectoryReadConnector connector = createConnector(
					"test/unit/src/", "test.txt", ".*\\.xml");

			// Set batch size
			connector.setBatchSize(b);

			// Use other reader than the default reader
			connector.setDataReader(new StringReader());
			connector.connect();

			while (!connector.isDry()) {
				Object[] data = connector.next(1);
				for (int i = 0; data != null && i < data.length; i++) {
					messageList.add(data[i]);
				}
			}

			// This test relies on the connector having read the files in the
			// order below.
			expectedMessageList.add(ResourceUtil.readFileContents(this,
					"test.xml"));
			expectedMessageList.add(ResourceUtil.readFileContents(this,
					"test2.xml"));
			assertEquals(messageList, expectedMessageList);

		}
	}

	public void testValidate() {
		// DirectoryReadConnector connector = new
		// DirectoryReadConnector("reader");
		// String f = ResourceUtil.getResourcePath(this, "test/unit/src/",
		// "test.txt");
		// File file = new File(f);
		// connector.setDirname(file.getParent());
		// connector.setFilenameRegex(".*\\.txt");
		// connector.setDataReader(new LineReader());

		// Default connector configuration
		DirectoryReadConnector connector = createConnector("test/unit/src/",
				"test.txt", ".*\\.txt");
		connector.setDataReader(new StringReader());

		List exceptions = new ArrayList();
		connector.validate(exceptions);
		assertTrue("There should be no validation exceptions",
				exceptions.size() == 0);
	}

	/**
	 * This test check if the connectors validate method generate exceptions
	 * when calling the method validate and when the given directory does not
	 * exists.
	 */
	public void testValidateFail() {
		DirectoryReadConnector connector = new DirectoryReadConnector("reader");
		// No directory set
		List exceptions = new ArrayList();
		connector.validate(exceptions);
		assertTrue("There should be one validation exception",
				exceptions.size() == 1);

		// set a nonexistent directory.
		connector.setDirname("thisdoesnotexist");
		exceptions = new ArrayList();
		connector.validate(exceptions);
		assertTrue("There should be one validation exception",
				exceptions.size() == 1);
	}

}
