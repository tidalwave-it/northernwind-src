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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * This class tries to resolve a property by loading it from a text file. The file is searched in the given folder
 * with the same name of the property, localized.If the searched property is {@code fullText}, and the managed locales
 * are {@code en} and {@code it}, then the following files are searched for:
 *
 * <ul>
 * <li>{@code fullText.xhtml}</li>
 * <li>{@code fullText.html}</li>
 * <li>{@code fullText.xml}</li>
 * <li>{@code fullText.txt}</li>
 * <li>{@code fullText_en.xhtml}</li>
 * <li>{@code fullText_en.html}</li>
 * <li>{@code fullText_en.xml}</li>
 * <li>{@code fullText_en.txt}</li>
 * <li>{@code fullText_it.xhtml}</li>
 * <li>{@code fullText_it.html}</li>
 * <li>{@code fullText_it.xml}</li>
 * <li>{@code fullText_it.txt}</li>
 * </ul>
 *
 * Files are pre-processed through the {@link FilterSetExpander}. If the file MIME type is XHTML, UTF-8 is used for the
 * encoding, otherwise the default charset is used.
 *
 * HTML support is a legacy and will be removed in a future version.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class TextResourcePropertyResolver implements ResourceProperties.PropertyResolver
  {
    private static final List<String> EXTENSIONS = List.of(".xhtml", ".html", ".xml", ".txt");

    @Inject
    private RequestLocaleManager localeRequestManager;

    @Inject
    private Provider<FilterSetExpander> filterSetExpander;

    @Nonnull
    private final ResourceFile folder;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public TextResourcePropertyResolver (@Nonnull final ResourceFile folder)
      {
        this.folder = folder;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    @Override @SuppressWarnings("unchecked")
    public <Type> Type resolveProperty (@Nonnull final Id propertyGroupId, @Nonnull final Key<Type> propertyName)
      throws NotFoundException, IOException
      {
        log.trace("resolveProperty({})", propertyName);

        final var propertyFile = findLocalizedFile(propertyName.stringValue());
        log.trace(">>>> reading from {}", propertyFile.getPath());
        final var mimeType = propertyFile.getMimeType();
        final var charset = "application/xhtml+xml".equals(mimeType) ? "UTF-8" : Charset.defaultCharset().name();

        try
          {
            return (Type)filterSetExpander.get().filter(propertyFile.asText(charset), mimeType);
          }
        catch (RuntimeException e) // FIXME: introduce a FilterException
          {
            throw new IOException(e);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResourceFile findLocalizedFile (@Nonnull final String fileName)
      throws NotFoundException
      {
        log.trace("findLocalizedFile({})", fileName);
        final var fileNamesNotFound = new StringBuilder();
        var separator = "";

        for (final var localeSuffix : localeRequestManager.getLocaleSuffixes())
          {
            for (final var extension : EXTENSIONS)
              {
                final var localizedFileName = fileName + localeSuffix + extension;

                try
                  {
                    return folder.findChildren().withName(localizedFileName).result();
                  }
                catch (NotFoundException e)
                  {
                    // continue
                  }

                fileNamesNotFound.append(separator);
                fileNamesNotFound.append(localizedFileName);
                separator = ",";
              }
          }

        throw new NotFoundException(String.format("%s/{%s}", folder.getPath().asString(), fileNamesNotFound));
      }
  }
