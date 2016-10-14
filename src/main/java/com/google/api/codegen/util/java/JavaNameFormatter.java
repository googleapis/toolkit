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
package com.google.api.codegen.util.java;

import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.NameFormatter;
import com.google.api.codegen.util.NamePath;

/**
 * The NameFormatter for Java.
 */
public class JavaNameFormatter implements NameFormatter {

  private String wrapIfKeywordOrBuiltIn(String name) {
    if (JavaTypeTable.RESERVED_IDENTIFIER_SET.contains(name)) {
      return name + "_";
    } else {
      return name;
    }
  }

  @Override
  public String className(Name name) {
    return name.toUpperCamel();
  }

  @Override
  public String localVarName(Name name) {
    return wrapIfKeywordOrBuiltIn(name.toLowerCamel());
  }

  @Override
  public String privateFieldName(Name name) {
    return wrapIfKeywordOrBuiltIn(name.toLowerCamel());
  }

  @Override
  public String publicFieldName(Name name) {
    return wrapIfKeywordOrBuiltIn(name.toLowerCamel());
  }

  @Override
  public String varReference(Name name) {
    return localVarName(name);
  }

  @Override
  public String publicMethodName(Name name) {
    return wrapIfKeywordOrBuiltIn(name.toLowerCamel());
  }

  @Override
  public String privateMethodName(Name name) {
    return wrapIfKeywordOrBuiltIn(name.toLowerCamel());
  }

  @Override
  public String staticFunctionName(Name name) {
    return wrapIfKeywordOrBuiltIn(name.toLowerCamel());
  }

  @Override
  public String inittedConstantName(Name name) {
    return name.toUpperUnderscore();
  }

  @Override
  public String keyName(Name name) {
    return name.toLowerCamel();
  }

  @Override
  public String qualifiedName(NamePath namePath) {
    return namePath.toDotted();
  }

  @Override
  public String packageFilePathPiece(Name name) {
    return name.toOriginal();
  }

  @Override
  public String classFileNameBase(Name name) {
    return name.toOriginal();
  }
}
