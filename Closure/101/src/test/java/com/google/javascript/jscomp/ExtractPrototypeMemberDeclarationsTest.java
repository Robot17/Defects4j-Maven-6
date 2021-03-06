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

/**
 * Tests for {@link ExtractPrototypeMemberDeclarations}.
 * 
*
 */
public class ExtractPrototypeMemberDeclarationsTest extends CompilerTestCase {
  private static final String TMP = "a";
  
  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ExtractPrototypeMemberDeclarations(compiler);
  }
  
  public void testNotEnoughPrototypeToExtract() {
    // switch statement with stuff after "return"
    for (int i = 0; i < 7; i++) {
      testSame(generatePrototypeDeclarations("x", i));
    }
  }
  
  public void testExtractingSingleClassPrototype() { 
    extract(generatePrototypeDeclarations("x", 7), 
        loadPrototype("x") +
        generateExtractedDeclarations(7));
  }

  public void testExtractingTwoClassPrototype() { 
    extract(
        generatePrototypeDeclarations("x", 6) +
        generatePrototypeDeclarations("y", 6),
        loadPrototype("x") +
        generateExtractedDeclarations(6) +
        loadPrototype("y") +
        generateExtractedDeclarations(6));
  }

  public void testExtractingTwoClassPrototypeInDifferentBlocks() { 
    extract(
        generatePrototypeDeclarations("x", 6) +
        "if (foo()) {" +
        generatePrototypeDeclarations("y", 6) +
        "}",
        loadPrototype("x") +
        generateExtractedDeclarations(6) +
        "if (foo()) {" +
        loadPrototype("y") +
        generateExtractedDeclarations(6) +
        "}");
  }  
  
  public void testNoMemberDeclarations() { 
    testSame(
        "x.prototype = {}; x.prototype = {}; x.prototype = {};" +
        "x.prototype = {}; x.prototype = {}; x.prototype = {};" +
        "x.prototype = {}; x.prototype = {}; x.prototype = {};");
  }
  
  public void testExtractingPrototypeWithQName() { 
    extract(
        generatePrototypeDeclarations("com.google.javascript.jscomp.x", 7), 
        loadPrototype("com.google.javascript.jscomp.x") +
        generateExtractedDeclarations(7));
  }

  public void testInterweaved() {
    testSame(
        "x.prototype.a=1; y.prototype.a=1;" +
        "x.prototype.b=1; y.prototype.b=1;" +
        "x.prototype.c=1; y.prototype.c=1;" +
        "x.prototype.d=1; y.prototype.d=1;" +
        "x.prototype.e=1; y.prototype.e=1;" +
        "x.prototype.f=1; y.prototype.f=1;");
  }

  public void testExtractingPrototypeWithNestedMembers() {
    extract(
        "x.prototype.y.a = 1;" +
        "x.prototype.y.b = 1;" +
        "x.prototype.y.c = 1;" +
        "x.prototype.y.d = 1;" +
        "x.prototype.y.e = 1;" +
        "x.prototype.y.f = 1;" +
        "x.prototype.y.g = 1;",
        loadPrototype("x") +
        TMP + ".y.a = 1;" +
        TMP + ".y.b = 1;" +
        TMP + ".y.c = 1;" +
        TMP + ".y.d = 1;" +
        TMP + ".y.e = 1;" +
        TMP + ".y.f = 1;" +
        TMP + ".y.g = 1;");
  }
  
  public void testUsedNameInScope() {
    test(
        "var a = 0;" +
        "x.prototype.y.a = 1;" +
        "x.prototype.y.b = 1;" +
        "x.prototype.y.c = 1;" +
        "x.prototype.y.d = 1;" +
        "x.prototype.y.e = 1;" +
        "x.prototype.y.f = 1;" +
        "x.prototype.y.g = 1;",
        "var b;" +
        "var a = 0;" +
        "b = x.prototype;" +
        "b.y.a = 1;" +
        "b.y.b = 1;" +
        "b.y.c = 1;" +
        "b.y.d = 1;" +
        "b.y.e = 1;" +
        "b.y.f = 1;" +
        "b.y.g = 1;");
  }

  public String loadPrototype(String qName) {
    return TMP + " = " + qName + ".prototype;";
  }
  
  public void extract(String src, String expected) {
    test(src, "var " + TMP + ";" + expected);
  }
  
  public String generatePrototypeDeclarations(String className, int num) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < num; i++) {
      char member = (char) ('a' + i);
      builder.append(generatePrototypeDeclaration(
          className, "" + member,  "" + member));
    }
    return builder.toString();
  }
  
  public String generatePrototypeDeclaration(String className, String member, 
      String value) {
    return className + ".prototype." + member + " = " + value + ";";
  }
  
  public String generateExtractedDeclarations(int num) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < num; i++) {
      char member = (char) ('a' + i);
      builder.append(generateExtractedDeclaration("" + member,  "" + member));
    }
    return builder.toString();
  }
  
  public String generateExtractedDeclaration(String member, String value) {
    return TMP + "." + member + " = " + value + ";";
  }
}
