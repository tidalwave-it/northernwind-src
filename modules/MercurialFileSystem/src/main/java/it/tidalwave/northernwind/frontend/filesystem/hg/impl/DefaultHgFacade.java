/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
import it.tidalwave.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultHgFacade implements HgFacade
  {
    @Nonnull
    private final Path workArea;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void clone (final @Nonnull URI uri) 
      throws IOException
      {
        try
          {
            if (!workArea.toFile().mkdirs())
              {
                throw new IOException("Cannot mkdirs " + workArea);  
              }
            
            final Executor executor = Executor.forExecutable("hg").withArgument("clone")
                                                                  .withArgument("--noupdate")
                                                                  .withArgument(uri.toASCIIString())
                                                                  .withArgument(".")
                                                                  .withWorkingDirectory(workArea)
                                                                  .start()
                                                                  .waitForCompletion();
          }
        catch (InterruptedException e)
          {
            throw new IOException(e);  
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Tag getId() 
      throws IOException
      {
        try
          {
            final Executor executor = Executor.forExecutable("hg").withArgument("id")
                                                                  .withWorkingDirectory(workArea)
                                                                  .start()
                                                                  .waitForCompletion();
            final Scanner scanner = executor.getStdout().filteredAndSplitBy("(.*)", " ");
            scanner.next();
            return new Tag(scanner.next());
          }
        catch (InterruptedException e)
          {
            throw new IOException(e);  
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public List<Tag> getTags()
      throws IOException
      {
        try
          {
            final Executor executor = Executor.forExecutable("hg").withArgument("tags")
                                                                  .withWorkingDirectory(workArea)
                                                                  .start()
                                                                  .waitForCompletion();
            return toTagList(executor.getStdout().filteredBy("(.*)"));
          }
        catch (InterruptedException e)
          {
            throw new IOException(e);  
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Tag getLatestTag()
      throws IOException, NotFoundException
      {
        final List<Tag> tags = getTags();
        Collections.reverse(tags);
        
        for (final Tag tag : tags)
          {
            if (!tag.isTip())
              {
                return tag;  
              }
          }
        
        throw new NotFoundException();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void updateTo (final @Nonnull Tag tag) 
      throws IOException
      {
        try
          {
            final Executor executor = Executor.forExecutable("hg").withArgument("update")
                                                                  .withArgument("-C")
                                                                  .withArgument(tag.getName())
                                                                  .withWorkingDirectory(workArea)
                                                                  .start()
                                                                  .waitForCompletion();
          }
        catch (InterruptedException e)
          {
            throw new IOException(e);  
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void pull() 
      throws IOException
      {
        try
          {
            final Executor executor = Executor.forExecutable("hg").withArgument("pull")
                                                                  .withWorkingDirectory(workArea)
                                                                  .start()
                                                                  .waitForCompletion();
          }
        catch (InterruptedException e)
          {
            throw new IOException(e);  
          }
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<Tag> toTagList (final @Nonnull List<String> strings) 
      {
        final List<Tag> tags = new ArrayList<>();
        
        for (final String string : strings)
          {
            tags.add(new Tag(string.replaceAll(" .*$", "")));
          }
        
        Collections.reverse(tags);
        return tags;
      }
  }
