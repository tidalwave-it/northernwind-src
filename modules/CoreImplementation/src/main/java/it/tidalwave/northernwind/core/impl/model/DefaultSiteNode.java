/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.impl.ui.LayoutLoggerVisitor;
import lombok.Cleanup;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.net.URLDecoder.*;
import static it.tidalwave.role.Unmarshallable.Unmarshallable;
import static it.tidalwave.northernwind.core.model.SiteNode.PROPERTY_EXPOSED_URI;
import it.tidalwave.northernwind.core.model.spi.SiteNodeSupport;

/***********************************************************************************************************************
 *
 * A node of the site, mapped to a given URL.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j @ToString(callSuper = true, of = "relativeUri")
/* package */ class DefaultSiteNode extends SiteNodeSupport
  {
    @Nonnull
    private final Map<Locale, Layout> layoutMapByLocale = new HashMap<>();

    @Nonnull
    /* package */ InternalSite site;

    @Inject @Nonnull
    private InheritanceHelper inheritanceHelper;

    @Inject @Nonnull
    private RequestLocaleManager localeRequestManager;

    @CheckForNull
    private ResourcePath relativeUri;

    /* package */ int uriComputationCounter;

    /*******************************************************************************************************************
     *
     * Creates a new instance with the given configuration file and mapped to the given URI.
     *
     * @param  file          the file with the configuration
     * @param  relativeUri   the bound URI
     *
     ******************************************************************************************************************/
    public DefaultSiteNode (final @Nonnull ModelFactory modelFactory,
                            final @Nonnull InternalSite site,
                            final @Nonnull ResourceFile file)
      throws IOException, NotFoundException
      {
        super(modelFactory, file);
        this.site = site;
        loadLayouts();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull @Override
    public synchronized ResourcePath getRelativeUri()
      {
        if (relativeUri == null) // FIXME: is lazy evaluation really needed?
          {
            uriComputationCounter++;

            relativeUri = new ResourcePath();
            final ResourceFile file = getResource().getFile();

            if (!file.equals(site.getNodeFolder()))
              {
                try
                  {
                    final String segment = getResource().getProperties()
                                                        .getProperty(PROPERTY_EXPOSED_URI, decode(file.getName(), "UTF-8"));
                    relativeUri = relativeUri.appendedWith(getParent().getRelativeUri()).appendedWith(segment);
                  }
                catch (IOException | NotFoundException e)
                  {
                    log.error("", e); // should never occur
                    throw new RuntimeException(e);
                  }
              }
          }

        log.debug(">>>> relativeUri: {}", relativeUri.asString());

        return relativeUri;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout getLayout()
      {
        return layoutMapByLocale.get(localeRequestManager.getLocales().get(0));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<SiteNode> findChildren()
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    private void loadLayouts()
      throws IOException, NotFoundException
      {
        for (final Locale locale : localeRequestManager.getLocales())
          {
            Layout layout = null;
            // Cannot be implemented by recursion, since each SiteNode could have a local override for its Layout -
            // local overrides are not inherited. Perhaps you could do if you keep two layouts per Node, one without the override.
            // On the other hand, inheritanceHelper encapsulates the local ovverride policy, which applies also to Properties...
            final List<ResourceFile> layoutFiles = inheritanceHelper.getInheritedPropertyFiles(getResource().getFile(),
                                                                                               locale,
                                                                                               "Components");
            for (final ResourceFile layoutFile : layoutFiles)
              {
                final DefaultLayout overridingLayout = loadLayout(layoutFile);
                layout = (layout == null) ? overridingLayout : layout.withOverride(overridingLayout);

                if (log.isDebugEnabled())
                  {
                    overridingLayout.accept(new LayoutLoggerVisitor(LayoutLoggerVisitor.Level.DEBUG));
                  }
              }

            layout = (layout != null) ? layout : modelFactory.createLayout()
                                                             .withId(new Id(""))
                                                             .withType("emptyPlaceholder")
                                                             .build();

            if (site.isLogConfigurationEnabled() || log.isDebugEnabled())
              {
                log.debug(">>>> layout for {} ():", getResource().getFile().getPath().asString(), locale);
                layout.accept(new LayoutLoggerVisitor(LayoutLoggerVisitor.Level.INFO));
              }

            layoutMapByLocale.put(locale, layout);
          }
      }

    /*******************************************************************************************************************
     *
     * Returns the parent {@code SiteNode}.
     *
     * @return  the parent node
     * @throws  NotFoundException               if the parent doesn't exist
     * @throws  UnsupportedEncodingException
     *
     ******************************************************************************************************************/
    @Nonnull
    private SiteNode getParent()
      throws NotFoundException
      {
        final ResourcePath parentRelativePath = getResource().getFile().getParent().getPath().urlDecoded()
                                              .relativeTo(site.getNodeFolder().getPath());

        return site.find(SiteNode.class).withRelativePath(parentRelativePath.asString()).result();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private DefaultLayout loadLayout (final @Nonnull ResourceFile layoutFile)
      throws IOException
      {
        log.trace(">>>> reading layout from {}...", layoutFile.getPath().asString());
        final @Cleanup InputStream is = layoutFile.getInputStream();
        return modelFactory.createLayout().build().as(Unmarshallable).unmarshal(is);
      }
  }
