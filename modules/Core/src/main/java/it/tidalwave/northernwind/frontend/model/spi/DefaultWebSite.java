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
package it.tidalwave.northernwind.frontend.model.spi;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Content;
import it.tidalwave.northernwind.frontend.model.Media;
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.northernwind.frontend.model.WebSiteNode;
import it.tidalwave.northernwind.frontend.filesystem.FileSystemProvider;
import it.tidalwave.northernwind.frontend.model.WebSiteFinder;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link WebSiteModel}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultWebSite implements WebSite
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
    
    private final Map<String, WebSiteNode> nodeMapByRelativeUri = new TreeMap<String, WebSiteNode>();
    
    private final Map<Class<?>, Map<String, ?>> mapsByType = new HashMap<Class<?>, Map<String, ?>>(); 
        
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    public void initialize()
      throws IOException, PropertyVetoException
      {
        log.info("initialize()");
        
        mapsByType.put(Content.class, documentMapByRelativeUri);
        mapsByType.put(Media.class, mediaMapByRelativeUri);
        mapsByType.put(WebSiteNode.class, nodeMapByRelativeUri);
        
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
                documentMapByRelativeUri.put(r(relativeUri.substring(documentPath.length() + 1)), new Content(folder));
              }
          });
        
        traverse(mediaFolder, FILE_FILTER, new FileVisitor() 
          {
            @Override
            public void visit (final @Nonnull FileObject file, final @Nonnull String relativeUri) 
              {
                mediaMapByRelativeUri.put(r(relativeUri.substring(mediaPath.length() + 1)), new Media(file));
              }
          });
        
        traverse(nodeFolder, DIRECTORY_FILTER, new FileVisitor() 
          {
            @Override
            public void visit (final @Nonnull FileObject folder, final @Nonnull String relativeUri) 
              {
                nodeMapByRelativeUri.put(r(relativeUri.substring(nodePath.length() + 1)), new WebSiteNode(folder, relativeUri));
              }
          });
        
        log.info(">>>> documents: {}", documentMapByRelativeUri);
        log.info(">>>> media:     {}", mediaMapByRelativeUri);
        log.info(">>>> nodes:     {}", nodeMapByRelativeUri);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <Type> WebSiteFinder<Type> find (final @Nonnull Class<Type> type)
      {
        final Map<String, Type> map = (Map<String, Type>)mapsByType.get(type);
        
        if (map == null)
          {
            throw new IllegalArgumentException("Illegal type: " + type + "; can be: " + mapsByType.keySet());  
          }
        
        return new DefaultWebSiteFinder<Type>(type.getSimpleName(), map);
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
        log.info("traverse({}}", file);
        final String relativeUri = decode(file.getPath());
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
     * Decodes an URL-encoded URI
     * 
     * @param   uri   the URL-encoded URI
     * @return        the plain text URI
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String decode (final @Nonnull String uri)
      throws UnsupportedEncodingException
      {
        final StringBuilder builder = new StringBuilder();
        
        for (final String part : uri.split("/"))
          {
            builder.append("/").append(URLDecoder.decode(part, "UTF-8"));
          }
        
        return builder.toString();
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
