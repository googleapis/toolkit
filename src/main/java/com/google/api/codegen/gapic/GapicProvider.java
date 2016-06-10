/* Copyright 2016 Google Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.gapic;

import com.google.api.codegen.GeneratedResult;

import java.util.List;

/**
 * A GapicProvider performs code or fragment generation using on a proto-based Model for a
 * particular language.
 */
public interface GapicProvider<InputElementT> {

  /**
   * Returns the snippet files that this provider will use for code generation.
   */
  List<String> getSnippetFileNames();

  /**
   * Runs code generation and puts the output in outputPath.
   */
  void generate(String outputPath) throws Exception;

  /**
   * Generates code for a single snippet. Returns a map from service interface to code for the
   * service. Returns null if generation failed.
   */
  List<GeneratedResult> generateSnip(String snippetFileName);
}
