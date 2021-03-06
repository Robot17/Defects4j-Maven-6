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

package com.google.javascript.jscomp.sourcemap;

import com.google.javascript.rhino.Node;

import java.io.IOException;

/**
 * Collects information mapping the generated (compiled) source back to
 * its original source for debugging purposes
 *
 * @author johnlenz@google.com (John Lenz)
 */
public interface SourceMapGenerator {

  enum Format {
     LEGACY {
       @Override SourceMapGenerator getInstance() {
         return new SourceMapGeneratorV1();
       }
     },
     EXPERIMENTIAL {
       @Override SourceMapGenerator getInstance() {
         return new SourceMapGeneratorV2();
       }
     };
     abstract SourceMapGenerator getInstance();
  }

  /**
   * Appends the source map to the given buffer.
   *
   * @param out The stream to which the map will be appended.
   * @param name The name of the generated source file that this source map
   *   represents.
   */
  void appendTo(Appendable out, String name) throws IOException;

  /**
   * Resets the source map for reuse. A reset needs to be called between
   * each generated output file.
   */
  void reset();

  /**
   * Adds a mapping for the given node.  Mappings must be added in order.
   *
   * @param node The node that the new mapping represents.
   * @param startPosition The position on the starting line
   * @param endPosition The position on the ending line.
   */
  void addMapping(Node node, Position startPosition, Position endPosition);

  /**
   * Sets the prefix used for wrapping the generated source file before
   * it is written. This ensures that the source map is adjusted for the
   * change in character offsets.
   *
   * @param prefix The prefix that is added before the generated source code.
   */
  void setWrapperPrefix(String prefix);

  /**
   * Sets the source code that exists in the buffer for which the
   * generated code is being generated. This ensures that the source map
   * accurately reflects the fact that the source is being appended to
   * an existing buffer and as such, does not start at line 0, position 0
   * but rather some other line and position.
   *
   * @param offsetLine The index of the current line being printed.
   * @param offsetIndex The column index of the current character being printed.
   */
  void setStartingPosition(int offsetLine, int offsetIndex);

  /**
   * Whether to perform additional validation on the source map.
   * @param validate
   */
  void validate(boolean validate);

}
