/*
 * Copyright 2004 Google Inc.
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


public class RemoveUnusedVarsTest extends CompilerTestCase {

  private boolean removeGlobal = true;
  private boolean preserveFunctionExpressionNames = false;

  public RemoveUnusedVarsTest() {
    super("", false);
  }

  @Override
  public void setUp() {
    removeGlobal = true;
    preserveFunctionExpressionNames = false;
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new RemoveUnusedVars(
        compiler, removeGlobal, preserveFunctionExpressionNames);
  }

  public void testRemoveUnusedVars() {
    // Test lots of stuff
    test("var a;var b=3;var c=function(){};var x=A();var y; var z;" +
         "function A(){B()}; function B(){C(b)}; function C(){};" +
         "function X(){Y()}; function Y(z){Z(x)}; function Z(){y};" +
         "P=function(){A()};" +
         "try{0}catch(e){a}",

         "var a;var b=3;A();function A(){B()}" +
         "function B(){C(b)}" +
         "function C(){}" +
         "P=function(){A()}" +
         ";try{0}catch(e){a}");

    // Test removal from if {} blocks
    test("var i=0;var j=0;if(i>0){var k=1;}",
         "var i=0;if(i>0);");

    // Test with for loop
    test("for (var i in booyah) {" +
         "  if (i > 0) x += ', ';" +
         "  var arg = 'foo';" +
         "  if (arg.length > 40) {" +
         "    var unused = 'bar';" +   // this variable is unused
         "    arg = arg.substr(0, 40) + '...';" +
         "  }" +
         "  x += arg;" +
         "}",

         "for(var i in booyah){if(i>0)x+=\", \";" +
         "var arg=\"foo\";if(arg.length>40)arg=arg.substr(0,40)+\"...\";" +
         "x+=arg}");

    // Test with function expressions in another function call
    test("function A(){}" +
         "if(0){function B(){}}win.setTimeout(function(){A()})",
         "function A(){}" +
         "if(0);win.setTimeout(function(){A()})");

    // Test with recursive functions
    test("function A(){A()}function B(){B()}B()",
         "function B(){B()}B()");

    // Test with multiple var declarations.
    test("var x,y=2,z=3;A(x);B(z);var a,b,c=4;C()",
         "var x,z=3;A(x);B(z);C()");

    // Test with for loop declarations
    test("for(var i=0,j=0;i<10;){}" +
         "for(var x=0,y=0;;y++){}" +
         "for(var a,b;;){a}" +
         "for(var c,d;;);" +
         "for(var item in items){}",

         "for(var i=0;i<10;);" +
         "for(var y=0;;y++);" +
         "for(var a;;)a;" +
         "for(;;);" +
         "for(var item in items);");

    // Test multiple passes required
    test("var a,b,c,d;var e=[b,c];var x=e[3];var f=[d];print(f[0])",
         "var b,c,d;var f=[d];print(f[0])");

    // Test proper scoping (static vs dynamic)
    test("var x;function A(){var x;B()}function B(){print(x)}A()",
         "var x;function A(){B()}function B(){print(x)}A()");

    // Test closures in a return statement
    test("function A(){var x;return function(){print(x)}}A()",
         "function A(){var x;return function(){print(x)}}A()");

    // Test other closures, multiple passes
    test("function A(){}function B(){" +
         "var c,d,e,f,g,h;" +
         "function C(){print(c)}" +
         "var handler=function(){print(d)};" +
         "var handler2=function(){handler()};" +
         "e=function(){print(e)};" +
         "if(1){function G(){print(g)}}" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()",

         "function B(){" +
         "var d,e,f,h;" +
         "e=function(){print(e)};" +
         "if(1);" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()");

    // Test exported names
    test("var a,b=1; function _A1() {this.foo(a)}",
         "var a;function _A1(){this.foo(a)}");

    // Test undefined (i.e. externally defined) names
    test("undefinedVar = 1", "undefinedVar=1");

    // Test unused vars with side effects
    test("var a,b=foo(),c=i++,d;var e=boo();var f;print(d);",
         "var b=foo(),c=i++,d;boo();print(d)");

    test("var a,b=foo()", "foo()");
    test("var b=foo(),a", "foo()");
    test("var a,b=foo(a)", "var a,b=foo(a)");
  }

  public void testFunctionArgRemoval() {
    // remove all function arguments
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b(1,2)");

    // remove no function arguments
    testSame("var b=function(c,d){return c+d};b(1,2)");
    testSame("var b=function(e,f,c,d){return c+d};b(1,2)");

    // remove some function arguments
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(e,c,f,d){return c+d};b(1,2)");
  }

  public void testVarInControlStructure() {
    test("if (true) var b = 3;", "if(true);");
    test("if (true) var b = 3; else var c = 5;", "if(true);else;");
    test("while (true) var b = 3;", "while(true);");
    test("for (;;) var b = 3;", "for(;;);");
    test("do var b = 3; while(true)", "do;while(true)");
    test("with (true) var b = 3;", "with(true);");
    test("f: var b = 3;","");
  }

  public void testRValueHoisting() {
    test("var x = foo();", "foo()");
    test("var x = {a: foo()};", "({a:foo()})");

    test("var x=function y(){}", "");
  }

  public void testModule() {
    test(createModules(
             "var unreferenced=1; function x() { foo(); }" +
             "function uncalled() { var x; return 2; }",
             "var a,b; function foo() { this.foo(a); } x()"),
         new String[] {
           "function x(){foo()}",
           "var a;function foo(){this.foo(a)}x()"
         });
  }

  public void testRecursiveFunction1() {
    testSame("(function x(){return x()})()");
  }

  public void testRecursiveFunction2() {
    test("var x = 3; (function x() { return x(); })();",
         "(function x(){return x()})()");
  }

  public void testFunctionWithName1() {
    test("var x=function f(){};x()",
         "var x=function(){};x()");

    preserveFunctionExpressionNames = true;
    testSame("var x=function f(){};x()");
  }

  public void testFunctionWithName2() {
    test("foo(function bar(){})",
         "foo(function(){})");

    preserveFunctionExpressionNames = true;
    testSame("foo(function bar(){})");
  }

  public void testRemoveGlobal1() {
    removeGlobal = false;
    testSame("var x=1");
    test("var y=function(x){var z;}", "var y=function(){}");
  }

  public void testRemoveGlobal2() {
    removeGlobal = false;
    testSame("var x=1");
    test("function y(x){var z;}", "function y(){}");
  }

  public void testRemoveGlobal3() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}y()}",
         "function x(){function y(){}y()}");
  }

  public void testRemoveGlobal4() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}}",
         "function x(){}");
  }

  public void testIssue168a() {
    test("function _a(){" +
         "  (function(x){ _b(); })(1);" +
         "}" +
         "function _b(){" +
         "  _a();" +
         "}",
         "function _a(){(function(){_b()})(1)}" +
         "function _b(){_a()}");
  }

  public void testIssue168b() {
    removeGlobal = false;
    test("function a(){" +
         "  (function(x){ b(); })(1);" +
         "}" +
         "function b(){" +
         "  a();" +
         "}",
         "function a(){(function(){b()})(1)}" +
         "function b(){a()}");
  }

  public void testUnusedAssign1() {
    test("var x = 3; x = 5;", "5");
  }

  public void testUnusedAssign2() {
    test("function f(a) { a = 3; } this.x = f;",
        "function f(){3}this.x=f");
  }

  public void testUnusedAssign3() {
    // e can't be removed, so we don't try to remove the dead assign.
    // We might be able to improve on this case.
    test("try { throw ''; } catch (e) { e = 3; }",
        "try{throw\"\";}catch(e){e=3}");
  }

  public void testUnusedAssign4() {
    // b can't be removed, so a can't be removed either. We might be able
    // to improve on this case.
    test("function f(a, b) { this.foo(b); a = 3; } this.x = f;",
        "function f(a,b){this.foo(b);a=3}this.x=f");
  }

  public void testUnusedAssign5() {
    test("var z = function f() { f = 3; }; z();",
         "var z=function(){3};z()");
  }

  public void testUnusedAssign6() {
    test("var z; z = 3;", "3");
  }

  public void testUnusedPropAssign1() {
    test("var x = {}; x.foo = 3;", "3");
  }

  public void testUnusedPropAssign2() {
    test("var x = {}; x['foo'] = 3;", "\"foo\",3");
  }

  public void testUnusedPropAssign3() {
    test("var x = {}; x['foo'] = {}; x['foo'].baz['bar'] = 3",
        "\"foo\",{};\"foo\",\"bar\",3");
  }

  public void testUnusedPropAssign4() {
    test("var x = {foo: 3}; x['foo'] = 5;", "\"foo\",5");
  }

  public void testUnusedPropAssign5() {
    // Because bar() has a side-effect, the whole variable stays in. We might
    // be able to improve on this case.
    test("var x = {foo: bar()}; x['foo'] = 5;",
         "var x={foo:bar()};x[\"foo\"]=5");
  }

  public void testUsedPropAssign1() {
    test("function f(x) { x.bar = 3; } f({});",
         "function f(x){x.bar=3}f({})");
  }

  public void testUsedPropAssign2() {
    test("try { throw z; } catch (e) { e.bar = 3; }",
         "try{throw z;}catch(e){e.bar=3}");
  }

  public void testUsedPropAssign3() {
    // This pass does not do flow analysis.
    test("var x = {}; x.foo = 3; x = bar();",
         "var x={};x.foo=3;x=bar()");
  }

  public void testUsedPropAssign4() {
    test("var y = foo(); var x = {}; x.foo = 3; y[x.foo] = 5;",
         "var y=foo();var x={};x.foo=3;y[x.foo]=5");
  }

  public void testUsedPropAssign5() {
    test("var y = foo(); var x = 3; y[x] = 5;",
         "var y=foo();var x=3;y[x]=5");
  }

  public void testUsedPropAssign6() {
    test("var x = newNodeInDom(doc); x.innerHTML = 'new text';",
         "var x=newNodeInDom(doc);x.innerHTML=\"new text\"");
  }
}
