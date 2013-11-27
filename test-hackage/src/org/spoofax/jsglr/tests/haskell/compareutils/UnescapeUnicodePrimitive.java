package org.spoofax.jsglr.tests.haskell.compareutils;

import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.Arrays;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.unicode.UnicodeConverter;
import org.spoofax.terms.Term;

public class UnescapeUnicodePrimitive extends AbstractPrimitive {

  public UnescapeUnicodePrimitive() {
    super("unescapeUnicode", 0, 1);
  }

  @Override
  public boolean call(IContext arg0, Strategy[] arg1, IStrategoTerm[] arg2)
      throws InterpreterException {
   
    if (arg2.length == 0) {
      return false;
    }
    IStrategoTerm intValue = arg2[0];
    if (!Term.isTermString(intValue)) {
      return false;
    }
    String number =  Term.asJavaString(intValue);
    int unicodeValue;
    try {
      unicodeValue = Integer.parseInt(number);
    } catch(NumberFormatException e) {
      return false;
    }
    // Validate that it is actuall unicode
    if (!UnicodeConverter.isUnicode(unicodeValue)) {
      return false;
    }
    String unescaped = UnicodeConverter.unicodeNumberToString(unicodeValue);
    //Return the unescaped
    final ITermFactory factory = arg0.getFactory();
    IStrategoTerm t =  factory.makeString(unescaped);
    arg0.setCurrent(t);
    return true;
  }

}
