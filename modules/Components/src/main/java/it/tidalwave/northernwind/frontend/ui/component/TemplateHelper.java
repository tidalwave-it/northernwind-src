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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor
public class TemplateHelper
  {
    @Nonnull
    private final Object owner;

    @Inject
    private Provider<SiteProvider> siteProvider;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getTemplate1 (final @Nonnull ResourceProperties viewProperties)
      {
        try
          {
            final Site site = siteProvider.get().getSite();
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_WRAPPER_TEMPLATE_RESOURCE).orElseThrow(NotFoundException::new); // FIXME
            final Content content = site.find(Content).withRelativePath(templateRelativePath).result();
            final ResourceProperties templateProperties = content.getProperties();
            return templateProperties.getProperty(PROPERTY_TEMPLATE).orElse("$content$");
          }
        catch (NotFoundException e)
          {
            return "$content$";
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<String> getTemplate2 (final @Nonnull ResourceProperties viewProperties)
      {
        try
          {
            final Site site = siteProvider.get().getSite();
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_PATH).orElseThrow(NotFoundException::new); // FIXME
            final Content template = site.find(Content).withRelativePath(templateRelativePath).result();
            return template.getProperty(PROPERTY_TEMPLATE);
          }
        catch (NotFoundException e)
          {
            // ok, use the default template
            return Optional.empty();
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<String> getTemplate4 (final @Nonnull ResourceProperties viewProperties)
      {
        // First search the template in a path, which could be useful for retrieving from a library; if not
        // found, a property with the contents is searched.
        try
          {
            final Site site = siteProvider.get().getSite();
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_PATH).orElseThrow(NotFoundException::new); // FIXME
            final Content template = site.find(Content).withRelativePath(templateRelativePath).result();
            return template.getProperty(PROPERTY_TEMPLATE);
          }
        catch (NotFoundException e)
          {
            // ok, use the default template
            return Optional.empty();
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<String> getTemplate5 (final @Nonnull ResourceProperties viewProperties)
      {
        try
          {
            final Site site = siteProvider.get().getSite();
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_PATH).orElseThrow(NotFoundException::new); // FIXME
            final Content template = site.find(Content).withRelativePath(templateRelativePath).result();
            return template.getProperty(PROPERTY_TEMPLATE);
          }
        catch (NotFoundException e)
          {
//            log.warn("Cannot find custom template, using default ({})", e.toString());
            return Optional.empty();
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getTemplate3 (final @Nonnull ResourceProperties viewProperties,
                                final @Nonnull Key<String> templateName)
      throws IOException
      {
        try
          {
            final Site site = siteProvider.get().getSite();
            final String templateRelativePath = viewProperties.getProperty(templateName).orElseThrow(NotFoundException::new); // FIXME
            final Content template = site.find(Content).withRelativePath(templateRelativePath).result();
            return template.getProperty(PROPERTY_TEMPLATE).orElseThrow(NotFoundException::new); // FIXME
          }
        catch (NotFoundException e)
          {
            return loadDefaultTemplate(templateName.stringValue().replaceAll("Path$", "") + ".txt");
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public String loadDefaultTemplate (final @Nonnull String templateName)
      throws IOException
      {
        final String packagePath = owner.getClass().getPackage().getName().replace('.', '/');
        final Resource resource = new ClassPathResource("/" + packagePath + "/" + templateName);
        final @Cleanup Reader r = new InputStreamReader(resource.getInputStream());
        final char[] buffer = new char[(int)resource.contentLength()];
        r.read(buffer);
        return new String(buffer);
      }
  }
