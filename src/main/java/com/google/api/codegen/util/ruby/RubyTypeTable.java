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
package com.google.api.codegen.util.ruby;

import com.google.api.codegen.util.DynamicLangTypeTable;
import com.google.api.codegen.util.NamePath;
import com.google.api.codegen.util.TypeAlias;
import com.google.api.codegen.util.TypeTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.TreeMap;

/** The TypeTable for Ruby. */
public class RubyTypeTable extends DynamicLangTypeTable {

  public RubyTypeTable(String implicitPackageName) {
    super(implicitPackageName);
  }

  @Override
  protected String getSeparator() {
    return "::";
  }

  @Override
  public TypeTable cloneEmpty() {
    return new RubyTypeTable(getImplicitPackageName());
  }

  @Override
  public NamePath getNamePath(String fullName) {
    return NamePath.doubleColoned(fullName);
  }

  @Override
  public Map<String, TypeAlias> getImports() {
    TreeMap<TypeAlias, String> inverseMap = new TreeMap<>(TypeAlias.getNicknameComparator());
    inverseMap.putAll(getImportsBimap().inverse());
    return HashBiMap.create(inverseMap).inverse();
  }

  /**:
   * A set of ruby keywords and built-ins. keywords:
   * http://docs.ruby-lang.org/en/2.3.0/keywords_rdoc.html
   */
  private static final ImmutableSet<String> KEYWORD_BUILT_IN_SET =
      ImmutableSet.<String>builder()
          .add(
              "__ENCODING__",
              "__LINE__",
              "__FILE__",
              "BEGIN",
              "END",
              "alias",
              "and",
              "begin",
              "break",
              "case",
              "class",
              "def",
              "defined?",
              "do",
              "else",
              "elsif",
              "end",
              "ensure",
              "false",
              "for",
              "if",
              "in",
              "module",
              "next",
              "nil",
              "not",
              "or",
              "redo",
              "rescue",
              "retry",
              "return",
              "self",
              "super",
              "then",
              "true",
              "undef",
              "unless",
              "until",
              "when",
              "while",
              "yield",
              // "options" is here because it's a common keyword argument to
              // specify a CallOptions instance.
              "options")
          .build();
}
