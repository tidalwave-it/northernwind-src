/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
@RequiredArgsConstructor
public class Resource
  {
    @Getter
    private final DateTime dateTime;

    private final String path;

    private final byte[] contents;

    // TODO: add an XML bag of properties (metadata.xml) with mime type and language list
    // XML must be formatted and fields sorted
    public void addAndCommit() 
      throws Exception
      {
        String fixedPath = this.path;
        final File file = new File(Main.hgFolder, fixedPath);
        file.getParentFile().mkdirs();
        System.err.println("Writing " + file.getAbsolutePath() + "...");
        final OutputStream os = new FileOutputStream(file);
        os.write(contents);
        os.close();
        Utilities.exec("/bin/sh", "-c", "cd " + Main.hgFolder.getAbsolutePath() + " && /usr/bin/hg add " + fixedPath);
        Utilities.exec("/bin/sh", "-c", "cd " + Main.hgFolder.getAbsolutePath() + " && /usr/bin/hg commit -m \"...\" " + fixedPath + " --date \'" + dateTime.toDate().getTime() / 1000 + " 0\'");
      }
  }

