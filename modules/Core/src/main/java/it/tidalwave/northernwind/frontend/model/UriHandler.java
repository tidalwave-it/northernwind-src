/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.frontend.model;

import it.tidalwave.northernwind.frontend.ui.PageViewController;
import it.tidalwave.util.NotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author fritz
 */
@Configurable(preConstruction=true) @Slf4j
public class UriHandler 
  {
    public static class DoNothingException extends Exception
      {
      }
                
    @Inject @Nonnull
    private WebSite webSite;
    
    @Inject @Nonnull
    private PageViewController pageViewController;
    
    @Nonnull
    public Resource handleUri (final @Nonnull URL context, final @Nonnull String relativeUri) 
      throws NotFoundException, IOException, DoNothingException
      {
        log.info("handleUri({})", relativeUri);

        // FIXME: pass thru JavaScript calls

        // FIXME: move to a filter
        if (relativeUri.startsWith("media"))
          {
            return webSite.findMediaByUri(relativeUri.replaceAll("^media", "")).getResource();      
          }

        // FIXME: move this to a filter too
        pageViewController.setContentsByUri("/" + relativeUri);   
        
        throw new DoNothingException(); 
      }
  } 
