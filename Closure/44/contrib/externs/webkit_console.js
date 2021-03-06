/*
 * Copyright 2009 Google Inc.
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

/**
 * @fileoverview Definitions for the Webkit console specification.
 * @see http://trac.webkit.org/browser/trunk/WebCore/page/Console.idl
 * @see http://trac.webkit.org/browser/trunk/WebCore/page/Console.cpp
 * @externs
 */

// TODO(nicksantos): We should delete this file and put this in
// the common externs.

/** @type {Console} */
var console;

// TODO(nicksantos): Tools that translate JS to Java blow up when there
// are properties called 'assert'. Ew.

/**
 * @param {*} condition
 * @param {...*} var_args
 */
Console.prototype.assert = function(condition, var_args) {};
