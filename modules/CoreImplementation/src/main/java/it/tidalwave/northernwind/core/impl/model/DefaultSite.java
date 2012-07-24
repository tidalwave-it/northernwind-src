/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
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
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import it.tidalwave.northernwind.core.impl.util.RegexTreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.impl.util.UriUtilities.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link Site}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
/* package */ class DefaultSite implements Site
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
            return file.isFolder() && !ignoredFolders.contains(file.getNameExt());
          }
      };
    
    private final FileFilter ALL_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull ResourceFile file) 
          {
            return !ignoredFolders.contains(file.getNameExt());
          }
      };
    
    @Inject @Nonnull
    private List<LinkPostProcessor> linkPostProcessors;
    
    @Inject @Nonnull
    private RequestHolder requestHolder;
    
    @Inject @Nonnull
    private ModelFactory modelFactory;
    
    @Inject @Named("fileSystemProvider") @Getter @Nonnull
    private FileSystemProvider fileSystemProvider;
    
    @Nonnull
    private final String documentPath;

    @Nonnull
    private final String mediaPath;

    @Nonnull
    private final String libraryPath;

    @Nonnull
    private final String nodePath;
    
    @Getter
    private final boolean logConfigurationEnabled;
    
    @Getter @Nonnull
    private final String contextPath;
    
    private final List<String> ignoredFolders = new ArrayList<String>();
        
    private ResourceFile documentFolder;
    
    private ResourceFile libraryFolder;
    
    private ResourceFile mediaFolder;
    
    private ResourceFile nodeFolder; 
    
    private final Map<String, Content> documentMapByRelativePath = new TreeMap<String, Content>();
    
    private final Map<String, Resource> libraryMapByRelativePath = new TreeMap<String, Resource>();
    
    private final Map<String, Media> mediaMapByRelativePath = new TreeMap<String, Media>();
    
    private final Map<String, SiteNode> nodeMapByRelativePath = new TreeMap<String, SiteNode>();
    
    private final RegexTreeMap<SiteNode> nodeMapByRelativeUri = new RegexTreeMap<SiteNode>();
    
    private final Map<Class<?>, Map<String, ?>> relativePathMapsByType = new HashMap<Class<?>, Map<String, ?>>();
    
    private final Map<Class<?>, RegexTreeMap<?>> relativeUriMapsByType = new HashMap<Class<?>, RegexTreeMap<?>>();
    
    private final List<Locale> configuredLocales = new ArrayList<Locale>();

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public DefaultSite (final @Nonnull String contextPath, 
                        final @Nonnull String documentPath, 
                        final @Nonnull String mediaPath, 
                        final @Nonnull String libraryPath, 
                        final @Nonnull String nodePath, 
                        final boolean logConfigurationEnabled, 
                        final @Nonnull List<Locale> configuredLocales,
                        final @Nonnull List<String> ignoredFolders) 
      {
        this.contextPath = contextPath;
        this.documentPath = documentPath;
        this.mediaPath = mediaPath;
        this.libraryPath = libraryPath;
        this.nodePath = nodePath;
        this.logConfigurationEnabled = logConfigurationEnabled;
        this.configuredLocales.addAll(configuredLocales);
        this.ignoredFolders.addAll(ignoredFolders);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <Type> SiteFinder<Type> find (final @Nonnull Class<Type> type)
      {
        final Map<String, Type> relativePathMap = (Map<String, Type>)relativePathMapsByType.get(type);
        final RegexTreeMap<Type> relativeUriMap = (RegexTreeMap<Type>)relativeUriMapsByType.get(type);
        return new DefaultSiteFinder<Type>(type.getSimpleName(), relativePathMap, relativeUriMap);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String createLink (final @Nonnull String relativeUri) 
      {
        String link = requestHolder.get().getBaseUrl() + contextPath + relativeUri;
        
        // FIXME: this should go to a ListPostProcessor
        if (!relativeUri.contains(".") && !relativeUri.contains("?") && !relativeUri.endsWith("/"))
          {
            link += "/";
          }
        
        for (final LinkPostProcessor linkPostProcessor : linkPostProcessors)
          {
            link = linkPostProcessor.postProcess(link);
          }
        
        return link;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Locale> getConfiguredLocales()
      {
        return new CopyOnWriteArrayList<Locale>(configuredLocales);
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
    public void initialize()
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
                documentMapByRelativePath.put(r(relativePath.substring(documentPath.length() + 1)), modelFactory.createContent(folder));
              }
          });
        
        traverse(libraryFolder, ALL_FILTER, new FileVisitor() 
          {
            @Override
            public void visit (final @Nonnull ResourceFile file, final @Nonnull String relativePath) 
              {
                if (file.isData())
                  {
                    libraryMapByRelativePath.put(r(relativePath.substring(libraryPath.length() + 1)), modelFactory.createResource(file));
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
                    mediaMapByRelativePath.put(r(relativePath.substring(mediaPath.length() + 1)), modelFactory.createMedia(file));
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
                        final String relativeUri = siteNode.getRelativeUri();

                        if ("true".equals(siteNode.getProperties().getProperty(SiteNode.PROPERTY_MANAGES_PATH_PARAMS, "false")))
                          {
                            nodeMapByRelativeUri.putRegex("^" + RegexTreeMap.escape(relativeUri) + "(|/.*$)", siteNode);
                          }
                        else
                          {
                            nodeMapByRelativeUri.put(relativeUri, siteNode);
                          }
                      }
                  }
                catch (IOException e) 
                  {
                    throw new RuntimeException(e);
                  } 
                catch (NotFoundException e) 
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
        log.trace("traverse({}}", file);
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
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    private static void logConfiguration (final @Nonnull String name, Map<String, ?> map)
      {
        log.info(name);
        
        for (final Entry<String, ?> entry : map.entrySet())
          {
            log.info(">>>> {}: {}", entry.getKey(), entry.getValue());  
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ResourceFile findMandatoryFolder (final @Nonnull ResourceFileSystem fileSystem, final @Nonnull String path) 
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
