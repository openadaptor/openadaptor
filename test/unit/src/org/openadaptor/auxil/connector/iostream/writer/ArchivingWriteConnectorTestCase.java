package org.openadaptor.auxil.connector.iostream.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanWriteFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * System tests for {@link FileWriteConnector}.
 * 
 * @author OA3 Core Team
 */
public class ArchivingWriteConnectorTestCase extends TestCase {

	private static final Log log = LogFactory.getLog(ArchivingWriteConnectorTestCase.class);
	private static String TEMPLATE_DIR = "test/unit/input/org/openadaptor/auxil/connector/iostream/writer/ArchivingWriteConnectorTest";
	private static String DIR = "build/test/unit/output/org/openadaptor/auxil/connector/iostream/writer/ArchivingWriteConnectorTest";

	private static String TEST_DIR_1 = "test1";
	private static String TEST_DIR_2 = "test2";
	private static String TEST_DIR_3 = "test3";
	private static String TEST_DIR_4 = "test4";
	private static String TEST_DIR_5 = "test5";
	private long time = 10000;

	protected void setUp() throws Exception {
		super.setUp();
		FileUtils.deleteQuietly(new File(DIR));
		FileUtils.copyDirectory(new File(TEMPLATE_DIR), new File(DIR));
		FileUtils.forceMkdir(new File(DIR + "/" + TEST_DIR_1));
		FileUtils.forceMkdir(new File(DIR + "/" + TEST_DIR_2));
		FileUtils.forceMkdir(new File(DIR + "/" + TEST_DIR_3));
		FileUtils.forceMkdir(new File(DIR + "/" + TEST_DIR_4));
		FileUtils.forceMkdir(new File(DIR + "/" + TEST_DIR_5));
	}

	/**
	 * Three of the files in the directory should be deleted as they are all writable
	 * 
	 */
	public void testSimpleTooLarge() {
		prepareTest1();
		String testPath = DIR + "/" + TEST_DIR_1;
		assertTrue("Directory should contain 4 files at the start",(new File(testPath)).listFiles().length == 4);
		ArchivingFileWriterConnector ac = new ArchivingFileWriterConnector();
		ac.setMaxDirSize(1);
		ac.tryToShrinkArchive(testPath + "/" + "File04.txt");
		assertTrue("File04.txt should still be left in directory",(new File(testPath + "/" + "File04.txt")).exists());
		assertTrue("File01.txt should not be left in directory",!(new File(testPath + "/" + "File01.txt")).exists());
		assertTrue("File02.txt should not be left in directory",!(new File(testPath + "/" + "File02.txt")).exists());
		assertTrue("File03.txt should not be left in directory",!(new File(testPath + "/" + "File03.txt")).exists());
	}

	/**
	 * Three of the files in the directory should be deleted as they are all writable<br>
	 * However as File02.txt is the one 'in use' so it should not be deleted
	 */
	public void testSimpleTooLarge_2() {
		prepareTest2();
		String testPath = DIR + "/" + TEST_DIR_2;
		assertTrue("Directory should contain 4 files at the start",(new File(testPath)).listFiles().length == 4);
		ArchivingFileWriterConnector ac = new ArchivingFileWriterConnector();
		ac.setMaxDirSize(1);
		ac.tryToShrinkArchive(testPath + "/" + "File02.txt");
		assertTrue("File02.txt should still be left in directory",(new File(testPath + "/" + "File02.txt")).exists());
		assertTrue("File01.txt should not be left in directory",!(new File(testPath + "/" + "File01.txt")).exists());
		assertTrue("File03.txt should not be left in directory",!(new File(testPath + "/" + "File03.txt")).exists());
		assertTrue("File04.txt should not be left in directory",!(new File(testPath + "/" + "File04.txt")).exists());
	}

	/**
	 * Two of the files in the directory should be deleted as they are all writable<br>
	 * File02.txt is 'read only' so it should not be deleted<br>
	 * File03.txt is the one 'in use' so it should not be deleted
	 */
	public void testOneNotWriteable() {
		prepareTest3();
		String testPath = DIR + "/" + TEST_DIR_3;
		assertTrue("Directory should contain 4 files at the start",(new File(testPath)).listFiles().length == 4);
		ArchivingFileWriterConnector ac = new ArchivingFileWriterConnector();
		ac.setMaxDirSize(1);
		ac.tryToShrinkArchive(testPath + "/" + "File03.txt");
		assertTrue("File02.txt should still be left in directory",(new File(testPath + "/" + "File02.txt")).exists());
		assertTrue("File03.txt should still be left in directory",(new File(testPath + "/" + "File03.txt")).exists());
		assertTrue("File01.txt should not be left in directory",!(new File(testPath + "/" + "File01.txt")).exists());
		assertTrue("File04.txt should not be left in directory",!(new File(testPath + "/" + "File04.txt")).exists());
	}

	/**
	 * Make sure it can handle an empty directory without falling over
	 */
	public void testEmptyDir() {
		String testPath = DIR + "/" + TEST_DIR_4;
		assertTrue("Directory should be empty the start",(new File(testPath)).listFiles().length == 0);
		ArchivingFileWriterConnector ac = new ArchivingFileWriterConnector();
		ac.setMaxDirSize(1);
		ac.tryToShrinkArchive(testPath + "/" + "File03.txt");
		assertTrue("Directory should be empty",(new File(testPath)).listFiles().length == 0);
	}

	/**
	 * The will be 4 files in the directory with the file in filename not being in the directory<br>
	 * As the 4 equal size files amount to a bit more than the 2Mb set as max directory size then<br>
	 * one file should be deleted. As File01.txt is the oldest then that is the one that should be deleted 
	 */
	public void testDeleteOnlyTheRequiredFile() {
		prepareTest5();
		String testPath = DIR + "/" + TEST_DIR_5;
		assertTrue("Directory should contain 4 files at the start",(new File(testPath)).listFiles().length == 4);
		ArchivingFileWriterConnector ac = new ArchivingFileWriterConnector();
		ac.setMaxDirSize(2);
		ac.tryToShrinkArchive(testPath + "/" + "File05.txt");
		assertTrue("File01.txt should not be left in directory",!(new File(testPath + "/" + "File01.txt")).exists());
		assertTrue("File02.txt should still be left in directory",(new File(testPath + "/" + "File02.txt")).exists());
		assertTrue("File03.txt should still be left in directory",(new File(testPath + "/" + "File03.txt")).exists());
		assertTrue("File04.txt should still be left in directory",(new File(testPath + "/" + "File04.txt")).exists());
	}

	private void prepareTest1() {
		String copyFile;
		String testDir = TEST_DIR_1;
		File retFile;
		
		copyFile = "File01.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File02.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File03.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File04.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);

	}
	

	private void prepareTest2() {
		String copyFile;
		String testDir = TEST_DIR_2;
		File retFile;
		
		copyFile = "File01.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File02.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File03.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File04.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
	}
	
	private void prepareTest3() {
		String copyFile;
		String testDir = TEST_DIR_3;
		File retFile;
		
		copyFile = "File01.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File02.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		retFile.setReadOnly();
		copyFile = "File03.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File04.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
	}
	
	private void prepareTest5() {
		String copyFile;
		String testDir = TEST_DIR_5;
		File retFile;
		
		copyFile = "File01.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File02.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File03.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
		copyFile = "File04.txt";
		retFile = preCopyFile(testDir, copyFile, 1000);
	}

	private File preCopyFile(String testDir, String copyFile, int delay) {
		String destPath = DIR + "/" + testDir + "/" + copyFile;
		File destFile = null;
		try {
			destFile = new File(destPath);
			if (destFile.exists()) {
				destFile.delete();
			}
			copyFile(new File(DIR + "/" + copyFile), destFile);
		} catch (IOException e) {
			log.error("Error copying file to " + destPath);
			e.printStackTrace();
		}
		//Need to make sure that the lastModified dates are different for each file
		/*
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			log.error("Error while sleeping");
			e.printStackTrace();
		}
		*/
		destFile.setLastModified(time++);
		return destFile;
	}
	

	private static void copyFile(File sourceFile, File destFile)
	throws IOException {
		if (!sourceFile.exists()) {
			return;
		}
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		if (destination != null) {
			destination.close();
		}

	}

}
