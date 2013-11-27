package org.spoofax.jsglr.tests.haskell.compareutils;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class CompareUtilsLibrary extends AbstractStrategoOperatorRegistry {
  
  public CompareUtilsLibrary() {
    this.add(new UnescapeUnicodePrimitive());
  }

  @Override
  public String getOperatorRegistryName() {
    return this.getClass().getName();
  }

}
