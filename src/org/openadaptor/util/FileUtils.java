/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.util;

/*
 * File: $Header$ Rev: $Revision$
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some useful helper methods to centralise common functions
 * 
 * @author Russ Fennell
 */
public class FileUtils {
  private static final Log log = LogFactory.getLog(FileUtils.class);

  /**
   * Moves the file to the new file name.
   * 
   * @param fileName
   *          the path of the file to be moved
   * @param newName
   *          the path of the new file. If the new name is an existing directory then the file will be moved into it and
   *          keep the same file name
   * 
   * @throws RuntimeException
   *           if the destination file exists
   */
  public static void moveFile(String fileName, String newName) {
    moveFile(new File(fileName), new File(newName));
  }

  /**
   * Moves the file to the new file name.
   * 
   * @param oldFile
   *          the file to be moved
   * @param newFile
   *          the new file. If the new file is an existing directory then the file will be moved into it and keep the
   *          same file name
   * 
   * @throws RuntimeException
   *           if the source file does not exist or is a directory or if the destination file exists
   */
  public static void moveFile(File oldFile, File newFile) {
    if (!oldFile.exists())
      throw new RuntimeException("File not found [" + oldFile.getName() + "]");

    if (oldFile.isDirectory())
      throw new RuntimeException("File is a directory");

    if (newFile.exists()) {
      if (newFile.isDirectory()) {
        newFile = new File(newFile.getPath() + File.separator + oldFile.getName());

        if (newFile.exists())
          throw new RuntimeException("New file name [" + newFile.getPath() + "] exists. Cannot move file");

        log.info("File will be moved to " + newFile.getPath());
      } else {
        throw new RuntimeException("New file name [" + newFile.getPath() + "] exists. Cannot move file");
      }
    }

    oldFile.renameTo(newFile);
    log.debug("File successfully moved to " + newFile.getPath());
  }

  /**
   * Displays a directory chooser to get the new directory and then moves the supplied file to this location and
   * displays a message to let you know it has been done
   * 
   * @param existing
   *          the file to be moved
   * @param title
   *          the dialog title
   * 
   * @return a reference to the new file or null if the user pressed cancel
   * 
   * @see SwingUtils
   */
  // TODO: review this!
  // public static File displayDialogAndMoveFile(File existing, String title) {
  // File pwd = existing.getParentFile();
  //
  // File newDir = SwingUtils.displayDirectoryChooserDialog(pwd, title);
  // if ( newDir == null ) {
  // log.info("User pressed cancel");
  // return null;
  // }
  //
  // File newFile = new File(newDir.getPath() + File.separator + existing.getName());
  // moveFile(existing, newFile);
  //
  // String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
  // String msg = SwingUtils.getMultilineComponentText(new String[] {
  // "File",
  // tab + "<b>" + existing.getPath() + "</b>",
  // "",
  // "moved to",
  // tab + "<b>" + newFile.getPath() + "</b>"
  // });
  // JOptionPane.showMessageDialog(null, msg, "File Move", JOptionPane.INFORMATION_MESSAGE);
  //
  // return newFile;
  // }

  /**
   * Creates a directory based on the supplied directory name. Note that any parent directories will also be created as
   * needed.
   * 
   * @param dirName
   *          the path to the new directory
   * 
   * @throws RuntimeException
   *           if the path points to an existing file or there was a problem creatinmg the directory
   */
  public static void mkdir(String dirName) {
    File d = new File(dirName);

    if (d.exists()) {
      if (d.isDirectory()) {
        log.warn("Directory [" + d.getPath() + "] already exists.");
        return;
      } else {
        throw new RuntimeException("Path [" + d.getPath() + "] exists and is a file");
      }
    }

    if (!d.mkdirs())
      throw new RuntimeException("Failed to create directory [" + d.getPath() + "]");

    log.debug("Directory [" + d.getPath() + "] successfully created");
  }

  /**
   * Creates a temporary file called fooXXXXbar (where XXXX is replaced by a system generated number) in the specified
   * directory. It will be automatically deleted once the app exits.
   * 
   * @param directory
   *          the directory to create the temp file in
   * 
   * @return reference to the file
   * 
   * @throws IOException
   *           if there was a problem creating the file
   */
  public static File createTempFile(File directory) throws IOException {
    File f = File.createTempFile("foo", "bar", directory);
    f.deleteOnExit();

    return f;
  }

  /**
   * Creates a temporary file in the local system tmp directory (or the current working directory if this doesn't exist)
   * 
   * @return reference to the file
   * 
   * @throws IOException
   *           if there was a problem creating the file
   */
  public static File createTempFile() throws IOException {
    File dir = getTmpDir();
    if (dir == null)
      dir = new File(".");

    return createTempFile(dir);
  }

  /**
   * Creates a temporary file in the local system tmp directory (or the current working directory if this doesn't exist)
   * and sets its contents to be those supplied.
   * 
   * @param content
   *          the text to write into the file
   * 
   * @return reference to the file
   * 
   * @throws IOException
   *           if there was a problem creating the file
   */
  public static File createTempFile(String content) throws IOException {
    File dir = getTmpDir();
    if (dir == null)
      dir = new File(".");

    return createTempFile(dir, content);
  }

  /**
   * Creates a temporary file in the specified directory. If the directory is null then the default temporary-file
   * directory is used.
   * 
   * @param directory
   *          the directory to create the temp file in
   * @param content
   *          the text to wrwite into the file
   * 
   * @return reference to the file
   * 
   * @throws IOException
   *           if there was a problem creating the file
   */
  public static File createTempFile(File directory, String content) throws IOException {
    File f = createTempFile(directory);

    FileWriter out = new FileWriter(f);
    out.write(content);
    out.flush();
    out.close();

    return f;
  }

  /**
   * Creates a temporary directory called "fooXXXXXbar" (where XXXX is replaced by a system generated number) which will
   * be automatically deleted once the test exits.
   * 
   * @return reference to the directory
   * 
   * @throws IOException
   *           if the file could not be created
   */
  public static File createTempDir() throws IOException {
    File f = createTempFile();
    String uniqueName = f.getName();
    f.delete();

    FileUtils.mkdir(uniqueName);
    File d = new File(uniqueName);
    d.deleteOnExit();

    return d;
  }

  /**
   * Uses the "java.io.tmpdir" system property to get a reference to the local temporary directory
   * 
   * @return a reference to the directory or null if not defined or does not exist or is not a directory or is read-only
   */
  public static File getTmpDir() {
    String tmpDir = System.getProperty("java.io.tmpdir");
    if (tmpDir == null)
      return null;

    File f = new File(tmpDir);
    if (f.exists() && f.isDirectory() && f.canWrite())
      return f;

    return null;
  }

  /**
   * Get the file name extension from the supplied file. Note this is defined as the part of the name after the last "."
   * character. Also, this will be converted to lowercase.
   * 
   * @param f
   *          the file to process
   * 
   * @return the file extention of the supplied file or null if none present
   */
  public static String getExtension(File f) {
    String ext = null;

    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1)
      ext = s.substring(i + 1).toLowerCase();

    return ext;
  }

  /**
   * Returns an URL to the supplied file. Will search the classpath if required.
   * 
   * @param path
   *          the path to convert
   * 
   * @return a URL pointing to the file represented by the supplied path string or null if it does not exist
   */
  public static URL toURL(String path) {
    URL url;
    try {
      /*
       * Let java do the work instead of us... if ( path.indexOf(":") == -1 ) url = new URL("file:" + path); else url =
       * new URL(path);
       */
      // todo: the File() constructor will not fail so we will not use the Classloasder
      url = new File(path).toURL();
    } catch (MalformedURLException e) {
      url = ClassLoader.getSystemResource(path);
    }

    return url;
  }

  /**
   * Uses the toURL() call to convert the path and as a result can find files on the classpath.
   * 
   * @param path
   *          the path to the file
   * 
   * @return the contents of the file referenced to by the supplied path
   * 
   * @throws IOException
   *           if the file doe not exist
   */
  public static String getFileContents(String path) throws IOException {
    URL url = toURL(path);
    if (url == null)
      throw new IOException("File not found: " + path);

    return getFileContents(new FileInputStream(url.getPath()));
  }

  /**
   * @param file
   *          reference to the file
   * 
   * @return the contents of the file referenced to by the supplied file and returns its contents as a string
   * 
   * @throws IOException
   *           if the file doe not exist
   */
  public static String getFileContents(File file) throws IOException {
    if (file == null)
      throw new IOException("Null file passed");

    if (!file.exists())
      throw new IOException("File not found: " + file.getPath());

    return getFileContents(new FileInputStream(file));
  }

  /**
   * @param fstream
   *          the stream used to access the file contents
   * 
   * @return the contents of the file referenced by the input stream supplied. The file is processed in 1K chunks.
   * 
   * @throws IOException
   *           if the file doe not exist
   */
  public static String getFileContents(InputStream fstream) throws IOException {
    InputStreamReader in = new InputStreamReader(fstream);

    StringWriter writer = new StringWriter();
    char[] buf = new char[1024];
    int count;

    while ((count = in.read(buf)) != -1)
      writer.write(buf, 0, count);

    in.close();
    fstream.close();

    return writer.toString();
  }

  /**
   * First off, we check that the named directory exists (creates it if necessary) and then returns a pointer to the
   * named file in that directory. Note, the resulting file may not exist
   * 
   * @param directory
   *          the location of the file
   * @param fileName
   *          the name of the file
   * 
   * @throws RuntimeException
   *           if the named directory points to a file
   */
  public static File createOrGetFile(String directory, String fileName) {
    File d = new File(directory);

    if (!d.exists()) {
      d.mkdirs();
      log.debug("Created directory [" + d.getPath() + "]");
    } else {
      if (d.isFile())
        throw new RuntimeException("The supplied directory [" + directory + "] points to a file");
    }

    File f = new File(d.getPath() + "/" + fileName);
    if (f.exists())
      log.debug("Preferences file found: " + f.getPath());

    return f;
  }

  /**
   * Creates a file based on the path supplied and writes the supplied contents ro it. If the file exists then the
   * overwrite and append flags come into play.
   * 
   * @param path
   *          the path to the file
   * @param contents
   *          the text to write into the file after it's created
   * @param overwrite
   *          if true then existing files will be overwritten
   * @param append
   *          if true then the contents will be appended to existing files
   * 
   * @return a pointer to the newly created file
   */
  public static File createFile(String path, String contents, boolean overwrite, boolean append) {
    log.debug("Creating file [" + path + "]");

    if (path == null)
      throw new RuntimeException("Null path passed");

    File f = new File(path);

    if (f.exists()) {
      if (!overwrite)
        throw new RuntimeException("File exists");
      else
        log.debug("File exists, will overwrite");

      if (append) {
        log.debug("Will append data");

        String s;
        try {
          s = FileUtils.getFileContents(f);
        } catch (IOException e) {
          throw new RuntimeException("Failed to append to contents: " + e.toString());
        }

        contents = s + "\n" + contents;
      }
    }

    writeToFile(f, contents);

    return f;
  }

  /**
   * writes the supplied contents tot he supplied file
   * 
   * @param f
   *          the file to be updated
   * @param contents
   *          the text to write into the file
   */
  public static void writeToFile(File f, String contents) {
    FileWriter fsStream = null;

    try {
      fsStream = new FileWriter(f);

      BufferedWriter out = new BufferedWriter(fsStream);
      out.write(contents);
      out.close();
    } catch (Exception e) {
      throw new RuntimeException("Error writing to file [" + f.getPath() + "}: " + e.toString(), null);
    } finally {
      if (fsStream != null)
        try {
          fsStream.close();
        } catch (IOException e) {
        }
    }
  }

  /**
   * Checks that all elements of a directory path exist and if the "create" flag is set and the directory is not found
   * then it will be created. In this case we always return true.
   * 
   * @param path
   *          the path representing the directory
   * @param create
   *          if true then the directory will be created
   * 
   * @return true if the supplied directory path exists.
   */
  public static boolean checkDirectoryPath(String path, boolean create) {
    File d = new File(path);

    if (d.exists()) {
      if (!d.isDirectory()) {
        log.error("Directory [" + path + "] points to a file");
        return false;
      }
      return true;
    }

    if (create) {
      log.info("Creating path: " + path);
      d.mkdirs();
      return true;
    }

    return false;
  }

  /**
   * Searches the classpath for a named file
   * 
   * @param name
   *          the name of the file to search for
   * 
   * @return pointer to the named file or null if it doesn't exist
   */
  public static File searchClasspath(String name) {

    log.debug("Searching classpath for: " + name);

    // just on the offchange that the file is in the working directory or we
    // were supplied a correct path to the file
    File f = new File(name);
    if (f.exists()) {
      log.debug("Found file: " + f.getPath());
      return f;
    }

    // try the classpath
    URL url = ClassLoader.getSystemResource(name);
    if (url != null) {
      log.debug("Found file: " + url.getPath());
      return new File(url.getPath());
    }

    log.warn("File not found");
    return null;
  }

}
