/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.util;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.util.UrlEncoding.encodedUtf8;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class CachedURIResolver implements URIResolver
  {
    @Getter @Setter
    private String cacheFolderPath = System.getProperty("java.io.tmpdir") + "/CachedURIResolver";

    @Getter @Setter
    private long expirationTime = 60 * 60 * 1000L; // 1 hour

    @Getter @Setter
    private int connectionTimeout = 10 * 1000; // 10 secs

    @Getter @Setter
    private int readTimeout = 10 * 1000; // 10 secs

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override
    public Source resolve (final String href, final String base)
      throws TransformerException
      {
        try
          {
            log.info("resolve({}, {})", href, base);

            final var cacheFolder = new File(cacheFolderPath);

            if (!cacheFolder.exists())
              {
                mkdirs(cacheFolder);
              }

            final var cachedFile = new File(cacheFolder, encodedUtf8(href));
            final var elapsed = System.currentTimeMillis() - cachedFile.lastModified();

            log.debug(">>>> cached file is {} elapsed time is {} msec", cachedFile, elapsed);

            if (!cachedFile.exists() || (elapsed > expirationTime))
              {
                cacheDocument(cachedFile, href);
              }

            return new StreamSource(new FileInputStream(cachedFile));
          }
        catch (IOException e)
          {
            throw new TransformerException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private void cacheDocument (@Nonnull final File cachedFile, @Nonnull final String href)
      throws IOException
      {
        log.debug(">>>> caching external document to {}", cachedFile);
        final var tempFile = File.createTempFile("temp", ".txt", new File(cacheFolderPath));
        tempFile.deleteOnExit();
        log.debug(">>>> waiting for lock...");

        try (final var channel = new RandomAccessFile(tempFile, "rw").getChannel();
             final var lock = channel.lock())
          {
            log.debug(">>>> got lock: {}", lock);
            FileUtils.copyURLToFile(new URL(href), tempFile, connectionTimeout, readTimeout);
            rename(tempFile, cachedFile);
          }
        catch (IOException e)
          {
            if (cachedFile.exists())
              {
                log.warn("Error while retrieving document from {}: {} - using previously cached file",
                         href, e.toString());
              }
            else
              {
                throw e;
              }
          }

        log.debug(">>>> done");
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void mkdirs (@Nonnull final File folder)
      throws IOException
      {
        if (!folder.mkdirs())
          {
            throw new IOException("Cannot mkdirs for " + folder);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void rename (@Nonnull final File from, @Nonnull final File to)
      throws IOException
      {
        if (!from.renameTo(to))
          {
            throw new IOException("Cannot rename " + from + " to " + to);
          }
      }
  }
