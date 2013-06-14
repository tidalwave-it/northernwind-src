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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.LinkPostProcessor;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.northernwind.core.impl.util.RegexTreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.impl.util.UriUtilities.*;
import it.tidalwave.northernwind.core.model.ResourcePath;

/***********************************************************************************************************************
 *
 * The default implementation of {@link Site}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
/* package */ class DefaultSite implements InternalSite
  {
    static interface FileVisitor
      {
        public void visit (@Nonnull ResourceFile file, @Nonnull String relativeUri);
      }

    static interface FileFilter
      {
        public boolean accept (@Nonnull ResourceFile file);
      }

    private final FileFilter DIRECTORY_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull ResourceFile file)
          {
            return file.isFolder() && !ignoredFolders.contains(file.getName());
          }
      };

    private final FileFilter ALL_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull ResourceFile file)
          {
            return !ignoredFolders.contains(file.getName());
          }
      };

    @Inject @Nonnull
    private List<LinkPostProcessor> linkPostProcessors;

    @Inject @Nonnull
    private RequestHolder requestHolder;

    @Inject @Nonnull
    private ModelFactory modelFactory;

    @Inject @Named("fileSystemProvider") @Getter @Nonnull
    private ResourceFileSystemProvider fileSystemProvider;

    @Nonnull
    /* package */ final String documentPath;

    @Nonnull
    /* package */ final String mediaPath;

    @Nonnull
    /* package */ final String libraryPath;

    @Nonnull
    /* package */ final String nodePath;

    @Getter
    /* package */ final boolean logConfigurationEnabled;

    @Getter @Nonnull
    /* package */ final String contextPath;

    /* package */ final List<String> ignoredFolders = new ArrayList<>();

    private ResourceFile documentFolder;

    private ResourceFile libraryFolder;

    private ResourceFile mediaFolder;

    @Getter
    private ResourceFile nodeFolder;

    /* package */  final Map<String, Content> documentMapByRelativePath = new TreeMap<>();

    /* package */  final Map<String, Resource> libraryMapByRelativePath = new TreeMap<>();

    /* package */  final Map<String, Media> mediaMapByRelativePath = new TreeMap<>();

    /* package */  final Map<String, SiteNode> nodeMapByRelativePath = new TreeMap<>();

    /* package */  final RegexTreeMap<SiteNode> nodeMapByRelativeUri = new RegexTreeMap<>();

    private final Map<Class<?>, Map<String, ?>> relativePathMapsByType = new HashMap<>();

    private final Map<Class<?>, RegexTreeMap<?>> relativeUriMapsByType = new HashMap<>();

    private final List<Locale> configuredLocales = new ArrayList<>();

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    protected DefaultSite (final @Nonnull Site.Builder siteBuilder)
      {
        this.contextPath = siteBuilder.getContextPath();
        this.documentPath = siteBuilder.getDocumentPath();
        this.mediaPath = siteBuilder.getMediaPath();
        this.libraryPath = siteBuilder.getLibraryPath();
        this.nodePath = siteBuilder.getNodePath();
        this.logConfigurationEnabled = siteBuilder.isLogConfigurationEnabled();
        this.configuredLocales.addAll(siteBuilder.getConfiguredLocales());
        this.ignoredFolders.addAll(siteBuilder.getIgnoredFolders());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull @SuppressWarnings("unchecked")
    public <Type> SiteFinder<Type> find (final @Nonnull Class<Type> type)
      {
        final Map<String, Type> relativePathMap = (Map<String, Type>)relativePathMapsByType.get(type);
        final RegexTreeMap<Type> relativeUriMap = (RegexTreeMap<Type>)relativeUriMapsByType.get(type);
        return new DefaultSiteFinder<>(type.getSimpleName(), relativePathMap, relativeUriMap);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String createLink (final @Nonnull ResourcePath relativeUri)
      {
        final ResourcePath link = new ResourcePath(contextPath).appendedWith(relativeUri);
        String linkAsString = requestHolder.get().getBaseUrl() + link.asString();

        for (final LinkPostProcessor linkPostProcessor : linkPostProcessors)
          {
            linkAsString = linkPostProcessor.postProcess(linkAsString);
          }

        return linkAsString;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Locale> getConfiguredLocales()
      {
        return new CopyOnWriteArrayList<>(configuredLocales);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String toString()
      {
        return String.format("DefaultSite(@%x)", System.identityHashCode(this));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    /* package */ void initialize()
      throws IOException, NotFoundException, PropertyVetoException
      {
        log.info("initialize()");

        relativePathMapsByType.put(Content.class, documentMapByRelativePath);
        relativePathMapsByType.put(Media.class, mediaMapByRelativePath);
        relativePathMapsByType.put(Resource.class, libraryMapByRelativePath);
        relativePathMapsByType.put(SiteNode.class, nodeMapByRelativePath);

        relativeUriMapsByType.put(SiteNode.class, nodeMapByRelativeUri);

        log.info(">>>> fileSystemProvider: {}", fileSystemProvider);
        final ResourceFileSystem fileSystem = fileSystemProvider.getFileSystem();
        documentFolder = findMandatoryFolder(fileSystem, documentPath);
        libraryFolder  = findMandatoryFolder(fileSystem, libraryPath);
        mediaFolder    = findMandatoryFolder(fileSystem, mediaPath);
        nodeFolder     = findMandatoryFolder(fileSystem, nodePath);

        log.info(">>>> contextPath:        {}", contextPath);
        log.info(">>>> ignoredFolders:     {}", ignoredFolders);
        log.info(">>>> fileSystem:         {}", fileSystem);
        log.info(">>>> documentPath:       {}", documentFolder.getPath());
        log.info(">>>> libraryPath:        {}", libraryFolder.getPath());
        log.info(">>>> mediaPath:          {}", mediaFolder.getPath());
        log.info(">>>> nodePath:           {}", nodeFolder.getPath());
        log.info(">>>> locales:            {}", configuredLocales);

        documentMapByRelativePath.clear();
        libraryMapByRelativePath.clear();
        mediaMapByRelativePath.clear();
        nodeMapByRelativePath.clear();
        nodeMapByRelativeUri.clear();

        traverse(documentFolder, DIRECTORY_FILTER, new FileVisitor()
          {
            @Override
            public void visit (final @Nonnull ResourceFile folder, final @Nonnull String relativePath)
              {
                documentMapByRelativePath.put(r(relativePath.substring(documentPath.length() + 1)),
                                              modelFactory.createContent(folder));
              }
          });

        traverse(libraryFolder, ALL_FILTER, new FileVisitor()
          {
            @Override
            public void visit (final @Nonnull ResourceFile file, final @Nonnull String relativePath)
              {
                if (file.isData())
                  {
                    libraryMapByRelativePath.put(r(relativePath.substring(libraryPath.length() + 1)),
                                                 modelFactory.createResource(file));
                  }
              }
          });

        traverse(mediaFolder, ALL_FILTER, new FileVisitor()
          {
            @Override
            public void visit (final @Nonnull ResourceFile file, final @Nonnull String relativePath)
              {
                if (file.isData())
                  {
                    mediaMapByRelativePath.put(r(relativePath.substring(mediaPath.length() + 1)),
                                               modelFactory.createMedia(file));
                  }
              }
          });

        traverse(nodeFolder, DIRECTORY_FILTER, new FileVisitor()
          {
            @Override
            public void visit (final @Nonnull ResourceFile folder, final @Nonnull String relativePath)
              {
                try
                  {
                    final SiteNode siteNode = modelFactory.createSiteNode(DefaultSite.this, folder);
                    nodeMapByRelativePath.put(r(relativePath.substring(nodePath.length() + 1)), siteNode);

                    if (!siteNode.isPlaceHolder())
                      {
                        final ResourcePath relativeUri = siteNode.getRelativeUri();

                        if ("true".equals(siteNode.getProperties().getProperty(SiteNode.PROPERTY_MANAGES_PATH_PARAMS, "false")))
                          {
                            nodeMapByRelativeUri.putRegex("^" + RegexTreeMap.escape(relativeUri.asString()) + "(|/.*$)", siteNode);
                          }
                        else
                          {
                            nodeMapByRelativeUri.put(relativeUri.asString(), siteNode);
                          }
                      }
                  }
                catch (IOException | NotFoundException e)
                  {
                    throw new RuntimeException(e);
                  }
              }
          });

        if (logConfigurationEnabled)
          {
            logConfiguration("Documents by relative path:", documentMapByRelativePath);
            logConfiguration("Library by relative path:", libraryMapByRelativePath);
            logConfiguration("Media by relative path:", mediaMapByRelativePath);
            logConfiguration("Nodes by relative path:", nodeMapByRelativePath);
            logConfiguration("Nodes by relative URI:", nodeMapByRelativeUri);
          }
      }

    /*******************************************************************************************************************
     *
     * Accepts a {@link FileVisitor} to visit a file or folder.
     *
     * @param  file        the file to visit
     * @param  fileFilter  the filter for directory contents
     * @param  visitor     the visitor
     *
     ******************************************************************************************************************/
    private void traverse (final @Nonnull ResourceFile file,
                           final @Nonnull FileFilter fileFilter,
                           final @Nonnull FileVisitor visitor)
      throws UnsupportedEncodingException
      {
        log.trace("traverse({})", file);
        final String relativeUri = urlDecodedPath(file.getPath());
        visitor.visit(file, relativeUri);

        for (final ResourceFile child : file.getChildren())
          {
            if (fileFilter.accept(child))
              {
                traverse(child, fileFilter, visitor);
              }
          }
      }

    /*******************************************************************************************************************
     *
     * Logs the configuration contained in the given map of properties.
     *
     ******************************************************************************************************************/
    private static void logConfiguration (final @Nonnull String name, Map<String, ?> propertyMap)
      {
        log.info(name);

        for (final Entry<String, ?> entry : propertyMap.entrySet())
          {
            log.info(">>>> {}: {}", entry.getKey(), entry.getValue());
          }
      }

    /*******************************************************************************************************************
     *
     * FIXME Wrapper against ResourceFileSystem: its methods should throw NFE by themselves
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ResourceFile findMandatoryFolder (final @Nonnull ResourceFileSystem fileSystem,
                                                     final @Nonnull String path)
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(fileSystem.findFileByPath(path), "Cannot find folder: " + path);
        // don't log fileSystem.getRoot() since if fileSystem is broken it can trigger secondary errors
                            // FileUtil.toFile(fileSystem.getRoot()).getAbsolutePath() + "/"  + path);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String r (final @Nonnull String s)
      {
        return "".equals(s) ? "/" : s;
      }
  }
