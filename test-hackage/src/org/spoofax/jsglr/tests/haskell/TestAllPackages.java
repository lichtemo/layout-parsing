package org.spoofax.jsglr.tests.haskell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;

import org.spoofax.interpreter.core.Pair;
import org.spoofax.jsglr.tests.result.FileResult;
import org.spoofax.jsglr.tests.result.FileResultObserver;
import org.strategoxt.lang.Context;

/**
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 * 
 */
public class TestAllPackages extends TestCase {

  private static File csvDir;
  private static File csvFile;

  private int warmupCount = 0;
  public static final int NUM_THREADS = 1;
  private static final boolean WARMUP = true;

  private static final Object MAIN_CSV_FILE_LOCK = new Object();

  public void warmup() throws IOException {
    String[] warmupPackages = new String[] { "matlab", "matrix-market",
        "matsuri", "maude", "maximal-cliques", "maybench", "mbox", "mdo",
        "dbf", "dbjava", "dbmigrations", "hakismet", "hakyll", "halfs",
        "halipeto", "haltavista", "hamlet", "hamtmap", "zenc", "zeno" };

    FileResultObserver observer = new FileResultObserver() {
      public void observe(FileResult result) {
        warmupCount++;
      }
    };
    Pair<Context, Context> contexts = TestFile.createContexts();
    try {
      for (String pkg : warmupPackages)
        new TestPackage(contexts).testPackage(pkg, observer);
    } catch (Throwable e) {
      // e.printStackTrace();
    }
    System.out.println("Warmed up with " + warmupCount + " files from "
        + warmupPackages.length + " packages.");
  }

  public void testAllPackages() throws IOException {
    String path = "all" + System.currentTimeMillis();
    csvDir = new File(path);
    csvDir.mkdirs();
    csvFile = new File(path + ".csv");
    new FileResult().writeCSVHeader(csvFile.getAbsolutePath());

    if (WARMUP) {
      warmup();
    }

    BufferedReader in = null;

    Pair<Context, Context> contexts;
    if (NUM_THREADS == 1) {
      contexts = TestFile.createContexts();
    }

    try {
      int i = 0;
      in = new BufferedReader(new FileReader(
          ExtractAllCabalPacakges.PACKAGE_LIST_FILE));
      String pkg;
      while ((pkg = in.readLine()) != null
          && !Thread.currentThread().isInterrupted()) {
        i++;
        System.out.println(i + " " + pkg + "...");

        if (TestConfiguration.SKIP_PACKAGES.contains(pkg)) {
          System.out.println("skipped");
          continue;
        }
        if (NUM_THREADS == 1) {
          new TestPackage(contexts).testPackage(pkg, new MyFileResultObserver(
              pkg));
        } else {

          new TestThread(pkg).start();

        }
      }
      TestThread.waitForThreads();

    } catch (Throwable e) {
      ;
    } finally {
      if (in != null)
        in.close();
    }

    System.out.println(csvFile.getAbsolutePath());
  }

  static class TestThread extends Thread {
    private static Pair<Context, Context>[] contexts;
    private static boolean[] used;
    private static int contextNum = 0;
    private static List<Thread> threads = Collections
        .synchronizedList(new LinkedList<Thread>());
    private static Semaphore s = new Semaphore(NUM_THREADS);

    static {
      contexts = new Pair[NUM_THREADS];
      used = new boolean[NUM_THREADS];
      for (int i = 0; i < NUM_THREADS; i++) {
        contexts[i] = TestFile.createContexts();
        used[i] = false;
      }
    }

    public static void waitForThreads() throws InterruptedException {
      while (!threads.isEmpty()) {
        Thread t = null;
        t = threads.get(0);
        t.join();
      }
    }

    private String pkgName;

    public TestThread(String pkgName) {
      this.pkgName = pkgName;
    }

    private int getFreeIndex() {
      for (int i = 0; i < NUM_THREADS; i++) {
        if (!used[i]) {
          return i;
        }
      }
      throw new Error("No free index");
    }

    public void start() {
      try {
        s.acquire();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      threads.add(this);
      super.start();
    }

    public void run() {
      int contextId;
      synchronized (contexts) {
        contextNum++;
        contextNum = contextNum % NUM_THREADS;
        contextId = getFreeIndex();
        if (used[contextId]) {
          System.err.println("Used context");
          System.exit(0);
        }
        used[contextId] = true;
      }

      //System.out.println("Using context: " + contextId);
      try {
        //new TestPackage(contexts[contextId]).testPackage(pkgName,
          //  new MyFileResultObserver(pkgName));
         Thread.sleep(Math.round(Math.random()*1000.f));
        used[contextId] = false;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        threads.remove(this);

        s.release();
      }
    }
  };

  private static class MyFileResultObserver implements FileResultObserver {
    private File pkgCsv;

    private MyFileResultObserver(String pkg) throws IOException {
      pkgCsv = new File(csvDir, pkg + ".csv");
      new FileResult().writeCSVHeader(pkgCsv.getAbsolutePath());
    }

    public void observe(FileResult packageResult) throws IOException {
      synchronized (MAIN_CSV_FILE_LOCK) {
        packageResult.appendAsCSV(csvFile.getAbsolutePath());
      }
      packageResult.appendAsCSV(pkgCsv.getAbsolutePath());

      // System.out.println(csvFile.getAbsolutePath());
    }
  }

  public static void main(String[] args) throws IOException {
    TestAllPackages tester = new TestAllPackages();
    int repetition = args.length > 0 ? Integer.parseInt(args[0]) : 1;

    if (WARMUP) {
      tester.warmup();
    }

    for (int i = 0; i < repetition; i++)
      tester.testAllPackages();
  }

}
