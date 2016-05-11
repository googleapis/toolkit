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
package com.google.api.codegen.php;

import com.google.api.codegen.ApiConfig;
import com.google.api.codegen.GapicContext;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.common.collect.ImmutableSet;

/**
 * A GapicContext specialized for PHP.
 */
public class PhpGapicContext extends GapicContext implements PhpContext {

  private PhpContextCommon phpCommon;

  public PhpGapicContext(Model model, ApiConfig apiConfig) {
    super(model, apiConfig);
  }

  @Override
  public void resetState(PhpSnippetSet<?> phpSnippetSet, PhpContextCommon phpCommon) {
    this.phpCommon = phpCommon;
  }

  // Snippet Helpers
  // ===============

  /**
   * Returns the PHP filename which holds the gRPC service definition.
   */
  public String getGrpcFilename(Interface service) {
    return service.getFile().getProto().getName().replace(".proto", "_services");
  }

  public String getTypeName(String typeName) {
    int lastBackslashIndex = typeName.lastIndexOf('\\');
    if (lastBackslashIndex < 0) {
      throw new IllegalArgumentException("expected fully qualified name");
    }
    String shortTypeName = typeName.substring(lastBackslashIndex + 1);
    return phpCommon.getMinimallyQualifiedName(typeName, shortTypeName);
  }

  /**
   * Adds the given type name to the import list. Returns an empty string so that the output is not
   * affected in snippets.
   */
  public String addImport(String typeName) {
    // used for its side effect of adding the type to the import list if the short name
    // hasn't been imported yet
    getTypeName(typeName);
    return "";
  }

  public String getServiceTitle(Interface service) {
    // TODO get the title from the service yaml file
    return "";
  }

  public String getServiceName(Interface service) {
    return service.getFullName();
  }

  /**
   * Gets the PHP package for the given proto file.
   */
  public String getPhpPackage(ProtoFile file) {
    return file.getProto().getPackage().replaceAll("\\.", "\\");
  }

  // Constants
  // =========

  /**
   * A set of PHP keywords and built-ins.
   * keywords: http://php.net/manual/en/reserved.keywords.php
   */
  private static final ImmutableSet<String> KEYWORD_BUILT_IN_SET =
      ImmutableSet.<String>builder()
      .add("__halt_compiler",
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
