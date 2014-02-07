package org.spoofax.jsglr.tests.haskell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.spoofax.interpreter.core.Pair;
import org.spoofax.jsglr.tests.result.FileResult;

public class CompareResultFiles {

  private static class FileIdentifier {
    private FileResult result;

    public FileIdentifier(FileResult result) {
      super();
      this.result = result;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((this.result.path == null) ? 0 : this.result.path.hashCode());
      result = prime * result
          + ((this.result.pkg == null) ? 0 : this.result.pkg.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FileIdentifier other = (FileIdentifier) obj;
      if (this.result.path == null) {
        if (other.result.path != null)
          return false;
      } else if (!this.result.path.equals(other.result.path))
        return false;
      if (this.result.pkg == null) {
        if (other.result.pkg != null)
          return false;
      } else if (!this.result.pkg.equals(other.result.pkg))
        return false;
      return true;
    }

  }

  private ArrayList<HashMap<FileIdentifier, FileResult>> results = new ArrayList<HashMap<CompareResultFiles.FileIdentifier, FileResult>>();

  private LinkedList<StringBuilder> readFile(File csvPath) throws IOException {
    LinkedList<StringBuilder> buffer = new LinkedList<StringBuilder>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(csvPath));
      String read;
      while ((read = reader.readLine()) != null) {
        buffer.add(new StringBuilder(read));
      }

    } catch (IOException e) {
      throw e;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          throw e;
        }
      }
    }
    return buffer;
  }

  private StringBuilder extractFirstSet(LinkedList<StringBuilder> builders) {
    StringBuilder b = new StringBuilder();
    StringBuilder s1 = builders.removeFirst();
    if (s1.toString().startsWith("\"package\";\"path\";\"run\"")) {
      return extractFirstSet(builders);
    }
    b.append(s1);
    b.append('\n');
    b.append(builders.removeFirst());
    b.append('\n');
    b.append(builders.removeFirst());
    b.append('\n');
    return b;
  }
  
  public int readFolder(String folder) throws IOException {
    System.out.println("Read: " + folder);
    HashMap<FileIdentifier, FileResult> result = new HashMap<CompareResultFiles.FileIdentifier, FileResult>();
 
    int id = this.results.size();
    int num = 0;
    File [] files =  new File(folder).listFiles();
    int total =files.length;
    double last = 0;
    results.add(result);
    for (File f : files) {
      readFile(f,id, false);
      num ++;
      if (num %100 == 0) {
        System.out.print(".");
      }
      double r= (double)num/(double)total;
      if ( r> last +0.1) {
        System.out.print((int)(r*100));
        last = r;
      }
    }
    System.out.println();
    return id;
  }
  
  public int readFile(String file)throws IOException {
    int id = results.size();
      results.add(new HashMap<CompareResultFiles.FileIdentifier, FileResult>());
    readFile(new File(file),id, true);
    return id;
  }

  private void readFile(File csvPath, int id, boolean verbose) throws IOException {
    if (verbose)
      System.out.println("Read: " + csvPath);
   LinkedList<StringBuilder> fileContents = readFile(csvPath);
    fileContents.removeFirst();
  if (verbose)
    System.out.println("Parse ");
    HashMap<FileIdentifier, FileResult> result = results.get(id);
    int num = 0;
   try {
    while (!fileContents.isEmpty()) {
      FileResult fileResult = new FileResult();
      fileResult.readFromCSVString(extractFirstSet(fileContents));
      result.put(new FileIdentifier(fileResult), fileResult);
      num++;
      if (num % 1000 == 0) {
        num = 0;
        System.out.print(".");
      }
    }
    }catch(Exception e) {
      System.err.println("Exception while reading " + csvPath);
      e.printStackTrace();
    }
   if (verbose)
    System.out.println("Num Values: " + result.keySet().size());
 
   
  }

  public LinkedList<Pair<FileResult, FileResult>> listFilesDiffParsed(int id1,
      int id2) {
    HashMap<FileIdentifier, FileResult> map1 = this.results.get(id1);
    HashMap<FileIdentifier, FileResult> map2 = this.results.get(id2);
    LinkedList<Pair<FileResult, FileResult>> diff = new LinkedList<Pair<FileResult, FileResult>>();
    for (FileIdentifier f : map1.keySet()) {
      FileResult f1 = map1.get(f);
      FileResult f2 = map2.get(f);
      if (f2 == null ||  (f1.allSuccess != f2.allSuccess) ) {
        diff.add(new Pair<FileResult, FileResult>(f1, f2));
      } else {
       
      }
    }
    System.out.println(diff.size());
    return diff;
  }
  
  public LinkedList<FileResult> listFilesWithImplExplDifference(int id) {
    return this.filter(id, new Filter() {
      
      @Override
      public boolean filter(FileResult r) {
        return (r.differencesToReferenceParser.t2 > 0 || r.differencesToReferenceParser.t3 > 0) && r.makeExplicitLayout && r.makeImplicitLayout;
      }
    });
  }
  
  private static interface Filter {
    public boolean filter(FileResult r);
  }
  
  public LinkedList<FileResult> filter (int id, Filter filter) {
    LinkedList<FileResult> list = new LinkedList<FileResult>();
    for (FileResult r : this.results.get(id).values()) {
      if (filter.filter(r)) {
        list.add(r);
      }
    }
    return list;
  }
  
  private static class FileResultComparator implements Comparator<FileResult> {

    @Override
    public int compare(FileResult o1, FileResult o2) {
     return o1.path.compareToIgnoreCase(o2.path);
    }
    
  }
  
  private static void sort(LinkedList<FileResult> list) {
    Collections.sort(list, new FileResultComparator());
  }
  
  private static void print(LinkedList<FileResult> list, int max) {
    int i = 0;
    for (FileResult r : list) {
      if (i ==max) {return;}
      System.out.println(r.pkg + " - " + r.path + " - " + r.allSuccess + " " + r.ambiguities);
      i++;
    }
  }
 
  public static void main(String[] args) throws IOException {
    CompareResultFiles comp = new CompareResultFiles();

  //  int id1 = comp.readFolder("all1379944237523");
    //int id2 = comp.readFile("/Users/moritzlichter/Desktop/EvalHaskell/allNew.csv");
    int id2 ;//= comp.readFile("/Users/moritzlichter/Developer/Java/Eclipse workspaces/HiwiSE/layout-parsing2/test-hackage/all1391507689397.csv");
    
    int id1 = comp.readFile("/Users/moritzlichter/Developer/Java/Eclipse workspaces/HiwiSE/layout-parsing2/test-hackage/all1391637565696.csv");
   // int id1 = comp.readFile("all1380654578736/buildbox.csv");
    
  /*  LinkedList<Pair<FileResult, FileResult>> diff = comp.listFilesDiffParsed(id2, id1);
    Collections.sort(diff, new Comparator<Pair<FileResult, FileResult>>() {

      @Override
      public int compare(Pair<FileResult, FileResult> o1,
          Pair<FileResult, FileResult> o2) {
       return (o1.first.pkg + o1.first.path).compareTo(o2.first.pkg + o2.first.path);
      }
    });
    int none = 0;
    for (Pair<FileResult, FileResult> p : diff) {
      if (p.second != null ) {
        
        System.out.print(p.first.pkg + " - " + p.first.path + " - "
            + p.first.allSuccess + " - ");
       
          System.out.println(p.second.allSuccess);
        } else {
       //   System.out.println(p.first.pkg + " - " + p.first.path + " - " + p.first.allSuccess + " NONE");
          none++;
        }
      
    }
    System.out.println("None: " + none);
    */
        /*
    
    LinkedList<FileResult> ambigious = comp.filter(id1, new Filter() {

      @Override
      public boolean filter(FileResult r) {
        return r.ambiguities.t1 != 0 || r.ambiguities.t2 != 0|| r.ambiguities.t3 != 0; 
      }
      
    });
    sort(ambigious);
    print(ambigious,100);*/
    
    LinkedList<FileResult> notOk = comp.filter(id1, new Filter() {
      
      @Override
      public boolean filter(FileResult r) {
         return  r.cppPreprocess && r.makeExplicitLayout && r.makeImplicitLayout && !r.allSuccess;
      }
    });
    sort(notOk);
    print(notOk,-1);
 /*   LinkedList<FileResult> diffs2 = comp.listFilesWithImplExplDifference(id2);
    LinkedList<FileResult> diffs1 = comp.listFilesWithImplExplDifference(id1);
    LinkedList<FileResult> diff = intersectLists(diffs1, diffs2);
    sort(diffs2);
    print(diffs2,1000);*/
    
  }

  private static LinkedList<FileResult> intersectLists(LinkedList<FileResult> diffs2,
      LinkedList<FileResult> diffs1) {
    LinkedList<FileResult> result = new LinkedList<FileResult>();
    for (FileResult r : diffs2) {
      boolean found = false;
      FileIdentifier comp = new FileIdentifier(r);
      for (FileResult r2 : diffs1) {
        if (comp.equals(new FileIdentifier(r2))) {
          found = true;
          break;
        }
      }
      if (!found) {
        result.add(r);
      }
    }
    return result;
  }

}
