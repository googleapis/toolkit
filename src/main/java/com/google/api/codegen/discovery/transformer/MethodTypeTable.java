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
package com.google.api.codegen.discovery.transformer;

import com.google.api.codegen.util.TypeTable;
import com.google.protobuf.Method;

/**
 * Manages the imports for a set of fully-qualified type names.
 */
public class MethodTypeTable implements MethodTypeFormatter {

  private MethodTypeFormatterImpl typeFormatter;
  private TypeTable typeTable;
  private TypeNameConverter typeNameConverter;

  public MethodTypeTable(TypeTable typeTable, TypeNameConverter typeNameConverter) {
    this.typeFormatter = new MethodTypeFormatterImpl(typeNameConverter);
    this.typeTable = typeTable;
    this.typeNameConverter = typeNameConverter;
  }

  @Override
  public String getMethodName(Method method) {
    return typeFormatter.getMethodName(method);
  }

  @Override
  public String getTypeName(String typeName) {
    return typeFormatter.getTypeName(typeName);
  }

  public String renderPrimitiveValue(String typeName, String value) {
    return typeFormatter.renderPrimitiveValue(typeName, value);
  }
}
