package org.spoofax.jsglr.tests.haskell;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.spoofax.interpreter.core.Pair;
import org.spoofax.jsglr.tests.result.FileResult;
import org.spoofax.jsglr.tests.result.FileResultObserver;
import org.strategoxt.lang.Context;

/**
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 */
public class TestPackage extends TestCase {

  private final static boolean LOGGING = true;

  private final static Pattern SOURCE_FILE_PATTERN = Pattern.compile(".*\\.hs");
  private static final String BASE_DIR = "hackage-data/";// "/Users/moritzlichter/Desktop/UnicodeFiles/";//

  private File csvFile;
  private FileResultObserver observer;

  private static final Object lock = new Object();
  private static int numFilesSuccessfully = 0;
  private static int numFilesNotSuccessfully = 0;

  private Pair<Context, Context> contexts;

  public TestPackage(Pair<Context, Context> contexts) {
    this.contexts = contexts;
  }

  public void testPackage() throws IOException {
    testPackage("Package", new FileResultObserver() {
      public void observe(FileResult result) {
      }
    });
    System.out.println(csvFile.getAbsolutePath());
  }

  public void testPackage(String pkg, FileResultObserver observer)
      throws IOException {
    if (LOGGING)
      System.out.println(pkg + " starting");
    this.observer = observer;

    File dir = new File(BASE_DIR + pkg);
    // Only do something if the package exists
    if (!dir.exists()) {
      return;
    }
    csvFile = new File(dir + ".csv");
    try {
      new FileResult().writeCSVHeader(csvFile.getAbsolutePath());
    } catch (IOException e) {
      // e.printStackTrace();
    }
    testFiles(dir, "", pkg);

    if (LOGGING)
      synchronized (lock) {
        System.out
            .println(pkg + " done. Total: " + numFilesSuccessfully
                + " success, " + numFilesNotSuccessfully
                + " unexpected exception.");
      }

  }

  private void logResult(FileResult result) throws IOException {
    observer.observe(result);
    try {
      result.appendAsCSV(csvFile.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public boolean testFiles(File dir, String path, String pkg)
      throws IOException {
    if (dir != null && dir.listFiles() != null) {
      for (File f : dir.listFiles()) {
        if (f.isFile() && SOURCE_FILE_PATTERN.matcher(f.getName()).matches()) {
          FileResult result;
          try {
            result = new TestFile(this.contexts).testFile(f,
                Utilities.extendPath(path, f.getName()), pkg);
            logResult(result);
            synchronized (lock) {
              numFilesSuccessfully++;
            }
          } catch (Exception e) {
            synchronized (lock) {
              numFilesNotSuccessfully++;
            }
            e.printStackTrace();
            result = new FileResult();
            result.pkg = pkg;
            result.path = path;
            result.otherExceptions.t1 = e.toString();
            result.otherExceptions.t2 = e.toString();
            result.otherExceptions.t3 = e.toString();
            logResult(result);
          }

        } else if (f.isDirectory()) {
          if (testFiles(f, Utilities.extendPath(path, f.getName()), pkg))
            return true;
        }
      }
    }
    return false;
  }

  private File cabalUnpack(String pkg) throws IOException {
    File tmpDir = File.createTempFile(pkg.length() < 3 ? pkg + "aaa" : pkg, "");
    tmpDir.delete();
    tmpDir.mkdirs();

    String[] cmds = new String[] { TestConfiguration.CABAL_COMMAND, "unpack",
        pkg, "-d" + tmpDir.getAbsolutePath() };

    if (LOGGING)
      System.out.println("[" + pkg + "] "
          + CommandExecution.commandLineAsString(cmds));

    CommandExecution.SILENT_EXECUTION = !LOGGING;
    CommandExecution.SUB_SILENT_EXECUTION = !LOGGING;

    CommandExecution.execute("[" + pkg + "]", cmds);
    return tmpDir;
  }
}
