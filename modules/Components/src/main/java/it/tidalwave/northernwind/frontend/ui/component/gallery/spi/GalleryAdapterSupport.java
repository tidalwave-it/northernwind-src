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
 * $Id: ef77bb34cb58c4461c5ed17b5318009f57e2df4d $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.GalleryView;
import lombok.Cleanup;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.PROPERTY_TEMPLATE;
import static it.tidalwave.northernwind.core.model.Content.Content;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: ef77bb34cb58c4461c5ed17b5318009f57e2df4d $
 *
 **********************************************************************************************************************/
@Configurable
public abstract class GalleryAdapterSupport implements GalleryAdapter
  {
    @Inject
    protected ModelFactory modelFactory;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getExtraViewProperties (final @Nonnull Id viewId)
      {
        return modelFactory.createProperties().withId(viewId).build();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getInlinedScript()
      {
        return "";
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    protected String loadTemplate (final @Nonnull GalleryAdapterContext context, final @Nonnull String templateName)
      throws IOException
      {
        try
          {
            final SiteNode siteNode = context.getSiteNode();
            final GalleryView view = context.getView();
            final Site site = context.getSite();
            final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
            final String templateRelativePath = viewProperties.getProperty(new Key<String>(templateName + "Path")).orElseThrow(NotFoundException::new); // FIXME
            final Content template = site.find(Content).withRelativePath(templateRelativePath).result();
            return template.getProperty(PROPERTY_TEMPLATE).orElseThrow(NotFoundException::new); // FIXME
          }
        catch (NotFoundException e)
          {
            return loadDefaultTemplate(templateName + ".txt");
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String loadDefaultTemplate (final @Nonnull String templateName)
      throws IOException
      {
        final String packagePath = getClass().getPackage().getName().replace('.', '/');
        final Resource resource = new ClassPathResource("/" + packagePath + "/" + templateName);
        final @Cleanup Reader r = new InputStreamReader(resource.getInputStream());
        final char[] buffer = new char[(int)resource.contentLength()];
        r.read(buffer);
        return new String(buffer);
      }
  }
