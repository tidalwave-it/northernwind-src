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

import com.vaadin.terminal.DownloadStream;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.WebSite;
import it.tidalwave.northernwind.frontend.ui.PageView;
import it.tidalwave.northernwind.frontend.ui.component.menu.vaadin.VaadinHorizontalMenuView;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.terminal.URIHandler;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.UriHandler;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.openide.filesystems.FileObject;

/***********************************************************************************************************************
 *
 * The Vaadin implementation of {@link PageView}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public class VaadinPageView extends Window implements PageView
  {
    @Nonnull @Inject
    private WebSite webSite;
    
    @Nonnull @Inject
    private VaadinPageViewController controller;
    
    @Nonnull @Inject
    private UriHandler uuu;
    
    /*******************************************************************************************************************
     *
     * Tracks the incoming URI.
     *
     ******************************************************************************************************************/
    private final URIHandler uriHandler = new URIHandler()
      {
        @Override
        public DownloadStream handleURI (final @Nonnull URL context,
                                         final @Nonnull String relativeUri) 
          {
            try 
              {
                final Resource resource = uuu.handleUri(context, relativeUri);
                final FileObject file = resource.getFile();
                log.info(">>>> serving contents of {} ...", file.getPath());
                return new DownloadStream(file.getInputStream(), null, null);
//                return new DownloadStream(file.getInputStream(), file.getNameExt(), file.getMIMEType());
                // TODO: I suppose DownloadStream closes the stream
              }
            catch (NotFoundException e) 
              {
                log.error("", e);
              }
            catch (IOException e) 
              {
                log.error("", e);
              }
            catch (UriHandler.DoNothingException e) 
              {
                // ok
              }
            
            return null;
          }
      };
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public VaadinPageView() 
      {
        log.info("VaadinPageView()");
        setStyleName("component-" + "page");
        ((AbstractLayout)getContent()).setMargin(false);
        addURIHandler(uriHandler);
        log.info(">>>> registered URI handler");
      }

    /*******************************************************************************************************************
     *
     * Set the contents.
     * 
     * @param  content  the new contents (must be a Vaadin {@link Component})
     *
     ******************************************************************************************************************/
    @Override
    public void setContents (final @Nonnull Object content) 
      throws IOException
      {
        log.info("setContents({} - {})", content.getClass(), content);
        removeAllComponents();
        final VaadinHorizontalMenuView menuView = new VaadinHorizontalMenuView("nav");
        
        try { // FIXME
        menuView.setLinks(Arrays.asList
          (
            "/",
            "/Features",
            "/Download",
            "/Screenshots",
            "/Getting started",
            "/Blog & News (new)",
//            "/Contact",
            "/License",
            "/Developers"
          )); }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
//        try // FIXME
//          {
//            final Media media = webSite.findMediaByUri("/blueBill_Mobile-Banner.png");
//            addComponent(new Embedded("", new FileResource(FileUtil.toFile(media.getResource().getFile()), getApplication()))); 
//          }
//        catch (NotFoundException e) 
//          {
//            log.error("", e);
//          }
        
        addComponent(menuView);
        addComponent((Component)content);
      }
  }
