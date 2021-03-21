package it.tidalwave.util;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import it.tidalwave.util.impl.DefaultProcessExecutor;

/***********************************************************************************************************************
 *
 * A helper class for launching an external process and handling its output.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface ProcessExecutor
  {
    /*******************************************************************************************************************
     *
     * A container of the console output of the process.
     *
     ******************************************************************************************************************/
    public static interface ConsoleOutput
      {
        /***************************************************************************************************************
         *
         * Starts collection output from the external process.
         *
         * @return itself
         *
         **************************************************************************************************************/
        @Nonnull
        public ConsoleOutput start();

        /***************************************************************************************************************
         *
         * Waits for the completion of the launched process.
         *
         * @return itself
         * @throws InterruptedException    if the process was interrupted
         *
         **************************************************************************************************************/
        @Nonnull
        public ConsoleOutput waitForCompleted ()
                throws InterruptedException;

        /***************************************************************************************************************
         *
         * Returns a {@link Scanner} on the latest line of output produced that matches a given regular expression,
         * split on the given delimiter. Remember that the {@code Scanner} must be closed when done.
         *
         * @param       filterRegexp            the regular expression to filter output
         * @param       delimiterRegexp         the regular expression to split the line
         * @return the {@code Scanner} to parse results
         *
         **************************************************************************************************************/
        @Nonnull @SuppressWarnings({"squid:S2095", "IOResourceOpenedButNotSafelyClosed"})
        public Scanner filteredAndSplitBy (@Nonnull String filterRegexp, @Nonnull String delimiterRegexp);

        /***************************************************************************************************************
         *
         * Returns the output produced by the launched process, filtered by the given regular expression.
         *
         * @param       filterRegexp            the regular expression to filter output
         * @return the output lines
         *
         **************************************************************************************************************/
        @Nonnull
        public List<String> filteredBy (@Nonnull String filterRegexp);

        /***************************************************************************************************************
         *
         * Waits for an output line matching the given regular expression to appear.
         *
         * @param  regexp                  the regular expression
         * @return itself
         * @throws IOException             if something goes wrong
         * @throws InterruptedException    if the process has been interrupted
         *
         **************************************************************************************************************/
        @Nonnull
        public ConsoleOutput waitFor (@Nonnull String regexp)
                throws InterruptedException, IOException;

        /***************************************************************************************************************
         *
         * Clears the contents collected so far.
         *
         **************************************************************************************************************/
        public void clear();

        /***************************************************************************************************************
         *
         * Reads the output until it's completed.
         *
         **************************************************************************************************************/
        public void read ()
            throws IOException;

        /***************************************************************************************************************
         *
         *
         **************************************************************************************************************/
        @Nonnull
        public List<String> getContent();
      }

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
    public static ProcessExecutor forExecutable (@Nonnull String executable)
            throws IOException
      {
        return new DefaultProcessExecutor(executable);
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
    public ProcessExecutor withArgument (@Nonnull String argument);

    /*******************************************************************************************************************
     *
     * Specifies some arguments for the executable. This method can be called multiple times.
     *
     * @param  arguments           the argument
     * @return itself
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor withArguments (@Nonnull String... arguments);

    /*******************************************************************************************************************
     *
     * Specifies the working directory for the executable.
     *
     * @param  workingDirectory    the working directory
     * @return itself
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor withWorkingDirectory (@Nonnull Path workingDirectory);

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
    ProcessExecutor start()
            throws IOException;

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
      throws IOException, InterruptedException;

    /*******************************************************************************************************************
     *
     * Sends some input to the external process.
     *
     * @param  string                  the input to send
     * @return itself
     *
     ******************************************************************************************************************/
    @Nonnull
    public ProcessExecutor send (@Nonnull String string);

    @Nonnull
    public ConsoleOutput getStdout();

    @Nonnull
    public ConsoleOutput getStderr();
  }
