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

import it.tidalwave.util.NotFoundException;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
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
import org.openide.util.Exceptions;

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
    
    @Getter @Setter @Nonnull
    private String contextPath = "";
    
    @Getter @Setter @Nonnull
    private FileSystemProvider fileSystemProvider;
    
    @Getter @Setter @Nonnull
    private String documentPath = "content/document";

    @Getter @Setter @Nonnull
    private String mediaPath = "content/media";

    @Getter @Setter @Nonnull
    private String nodePath = "structure";
    
    private FileObject documentFolder;
    
    private FileObject mediaFolder;
    
    private FileObject nodeFolder; 
    
    private final Map<String, Content> documentMapByRelativeUri = new TreeMap<String, Content>();
    
    private final Map<String, Media> mediaMapByRelativeUri = new TreeMap<String, Media>();
    
    private final Map<String, SiteNode> nodeMapByRelativeUri = new TreeMap<String, SiteNode>();
    
    private final Map<Class<?>, Map<String, ?>> mapsByType = new HashMap<Class<?>, Map<String, ?>>(); 
        
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    public void initialize()
      throws IOException, PropertyVetoException
      {
        log.debug("initialize()");
        
        mapsByType.put(Content.class, documentMapByRelativeUri);
        mapsByType.put(Media.class, mediaMapByRelativeUri);
        mapsByType.put(SiteNode.class, nodeMapByRelativeUri);
        
        final FileSystem fileSystem = fileSystemProvider.getFileSystem();
        documentFolder = fileSystem.findResource(documentPath);
        mediaFolder = fileSystem.findResource(mediaPath);
        nodeFolder = fileSystem.findResource(nodePath);
        
        log.info(">>>> contextPath:  {}", contextPath);
        log.info(">>>> fileSystem:   {}", fileSystem);
        log.info(">>>> documentPath: {}", documentFolder.getPath());
        log.info(">>>> mediaPath:    {}", mediaFolder.getPath());
        log.info(">>>> nodePath:     {}", nodeFolder.getPath());
        
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
                    nodeMapByRelativeUri.put(r(relativeUri.substring(nodePath.length() + 1)), new DefaultSiteNode(folder, relativeUri));
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
        
        logConfiguration("Documents:", documentMapByRelativeUri);
        logConfiguration("Media:", mediaMapByRelativeUri);
        logConfiguration("Nodes:", nodeMapByRelativeUri);
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
    private static String r (final @Nonnull String s)
      {
        return "".equals(s) ? "/" : s;  
      }
  }
