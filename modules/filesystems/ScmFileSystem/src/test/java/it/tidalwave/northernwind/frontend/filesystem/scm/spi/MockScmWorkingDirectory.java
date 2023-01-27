/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.scm.spi;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import org.apache.commons.io.FileUtils;

/***********************************************************************************************************************
 *
 * A mock implementation of {@link it.tidalwave.northernwind.frontend.filesystem.scm.spi.ScmWorkingDirectory}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class MockScmWorkingDirectory extends ScmWorkingDirectorySupport
  {
    public static final String MOCKSCM = ".mockscm";

    public static class Preparer extends ScmPreparer
      {
        @Override
        protected void stripChangesetsAfter (@Nonnull final String tag)
                throws IOException
          {
            final var n = Integer.parseInt(tag.replace("published-0.", ""));
            final var tags = createTagNames(n);
            final var configFolder = ScmPreparer.REPOSITORY_FOLDER.resolve(MOCKSCM);
            Files.createDirectories(configFolder);
            Files.write(configFolder.resolve("tags"), tags);
          }
      }

    private final Path configFolder;

    private final Path configUri;

    private final Path configTags;

    private final Path configCurrentTag;

    public MockScmWorkingDirectory (@Nonnull final Path workArea)
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

    @Override
    public void checkOut (@Nonnull final Tag tag)
            throws IOException, InterruptedException
      {
        if (!getTags().contains(tag))
          {
            throw new IllegalArgumentException("Invalid tag: " + tag.getName());
          }

        Files.writeString(configCurrentTag, tag.getName());
      }

    @Override @Nonnull
    public List<String> listTags()
            throws IOException
      {
        return Files.readAllLines(configTags);
      }

    @Override
    public void cloneFrom (@Nonnull final URI uri)
            throws IOException
      {
        Files.createDirectories(configFolder);
        Files.writeString(configUri, uri.toString());
        fetchChangesets();
      }

    @Override
    public void fetchChangesets()
            throws IOException
      {
        final var uri = URI.create(Files.readString(configUri));
        FileUtils.deleteDirectory(folder.toFile());
        FileUtils.copyDirectory(Path.of(uri).toFile(), folder.toFile());
        Files.createDirectories(configFolder);
        Files.writeString(configUri, uri.toString());
      }
  }
