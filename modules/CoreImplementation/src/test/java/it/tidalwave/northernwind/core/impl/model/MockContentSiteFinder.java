/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.SiteFinder;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class MockContentSiteFinder extends FinderSupport<Content, DefaultSiteFinder<Content>> implements SiteFinder<Content>
  {
    private String relativePath;
    
    private String relativeUri;
    
    @Override @Nonnull
    public SiteFinder<Content> withRelativePath (final @Nonnull String relativePath) 
      {
        this.relativePath = relativePath;
        return this;
      }

    @Override @Nonnull
    public SiteFinder<Content> withRelativeUri (final @Nonnull String relativeUri)
      {
        this.relativeUri = relativeUri;
        return this;
      }

    @Override @Nonnull
    protected List<? extends Content> computeResults() 
      {
        try
          {
            final Content content = mock(Content.class);

            if (relativePath.equals("/"))
              {
                when(content.getExposedUri()).thenReturn("");
              }
            else
              {
                when(content.getExposedUri()).thenReturn("EXPOSED-" + relativePath.substring(1).replace('/', '-').replace(' ', '-'));
              }
            
            return Arrays.asList(content);
          } 
        catch (NotFoundException e) 
          { 
            throw new RuntimeException(e);  
          }
        catch (IOException e) 
          {
            throw new RuntimeException(e);  
          }
      }

    @Override
    public void doWithResults (final @Nonnull Predicate<Content> predicate) 
      {
        throw new UnsupportedOperationException("Not supported.");
      }
  }
