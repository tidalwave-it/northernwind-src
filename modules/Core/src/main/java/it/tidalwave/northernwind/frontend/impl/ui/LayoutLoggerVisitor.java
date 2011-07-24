/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.frontend.impl.ui;

import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.role.Composite.Visitor;
import it.tidalwave.util.NotFoundException;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author fritz
 */
@NotThreadSafe @Slf4j
public class LayoutLoggerVisitor implements Visitor<Layout, Void>
  {
    private static final String SPACES = "                                                               ";
    
    private int level = 0;
    
    @Override
    public void preVisit (final @Nonnull Layout layout) 
      {
        log.info("{}{}", SPACES.substring(0, level++ * 2), layout);
      }

    @Override
    public void visit (final @Nonnull Layout layout) 
      {
      }  

    @Override
    public void postVisit (final @Nonnull Layout layout) 
      {
        level--;
      }

    @Override
    public Void getValue() 
      throws NotFoundException 
      {
        return null;
      }
  }
