/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import org.openide.util.NbBundle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.util.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString
public class DefaultSiteProvider implements SiteProvider
  {
    private static final String DEFAULT_DOCUMENT_PATH = "/content/document";
    private static final String DEFAULT_MEDIA_PATH = "/content/media";
    private static final String DEFAULT_LIBRARY_PATH = "/content/library";
    private static final String DEFAULT_NODE_PATH = "/structure";
    public static final String DEFAULT_CONTEXT_PATH = "/";

    @Inject @Nonnull
    private Provider<ServletContext> servletContext;

    @Inject @Nonnull
    private ModelFactory modelFactory;

    @Inject @Named("taskExecutor") @Nonnull
    private TaskExecutor executor;

    @Getter @Setter @Nonnull
    private String documentPath = DEFAULT_DOCUMENT_PATH;

    @Getter @Setter @Nonnull
    private String mediaPath = DEFAULT_MEDIA_PATH;

    @Getter @Setter @Nonnull
    private String libraryPath = DEFAULT_LIBRARY_PATH;

    @Getter @Setter @Nonnull
    private String nodePath = DEFAULT_NODE_PATH;

    @Getter @Setter
    private boolean logConfigurationEnabled = false;

    @Getter @Setter @Nonnull
    private String localesAsString;

    @Getter @Setter @Nonnull
    private String ignoredFoldersAsString = "";

    private final List<String> ignoredFolders = new ArrayList<>();

    private final List<Locale> configuredLocales = new ArrayList<>();

    @CheckForNull
    private DefaultSite site;

    @Getter
    private boolean siteAvailable = false;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Site getSite()
      {
        if (site == null) // must never occur, since site is created during initialize()
          {
            throw new IllegalStateException("Initialization internal error - @PostConstruct not called?");
          }

        return site;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void reload()
      {
        log.info("reload()");
        siteAvailable = false;

        site = (DefaultSite)modelFactory.createSite().withContextPath(getContextPath())
                                                     .withDocumentPath(documentPath)
                                                     .withMediaPath(mediaPath)
                                                     .withLibraryPath(libraryPath)
                                                     .withNodePath(nodePath)
                                                     .withLogConfigurationEnabled(logConfigurationEnabled)
                                                     .withConfiguredLocales(configuredLocales)
                                                     .withIgnoredFolders(ignoredFolders)
                                                     .build();
        executor.execute(new Runnable()
          {
            @Override
            public void run()
              {
                try
                  {
                    final long time = System.currentTimeMillis();
                    site.initialize();
                    siteAvailable = true;
                    log.info("****************************************");
                    log.info("SITE INITIALIZATION COMPLETED (in {} msec)", System.currentTimeMillis() - time);
                    log.info("****************************************");
                  }
                catch (IOException | NotFoundException | PropertyVetoException | RuntimeException e)
                  {
                    log.error("****************************************");
                    log.error("SITE INITIALIZATION FAILED!", e);
                    log.error("****************************************");
                  }
              }
          });
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getVersionString()
      {
        return NbBundle.getMessage(DefaultSiteProvider.class, "NorthernWind.version");
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      {
        log.info("initialize()");
        ignoredFolders.addAll(Arrays.asList(ignoredFoldersAsString.trim().split(File.pathSeparator)));

        for (final String localeAsString : localesAsString.split(","))
          {
            configuredLocales.add(new Locale(localeAsString.trim()));
          }

        reload();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    /* package */ String getContextPath()
      {
        try
          {
            return servletContext.get().getContextPath();
          }
        catch (NoSuchBeanDefinitionException e)
          {
            log.warn("Running in a non-web environment, set contextPath = {}", DEFAULT_CONTEXT_PATH);
            return DEFAULT_CONTEXT_PATH;
          }
      }
  }
