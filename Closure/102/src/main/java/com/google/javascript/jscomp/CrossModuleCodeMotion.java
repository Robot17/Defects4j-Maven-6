/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A {@link Compiler} pass for moving code to a deeper module if possible.
 * - currently it only moves functions + variables
 *
*
 */
class CrossModuleCodeMotion extends AbstractPostOrderCallback
    implements CompilerPass {

  private static final Logger logger =
      Logger.getLogger(CrossModuleCodeMotion.class.getName());

  private final AbstractCompiler compiler;
  private final JSModuleGraph graph;

  /**
   * Map from module to the node in that module that should parent any string
   * variable declarations that have to be moved into that module
   */
  private final Map<JSModule, Node> moduleVarParentMap =
      new HashMap<JSModule, Node>();

  /*
   * NOTE - I made this a LinkedHashMap to make testing easier. With a regular
   * HashMap, the variables may not output in a consistent order
   */
  private final Map<Scope.Var, NamedInfo> namedInfo =
      new LinkedHashMap<Var, NamedInfo>();

  /**
   * Creates an instance.
   *
   * @param compiler The compiler
   */
  CrossModuleCodeMotion(AbstractCompiler compiler, JSModuleGraph graph) {
    this.compiler = compiler;
    this.graph = graph;
  }

  /**
   * {@inheritDoc}
   */
  public void process(Node externs, Node root) {
    logger.info("Moving functions + variable into deeper modules");

    // If there are <2 modules, then we will never move anything, so we're done
    if (graph != null && graph.getModuleCount() > 1) {

      // Traverse the tree and find the modules where a var is declared + used
      NodeTraversal.traverse(compiler, root, this);

      // Move the functions + variables to a deeper module [if possible]
      moveCode();
    }
  }

  /** move the code accordingly */
  private void moveCode() {
    for (Map.Entry<Var, NamedInfo> e : namedInfo.entrySet()) {
      NamedInfo info = e.getValue();

      JSModule deepestDependency = info.deepestModule;

      // Only move if all are true:
      // a) allowMove is true
      // b) it was used + declared somewhere [if not, then it will be removed
      // as dead or invalid code elsewhere]
      // c) the new dependency depends on the declModule
      if (info.allowMove && deepestDependency != null) {
        Iterator<Declaration> it = info.declarationIterator();
        JSModuleGraph moduleGraph = compiler.getModuleGraph();
        while (it.hasNext()) {
          Declaration decl = it.next();
          if (decl.module != null &&
              moduleGraph.dependsOn(deepestDependency,
                  decl.module)) {

            // Find the appropriate spot to move it to
            Node destParent = moduleVarParentMap.get(deepestDependency);
            if (destParent == null) {
              destParent = compiler.getNodeForCodeInsertion(deepestDependency);
              moduleVarParentMap.put(deepestDependency, destParent);
            }

            // Nodes which are 1 of many children of a VAR need to be moved
            // carefully. We must dissect them out of the VAR, and create a new
            // VAR to hold them.
            Node declParent = decl.node.getParent();
            if (declParent.getType() == Token.VAR &&
                declParent.getChildCount() > 1) {
              declParent.removeChild(decl.node);

              // Make a new node
              Node var = new Node(Token.VAR, decl.node);
              destParent.addChildToFront(var);
            } else {
              // Remove it
              declParent.detachFromParent();

              // Add it to the new spot
              destParent.addChildToFront(declParent);
            }

            compiler.reportCodeChange();
          }
        }
      }
    }
  }

  /** useful information for each variable candidate */
  private class NamedInfo {
    boolean allowMove = true;

    // The deepest module where the variable is used. Starts at null
    private JSModule deepestModule = null;

    // The module where declarations appear
    private JSModule declModule = null;

    // information on the spot where the item was declared
    private final Deque<Declaration> declarations =
        new ArrayDeque<Declaration>();

    // Add a Module where it is used
    void addUsedModule(JSModule m) {
      // If we are not allowed to move it, all bets are off
      if (!allowMove) {
        return;
      }

      // If we have no deepest module yet, set this one
      if (deepestModule == null) {
        deepestModule = m;
      } else {
        // Find the deepest common dependency
        deepestModule =
            graph.getDeepestCommonDependencyInclusive(m, deepestModule);
      }
    }

    /**
     * Add a declaration for this name.
     * @return Whether this is a valid declaration. If this returns false,
     *    this should be added as a reference.
     */
    boolean addDeclaration(Declaration d) {
      // all declarations must appear in the same module.
      if (declModule != null && d.module != declModule) {
        return false;
      }
      declarations.push(d);
      declModule = d.module;
      return true;
    }

    /**
     * Returns an iterator over the declarations, in the order that they were
     * declared.
     */
    Iterator<Declaration> declarationIterator() {
      return declarations.iterator();
    }
  }

  private class Declaration {
    final JSModule module;
    final Node node;

    Declaration(JSModule module, Node node, Node parent, Node gramps) {
      this.module = module;
      this.node = node;
    }
  }

  /**
   * return true if the node has any form of conditional in its ancestry
   * TODO(nicksantos) keep track of the conditionals in the ancestory, so
   * that we don't have to recrawl it.
   */
  private boolean hasConditionalAncestor(Node n) {
    for (Node ancestor : n.getAncestors()) {
      switch (ancestor.getType()) {
        case Token.DO:
        case Token.FOR:
        case Token.HOOK:
        case Token.IF:
        case Token.SWITCH:
        case Token.WHILE:
          return true;
      }
    }
    return false;
  }

  /**
   * get the information on a variable
   */
  private NamedInfo getNamedInfo(Var v) {
    NamedInfo info = namedInfo.get(v);
    if (info == null) {
      info = new NamedInfo();
      namedInfo.put(v, info);
    }
    return info;
  }

  /**
   * Process the references to named variables
   */
  private void processReference(NodeTraversal t, NamedInfo info, String name) {
    // Random tidbit: A recursive function should not block movement.
    // If the inlineName matches the scope function which contains it,
    // we can ignore the module [this one time].
    boolean recursive = false;
    Node rootNode = t.getScope().getRootNode();
    if (rootNode.getType() == Token.FUNCTION) {
      String scopeFuncName = rootNode.getFirstChild().getString();
      if (scopeFuncName.equals(name)) {
        recursive = true;
      }
    }

    if (!recursive) {
      info.addUsedModule(t.getModule());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void visit(NodeTraversal t, Node n, Node parent) {
    if (n.getType() != Token.NAME) {
      return;
    }

    // Skip empty and exported names
    String name = n.getString();
    if (name.isEmpty() || compiler.getCodingConvention().isExported(name)) {
      return;
    }

    // If the JSCompiler can't find a Var for this string, then all
    // bets are off. This sometimes occurs with closures. Alternately, we skip
    // non-global variables
    Var v = t.getScope().getVar(name);
    if (v == null || !v.isGlobal()) {
      return;
    }

    NamedInfo info = getNamedInfo(v);
    if (info.allowMove) {
      if (maybeProcessDeclaration(t, n, parent, info)) {
        if (hasConditionalAncestor(n)) {
          info.allowMove = false;
        }
      } else {
        // Otherwise, it's a reference
        processReference(t, info, name);
      }
    }
  }

  /**
   * Determines whether the given NAME node belongs to a delcaration that
   * can be moved across modules. If it is, registers it properly.
   *
   * There are four types of movable declarations:
   * 1) var NAME = [movable object];
   * 2) function NAME() {}
   * 3) NAME = [movable object];
   *    NAME.prop = [movable object];
   *    NAME.prop.prop2 = [movable object];
   *    etc.
   * 4) Class-defining function calls, like "inherits" and "mixin".
   *    NAME.inherits([some other name]);
   * where "movable object" is a literal or a function.
   */
  private boolean maybeProcessDeclaration(NodeTraversal t, Node name,
      Node parent, NamedInfo info) {
    Node gramps = parent.getParent();
    switch (parent.getType()) {
      case Token.VAR:
        if (canMoveValue(name.getFirstChild())) {
          return info.addDeclaration(
              new Declaration(t.getModule(), name, parent, gramps));
        }
        return false;

      case Token.FUNCTION:
        if (NodeUtil.isFunctionDeclaration(parent)) {
          return info.addDeclaration(
              new Declaration(t.getModule(), name, parent, gramps));
        }
        return false;

      case Token.ASSIGN:
      case Token.GETPROP:
        Node child = name;

        // Look for assignment expressions where the name is the root
        // of a qualified name on the left hand side of the assignment.
        for (Node current : name.getAncestors()) {
          if (current.getType() == Token.GETPROP) {
            // fallthrough
          } else if (current.getType() == Token.ASSIGN &&
                     current.getFirstChild() == child) {
            Node currentParent = current.getParent();
            if (NodeUtil.isExpressionNode(currentParent) &&
                canMoveValue(current.getLastChild())) {
              return info.addDeclaration(
                  new Declaration(t.getModule(), current, currentParent,
                      currentParent.getParent()));
            }
          } else {
            return false;
          }

          child = current;
        }
        return false;

      case Token.CALL:
        if (NodeUtil.isExprCall(gramps)) {
          SubclassRelationship relationship =
              compiler.getCodingConvention().getClassesDefinedByCall(parent);
          if (relationship != null &&
              name.getString().equals(relationship.subclassName)) {
            return info.addDeclaration(
                new Declaration(t.getModule(), parent, gramps,
                    gramps.getParent()));
          }
        }
        return false;

      default:
        return false;
    }
  }

  /**
   * Determines whether the given value is eligible to be moved across modules.
   */
  private boolean canMoveValue(Node n) {
    // the value is only movable if it's
    // a) nothing,
    // b) a constant literal,
    // c) a function, or
    // d) an array/object literal of movable values.
    // e) a function stub generated by CrossModuleMethodMotion.
    if (n == null || NodeUtil.isLiteralValue(n) ||
        n.getType() == Token.FUNCTION) {
      return true;
    } else if (n.getType() == Token.CALL) {
      Node functionName = n.getFirstChild();
      return functionName.getType() == Token.NAME &&
          (functionName.getString().equals(
              CrossModuleMethodMotion.STUB_METHOD_NAME) ||
           functionName.getString().equals(
              CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
    } else if (n.getType() == Token.ARRAYLIT ||
        n.getType() == Token.OBJECTLIT) {
      for (Node child = n.getFirstChild(); child != null;
           child = child.getNext()) {
        if (!canMoveValue(child)) {
          return false;
        }
      }

      return true;
    }

    return false;
  }
}
