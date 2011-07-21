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
import lombok.RequiredArgsConstructor;
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
    private final File root = new File("/workarea/home/fritz/Business/Tidalwave/Projects/WorkAreas/Tidalwave/tidalwave~other/InfoglueExporter/target/export");
    
    @Override @Nonnull
    public Content getContent (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getContent({})", uri);
        return new Content(new File(root, "content/document/" + encode(uri)));
      }
    
    @Override @Nonnull
    public Media getMedia (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getMedia({})", uri);
        return new Media(new File(root, "content/media/" + encode(uri)));
      }
    
    @Override @Nonnull
    public Node getNode (final @Nonnull String uri) 
      throws UnsupportedEncodingException 
      {
        log.info("getNode({})", uri);
        return new Node(this, new File(root, "structure/" + encode(uri)), uri);
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