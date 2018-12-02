/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import it.tidalwave.northernwind.core.model.Site;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A facility class to retrieve templates.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class TemplateHelper
  {
    @Nonnull
    private final Object owner;

    @Nonnull
    private final Supplier<Site> site;

    /*******************************************************************************************************************
     *
     * Gets a template from a {@link Content}, whose relative path is provided. The template is retrieve through the
     * {@code PROPERTY_TEMPLATE} of the {@code Content}.
     *
     * @param       contentRelativePath     the path of the {@code Content}
     * @return                              the template contents
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<String> getTemplate (final @Nonnull String contentRelativePath)
      {
        return site.get().find(Content).withRelativePath(contentRelativePath)
                                       .optionalResult()
                                       .flatMap(content -> content.getProperty(PROPERTY_TEMPLATE));
      }

    /*******************************************************************************************************************
     *
     * Gets an embedded, default template with the given name. The template file should be in the classpath, in the same
     * package as the owner class.
     *
     * @param       fileName            the name of the file
     * @return                          the template contents
     * @throws      RuntimeException    if the template can't be loaded
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getEmbeddedTemplate (final @Nonnull String fileName)
      {
        final String packagePath = owner.getClass().getPackage().getName().replace('.', '/');
        final Resource resource = new ClassPathResource("/" + packagePath + "/" + fileName);

        try (final Reader r = new InputStreamReader(resource.getInputStream()))
          {
            final char[] buffer = new char[(int)resource.contentLength()];
            r.read(buffer);
            return new String(buffer);
          }
        catch (IOException e)
          {
            throw new RuntimeException("Missing resource: " + fileName, e);
          }
      }
  }
