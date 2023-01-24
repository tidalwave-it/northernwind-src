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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.spi.SiteNodeSupport;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.impl.ui.LayoutLoggerVisitor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.io.Unmarshallable._Unmarshallable_;
import static it.tidalwave.northernwind.util.UrlEncoding.*;

/***********************************************************************************************************************
 *
 * A node of the site, mapped to a given URL.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j @ToString(callSuper = false, of = "relativeUri")
/* package */ class DefaultSiteNode extends SiteNodeSupport
  {
    @Nonnull
    private final Map<Locale, Layout> layoutMapByLocale = new HashMap<>();

    @Getter @Nonnull
    final /* package */ InternalSite site;

    @Inject
    private InheritanceHelper inheritanceHelper;

    @Inject
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
    public DefaultSiteNode (@Nonnull final ModelFactory modelFactory,
                            @Nonnull final InternalSite site,
                            @Nonnull final ResourceFile file)
      {
        super(modelFactory, file);
        this.site = site;
        try
          {
            loadLayouts();
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
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

            relativeUri = ResourcePath.EMPTY;
            final var file = getResource().getFile();

            if (!file.equals(site.getNodeFolder()))
              {
                try
                  {
                    final var segment = getResource().getProperty(P_EXPOSED_URI).orElse(decodedUtf8(file.getName()));
                    relativeUri = relativeUri.appendedWith(getParent().getRelativeUri()).appendedWith(segment);
                  }
                catch (NotFoundException e) // FIXME: for getParent()
                  {
                    log.error("", e); // should never occur
                    throw new RuntimeException(e);
                  }
              }
          }

        log.trace(">>>> relativeUri: {}", relativeUri);

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
    private void loadLayouts()
      throws IOException
      {
        for (final var locale : localeRequestManager.getLocales())
          {
            Layout layout = null;
            // Cannot be implemented by recursion, since each SiteNode could have a local override for its Layout -
            // local overrides are not inherited. Perhaps you could do if you keep two layouts per Node, one without the override.
            // On the other hand, inheritanceHelper encapsulates the local override policy, which applies also to Properties...
            final var layoutFiles = inheritanceHelper.getInheritedPropertyFiles(getResource().getFile(),
                                                                                locale,
                                                                                "Components");
            for (final var layoutFile : layoutFiles)
              {
                final var overridingLayout = loadLayout(layoutFile);
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
                log.debug(">>>> layout for {} {}:", getResource().getFile().getPath().asString(), locale);
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
     *
     ******************************************************************************************************************/
    @Nonnull
    private SiteNode getParent()
      throws NotFoundException
      {
        final var parentRelativePath = getResource().getFile().getParent().getPath().urlDecoded()
                                                    .relativeTo(site.getNodeFolder().getPath());

        return site.find(_SiteNode_).withRelativePath(parentRelativePath).result();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private DefaultLayout loadLayout (@Nonnull final ResourceFile layoutFile)
      throws IOException
      {
        log.trace(">>>> reading layout from {}...", layoutFile.getPath().asString());
        @Cleanup final var is = layoutFile.getInputStream();
        return modelFactory.createLayout().build().as(_Unmarshallable_).unmarshal(is);
      }
  }
