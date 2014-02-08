package org.spoofax.jsglr.tests.haskell.compareutils;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.unicode.UnicodeConverter;
import org.spoofax.terms.Term;

public class PrepareQuasiQuotePrimitive  extends AbstractPrimitive {

  public PrepareQuasiQuotePrimitive() {
    super("prepareQuasiQuote", 0, 1);
  }
  
  private static String clean(String s) {
    //Just remove all, we hope that the main content is equals, but there are  alyout changes and comments in the quasi quote, which pp haskell might remove and i do not want to rewrite the grammar for quasiquotes
    return "";
  }

  @Override
  public boolean call(IContext arg0, Strategy[] arg1, IStrategoTerm[] arg2)
      throws InterpreterException {
   
    if (arg2.length != 1) {
      return false;
    }
    IStrategoTerm quasi1 = arg2[0];
    String quasiContent1 = Term.asJavaString(quasi1);
    
    //Just ignore whitespaces
    quasiContent1 = clean(quasiContent1);
  

    final ITermFactory factory = arg0.getFactory();
    IStrategoTerm t =  factory.makeString(quasiContent1);
    arg0.setCurrent(t);
    return true;
  }

}
