/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model.util;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.io.FileUtils;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
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
        
    @Override
    public Source resolve (final String href, final String base) 
      throws TransformerException 
      {
        log.info("resolve({}, {})", href, base);
        
        final File cacheFolder = new File(cacheFolderPath);
        
        if (!cacheFolder.exists())
          {
            cacheFolder.mkdirs();  
          }
        
        try
          {
            final String mangledName = URLEncoder.encode(href, "UTF-8");
            final File cachedFile = new File(cacheFolder, mangledName);
            final long elapsed = System.currentTimeMillis() - cachedFile.lastModified();
            
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

    private void cacheDocument (final @Nonnull File cachedFile, final @Nonnull String href)
      throws IOException 
      {
        log.debug(">>>> caching external document to {}", cachedFile);
        final File tempFile = File.createTempFile("temp", ".txt", new File(cacheFolderPath));
        tempFile.deleteOnExit();
        final FileChannel channel = new RandomAccessFile(tempFile, "rw").getChannel();
        log.debug(">>>> waiting for lock...");
        @Cleanup(value="release") final FileLock lock = channel.lock();
        log.debug(">>>> got lock...");
        
        try
          {
            FileUtils.copyURLToFile(new URL(href), tempFile, connectionTimeout, readTimeout);   
            tempFile.renameTo(cachedFile);
          }
        catch (IOException e)
          { 
            if (cachedFile.exists())
              {
                log.warn("Error while retrieving document from {}: {} - using previously cached file", href, e.toString());
              }
            else
              {
                throw e;   
              }
          }
        
        log.debug(">>>> done");
      }
  }