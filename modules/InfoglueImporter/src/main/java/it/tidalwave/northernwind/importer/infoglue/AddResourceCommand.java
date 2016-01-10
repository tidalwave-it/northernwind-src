/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.importer.infoglue;

import java.time.ZonedDateTime;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class AddResourceCommand
  {
    @Getter
    private final ZonedDateTime dateTime;

    @Getter
    private final String path;

    @Getter
    private final byte[] contents;

    private final String comment;

    public void addAndCommit()
      throws Exception
      {
        String fixedPath = this.path;

        if (fixedPath.startsWith("/"))
          {
            fixedPath = "." + fixedPath;
          }

        final File file = new File(ResourceManager.hgFolder, fixedPath);
        file.getParentFile().mkdirs();
        log.info("Adding and committing {} {} ...", dateTime, file.getAbsolutePath());
        final @Cleanup OutputStream os = new FileOutputStream(file);
        os.write(contents);
        os.close();
        Utilities.exec("/bin/sh", "-c", "cd " + ResourceManager.hgFolder.getAbsolutePath() + " && /usr/bin/hg add \"" + fixedPath + "\"");
        Utilities.exec("/bin/sh", "-c", "cd " + ResourceManager.hgFolder.getAbsolutePath() + " && /usr/bin/hg commit -m \"" + comment + "\" \"" + fixedPath + "\" --date \'" + dateTime.toInstant().getEpochSecond() + " 0\'");
      }
  }

