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
package it.tidalwave.util.impl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import it.tidalwave.util.ProcessExecutorException;
import it.tidalwave.util.ProcessExecutor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * A helper class for launching an external process and handling its output.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @Slf4j
public final class DefaultProcessExecutor implements ProcessExecutor
  {
    private static final String PROCESS_EXITED_WITH = "Process exited with ";

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public class DefaultConsoleOutput implements ConsoleOutput
      {
        @Nonnull
        private final String name;

        @Nonnull
        private final InputStream input;

        @Getter
        private final List<String> content = Collections.synchronizedList(new ArrayList<>());

        private volatile boolean completed;

        /** The consumer for output. */
        private final Runnable consoleConsumer = () ->
          {
            try
              {
                read();
              }
            catch (IOException e)
              {
                log.warn("while reading from process console", e);
              }

            synchronized (DefaultConsoleOutput.this)
              {
                completed = true;
                DefaultConsoleOutput.this.notifyAll();
              }
          };

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public ConsoleOutput start()
          {
            Executors.newSingleThreadExecutor().submit(consoleConsumer);
            return this;
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public synchronized ConsoleOutput waitForCompleted ()
                throws InterruptedException
          {
            while (!completed)
              {
                wait();
              }

            return this;
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull @SuppressWarnings({"squid:S2095", "IOResourceOpenedButNotSafelyClosed"})
        public Scanner filteredAndSplitBy (@Nonnull final String filterRegexp, @Nonnull final String delimiterRegexp)
          {
            return new Scanner(filteredBy(filterRegexp).get(0)).useDelimiter(Pattern.compile(delimiterRegexp));
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public List<String> filteredBy (@Nonnull final String filterRegexp)
          {
            final Pattern p = Pattern.compile(filterRegexp);
            final List<String> result = new ArrayList<>();

            for (final String s : new ArrayList<>(content))
              {
                final Matcher m = p.matcher(s);

                if (m.matches())
                  {
                    result.add(m.group(1));
                  }
              }

            return result;
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public ConsoleOutput waitFor (@Nonnull final String regexp)
                throws InterruptedException, IOException
          {
            log.debug("waitFor({})", regexp);

            while (filteredBy(regexp).isEmpty())
              {
                try
                  {
                    final int exitValue = process.exitValue();
                    throw new IOException(PROCESS_EXITED_WITH + exitValue);
                  }
                catch (IllegalThreadStateException e) // ok, process not terminated yet
                  {
                    synchronized (this)
                      {
                        wait(50); // FIXME: polls because it doesn't get notified
                      }
                  }
              }

            return this;
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override
        public void clear()
          {
            content.clear();
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override
        public void read()
              throws IOException
          {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(input)))
              {
                for (; ; )
                  {
                    final String s = br.readLine();

                    if (s == null)
                      {
                        break;
                      }

                    log.trace(">>>>>>>> {}: {}", name, s);
                    content.add(s);

                    synchronized (this)
                      {
                        notifyAll();
                      }
                  }
              }
          }
      }

    /** The arguments to pass to the external process. */
    private final List<String> arguments = new ArrayList<>();

    /** The working directory for the external process. */
    private Path workingDirectory = new File(".").toPath();

    /** The external process. */
    private Process process;

    /** The processor of stdout. */
    @Getter
    private ConsoleOutput stdout;

    /** The processor of stderr. */
    @Getter
    private ConsoleOutput stderr;

    /** The writer to feed the process' stdin. */
    private PrintWriter stdin;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public DefaultProcessExecutor (@Nonnull String executable)
            throws IOException
      {
        arguments.add(DefaultProcessExecutor.findPathFor(executable));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ProcessExecutor withArgument (@Nonnull final String argument)
      {
        arguments.add(argument);
        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ProcessExecutor withArguments (@Nonnull final String... arguments)
      {
        this.arguments.addAll(List.of(arguments));
        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ProcessExecutor withWorkingDirectory (@Nonnull final Path workingDirectory)
      {
        this.workingDirectory = workingDirectory;
        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ProcessExecutor start()
            throws IOException
      {
        log.debug(">>>> executing: {}", String.join(" ", arguments));

        final List<String> environment = new ArrayList<>();

//        for (final Entry<String, String> e : System.getenv().entrySet())
//          {
//            environment.add(String.format("%s=%s", e.getKey(), e.getValue()));
//          }

        log.debug(">>>> working directory: {}", workingDirectory.toFile().getCanonicalPath());
        log.debug(">>>> environment:       {}", environment);
        process = Runtime.getRuntime().exec(arguments.toArray(new String[0]),
                                            environment.toArray(new String[0]),
                                            workingDirectory.toFile());

        stdout = new DefaultConsoleOutput("STDOUT", process.getInputStream()).start();
        stderr = new DefaultConsoleOutput("STDERR", process.getErrorStream()).start();
        stdin = new PrintWriter(process.getOutputStream(), true);

        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ProcessExecutor waitForCompletion()
            throws IOException, InterruptedException
      {
        if (process.waitFor() != 0)
          {
            final List<String> environment = new ArrayList<>();

            for (final Entry<String, String> e : System.getenv().entrySet())
              {
                environment.add(String.format("%s=%s, ", e.getKey(), e.getValue()));
              }

            log.warn(PROCESS_EXITED_WITH + process.exitValue());
            log.debug(">>>> executed:          {}", arguments);
            log.debug(">>>> working directory: {}", workingDirectory.toFile().getCanonicalPath());
            log.debug(">>>> environment:       {}", environment);
            // log("STDOUT", stdout);
            // log("STDERR", stderr);
            throw new ProcessExecutorException(PROCESS_EXITED_WITH + process.exitValue(),
                                               process.exitValue(),
                                               stdout.waitForCompleted().getContent(),
                                               stderr.waitForCompleted().getContent());
          }

        return this;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ProcessExecutor send (@Nonnull final String string)
      {
        log.info(">>>> sending '{}'...", string);
        stdin.println(string);
        return this;
      }

    /*******************************************************************************************************************
     *
     * Scans the {@code PATH} for finding the absolute path of the given executable.
     *
     * @param executable the executable to search for
     * @return the absolute path
     * @throws IOException if the executable can't be found
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String findPathFor (@Nonnull final String executable)
            throws IOException
      {
        final String pathEnv = System.getenv("PATH") + File.pathSeparator + "/usr/local/bin";

        for (final String path : pathEnv.split(File.pathSeparator))
          {
            final File file = new File(new File(path), executable);

            if (file.canExecute())
              {
                return file.getAbsolutePath();
              }
          }

        throw new IOException("Can't find " + executable + " in PATH");
      }

    /*******************************************************************************************************************
     *
     * Logs a whole console output.
     *
     * @param prefix a log prefix
     * @param consoleOutput the output
     *
     ******************************************************************************************************************/
    private static void log (@Nonnull final String prefix, @Nonnull final DefaultConsoleOutput consoleOutput)
      {
        for (final String line : consoleOutput.getContent())
          {
            log.error("{}: {}", prefix, line);
          }
      }
  }

