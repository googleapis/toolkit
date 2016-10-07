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
package com.google.api.codegen.util.php;

import com.google.api.codegen.util.DynamicLangTypeTable;
import com.google.api.codegen.util.NamePath;
import com.google.api.codegen.util.TypeAlias;
import com.google.api.codegen.util.TypeTable;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * The TypeTable for PHP.
 */
public class PhpTypeTable extends DynamicLangTypeTable {

  public PhpTypeTable(String implicitPackageName) {
    super(implicitPackageName);
  }

  @Override
  protected String getSeparator() {
    return "\\";
  }

  @Override
  public TypeTable cloneEmpty() {
    return new PhpTypeTable(getImplicitPackageName());
  }

  @Override
  public NamePath getNamePath(String fullName) {
    return NamePath.backslashed(fullName);
  }

  @Override
  public Map<String, TypeAlias> getImports() {
    Map<String, TypeAlias> imports = super.getImports();
    // Clean up the imports.
    Map<String, TypeAlias> cleanedImports = new TreeMap<>();
    // Imported type is in package, can be ignored.
    for (String imported : imports.keySet()) {
      if (!getImplicitPackageName().isEmpty() && imported.startsWith(getImplicitPackageName())) {
        if (!imported.substring(getImplicitPackageName().length() + 1).contains(getSeparator())) {
          continue;
        }
      }
      cleanedImports.put(imported, imports.get(imported));
    }
    return cleanedImports;
  }

  /**
   * A set of PHP keywords and built-ins. keywords: http://php.net/manual/en/reserved.keywords.php
   */
  private static final ImmutableSet<String> KEYWORD_BUILT_IN_SET =
      ImmutableSet.<String>builder()
          .add(
              "__halt_compiler",
              "abstract",
              "and",
              "array",
              "as",
              "break",
              "callable",
              "case",
              "catch",
              "class",
              "clone",
              "const",
              "continue",
              "declare",
              "default",
              "die",
              "do",
              "echo",
              "else",
              "elseif",
              "empty",
              "enddeclare",
              "endfor",
              "endforeach",
              "endif",
              "endswitch",
              "endwhile",
              "eval",
              "exit",
              "extends",
              "final",
              "finally",
              "for",
              "foreach",
              "function",
              "global",
              "goto",
              "if",
              "implements",
              "include",
              "include_once",
              "instanceof",
              "insteadof",
              "interface",
              "isset",
              "list",
              "namespace",
              "new",
              "or",
              "print",
              "private",
              "protected",
              "public",
              "require",
              "require_once",
              "return",
              "static",
              "switch",
              "throw",
              "trait",
              "try",
              "unset",
              "use",
              "var",
              "while",
              "xor",
              "yield",
              "__CLASS__",
              "__DIR__",
              "__FILE__",
              "__FUNCTION__",
              "__LINE__",
              "__METHOD__",
              "__NAMESPACE__",
              "__TRAIT__")
          .build();
}
