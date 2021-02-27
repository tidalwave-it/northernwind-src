/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.filesystem.scm.impl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.net.URI;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmRepositorySupport;
import it.tidalwave.northernwind.frontend.filesystem.scm.spi.Tag;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class MockScmRepository extends ScmRepositorySupport
  {
    public static final String MOCKSCM = ".mockscm";

    public static class Preparer extends ScmPreparer
      {
        @Override @Nonnull
        protected void doPrepare (final @Nonnull String tag)
          throws IOException
          {
            final int n = Integer.parseInt(tag.replace("published-0.", ""));
            List<String> tags = createTagNames(n);
            tags.add("tip");
            Collections.reverse(tags);
            final Path configFolder = ScmPreparer.SOURCE_REPOSITORY_FOLDER.resolve(MOCKSCM);
            Files.createDirectories(configFolder);
            Files.write(configFolder.resolve("tags"), tags);
          }
      }

    private Path configFolder;

    private Path configUri;

    private Path configTags;

    private Path configCurrentTag;

    public MockScmRepository (final @Nonnull Path workArea)
      throws IOException
      {
        super(MOCKSCM, workArea);
        configFolder = workArea.resolve(MOCKSCM);
        configUri = configFolder.resolve("uri");
        configTags = configFolder.resolve("tags");
        configCurrentTag = configFolder.resolve("current_tag");
      }

    @Override @Nonnull
    public Optional<Tag> getCurrentTag()
      throws IOException
      {
        if (!Files.exists(configCurrentTag))
          {
            return Optional.empty();
          }

        return Optional.of(new Tag(Files.readString(configCurrentTag)));
      }

    @Override @Nonnull
    public void updateTo (final @Nonnull Tag tag)
      throws IOException, InterruptedException
      {
        if (!getTags().contains(tag))
          {
            // throw new IOException("Unknown tag: " + tag.getName());
            throw new IOException("Process exited with 255"); // FIXME
          }

        Files.writeString(configCurrentTag, tag.getName());
      }

    @Override @Nonnull
    public List<String> listTags()
      throws IOException
      {
        return Files.readAllLines(configTags);
      }

    @Override @Nonnull
    public void cloneRepository (final @Nonnull URI uri)
      throws IOException
      {
        Files.createDirectories(configFolder);
        Files.writeString(configUri, uri.toString());
        pull();
      }

    @Override @Nonnull
    public void pull()
      throws IOException
      {
        final URI uri = URI.create(Files.readString(configUri));
        FileUtils.deleteDirectory(workArea.toFile());
        FileUtils.copyDirectory(Path.of(uri).toFile(), workArea.toFile());
        Files.createDirectories(configFolder);
        Files.writeString(configUri, uri.toString());
      }
  }
