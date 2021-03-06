/*
 * Copyright 2009 The Closure Compiler Authors.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.DefinitionsRemover.AssignmentDefinition;
import com.google.javascript.jscomp.DefinitionsRemover.Definition;
import com.google.javascript.jscomp.DefinitionsRemover.NamedFunctionDefinition;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.jscomp.graph.LinkedDirectedGraph;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A graph represents all the referencing of global names in the program. In
 * other words, it is a call and variable-name graph.
 *
 * <p>The NameReferenceGraph G for a program P is a directed graph G = (V, E)
 * where:
 *
 * <P>V ({@link Name}) represents all global names in P and E = (v, v'), v and
 * v' in V ({@link Reference} represents a reference use or definition from the
 * name v to v' in P.
 *
 * <p>There are two core results we are trying to compute. The first being able
 * to precisely identify the function body at any given call site with
 * {@link #getReferencesAt(Node)}.
 *
 * <p>The second result come directly from the previous one. The directed edge
 * provides us with dependency information. If A->B, B might be needed (in this
 * module) if A is needed (in this module). The converse of the this result is
 * more useful. B is not needed if A is not needed.
 *
 */
class NameReferenceGraph extends
    LinkedDirectedGraph<NameReferenceGraph.Name, NameReferenceGraph.Reference>
    implements DefinitionProvider {

  // This is the key result of the name graph. Given a node in the AST, this map
  // will give us the Reference edges. For example a CALL node will map to a
  // list of possible call edge destinations.
  private final Multimap<Node, Name>
      referenceMap = HashMultimap.create();

  // Given a qualified name, provides the Name object.
  private Map<String, Name> nameMap = Maps.newHashMap();

  // The following are some implicit nodes of the graph.

  // If we have a call site that we absolutely have no idea what variable it
  // it calls or reference, we'd point it to UKNOWN.
  final Name UNKNOWN;

  // Represents the "main" global block as well as externs.
  final Name MAIN;

  // The implict "window" object.
  final Name WINDOW;

  final AbstractCompiler compiler;

  public NameReferenceGraph(AbstractCompiler compiler) {
    this.compiler = compiler;

    // Initialize builtins.
    UNKNOWN = new Name("{UNKNOWN}", true);
    UNKNOWN.isAliased = true;
    UNKNOWN.type = compiler.getTypeRegistry().getNativeType(
        JSTypeNative.NO_TYPE);
    this.createNode(UNKNOWN);

    MAIN = new Name("{Global Main}", true);
    this.createNode(MAIN);

    WINDOW = new Name("window", true);
    this.createNode(WINDOW);
  }

  public Name defineNameIfNotExists(String name, boolean isExtern) {
    Name symbol = null;
    if (nameMap.containsKey(name)) {
      // This is a re-declaration.
      symbol = nameMap.get(name);
    } else {
      symbol = new Name(name, isExtern);
      nameMap.put(name, symbol);
      createNode(symbol);
    }
    return symbol;
  }

  /**
   * Retrieves a list of all possible Names that this site is referring to.
   */
  public List<Name> getReferencesAt(Node site) {
    Preconditions.checkArgument(
        NodeUtil.isGetProp(site) || NodeUtil.isName(site));
    List<Name> result = new ArrayList<Name>();
    for (Name target : referenceMap.get(site)) {
      result.add(target);
    }
    return result;
  }

  @Override
  public Collection<Definition> getDefinitionsReferencedAt(Node useSite) {
    List<Name> nameRefs = getReferencesAt(useSite);
    if (nameRefs.isEmpty()) {
      return null;
    }

    List<Definition> result = Lists.newArrayList();
    for (Name nameRef : nameRefs) {
      List<Definition> decls = nameRef.getDeclarations();
      if (!decls.isEmpty()) {
        result.addAll(decls);
      }
    }

    if (!result.isEmpty()) {
      return result;
    } else {
      return null;
    }
  }

  public Name getSymbol(String name) {
    return nameMap.get(name);
  }

  @Override
  public GraphNode<Name, Reference> createNode(Name value) {
    nameMap.put(value.qName, value);
    return super.createNode(value);
  }

  @Override
  public void connect(Name src, Reference ref, Name dest) {
    super.connect(src, ref, dest);
    referenceMap.put(ref.site, dest);
  }

  /**
   * Represents function or variable names that can be referenced globally.
   */
  class Name {
    // Full name
    private final String qName;

    private JSType type;

    // A list (re)declarations
    private List<Definition> declarations = Lists.newLinkedList();

    final boolean isExtern;

    private boolean isExported = false;

    private boolean isAliased = false;

    // Function invocations that use ".call" and ".apply" syntax may prevent
    // several of the possible optimizations.  We keep track of all functions
    // invoked in this way so those passes can exclude them.
    // Ex:
    // some_func.call(some_obj, 1, 2 , 3);
    // The name graph does not currently recognize this as a call to some_func.
    // This Set is meant to keep track of such occurrence until the name graph
    // becomes aware of those cases.
    private boolean exposedToCallOrApply = false;

    public Name(String qName, boolean isExtern) {
      this.qName = qName;
      this.isExtern = isExtern;
      int lastDot = qName.lastIndexOf('.');
      String name = (lastDot == -1) ? qName : qName.substring(lastDot + 1);
      this.isExported = compiler.getCodingConvention().isExported(name);
      this.type = compiler.getTypeRegistry().getNativeType(
          JSTypeNative.UNKNOWN_TYPE);
    }

    public JSType getType() {
      return type;
    }

    public void setType(JSType type) {
      this.type = type;
    }

    public List<Definition> getDeclarations() {
      return declarations;
    }

    public void addAssignmentDeclaration(Node node) {
      declarations.add(new AssignmentDefinition(node, isExtern));
    }

    public void addFunctionDeclaration(Node node) {
      declarations.add(new NamedFunctionDefinition(node, isExtern));
    }

    public boolean isExtern() {
      return isExtern;
    }

    public void markExported() {
      this.isExported = true;
    }

    public boolean isExported() {
      return isExported;
    }

    /** Removes all of the declarations of this name. */
    public final void remove() {
      for (Definition declaration : getDeclarations()) {
        declaration.remove();
      }
    }

    /**
     * @return {@code} True if this name has been dereferenced. Removing from
     *     the program or the module is no longer safe unless further analysis
     *     can prove otherwise.
     */
    public boolean isAliased() {
      return isAliased;
    }

    public void setAliased(boolean isAliased) {
      this.isAliased = isAliased;
    }

    public boolean hasSideEffect() {
      return isCallable();
    }

    public String getQualifiedName() {
      return qName;
    }

    /**
     * @return The short property name of this object if it is a property, else
     *     {@code null}.
     */
    public String getPropertyName() {
      int lastIndexOfDot = qName.lastIndexOf('.');
      if (lastIndexOfDot == -1) {
        return null;
      } else {
        return qName.substring(lastIndexOfDot + 1);
      }
    }

    public boolean isCallable() {
      return type.canBeCalled();
    }

    public boolean exposedToCallOrApply() {
      return exposedToCallOrApply;
    }

    public void markExposedToCallOrApply() {
      exposedToCallOrApply = true;
    }

    @Override
    public String toString() {
      return qName + " : " + type;
    }

    @Override
    public int hashCode() {
      return qName.hashCode();
    }

    /**
     * Return true if it's safe to change the signature of the function
     * references by this name. It is safe to change the signature if the Name
     * is:
     * <ul>
     * <li>callable</li>
     * <li>not an extern</li>
     * <li>not been aliased</li>
     * <li>not been exported</li>
     * <li>Referred by call or apply functions</li>
     * <li>The function uses the arguments property</li>
     * </ul>
     *
     * @return true if it's safe to change the signature of the name.
     */
    public boolean canChangeSignature() {
      // Ignore anything that is extern as they should not be changed.
      // Also skip over any non-function names. Finally if a function has been
      // alias, we don't know all of its callers and should not optimize.
      //
      // Also, if the function is called using .call or .apply, we don't try to
      // optimize those call because the name graph does not give us enough
      // information on the parameters.

      // TODO(user) We'll be able to remove the check for call or apply once
      // the name graph handles those call. The issue for now is that those
      // calls aren't edges in the graph, so we don't have enough information to
      // know ifit's safe to change the method's signature.
      return !(isExtern() ||
          !isCallable() ||
          isAliased() ||
          isExported() ||
          exposedToCallOrApply() ||
          nameUsesArgumentsProperty());
    }

    /**
     * Returns true if the the arguments property is used in any of the function
     * definition.
     * Ex. function foo(a,b,c) {return arguments.size;};
     * @return True is arguments is present in one of the definitions.
     */
    private boolean nameUsesArgumentsProperty() {
      for (Definition definition : getDeclarations()) {
        if (NodeUtil.isVarArgsFunction(definition.getRValue())) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * A reference site for a function or a variable reference. It can be a
   * reference use or an assignment to that name.
   */
  static class Reference {
    // The node that references the name.
    public final Node site;

    // Parent pointer.
    public final Node parent;

    private JSModule module = null;

    // A reference is unknown because we don't know the object's type.
    // If A.x->B.y in the name graph and the edge is unknown. It implies
    // A.x() reference to someObject.y and B.y MAY be the site.
    private boolean isUnknown = false;

    public Reference(Node site, Node parent) {
      this.site = site;
      this.parent = parent;
    }

    public boolean isUnknown() {
      return isUnknown;
    }

    public void setUnknown(boolean isUnknown) {
      this.isUnknown = isUnknown;
    }

    public JSModule getModule() {
      return module;
    }

    public void setModule(JSModule module) {
      this.module = module;
    }

    boolean isCall() {
      return NodeUtil.isCall(site);
    }

    /**
     * Get accessor for retrieving the actual node corresponding to the
     * reference.
     *
     * @return node representing the access/reference/call site
     */
    public Node getSite() {
      return site;
    }
  }
}
