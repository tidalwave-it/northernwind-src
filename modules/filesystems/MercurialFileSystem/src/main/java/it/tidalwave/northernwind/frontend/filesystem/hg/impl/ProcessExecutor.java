/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.filesystem.hg.impl;

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
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @NoArgsConstructor(access=AccessLevel.PRIVATE) @Slf4j
public class ProcessExecutor
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor(access=AccessLevel.PACKAGE)
    public class ConsoleOutput
      {
        @Nonnull
        private final InputStream input;

        @Getter
        private final List<String> content = Collections.synchronizedList(new ArrayList<String>());

        private boolean completed;

        /***************************************************************************************************************
         *
         *
         ***************************************************************************************************************/
        @Nonnull
        public ConsoleOutput start()
          {
            Executors.newSingleThreadExecutor().submit(runnable);
            return this;
          }

        /***************************************************************************************************************
         *
         *
         ***************************************************************************************************************/
        private final Runnable runnable = new Runnable()
          {
            @Override
            public void run()
              {
                try
                  {
                    read();
                  }
                catch (IOException e)
                  {
                    log.warn("while reading from process console", e);
                  }

                completed = true;

                synchronized (ConsoleOutput.this)
                  {
                    ConsoleOutput.this.notifyAll();
                  }
              }
          };

        @Nonnull
        public synchronized ConsoleOutput waitForCompleted()
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
         *
         ***************************************************************************************************************/
        @Nonnull
        public Scanner filteredAndSplitBy (final @Nonnull String filterRegexp, final @Nonnull String delimiterRegexp)
          {
            final String string = filteredBy(filterRegexp).get(0);
            return new Scanner(string).useDelimiter(Pattern.compile(delimiterRegexp));
          }

        /***************************************************************************************************************
         *
         *
         ***************************************************************************************************************/
        @Nonnull
        public List<String> filteredBy (final @Nonnull String filter)
          {
            final Pattern p = Pattern.compile(filter);
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
         *
         ***************************************************************************************************************/
        @Nonnull
        public ConsoleOutput waitFor (final @Nonnull String regexp)
          throws InterruptedException, IOException
          {
            log.debug("waitFor({})", regexp);

            while (filteredBy(regexp).isEmpty())
              {
                try
                  {
                    final int exitValue = process.exitValue();
                    throw new IOException("Process exited with " + exitValue);
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
         *
         ***************************************************************************************************************/
        public void clear()
          {
            content.clear();
          }

        /***************************************************************************************************************
         *
         *
         ***************************************************************************************************************/
        private void read()
          throws IOException
          {
            final @Cleanup BufferedReader br = new BufferedReader(new InputStreamReader(input));

            for (;;)
              {
                final String s = br.readLine();

                if (s == null)
                  {
                    break;
                  }

                log.trace(">>>>>>>> {}", s);
                content.add(s);

                synchronized (this)
                  {
                    notifyAll();
                  }
              }

            br.close();
          }
      }

    private final List<String> arguments = new ArrayList<>();

    private Path workingDirectory = new File(".").toPath();

    private Process process;

    @Getter
    private ConsoleOutput stdout;

    @Getter
    private ConsoleOutput stderr;

    private PrintWriter stdin;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public static ProcessExecutor forExecutable (final @Nonnull String executable)
      throws IOException
      {
        final ProcessExecutor executor = new ProcessExecutor();
        executor.arguments.add(findPathFor(executable));
        return executor;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor withArgument (final @Nonnull String argument)
      {
        arguments.add(argument);
        return this;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor withWorkingDirectory (final @Nonnull Path workingDirectory)
      {
        this.workingDirectory = workingDirectory;
        return this;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor start()
      throws IOException
      {
        log.debug(">>>> executing {} ...", arguments);

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

        stdout = new ConsoleOutput(process.getInputStream()).start();
        stderr = new ConsoleOutput(process.getErrorStream()).start();
        stdin  = new PrintWriter(process.getOutputStream(), true);

        return this;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
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

            log.error("Process exited with " + process.exitValue());
            log.error(">>>> executed:          {}", arguments);
            log.error(">>>> working directory: {}", workingDirectory.toFile().getCanonicalPath());
            log.error(">>>> environment:       {}", environment);
            log("STDOUT", stdout);
            log("STDERR", stderr);
            throw new IOException("Process exited with " + process.exitValue());
          }

        return this;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor send (final @Nonnull String string)
      throws IOException
      {
        log.info(">>>> sending '{}'...", string);
        stdin.println(string);
        return this;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String findPathFor (final @Nonnull String executable)
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
     *
     ******************************************************************************************************************/
    private static void log (final @Nonnull String prefix, final @Nonnull ProcessExecutor.ConsoleOutput consoleOutput)
      {
        for (final String line : consoleOutput.getContent())
          {
            log.error("{}: {}", prefix, line);
          }
      }
  }

