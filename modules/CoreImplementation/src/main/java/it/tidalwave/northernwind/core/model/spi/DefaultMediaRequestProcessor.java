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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.time.Duration;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.ResourcePath;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;
import static it.tidalwave.northernwind.core.model.Media._Media_;
import static it.tidalwave.northernwind.core.model.RequestProcessor.Status.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j @Order(HIGHEST_PRECEDENCE+2)
public class DefaultMediaRequestProcessor<ResponseType> implements RequestProcessor
  {
    @Inject
    private Provider<SiteProvider> siteProvider;

    @Inject
    protected ResponseHolder<ResponseType> responseHolder;

    @Getter @Setter
    private Duration duration = Duration.ofDays(7); // FIXME: rename to expirationDuration

    @Getter @Setter
    private String uriPrefix = "media"; // FIXME

    @Override @Nonnull
    public Status process (@Nonnull final Request request)
      throws NotFoundException, IOException
      {
        var mediaUri = ResourcePath.of(request.getRelativeUri());

        if (!mediaUri.startsWith(uriPrefix))
          {
            return CONTINUE;
          }

        mediaUri = mediaUri.withoutLeading(); // media
        //
        // Media that can be served at different sizes are mapped to URLs such as:
        //
        //     /media/stillimages/<media-id>/<size>/image.jpg
        //     /media/movies/<media-id>/<size>/movie.jpg
        //
        // TODO: perhaps this logic should be moved to the media finder? Such as:
        //     find(Media).withSize(1920).withRelativePath(uri).result()
        //
        if (mediaUri.startsWith("stillimages") || mediaUri.startsWith("movies"))
          {
            //
            // TODO: retrocompatibility with StoppingDown and Bluette
            // http://stoppingdown.net/media/stillimages/1920/20120802-0010.jpg
            // Should be dealt with a specific redirector in the website and removed from here.
            //
            if (mediaUri.getSegmentCount() == 3)
              {
                final var extension = mediaUri.getExtension();
                final var fileName = mediaUri.getTrailing(); // 20120802-0010.jpg
                mediaUri = mediaUri.withoutTrailing();
                final var size = mediaUri.getTrailing();     // 1920
                mediaUri = mediaUri.withoutTrailing();
                mediaUri = mediaUri.appendedWith(fileName.replaceAll("\\..*$", ""))
                                   .appendedWith("" + size)
                                   .appendedWith("image." + extension);
                mediaUri = mediaUri.prependedWith(uriPrefix);
                final var redirect = mediaUri.asString();
                log.info(">>>> permanently redirecting to {}", redirect);
                responseHolder.response().permanentRedirect(redirect).put();
                return BREAK;
              }
            // END TODO

            final var extension = mediaUri.getExtension(); // jpg
//            final String fileName = mediaUri.getTrailing();   // image.jpg
            mediaUri = mediaUri.withoutTrailing();
            final var size = mediaUri.getTrailing();       // 1920
            mediaUri = mediaUri.withoutTrailing();
            final var mediaId = mediaUri.getTrailing();    // 20120802-0010
            mediaUri = mediaUri.withoutTrailing();
            mediaUri = mediaUri.appendedWith(size).appendedWith(mediaId + "." + extension);
          }

        final var media = siteProvider.get().getSite().find(_Media_).withRelativePath(mediaUri).result();
        final var file = media.getFile();
        log.info(">>>> serving contents of {} ...", file.getPath().asString());

        responseHolder.response().fromFile(file)
                                 .withExpirationTime(duration)
                                 .forRequest(request)
                                 .put();
        return BREAK;
      }
  }
