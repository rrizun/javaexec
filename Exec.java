
import java.io.*;
import java.util.*;

import com.google.common.io.*;

// exec
public class Exec {
  // return stdout, throw stderr
  public static ByteSource asByteSourceOut(final List<String> args) {
    return asByteSource(args, 1);
  }
  // return stderr, throw stdout
  public static ByteSource asByteSourceErr(final List<String> args) {
    return asByteSource(args, 2);
  }
  private static ByteSource asByteSource(final List<String> args, final int fd) {
    return new ByteSource() {
      @Override
      public InputStream openStream() throws IOException {
        final File tmpFile = File.createTempFile("tmp", ".tmp");
        try {
          final Process p = new ProcessBuilder(args).redirectError(tmpFile).start();
          return new FilterInputStream(fd == 1 ? p.getInputStream() : p.getErrorStream()) {
            final InputStream in = new FileInputStream(tmpFile);
            public void close() throws IOException {
              try {
                try {
                  if (p.waitFor() != 0)
                    throw new IOException(p.exitValue() + CharStreams.toString(new InputStreamReader(in))); //###TODO just use Files.toString?!?
                } catch (InterruptedException ie) {
                  throw new IOException(ie);
                } finally {
                  p.destroy();
                }
              } finally {
                in.close();
              }
            }
          };
        } finally {
          tmpFile.delete();
        }
      }
    };
  }
}
