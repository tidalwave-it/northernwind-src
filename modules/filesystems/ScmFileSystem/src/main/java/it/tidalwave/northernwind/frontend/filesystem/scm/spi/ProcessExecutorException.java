package it.tidalwave.northernwind.frontend.filesystem.scm.spi;

import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

public class ProcessExecutorException extends IOException
  {
    @Getter
    private final int exitCode;

    @Getter @Nonnull
    private final List<String> stdout;

    @Getter @Nonnull
    private final List<String> stderr;

    public ProcessExecutorException (final @Nonnull String message, final int exitCode, final @Nonnull List<String> stdout, final @Nonnull List<String> stderr)
      {
        super(message);
        this.exitCode = exitCode;
        this.stdout = new CopyOnWriteArrayList<>(stdout);
        this.stderr = new CopyOnWriteArrayList<>(stderr);
      }
  }
