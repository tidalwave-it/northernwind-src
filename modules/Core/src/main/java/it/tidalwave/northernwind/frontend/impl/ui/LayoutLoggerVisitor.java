/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.northernwind.frontend.ui.Layout;
import org.slf4j.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author fritz
 */
@NotThreadSafe @RequiredArgsConstructor @Slf4j
public class LayoutLoggerVisitor implements Visitor<Layout, Void>
  {
    public static enum Level
      {
        DEBUG
          {
            @Override
            protected void log (final @Nonnull Logger log,
                                final @Nonnull String template, 
                                final @Nonnull Object arg1, 
                                final @Nonnull Object arg2)
              {
                log.debug(template, arg1, arg2);                    
              }
          },
        INFO
          {
            @Override
            protected void log (final @Nonnull Logger log,
                                final @Nonnull String template, 
                                final @Nonnull Object arg1, 
                                final @Nonnull Object arg2)
              {
                log.info(template, arg1, arg2);                    
              }
          };
        
        protected abstract void log (@Nonnull Logger log,
                                     @Nonnull String template, 
                                     @Nonnull Object arg1, 
                                     @Nonnull Object arg2);
      }
    
    private static final String SPACES = "                                                               ";
    
    private int indent = 0;
    
    @Nonnull
    private final Level logLevel;
    
    @Override
    public void preVisit (final @Nonnull Layout layout) 
      {
        logLevel.log(log, "{}{}", SPACES.substring(0, indent++ * 2), layout);
      }

    @Override
    public void visit (final @Nonnull Layout layout) 
      {
      }  

    @Override
    public void postVisit (final @Nonnull Layout layout) 
      {
        indent--;
      }

    @Override
    public Void getValue() 
      throws NotFoundException 
      {
        return null;
      }
  }
