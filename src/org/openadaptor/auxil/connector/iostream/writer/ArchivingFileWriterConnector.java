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

package org.openadaptor.auxil.connector.iostream.writer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.IFileWriteConnector;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;
/**
 * <table border="0">
 * <tr>
 * <td colspan="2" width="900px">
 * The ArchivingFileWriterConnector is a component intended to:
 * <br>1)	Give the user the option of attempting to limit the size of the directory being written to.
 * <br>2)	Allow the user to make use of existing File Connectors
 * <br>
 * <br>If does this by allowing the user to:
 * <br>1)	Specify archiving rules via archive properties for the ArchivingFileWriterConnector bean.
 * <br>2)	Delegating the file writing to a file write connector bean.
 * <br>
 * <br>There are a few restrictions and considerations:
 * <br>1)	The file write connector class must implement the IFileWriteConnector interface.
 * <br>2)	If it does not then a ValidationException will be thrown by the ArchivingFileWriterConnector.
 * <br>3)	If the file write connector determines the path that will be written to based on the payload then the ArchivingFileWriterConnector should
 * <br>		only check the size of the directory being written to after the write.
 * <br>		Note that if the path does not dynamically change (i.e. only the file name changes) then you can also check the directory size at
 * <br>		connection time, as long as the path is specified in the filename property for the file write connector.
 * <br>4)  Checking the directory size on every write will consume processing time
 * <br>5)	The directory size check does not include the contents of sub directories
 * <br>6)	The 'delete' will not try to delete directories contained in the checked directory nor will it try to delete files that are not writable.
 * <br>7)	The 'delete' will not try to delete the file being written to. If it is the only file writable file in the directory the directory may exceed the specified size limit.
 * <br>
 * </td>
 * </tr> 
 * <tr>
 * <td valign="top"><b>Properies</b></td >
 * <tr>
 * <td valign="top">checkAtConnection</td > <td>[false] When set to true causes the component to check/shrink the directory size immediately after connecting.</td> 
 * </tr>
 * <tr>
 * <td valign="top">checkAtDisconnection</td > <td>[false] When set to true causes the component to check/shrink the directory size immediately before disconnecting.</td> 
 * </tr>
 * <tr>
 * <td valign="top">checkAfterDelivery</td > <td>[false] When set to true causes the component to check/shrink the directory size immediately after writing to the file.</td> 
 * </tr>
 * <tr>
 * <td valign="top">numDeliveriesBetweenChecks</td > <td>[0] When checkAfterDelivery is true, check every 0 to n deliveries, where 0 and 1 means check every time.</td> 
 * </tr>
 * <tr>
 * <td valign="top">maxDirSize</td> <td>[0] Size (Mb) at which an attempt will be made to delete the oldest file in the directory:
 * <br> 1  =  1*1024*1024 bytes</td>
 * </tr>
 * <tr><td>&nbsp;</td></tr>
 * </table>
 * 
 * @author OA3 Core Team
 */
public class ArchivingFileWriterConnector extends Component implements IWriteConnector,
		IMetadataAware, ITransactional {

	private static final Log log = LogFactory.getLog(ArchivingFileWriterConnector.class);

	//private File dirFile;
	//private SortedMap sortedFiles = new TreeMap();

	private IFileWriteConnector delegate;
	private boolean checkAtConnection = false;
	private boolean checkAtDisconnection = false;
	private boolean checkAfterDelivery = false;
	private int numDeliveriesBetweenChecks = 0;
	private int maxDirSize = 0;

	private boolean failedDelete = false;
	private int numDeliveriesCount = 0;
	private Directory dir;
	//private String directoryPath = "";

	/**
	 * Constructor
	 */
	public ArchivingFileWriterConnector() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 */
	public ArchivingFileWriterConnector(String id) {
		super(id);
	}

	public void setMetadata(Map metadata) {
		if (this.delegate instanceof IMetadataAware) {
			((IMetadataAware) this.delegate).setMetadata(metadata);
		}
	}

	/**
	 * @return the delegate#getResource() if the delegate is an ITransactional,
	 *         null otherwise.
	 * @see ITransactional#getResource()
	 */
	public Object getResource() {
		if (this.delegate instanceof ITransactional) {
			return ((ITransactional) this.delegate).getResource();
		} else {
			return null;
		}
	}

	/**
	 * Sets the delegate IWriteConnector.
	 * 
	 * @param delegate
	 *            the delegate IFileWriteConnector
	 * @see IFileWriteConnector#getDelegate()
	 */
	public void setDelegate(IFileWriteConnector delegate) {
		this.delegate = delegate;
	}

	/**
	 * @see IWriteConnector#getDelegate()
	 */
	public IFileWriteConnector getDelegate() {
		return delegate;
	}

	/**
	 * Make sure that the delegated writer component supports the required interface
	 * <br>
	 * Make sure that maxDirSize is set > 0
	 * <br>
	 * Make sure that numDeliveriesBetweenChecks is set > 0
	 */
	public void validate(List exceptions) {
		this.delegate.validate(exceptions);
		
		if (this.delegate instanceof IFileWriteConnector) {
			if (this.delegate.getFilename()!= null &&  this.delegate.getFilename().length() > 0) {
				log.debug(this.getId() + " Class suports required interfaces and filename was set in delegate: " + ((IComponent) this.delegate).getId());
			}
			else {
				exceptions.add(new ValidationException(this.getId() + " delegate: " + ((IComponent) this.delegate).getId() + " filename property needs to be set", this));
			}
		}
		else {
			exceptions.add(new ValidationException(this.getId() + " delegate: " + ((IComponent) this.delegate).getId() + " class must implement IFileWriteConnector", this));
		}
		if (getMaxDirSize()<= 0) {
			exceptions.add(new ValidationException(this.getId() + " You must specify a maxDirSize > 0", this));
		}
		if (getNumDeliveriesBetweenChecks()< 0) {
			exceptions.add(new ValidationException(this.getId() + " You must specify a numDeliveriesBetweenChecks >= 0", this));
		}
	}

	/* (non-Javadoc)
	 * @see org.openadaptor.core.IFileWriteConnector#connect()
	 */
	public void connect() {
		this.delegate.connect();
		//createSortedFilesMap(dirFile);

		// Do check at connection?
		if (isCheckAtConnection()) {
			tryToShrinkArchive(this.delegate.getFilename());
		}
	}

	/* (non-Javadoc)
	 * @see org.openadaptor.core.IFileWriteConnector#disconnect()
	 */
	public void disconnect() {
		// Do check at disconnection?
		if (isCheckAtDisconnection()) {
			tryToShrinkArchive(this.delegate.getFilename());
		}
		
		this.delegate.disconnect();
	}

	/* (non-Javadoc)
	 * @see org.openadaptor.core.IFileWriteConnector#deliver(java.lang.Object[])
	 */
	public Object deliver(Object[] data) {
		Object anObject = this.delegate.deliver(data);
		
		// Do check after delivery?	
		if (isCheckAfterDelivery()){
			if (numDeliveriesBetweenChecks > 0 ){
				numDeliveriesCount++;
			}
			//Test if check should occur every delivery or every n deliveries?
			if (numDeliveriesBetweenChecks == 0 || (numDeliveriesCount >= numDeliveriesBetweenChecks)) { 
				tryToShrinkArchive(this.delegate.getFilename());
				numDeliveriesCount = 0;
			}
		}
		return anObject;
	}
	
	/**
	 * Calls method to shrink the archive if the directory size exceeds the set maxDirSize.<br>
	 * Use this signature when we may have a new filename
	 */
	public void tryToShrinkArchive(String fileName) {
		//Get directory path every time as the dynamic file writer may change the path while the adaptor is running
		dir = new Directory(fileName);
		long dirSize = dir.size;
		log.debug(this.getId() + " Directory/Max size: " + String.valueOf(dirSize) + "/" + getMaxDirSize());
		
		// shrink archive if required
		if (dirSize > getMaxDirSize()) { //Is the directory bigger than wanted?
			dir.createSortedFilesMap(dir.dirFile); //Build file map first time round
			shrinkArchive(dir);
		}
	}
	/**
	 * Calls method to shrink the archive if the directory size exceeds the set maxDirSize.
	 * Use this signature when we are sure the filename has not changed since we built the Directory class 'instance' dir 
	 */	
	private void tryToShrinkArchive(Directory dir) {
		long dirSize = dir.getDirSize(dir.dirFile);
		log.debug(this.getId() + " Directory/Max size: " + String.valueOf(dirSize) + "/" + getMaxDirSize());
		
		// shrink archive if required
		if (dirSize > getMaxDirSize()) { //Is the directory still bigger than wanted?
			shrinkArchive(dir);
		}
	}

	/**
	 * This method shrink the archive based on the given configuration.
	 * It will check to ensure that you do not delete the file currently being written to
	 */
	protected void shrinkArchive(Directory dir) {
		// Reset this in case of failure last time
		failedDelete = false;
		log.debug(this.getId() + " We will attempt to delete something from " + dir.dirPath);
		
		// Only go in here if there is more than one file in the directory.
		// Otherwise it is probably the one that we are writing to!
		if (dir.sortedFiles.size() > 0) { 
			// The files are sorted in age older and the oldest is deleted as long as it is not the one being written to
			shrinkSortedfiles(dir);
		} else {
			log.info(this.getId() + " Failed to delete as there is nothing that can be deleted in the directory "); //Is this true? What about test on connect?
			failedDelete = true;
		}

		// Keep on repeating delete until we have shrunk the archive enough,
		// but not if we have failed to delete anything last time!
		if (!failedDelete) {
			tryToShrinkArchive(dir);
		}
	}

	/**
	 * Using the map of sorted files get the oldest file that is not the same file as the file being written to, and delete it.
	 */
	protected void shrinkSortedfiles(Directory dir) {
		log.debug(this.getId() + " First file modified timestamp: " + dir.sortedFiles.firstKey());
		File oldestFile = (File) dir.sortedFiles.get(dir.sortedFiles.firstKey());
		log.debug(this.getId() + " First file: " + oldestFile);
		
		if (oldestFile.exists()) { //Is the file still in the directory?
			if (oldestFile.delete()) { //We deleted OK
				log.info(this.getId() + " Deleted: " + oldestFile);
				dir.sortedFiles.remove(dir.sortedFiles.firstKey());
			}
			else { //We failed to delete
				log.error(this.getId() + " Failed to delete: " + oldestFile);
				dir.sortedFiles.remove(dir.sortedFiles.firstKey());
				failedDelete = true;
			}
		}
		else { //The file is no longer in the directory, so remove it from our list
			dir.sortedFiles.remove(dir.sortedFiles.firstKey());
		}
	}
	
	public boolean isCheckAtConnection() {
		return checkAtConnection;
	}

	public void setCheckAtConnection(boolean checkAtConnection) {
		this.checkAtConnection = checkAtConnection;
	}

	public boolean isCheckAtDisconnection() {
		return checkAtDisconnection;
	}

	public void setCheckAtDisconnection(boolean checkAtDisconnection) {
		this.checkAtDisconnection = checkAtDisconnection;
	}

	public boolean isCheckAfterDelivery() {
		return checkAfterDelivery;
	}

	public void setCheckAfterDelivery(boolean checkAfterDelivery) {
		this.checkAfterDelivery = checkAfterDelivery;
	}

	public int getNumDeliveriesBetweenChecks() {
		return numDeliveriesBetweenChecks;
	}

	public void setNumDeliveriesBetweenChecks(int numDeliveriesBetweenChecks) {
		this.numDeliveriesBetweenChecks = numDeliveriesBetweenChecks;
	}

	public int getMaxDirSize() {
		return maxDirSize;
	}

	/**
	 * Multiplies the supplied maxDirSize by 1024*1024 to get the bytes equivalent and sets that calculated value
	 * @param maxDirSize
	 */
	public void setMaxDirSize(int maxDirSize) {
		log.info(this.getId() + " The max directory size has been set to " + maxDirSize + " Mb");
		this.maxDirSize = maxDirSize*1024*1024;
	}
	
	public long getDirSize() {
		return dir.size;
	}
	
	public SortedMap getSortedFilesMap(){
		return dir.sortedFiles;
	}
	

	private class Directory {
		public String fileName;
		public String dirPath;
		public File dirFile;
		public long size;
		public SortedMap sortedFiles;
		
		public Directory (String fileName) {
			this.fileName = fileName;
			this.dirPath = getDirectoryPath(this.fileName);
			this.dirFile = new File(this.dirPath);
			if (!this.dirFile.isDirectory()) {
				log.error(getId() + " " + this.dirFile.getPath() + " is not a directory");
			}
			this.size = getDirSize(this.dirFile);
			this.sortedFiles = createSortedFilesMap(this.dirFile);
		}
		/**
		 * Extracts the directory path from filename.
		 * User must be careful to correctly specify the filename as otherwise there is a danger that the wrong directory will be reduced in size!!!
		 * @param filename
		 * @return
		 */
		
		private String getDirectoryPath (String fileName) {
			File file = new File(fileName);
			//Might we end up going back 1 directory when we shouldn't?
			if (!file.isDirectory()) {
				return file.getParent();
			}
			return fileName; //In this situation fileName is already the directory path
		}
		
		/**
		 * Gets the directory size of the directory path dirFile.<br>
		 * Note that this does not include the size of any sub-directories but does include the size of non-writable files in directory dirFile.
		 * @return
		 */
		private long getDirSize(File dirFile) {
			//return FileUtils.sizeOfDirectory(dirFile);
			 long dirsize = 0;
			 File[] currentFolder = dirFile.listFiles();

			 for (int q = 0; q < currentFolder.length; q++) {
				 if (!currentFolder[q].isDirectory()) {
					 dirsize += currentFolder[q].length();
				 }
			 }
			 return dirsize;
		}
		
		/**
		 * This method will look in the supplied directory and create a sorted map of the writable 
		 * files in that directory.<br>
		 * It will not include any non-writable files on sub directories within the supplied directory.<br>
		 * The entries are sorted on the key, which is the lastModified date/time for the writable files.
		 * @param dir
		 */
		private SortedMap createSortedFilesMap(File dirFile) {
			SortedMap sf = new TreeMap();
			File[] files = dirFile.listFiles();
			int i = 0;
			while (i<files.length) {
				File file = files[i];
				log.debug(getId() + " Directory content: " + file + " " + file.lastModified());
				//Make sure that both use the "/" file separator (just for the purposes of the .equals test
				String rep1 = this.fileName.replaceAll("\\\\", "/");
				String rep2 = file.getPath().replaceAll("\\\\", "/");
				
				if (!rep1.equals(rep2) ) { //Don't add the one being written to into the list
					if (file.isFile() && file.canWrite() && !file.isHidden()) { //Must be a file that is writable but not hidden
						sf.put(String.valueOf(file.lastModified()), file);
					}
					else {
						log.info(getId() + " Did not add " + file.getName() + " to deletable list as it is a dir or is not writeable or it is hidden");
					}
				}
				i++;
			}
				
			return sf;
		}
	}
}
