/**
 * 
 */
package org.spoofax.jsglr.tests.result;

/**
 * @author seba
 * 
 */
public class DataPoint<T> {
  public T t1;
  public T t2;
  public T t3;

  public DataPoint() {
  }

  public DataPoint(T t) {
    t1 = t;
    t2 = t;
    t3 = t;
  }

  public DataPoint(T t1, T t2, T t3) {
    this.t1 = t1;
    this.t2 = t2;
    this.t3 = t3;
  }

  public T get(int run) {
    switch (run) {
    case 1:
      return t1;
    case 2:
      return t2;
    case 3:
      return t3;
    }

    throw new IllegalArgumentException();
  }

  public void set(T val, int run) {
    switch (run) {
    case 1:
      t1 = val;
      break;
    case 2:
      t2 = val;
      break;
    case 3:
      t3 = val;
      break;
    default:
      throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {
    return "DataPoint [t1=" + t1 + ", t2=" + t2 + ", t3=" + t3 + "]";
  }
  
  
}
