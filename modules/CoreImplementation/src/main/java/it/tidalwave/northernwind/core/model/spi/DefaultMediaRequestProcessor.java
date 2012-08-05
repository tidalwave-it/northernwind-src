/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model.spi; 

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import org.joda.time.Duration;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.Media.Media;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @Order(HIGHEST_PRECEDENCE)
public class DefaultMediaRequestProcessor<ResponseType> implements RequestProcessor
  {
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    @Inject @Nonnull
    protected ResponseHolder<ResponseType> responseHolder;

    @Getter @Setter
    private Duration duration = Duration.standardDays(7); // FIXME: rename to expirationTime

    @Override @Nonnull
    public Status process (final @Nonnull Request request) 
      throws NotFoundException, IOException
      {
        final String relativeUri = request.getRelativeUri();
        
        if (relativeUri.startsWith("/media"))
          {
            final String mediaUri = relativeUri.replaceAll("^/media", "");
            
            // E.g. http://stoppingdown.net/media/stillimages/20120802-0010/1920/image.jpg
            if (mediaUri.startsWith("/stillimages/") || mediaUri.startsWith("/movies/"))
              {
                final List<String> parts = new ArrayList<>(Arrays.asList(mediaUri.substring(1).split("/")));
                // 
                // TODO: retrocompatibility with StoppingDown and Bluette
                // http://stoppingdown.net/media/stillimages/1920/20120802-0010.jpg
                // Should be dealt with a specific redirector in the website and removed from here.
                //
                if (parts.size() == 3)
                  {
                    final String fileName = parts.remove(parts.size() - 1); // 20120802-0010.jpg
                    final String size = parts.remove(parts.size() - 1);     // 1920
                    final String redirect = "/media" + joined(parts) + "/" + fileName.replaceAll("\\..*$", "") + "/" + size + "/" + fileName;
                    log.info(">>>> permanently redirecting to {}", redirect);
                    responseHolder.response().permanentRedirect(redirect).put();
                    return BREAK;
                  }
                
                final String fileName = parts.remove(parts.size() - 1); // image.jpg
                final String size = parts.remove(parts.size() - 1);     // 1920
                final String mediaId = parts.remove(parts.size() - 1);  // 20120802-0010
                final String base = joined(parts);                      
                final String mediaUri2 = base + "/" + size + "/" + mediaId + ".jpg"; // FIXME: take extension from fileName
                final Media media = siteProvider.get().getSite().find(Media).withRelativePath(mediaUri2).result();
                final ResourceFile file = media.getFile();
                log.info(">>>> serving contents of /{} ...", file.getPath());
                responseHolder.response().fromFile(file)
                                         .withExpirationTime(duration)
//                                         .withContentDisposition(fileName)
                                         .put();
                return BREAK;
              }
            else
              {
                final Media media = siteProvider.get().getSite().find(Media).withRelativePath(mediaUri).result();
                final ResourceFile file = media.getFile();
                log.info(">>>> serving contents of /{} ...", file.getPath());
                responseHolder.response().fromFile(file)
                                         .withExpirationTime(duration)
                                         .put();
                return BREAK;
              }
          }
        
        return CONTINUE;
      }
    
    @Nonnull
    private static String joined (final @Nonnull List<String> list)
      {
        final StringBuilder buffer = new StringBuilder();
        
        for (final String s : list)
          {
            buffer.append("/").append(s);
          }
        
        return buffer.toString();
      }
  }
