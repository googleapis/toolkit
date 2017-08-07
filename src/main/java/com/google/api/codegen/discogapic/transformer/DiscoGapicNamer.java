/* Copyright 2017 Google Inc
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
package com.google.api.codegen.discogapic.transformer;

import com.google.api.codegen.config.MethodConfig;
import com.google.api.codegen.config.MethodModel;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.NameFormatter;

/** Provides language-specific names for variables and classes. */
public class DiscoGapicNamer extends SurfaceNamer {
  private final SurfaceNamer languageNamer;
  private static final String regexDelimiter = "\\.";

  /* Create a JavaSurfaceNamer for a Discovery-based API. */
  public DiscoGapicNamer(SurfaceNamer parentNamer, NameFormatter formatter) {
    super(
        formatter,
        parentNamer.getSchemaTypeFormatter(),
        parentNamer.getTypeNameConverter(),
        parentNamer.getCommentReformatter(),
        parentNamer.getPackageName());
    this.languageNamer = parentNamer;
  }

  /* @return the underlying language surface namer. */
  public SurfaceNamer getLanguageNamer() {
    return languageNamer;
  }

  @Override
  /* The return type name in a static language for the given method. */
  public String getStaticLangReturnTypeName(MethodModel method, MethodConfig methodConfig) {
    return getNotImplementedString("idk yet");
  }

  /** Returns the variable name for a field. */
  public String getFieldVarName(String fieldName) {
    return privateFieldName(Name.anyCamel(fieldName));
  }

  /** Returns the resource getter method name for a resource field. */
  public String getResourceGetterName(String fieldName) {
    Name name;
    if (fieldName.contains("_")) {
      name = Name.anyCamel(fieldName.split("_"));
    } else {
      name = Name.anyCamel(fieldName);
    }
    return publicMethodName(Name.anyCamel("get").join(name));
  }
  /** Returns the resource getter method name for a resource field. */
  public String getResourceSetterName(String fieldName) {
    Name name;
    if (fieldName.contains("_")) {
      name = Name.anyCamel(fieldName.split("_"));
    } else {
      name = Name.anyCamel(fieldName);
    }
    return publicMethodName(Name.anyCamel("set").join(name));
  }

  /**
   * Returns the array of substrings after the input is split by periods. Ex: Input of
   * "compute.addresses.aggregatedList" returns the array: ["compute", "addresses", "List"].
   */
  public static Name methodAsName(Method method) {
    String[] pieces = method.id().split(regexDelimiter);
    Name result = Name.anyCamel(pieces[0]);
    for (int i = 1; i < pieces.length; i++) {
      result = result.join(Name.anyCamel(pieces[i]));
    }
    return result;
  }

  public String getSimpleInterfaceName(String interfaceName) {
    String[] pieces = interfaceName.split(regexDelimiter);
    return pieces[pieces.length - 1];
  }

  /**
   * Returns the last substring after the input is split by periods. Ex: Input
   * "compute.addresses.aggregatedList" returns "aggregatedList".
   */
  public String getRequestName(Method method) {
    String[] pieces = method.id().split(regexDelimiter);
    return privateFieldName(
        Name.anyCamel(pieces[pieces.length - 2], pieces[pieces.length - 1], "http", "request"));
  }

  /**
   * Returns the last substring after the input is split by periods. Ex: Input
   * "compute.addresses.aggregatedList" returns "aggregatedList".
   */
  public String getResponseName(Method method) {
    String[] pieces = method.id().split(regexDelimiter);
    return privateFieldName(
        Name.anyCamel(pieces[pieces.length - 2], pieces[pieces.length - 1], "http", "response"));
  }

  //TODO(andrealin): Naming methods for service name.
}
