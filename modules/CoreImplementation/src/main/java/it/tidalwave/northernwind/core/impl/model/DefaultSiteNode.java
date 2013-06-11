/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLDecoder;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.impl.util.UriUtilities;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.impl.ui.LayoutLoggerVisitor;
import lombok.Cleanup;
import lombok.Delegate;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.Unmarshallable.Unmarshallable;

/***********************************************************************************************************************
 *
 * A node of the site, mapped to a given URL.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j @ToString(exclude = { "layout", "site", "modelFactory", "relativeUri" })
/* package */ class DefaultSiteNode implements SiteNode
  {
    @Nonnull @Delegate(types = Resource.class)
    private final Resource resource;

    @Nonnull @Getter
    private final Layout layout;

    @Nonnull
    private DefaultSite site;

    @Inject @Nonnull
    private ModelFactory modelFactory;

    @CheckForNull
    private String relativeUri;

    /*******************************************************************************************************************
     *
     * Creates a new instance with the given configuration file and mapped to the given URI.
     *
     * @param  file          the file with the configuration
     * @param  relativeUri   the bound URI
     *
     ******************************************************************************************************************/
    public DefaultSiteNode (final @Nonnull DefaultSite site, final @Nonnull ResourceFile file)
      throws IOException, NotFoundException
      {
        this.site = site;
        resource = modelFactory.createResource(file);
        layout = loadLayout();

        if (site.isLogConfigurationEnabled() || log.isDebugEnabled())
          {
            log.info(">>>> layout for /{}:", resource.getFile().getPath());
            layout.accept(new LayoutLoggerVisitor(LayoutLoggerVisitor.Level.INFO));
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull @Override
    public synchronized String getRelativeUri()
      {
        if (relativeUri == null)
          {
            try
              {
                // FIXME: this works, but it's messy code!!!
                final ResourceFile file = resource.getFile();
                final ResourceFile parentFile = file.getParent();
                log.debug("Compute relativeUri for {}: parentFile: {}", file, parentFile);
                String parentRelativePath = UriUtilities.urlDecodedPath(parentFile.getPath());

                if (!parentRelativePath.startsWith("/"))
                  {
                    parentRelativePath = "/" + parentRelativePath;
                  }

                log.debug(">>>> parent path '{}'", parentRelativePath);

                if ("structure".equals(file.getPath()))
                  {
                    relativeUri = "/";
                  }
                else
                  {
                    parentRelativePath = parentRelativePath.replaceAll("^/structure", "");

                    if (parentRelativePath.equals(""))
                      {
                        parentRelativePath = "/";
                      }

                    final SiteNode parentSiteNode = site.find(SiteNode.class)
                                                        .withRelativePath(parentRelativePath)
                                                        .result();
                    log.debug(">>>> found {}", parentSiteNode);
                    String p = parentSiteNode.getRelativeUri();

                    if (!p.endsWith("/"))
                      {
                        p += "/";
                      }

                    final String localRelativePathPortion = URLDecoder.decode(file.getName(), "UTF-8");
                    relativeUri = p + resource.getProperties()
                                              .getProperty(PROPERTY_EXPOSED_URI, localRelativePathPortion);
                  }
                // END FIXME
              }
            catch (IOException e)
              {
                log.error("", e); // should never occur
                throw new RuntimeException(e);
              }
            catch (NotFoundException e)
              {
                log.error("", e); // should never occur
                throw new RuntimeException(e);
              }
          }

        log.debug(">>>> relativeUri: {}", relativeUri);

        return relativeUri;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Layout loadLayout()
      throws IOException, NotFoundException
      {
        Layout layout = null;
        // FIXME: Components must be localized
        final List<ResourceFile> files = Utilities.getInheritedPropertyFiles(resource.getFile(), "Components_en.xml");

        for (final ResourceFile layoutFile : files)
          {
            log.trace(">>>> reading layout from /{}...", layoutFile.getPath());
            final @Cleanup InputStream is = layoutFile.getInputStream();
            final DefaultLayout overridingLayout = new DefaultLayout().as(Unmarshallable).unmarshal(is);
            is.close();
            layout = (layout == null) ? overridingLayout : layout.withOverride(overridingLayout);

            if (log.isDebugEnabled())
              {
                overridingLayout.accept(new LayoutLoggerVisitor(LayoutLoggerVisitor.Level.DEBUG));
              }
          }

        return (layout != null) ? layout : modelFactory.createLayout(new Id(""), "emptyPlaceholder");
      }
  }
