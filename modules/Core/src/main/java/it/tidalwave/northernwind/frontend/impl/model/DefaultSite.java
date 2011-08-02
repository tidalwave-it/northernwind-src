/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
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
import javax.servlet.ServletContext;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Content;
import it.tidalwave.northernwind.frontend.model.Media;
import it.tidalwave.northernwind.frontend.model.Site;
import it.tidalwave.northernwind.frontend.model.SiteFinder;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import it.tidalwave.northernwind.frontend.filesystem.FileSystemProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.impl.util.UriUtilities.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link Site}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
/* package */ class DefaultSite implements Site
  {
    static interface FileVisitor 
      {
        public void visit (@Nonnull FileObject file, @Nonnull String relativeUri);   
      }
    
    static interface FileFilter
      {
        public boolean accept (@Nonnull FileObject file);  
      }
    
    private static final FileFilter DIRECTORY_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull FileObject file) 
          {
            return file.isFolder();
          }
      };
    
    private static final FileFilter FILE_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull FileObject file) 
          {
            return file.isData();
          }
      };
    
    @Inject
    private ApplicationContext applicationContext;
    
    @Inject @Named("fileSystemProvider") @Getter @Nonnull
    private FileSystemProvider fileSystemProvider;
    
    @Getter @Setter @Nonnull
    private String documentPath = "content/document";

    @Getter @Setter @Nonnull
    private String mediaPath = "content/media";

    @Getter @Setter @Nonnull
    private String nodePath = "structure";
    
    @Getter @Setter
    private boolean logConfigurationEnabled = false;
    
    @Getter @Nonnull
    private String contextPath = "NOT SET YET";
    
    @Getter @Setter @Nonnull
    private String localesAsString;
            
    private FileObject documentFolder;
    
    private FileObject mediaFolder;
    
    private FileObject nodeFolder; 
    
    private final Map<String, Content> documentMapByRelativeUri = new TreeMap<String, Content>();
    
    private final Map<String, Media> mediaMapByRelativeUri = new TreeMap<String, Media>();
    
    private final Map<String, SiteNode> nodeMapByRelativeUri = new TreeMap<String, SiteNode>();
    
    private final Map<Class<?>, Map<String, ?>> mapsByType = new HashMap<Class<?>, Map<String, ?>>();
    
    private final List<Locale> configuredLocales = new ArrayList<Locale>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void reset()
      throws IOException, NotFoundException
      {
        log.info("reset()");
        
        documentMapByRelativeUri.clear();
        mediaMapByRelativeUri.clear();
        nodeMapByRelativeUri.clear();
        
        traverse(documentFolder, DIRECTORY_FILTER, new FileVisitor() 
          {
            @Override
            public void visit (final @Nonnull FileObject folder, final @Nonnull String relativeUri) 
              {
                documentMapByRelativeUri.put(r(relativeUri.substring(documentPath.length() + 1)), new DefaultContent(folder));
              }
          });
        
        traverse(mediaFolder, FILE_FILTER, new FileVisitor() 
          {
            @Override
            public void visit (final @Nonnull FileObject file, final @Nonnull String relativeUri) 
              {
                mediaMapByRelativeUri.put(r(relativeUri.substring(mediaPath.length() + 1)), new DefaultMedia(file));
              }
          });
        
        traverse(nodeFolder, DIRECTORY_FILTER, new FileVisitor() 
          {
            @Override
            public void visit (final @Nonnull FileObject folder, final @Nonnull String relativeUri) 
              {
                try 
                  {
                    nodeMapByRelativeUri.put(r(relativeUri.substring(nodePath.length() + 1)), new DefaultSiteNode(folder));
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
            logConfiguration("Documents:", documentMapByRelativeUri);
            logConfiguration("Media:", mediaMapByRelativeUri);
            logConfiguration("Nodes:", nodeMapByRelativeUri);
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <Type> SiteFinder<Type> find (final @Nonnull Class<Type> type)
      {
        final Map<String, Type> map = (Map<String, Type>)mapsByType.get(type);
        
        if (map == null)
          {
            throw new IllegalArgumentException("Illegal type: " + type + "; can be: " + mapsByType.keySet());  
          }
        
        return new DefaultSiteFinder<Type>(type.getSimpleName(), map);
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
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      throws IOException, NotFoundException, PropertyVetoException
      {
        log.info("initialize()");
        
        mapsByType.put(Content.class, documentMapByRelativeUri);
        mapsByType.put(Media.class, mediaMapByRelativeUri);
        mapsByType.put(SiteNode.class, nodeMapByRelativeUri);
                
        try
          {
            contextPath = applicationContext.getBean(ServletContext.class).getContextPath();
          }
        catch (NoSuchBeanDefinitionException e)
          {
            contextPath = "/";
            log.warn("Running in a non-web environment, set contextPath = {}", contextPath);
          }  
        
        final FileSystem fileSystem = fileSystemProvider.getFileSystem();
        documentFolder = findMandatoryFolder(fileSystem, documentPath);
        mediaFolder = findMandatoryFolder(fileSystem, mediaPath);
        nodeFolder = findMandatoryFolder(fileSystem, nodePath);
        
        for (final String localeAsString : localesAsString.split(","))
          {
            configuredLocales.add(new Locale(localeAsString.trim()));  
          }
        
        log.info(">>>> contextPath:  {}", contextPath);
        log.info(">>>> fileSystem:   {}", fileSystem);
        log.info(">>>> documentPath: {}", documentFolder.getPath());
        log.info(">>>> mediaPath:    {}", mediaFolder.getPath());
        log.info(">>>> nodePath:     {}", nodeFolder.getPath());
        log.info(">>>> locales:      {}", configuredLocales);
        
        reset();
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
    private void traverse (final @Nonnull FileObject file, 
                           final @Nonnull FileFilter fileFilter, 
                           final @Nonnull FileVisitor visitor)
      throws UnsupportedEncodingException
      {
        log.trace("traverse({}}", file);
        final String relativeUri = urlDecodedPath(file.getPath());
        visitor.visit(file, relativeUri);

        for (final FileObject child : file.getChildren())
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
    private static FileObject findMandatoryFolder (final @Nonnull FileSystem fileSystem, final @Nonnull String path) 
      throws NotFoundException
      {
        return NotFoundException.throwWhenNull(fileSystem.findResource(path), "Cannot find folder: " + 
                            FileUtil.toFile(fileSystem.getRoot()).getAbsolutePath() + "/" + path);  
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