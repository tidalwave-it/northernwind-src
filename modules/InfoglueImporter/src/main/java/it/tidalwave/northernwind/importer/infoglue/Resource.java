/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.importer.infoglue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
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
        log.info("Adding and committing {} {} ...", dateTime, file.getAbsolutePath());
        final OutputStream os = new FileOutputStream(file);
        os.write(contents);
        os.close();
        Utilities.exec("/bin/sh", "-c", "cd " + Main.hgFolder.getAbsolutePath() + " && /usr/bin/hg add " + fixedPath);
        Utilities.exec("/bin/sh", "-c", "cd " + Main.hgFolder.getAbsolutePath() + " && /usr/bin/hg commit -m \"...\" " + fixedPath + " --date \'" + dateTime.toDate().getTime() / 1000 + " 0\'");
      }
  }

