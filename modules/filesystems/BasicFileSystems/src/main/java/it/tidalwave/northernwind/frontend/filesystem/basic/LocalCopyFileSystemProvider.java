/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.basic;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.File;
import java.time.ZonedDateTime;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.model.ResourceFileSystemChangedEvent;
import it.tidalwave.northernwind.core.model.ResourceFileSystemProvider;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.messagebus.MessageBus.Listener;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A provider for the {@link NwFileSystem} that clones a source provider into a local NwFileSystem for performance
 * purposes...
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j @ToString(of = { "rootPath" })
public class LocalCopyFileSystemProvider implements ResourceFileSystemProvider
  {
    @Getter @Setter @Nonnull
    private ResourceFileSystemProvider sourceProvider;

    @Getter @Setter @Nonnull
    private String rootPath = "";

    private LocalFileSystemProvider targetProvider = new LocalFileSystemProvider();

    @Inject @Named("applicationMessageBus")
    private MessageBus messageBus;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private final Listener<ResourceFileSystemChangedEvent> sourceProviderChangeListener =
            new Listener<ResourceFileSystemChangedEvent>()
      {
        @Override
        public void notify (final @Nonnull ResourceFileSystemChangedEvent event)
          {
            if (event.getFileSystemProvider() == sourceProvider)
              {
                try
                  {
                    log.info("Detected file change, regenerating local file system...");
                    generateLocalFileSystem();
                    messageBus.publish(new ResourceFileSystemChangedEvent(LocalCopyFileSystemProvider.this, ZonedDateTime.now()));
                  }
                catch (IOException e)
                  {
                    log.error("While resetting site: ", e);
                  }
              }
          }
      };

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public synchronized ResourceFileSystem getFileSystem()
      throws IOException
      {
        return targetProvider.getFileSystem();
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      throws IOException
      {
        log.info("initialize()");
        generateLocalFileSystem();
        messageBus.subscribe(ResourceFileSystemChangedEvent.class, sourceProviderChangeListener);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void generateLocalFileSystem()
      throws IOException
      {
        log.info("generateLocalFileSystem()");

        if (!new File(rootPath).mkdirs()) // TODO: use NwFileSystem API
          {
            throw new IOException("Cannot create dirs for " + rootPath);
          }

        // FIXME: shouldn't be needed, but otherwise after a second call to this method won't find files
        targetProvider = new LocalFileSystemProvider();
        targetProvider.setRootPath(rootPath);
        final ResourceFile targetRoot = targetProvider.getFileSystem().getRoot();
        final String path = targetRoot.toFile().getAbsolutePath();
        log.info(">>>> scratching {} ...", path);
        emptyFolder(targetRoot);
        log.info(">>>> copying files to {} ...", path);
        copyFolder(sourceProvider.getFileSystem().getRoot(), targetRoot);
//                    targetProvider.getFileSystem().refresh(true);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void emptyFolder (final @Nonnull ResourceFile folder)
      throws IOException
      {
        log.trace("emptyFolder({}, {}", folder);

        for (final ResourceFile child : folder.findChildren().results())
          {
            child.delete();
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void copyFolder (final @Nonnull ResourceFile sourceFolder, final @Nonnull ResourceFile targetFolder)
      throws IOException
      {
        log.trace("copyFolder({}, {}", sourceFolder, targetFolder);

        for (final ResourceFile sourceChild : sourceFolder.findChildren().results())
          {
            if (!sourceChild.isFolder())
              {
                log.trace(">>>> copying {} into {} ...", sourceChild, targetFolder);
                sourceChild.copyTo(targetFolder);
              }
          }

        for (final ResourceFile sourceChild : sourceFolder.findChildren().results())
          {
            if (sourceChild.isFolder())
              {
                copyFolder(sourceChild, targetFolder.createFolder(sourceChild.getName()));
              }
          }
      }
  }
