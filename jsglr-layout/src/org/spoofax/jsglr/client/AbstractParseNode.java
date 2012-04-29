/*
 * Created on 30.mar.2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
 *
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public abstract class AbstractParseNode {

  public static final int PARSE_PRODUCTION_NODE = 1;
  public static final int PARSENODE = 2;
  public static final int AMBIGUITY = 3;
  public static final int PREFER = 4;
  public static final int AVOID = 5;
  public static final int REJECT = 6;
  public static final int CYCLE = 7;

  private final int line;
  private final int column;

  public AbstractParseNode(int line, int column) {
    this.line = line;
    this.column = column;
  }

  public final boolean isAmbNode() {
    return getNodeType() == AbstractParseNode.AMBIGUITY;
  }

  public final boolean isParseNode() {
    switch (getNodeType()) {
    case AbstractParseNode.PARSENODE:
    case AbstractParseNode.REJECT:
    case AbstractParseNode.PREFER:
    case AbstractParseNode.AVOID:
      return true;
    default:
      return false;
    }
  }

  public final boolean isParseRejectNode() {
    return getNodeType() == AbstractParseNode.REJECT;
  }

  public final boolean isParseProductionNode() {
    return getNodeType() == AbstractParseNode.PARSE_PRODUCTION_NODE;
  }

  public final boolean isCycle() {
    return getNodeType() == CYCLE;
  }

  public int getColumn() {
    return column;
  }

  public int getLine() {
    return line;
  }

  public abstract void reject();

  abstract public int getLabel();

  abstract public int getNodeType();

  abstract public AbstractParseNode[] getChildren();

  protected static final int NO_HASH_CODE = 0;

  public abstract Object toTreeBottomup(BottomupTreeBuilder builder);

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractParseNode))
      return false;
    
    return ((AbstractParseNode) obj).line == line && ((AbstractParseNode) obj).column == column;
  }

  @Override
  public int hashCode() {
    return line * 9197 + column;
  }

  abstract public String toStringShallow();

  @Override
  abstract public String toString();

  /**
   * Returns true if this node is in a parse production chain, i.e. it is
   * either: - a {@link ParseProductionNode}. - a ParseNode with a
   * {@link ParseProductionNode} child and an {@link #isParseProductionChain()}
   * child. - a ParseNode with a single {@link #isParseProductionChain()} child.
   * 
   * Implementations may also return true only for the topmost node of a parse
   * production chain.
   */
  public abstract boolean isParseProductionChain();

  /**
   * @return true iff this node does not contain any character data
   */
  public abstract boolean isEmpty();
  
  public abstract AbstractParseNode getLeft(ParseTable parseTable);
  
  public abstract boolean isLayout();
  
  public int getAmbiguityCount() {
    Stack<AbstractParseNode> nodes = new Stack<AbstractParseNode>();
    nodes.push(this);
    
    int amb = 0;
    
    while (!nodes.isEmpty()) {
      AbstractParseNode next = nodes.pop();
      if (next.isAmbNode())
        amb++;
      
      nodes.addAll(Arrays.asList(next.getChildren()));  
    }
    
    return amb;
  }

  public boolean ignoreIndent(ParseTable parseTable) {
    if (isAmbNode())
      return false;
    Label l = parseTable.getLabel(getLabel());
    if (l == null)
      return false;
    return l.getAttributes().isIgnoreIndent();
  }

  public final static int NEWLINE_LAYOUT = 2;
  public final static int NONEWLINE_LAYOUT = 1;
  public final static int OTHER_LAYOUT = 0;
  
  /**
   * @return NEWLINE_LAYOUT if node ends with layout that contains a newline
   * @return NONEWLINE_LAYOUT if node only contains layout character data (also true if node is empty)
   * @return OTHER_LAYOUT if node contains non-layout character data and does not end with layout that
   *           contains a newline 
   */
  public int getLayoutStatus() {
    if (isLayout() && hasNewline())
        return NEWLINE_LAYOUT;
    if (isLayout())
      return NONEWLINE_LAYOUT;
    
    /*
     * Elements of `nodes` are never descendant of a `isLayout()` node. 
     */
    LinkedList<AbstractParseNode> nodes = new LinkedList<AbstractParseNode>();
    nodes.addFirst(this);
    
    while (!nodes.isEmpty()) {
      AbstractParseNode node = nodes.pollFirst();
      
      if (node.isLayout()) {
        if (node.hasNewline())
          return NEWLINE_LAYOUT;
      }
      else if (node.isParseProductionNode())
        return OTHER_LAYOUT;
      else 
        for (int i = node.getChildren().length - 1; i >= 0; i--)
          nodes.addFirst(node.getChildren()[i]);
    }
    
    // this is not layout but also does not contain parse production nodes => treat like layout
    return NONEWLINE_LAYOUT;
  }
  
  private boolean hasNewline() {
    LinkedList<AbstractParseNode> nodes = new LinkedList<AbstractParseNode>();
    nodes.add(this);
    
    while (!nodes.isEmpty()) {
      AbstractParseNode node = nodes.poll();
      if (node.isParseProductionNode() && node.getLabel() == 10 || node.getLabel() == 13)
        return true;

      for (AbstractParseNode kid : node.getChildren())
        nodes.add(kid);
    }
    
    return false;
  }
}
