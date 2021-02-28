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
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.joining;

/***********************************************************************************************************************
 *
 * A helper class for launching an external process and handling its output.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @NoArgsConstructor(access = AccessLevel.PRIVATE) @Slf4j
public class ProcessExecutor
  {
    private static final String PROCESS_EXITED_WITH = "Process exited with ";

    /*******************************************************************************************************************
     *
     * A container of the console output of the process.
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public class ConsoleOutput
      {
        @Nonnull
        private final InputStream input;

        @Getter
        private final List<String> content = Collections.synchronizedList(new ArrayList<>());

        private volatile boolean completed;

        /** The consumer for output. */
        private final Runnable consoleConsumer =() ->
          {
            try
              {
                read();
              }
            catch (IOException e)
              {
                log.warn("while reading from process console", e);
              }

            synchronized (ConsoleOutput.this)
              {
                completed = true;
                ConsoleOutput.this.notifyAll();
              }
          };

        /***************************************************************************************************************
         *
         * Starts collection output from the external process.
         *
         * @return itself
         *
         ***************************************************************************************************************/
        @Nonnull
        public ConsoleOutput start()
          {
            Executors.newSingleThreadExecutor().submit(consoleConsumer);
            return this;
          }

        /***************************************************************************************************************
         *
         * Waits for the completion of the launched process.
         *
         * @return itself
         * @throws InterruptedException    if the process was interrupted
         *
         ***************************************************************************************************************/
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
         * Returns a {@link Scanner} on the latest line of output produced that matches a given regular expression,
         * split on the given delimiter.
         *
         * @param       filterRegexp            the regular expression to filter output
         * @param       delimiterRegexp         the regular expression to split the line
         * @return the {@code Scanner} to parse results
         *
         ***************************************************************************************************************/
        @Nonnull @SuppressWarnings("squid:S2095")
        public Scanner filteredAndSplitBy (final @Nonnull String filterRegexp, final @Nonnull String delimiterRegexp)
          {
            return new Scanner(filteredBy(filterRegexp).get(0)).useDelimiter(Pattern.compile(delimiterRegexp));
          }

        /***************************************************************************************************************
         *
         * Returns the output produced by the launched process, filtered by the given regular expression.
         *
         * @param       filterRegexp            the regular expression to filter output
         * @return the output lines
         *
         ***************************************************************************************************************/
        @Nonnull
        public List<String> filteredBy (final @Nonnull String filterRegexp)
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
         * Waits for an output line matching the given regular expression to appear.
         *
         * @param  regexp                  the regular expression
         * @return itself
         * @throws IOException             if something goes wrong
         * @throws InterruptedException    if the process has been interrupted
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
         * Clears the contents collected so far.
         *
         ***************************************************************************************************************/
        public void clear()
          {
            content.clear();
          }

        /***************************************************************************************************************
         *
         * Reads the output until it's completed.
         *
         ***************************************************************************************************************/
        private void read()
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

                    log.trace(">>>>>>>> {}", s);
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
     * Specifies the executable to run. It is searched in the path.
     *
     * @param  executable          the executable
     * @return itself
     * @throws IOException         if something goes wrong
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
     * Specifies a single argument for the executable. This method can be called multiple times.
     *
     * @param  argument            the argument
     * @return itself
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
     * Specifies some arguments for the executable. This method can be called multiple times.
     *
     * @param  arguments           the argument
     * @return itself
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor withArguments (final @Nonnull String... arguments)
      {
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
      }

    /*******************************************************************************************************************
     *
     * Specifies the working directory for the executable.
     *
     * @param  workingDirectory    the working directory
     * @return itself
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
     * Launches the external process and starts collecting its output (which can be analyzed by calling
     * {@code getStdout()} and {@code getStderr()}.
     *
     * @return itself
     * @throws IOException         if something goes wrong
     *
     ******************************************************************************************************************/
    @Nonnull
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

        stdout = new ConsoleOutput(process.getInputStream()).start();
        stderr = new ConsoleOutput(process.getErrorStream()).start();
        stdin = new PrintWriter(process.getOutputStream(), true);

        return this;
      }

    /*******************************************************************************************************************
     *
     * Waits for the completion of the external process. If the process terminates with a non-zero status code, an
     * {@link IOException} is thrown.
     *
     * @return itself
     * @throws ProcessExecutorException if the process terminates with a non zero exit code
     * @throws IOException             if something went wrong
     * @throws InterruptedException    if the process has been interrupted
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

            log.error(PROCESS_EXITED_WITH + process.exitValue());
            log.error(">>>> executed:          {}", arguments);
            log.error(">>>> working directory: {}", workingDirectory.toFile().getCanonicalPath());
            log.error(">>>> environment:       {}", environment);
            log("STDOUT", stdout);
            log("STDERR", stderr);
            throw new ProcessExecutorException(PROCESS_EXITED_WITH + process.exitValue(),
                                               process.exitValue(),
                                               stdout.getContent(),
                                               stderr.getContent());
          }

        return this;
      }

    /*******************************************************************************************************************
     *
     * Sends some input to the external process.
     *
     * @param  string                  the input to send
     * @return itself
     * @throws IOException             if something fails
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
     * Scans the {@code PATH} for finding the absolute path of the given executable.
     *
     * @param executable the executable to search for
     * @return the absolute path
     * @throws IOException if the executable can't be found
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
     * Logs a whole console output.
     *
     * @param prefix a log prefix
     * @param consoleOutput the output
     *
     ******************************************************************************************************************/
    private static void log (final @Nonnull String prefix, final @Nonnull ConsoleOutput consoleOutput)
      {
        for (final String line : consoleOutput.getContent())
          {
            log.error("{}: {}", prefix, line);
          }
      }
  }

