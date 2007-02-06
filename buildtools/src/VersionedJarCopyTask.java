package org.openadaptor.util.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

public class VersionedJarCopyTask extends Task {

  private FileUtils fileUtils = FileUtils.newFileUtils();
  
  private List filesets = new ArrayList();

  private File toDir;

  private Properties versionProperties = new Properties();

  private String manifestFilename;

  private String version = "unknown";

  public void setManifest(String filename) {
    manifestFilename = filename;
  }
  
  public void addFileset(FileSet set) {
    filesets.add(set);
  }

  public void setToDir(String dirname) {
    toDir = new File(dirname);
  }

  public void setVersionProperties(String filename) {
    FileInputStream in;
    try {
      in = new FileInputStream(filename);
      versionProperties.load(in);
      in.close();
    } catch (FileNotFoundException e) {
      throw new BuildException(e);
    } catch (IOException e) {
      throw new BuildException(e);
    }
  }

  public void setVersion(String version) {
    this.version = version;
  }
  
  public void execute() throws BuildException {
    Manifest manifest = null;
    if (manifestFilename != null) {
      manifest = new Manifest();
      manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
      manifest.getMainAttributes().putValue("Built-By", getProject().getProperty("user.name"));
      manifest.getMainAttributes().putValue("Created-By", this.getClass().getName());
      manifest.getMainAttributes().putValue("Created", (new Date()).toString());
      manifest.getMainAttributes().putValue("Version", version);
    }
    
    for (Iterator iter = filesets.iterator(); iter.hasNext();) {
      FileSet fileSet = (FileSet) iter.next();
      DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
      String[] files = ds.getIncludedFiles();
      for (int i = 0; i < files.length; i++) {
        File jar = new File(fileSet.getDir(getProject()).getAbsolutePath() + File.separator +  files[i]);
        String jarName = jar.getName();

        // write manifest entry
        if (manifest != null) {
          Attributes atts = new Attributes();
          manifest.getEntries().put(jarName, atts);
          String originalJar = versionProperties.containsKey(jarName) ? versionProperties.getProperty(jarName) : "unknown";
          atts.putValue("originaljar", originalJar);
        }

        // copy jar
        jarName = versionProperties.containsKey(jarName) ? versionProperties.getProperty(jarName) : jarName;
        File destFile = new File(toDir, jarName);
        try {
          fileUtils.copyFile(jar, destFile);
        } catch (IOException e) {
          throw new BuildException(e);
        }
      }
    }
    if (manifest != null) {
      try {
        OutputStream os = new FileOutputStream(manifestFilename);
        manifest.write(os);
        os.close();
      } catch (Exception e) {
        throw new BuildException("failed to write manifest, " + e.getMessage(), e);
      }
    }
  }

}
