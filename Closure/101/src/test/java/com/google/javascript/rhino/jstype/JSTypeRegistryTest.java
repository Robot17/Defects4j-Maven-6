/*
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Nick Santos
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.SimpleErrorReporter;

import junit.framework.TestCase;

/**
 * Tests {@link JSTypeRegistry}.
 *
*
 */
public class JSTypeRegistryTest extends TestCase {
  // TODO(user): extend this class with more tests, as JSTypeRegistry is
  // now much larger
  public void testGetBuiltInType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    assertEquals(typeRegistry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        typeRegistry.getType("boolean"));
  }

  public void testGetDeclaredType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "Foo";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));

    // Ensure different instances are independent.
    JSTypeRegistry typeRegistry2 = new JSTypeRegistry(null);
    assertEquals(null, typeRegistry2.getType(name));
    assertEquals(type, typeRegistry.getType(name));
  }

  public void testGetDeclaredTypeInNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));
    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
  }

  public void testTypeAsNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);

    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));

    type = typeRegistry.createAnonymousObjectType();
    name = "a.b.Foo.Bar";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));

    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
    assertTrue(typeRegistry.hasNamespace("a.b.Foo"));
  }

  public void testGenerationIncrementing() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    final JSTypeRegistry typeRegistry = new JSTypeRegistry(reporter);

    StaticScope<JSType> scope = new StaticScope<JSType>() {
          public StaticSlot<JSType> getSlot(final String name) {
            return new SimpleSlot(
                name,
                typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
                false);
          }
          public StaticSlot<JSType> getOwnSlot(String name) {
            return getSlot(name);
          }
          public StaticScope<JSType> getParentScope() { return null; }
          public JSType getTypeOfThis() { return null; }
        };

    ObjectType namedType =
        (ObjectType) typeRegistry.getType(scope, "Foo", null, 0, 0);
    ObjectType subNamed =
        typeRegistry.createObjectType(typeRegistry.createObjectType(namedType));

    // Subclass of named type is initially unresolved.
    typeRegistry.setLastGeneration(false);
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    // Subclass of named type is still unresolved, even though the named type is
    // now present in the registry.
    typeRegistry.declareType("Foo", typeRegistry.createAnonymousObjectType());
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    assertNull("Unexpected errors: " + reporter.errors(),
        reporter.errors());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    // After incrementing the generation, resolve works again.
    typeRegistry.incrementGeneration();
    typeRegistry.setLastGeneration(true);
    typeRegistry.resolveTypesInScope(scope);
    assertFalse(subNamed.isUnknownType());
  }
}
