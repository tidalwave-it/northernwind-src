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
package it.tidalwave.northernwind.frontend.ui.vaadin;

import it.tidalwave.northernwind.frontend.model.Content;
import it.tidalwave.northernwind.frontend.model.Media;
import it.tidalwave.northernwind.frontend.model.Node;
import it.tidalwave.northernwind.frontend.model.WebSiteModel;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultWebSiteModel implements WebSiteModel 
  {
    private static final FileFilter DIRECTORY_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull File file) 
          {
            return file.isDirectory();
          }
      };
    
    private static final FileFilter FILE_FILTER = new FileFilter()
      {
        @Override
        public boolean accept (final @Nonnull File file) 
          {
            return file.isFile();
          }
      };
    
    static interface FolderVisitor
      {
        public void visit (@Nonnull File folder, @Nonnull String relativeUri);   
      }
    
    @Getter @Setter @Nonnull
    private String contextPath = "";
    
    @Getter @Setter @Nonnull
    private String rootPath = "";
    
    @Getter @Setter @Nonnull
    private String documentPath = "content/document";

    @Getter @Setter @Nonnull
    private String mediaPath = "content/media";

    @Getter @Setter @Nonnull
    private String nodePath = "structure";
    
    private File documentFolder;
    
    private File mediaFolder;
    
    private File nodeFolder; 
    
    private final Map<String, Content> documentMapByRelativeUri = new TreeMap<String, Content>();
    
    private final Map<String, Media> mediaMapByRelativeUri = new TreeMap<String, Media>();
    
    private final Map<String, Node> nodeMapByRelativeUri = new TreeMap<String, Node>();
        
    @PostConstruct
    public void initialize()
      throws UnsupportedEncodingException
      {
        log.info("initialize()");
        final File rootFile = new File(rootPath);
        documentFolder = new File(rootFile, documentPath);
        mediaFolder = new File(rootFile, mediaPath);
        nodeFolder = new File(rootFile, nodePath);
        log.info(">>>> contextPath:  {}", contextPath);
        log.info(">>>> rootPath:     {}", rootFile.getAbsolutePath());
        log.info(">>>> documentPath: {}", documentFolder.getAbsolutePath());
        log.info(">>>> mediaPath:    {}", mediaFolder.getAbsolutePath());
        log.info(">>>> nodePath:     {}", nodeFolder.getAbsolutePath());
        
        accept(documentFolder, DIRECTORY_FILTER, new FolderVisitor() 
          {
            @Override
            public void visit (final @Nonnull File folder, final @Nonnull String relativeUri) 
              {
                documentMapByRelativeUri.put(r(relativeUri.substring(documentPath.length() + 2)), new Content(folder));
              }
          });
        
        accept(mediaFolder, FILE_FILTER, new FolderVisitor() 
          {
            @Override
            public void visit (final @Nonnull File folder, final @Nonnull String relativeUri) 
              {
                mediaMapByRelativeUri.put(r(relativeUri.substring(mediaPath.length() + 2)), new Media(folder));
              }
          });
        
        accept(nodeFolder, DIRECTORY_FILTER, new FolderVisitor() 
          {
            @Override
            public void visit (final @Nonnull File folder, final @Nonnull String relativeUri) 
              {
                nodeMapByRelativeUri.put(r(relativeUri.substring(nodePath.length() + 2)), new Node(folder, relativeUri));
              }
          });
        
        log.info(">>>> documents: {}", documentMapByRelativeUri);
        log.info(">>>> media:     {}", mediaMapByRelativeUri);
        log.info(">>>> nodes:     {}", nodeMapByRelativeUri);
      }
    
    @Override @Nonnull
    public Content getContent (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getContent({})", uri);
        return documentMapByRelativeUri.get(uri);
      }
    
    @Override @Nonnull
    public Media getMedia (final @Nonnull String uri) 
      {
        log.info("getMedia({})", uri);
        return mediaMapByRelativeUri.get(uri);
      }
    
    @Override @Nonnull
    public Node getNode (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getNode({})", uri);
        return nodeMapByRelativeUri.get(uri);
      }
    
    @Nonnull
    private String decode (final @Nonnull String uri)
      throws UnsupportedEncodingException
      {
        final StringBuilder builder = new StringBuilder();
        
        for (final String part : uri.split("/"))
          {
            builder.append("/").append(URLDecoder.decode(part, "UTF-8"));
          }
        
        return builder.toString();
      }
    
    private void accept (final @Nonnull File folder, final @Nonnull FileFilter fileFilter,  final @Nonnull FolderVisitor visitor)
      throws UnsupportedEncodingException
      {
        log.info("accept({}}", folder, fileFilter);
        final String relativeUri = decode(folder.getAbsolutePath().substring(rootPath.length()));
        visitor.visit(folder, relativeUri);
        final File[] subFolders = folder.listFiles(fileFilter);
        
        if (subFolders != null)
          {
            for (final File subFolder : subFolders)
              {
                accept(subFolder, fileFilter, visitor);                    
              } 
          }
      }  
    
    @Nonnull
    private static String r (final @Nonnull String s)
      {
        return "".equals(s) ? "/" : s;  
      }
  }
