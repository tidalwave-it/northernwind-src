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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        
    @PostConstruct
    public void initialize()
      {
        log.info("initialize()");
        final File rootFile = new File(rootPath);
        documentFolder = new File(rootFile, documentPath);
        mediaFolder = new File(rootFile, mediaPath);
        nodeFolder = new File(rootFile, nodePath);
        log.info(">>>> rootPath:     {}", rootFile.getAbsolutePath());
        log.info(">>>> documentPath: {}", documentFolder.getAbsolutePath());
        log.info(">>>> mediaPath:    {}", mediaFolder.getAbsolutePath());
        log.info(">>>> nodePath:     {}", nodeFolder.getAbsolutePath());
      }
    
    @Override @Nonnull
    public Content getContent (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getContent({})", uri);
        return new Content(new File(documentFolder, encode(uri)));
      }
    
    @Override @Nonnull
    public Media getMedia (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getMedia({})", uri);
        return new Media(new File(mediaFolder, encode(uri)));
      }
    
    @Override @Nonnull
    public Node getNode (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getNode({})", uri);
        return new Node(new File(nodeFolder, encode(uri)), uri);
      }
    
    @Nonnull
    private String encode (final @Nonnull String uri)
      throws UnsupportedEncodingException
      {
        final StringBuilder builder = new StringBuilder();
        
        for (final String part : uri.split("/"))
          {
            builder.append("/").append(URLEncoder.encode(part, "UTF-8"));
          }
        
        return builder.toString();
      }
  }