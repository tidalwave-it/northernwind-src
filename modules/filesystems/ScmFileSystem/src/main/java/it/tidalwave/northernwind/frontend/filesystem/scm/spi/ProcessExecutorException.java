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
package it.tidalwave.northernwind.frontend.filesystem.scm.spi;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * An exception thrown when an external process terminated with a non-zero exit code. Getter methods allow to access
 * the exit code as well as the stdout and stderr output.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class ProcessExecutorException extends IOException
  {
    /** The exit code. */
    @Getter
    private final int exitCode;

    /** The stdout. */
    @Getter @Nonnull
    private final List<String> stdout;

    /** The stderr. */
    @Getter @Nonnull
    private final List<String> stderr;

    protected ProcessExecutorException (@Nonnull final String message,
                                        final int exitCode,
                                        @Nonnull final List<String> stdout,
                                        @Nonnull final List<String> stderr)
      {
        super(message);
        this.exitCode = exitCode;
        this.stdout = new CopyOnWriteArrayList<>(stdout);
        this.stderr = new CopyOnWriteArrayList<>(stderr);
      }
  }
