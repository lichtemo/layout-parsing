package org.spoofax.jsglr_layout.client.indentation;

/**
 * @author Sebastian Erdweg <seba at informatik uni-marburg de>
 *
 */
public class DefaultLayoutRule implements ILayoutRule {
  public int newIndent(int oldIndent) {
    return 1;
  }
}