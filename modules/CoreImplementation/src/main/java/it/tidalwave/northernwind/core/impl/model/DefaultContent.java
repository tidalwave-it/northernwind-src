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
import javax.inject.Inject;
import java.util.regex.Pattern;
import java.text.Normalizer;
import java.io.IOException;
import org.openide.filesystems.NwFileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import lombok.Delegate;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
//import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A piece of content to be composed into a {@code Node}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j @ToString
/* package */ class DefaultContent implements Content
  {
    @Inject @Nonnull
    private ModelFactory modelFactory;
    
    @Nonnull @Delegate(types=Resource.class)
    private final Resource resource;

    /*******************************************************************************************************************
     *
     * Creates a new {@code DefaultContent} with the given configuration file.
     * 
     * @param   file   the configuration file
     *
     ******************************************************************************************************************/
    public DefaultContent (final @Nonnull NwFileObject file)
      {
        resource = modelFactory.createResource(file);  
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<Content> findChildren() 
      {
        return new FolderBasedFinderSupport(this);
      }

    // FIXME: this is declared in Frontend Components. Either move some properties in this module, or this the next
    // method can't stay here.
    public static final Key<String> PROPERTY_TITLE = new Key<String>("title");
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String getExposedUri() // TODO: rename to getDefaultExposedUri
      throws NotFoundException, IOException 
      {
        String title = resource.getProperties().getProperty(PROPERTY_TITLE);
        title = deAccent(title);
        title = title.replaceAll(" ", "-")
                     .replaceAll(",", "")
                     .replaceAll("\\.", "")
                     .replaceAll(";", "")
                     .replaceAll("/", "")
                     .replaceAll("!", "")
                     .replaceAll("\\?", "")
                     .replaceAll(":", "")
                     .replaceAll("[^\\w-]*", ""); 
        return title.toLowerCase();
      }
    
    /*******************************************************************************************************************
     *
     * See http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet
     *
     ******************************************************************************************************************/
    @Nonnull
    public String deAccent (final @Nonnull String string) 
      {
        final String nfdNormalizedString = Normalizer.normalize(string, Normalizer.Form.NFD); 
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
      }
  }