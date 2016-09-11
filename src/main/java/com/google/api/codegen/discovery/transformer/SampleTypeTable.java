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

import java.util.List;

import com.google.api.codegen.discovery.SampleConfig;
import com.google.api.codegen.discovery.TypeInfo;
import com.google.api.codegen.util.TypeName;
import com.google.api.codegen.util.TypeTable;
import com.google.api.codegen.util.TypedValue;
import com.google.protobuf.Method;

/**
 * Manages the imports for a set of fully-qualified type names.
 */
public class SampleTypeTable implements SampleTypeFormatter {

  private SampleTypeFormatterImpl typeFormatter;
  private TypeTable typeTable;
  private ProtobufTypeNameConverter typeNameConverter;

  public SampleTypeTable(TypeTable typeTable, ProtobufTypeNameConverter typeNameConverter) {
    this.typeFormatter = new SampleTypeFormatterImpl(typeNameConverter);
    this.typeTable = typeTable;
    this.typeNameConverter = typeNameConverter;
  }

  public String getZeroValueAndSaveNicknameFor(TypeInfo type) {
    return typeNameConverter.getZeroValue(type).getValueAndSaveTypeNicknameIn(typeTable);
  }

  public String getAndSaveNicknameFor(SampleConfig sampleConfig) {
    return typeTable.getAndSaveNicknameFor(typeNameConverter.getServiceTypeName());
  }

  @Override
  public String getTypeName(String typeName) {
    // TODO(saicheems): Auto-generated method stub
    return "TODO";
  }

  public TypeName getTypeName(TypeInfo typeInfo) {
    return typeNameConverter.getTypeName(typeInfo);
  }

  public TypedValue getZeroValue(TypeInfo typeInfo) {
    return typeNameConverter.getZeroValue(typeInfo);
  }

  @Override
  public String getMethodName(Method method) {
    return typeFormatter.getMethodName(method);
  }

  @Override
  public String renderPrimitiveValue(String typeName, String value) {
    return typeFormatter.renderPrimitiveValue(typeName, value);
  }

  public String getAndSaveNicknameFor(TypeInfo typeInfo) {
    return typeTable.getAndSaveNicknameFor(typeNameConverter.getTypeName(typeInfo));
  }

  public void saveNicknameFor(String fullName) {
    typeTable.getAndSaveNicknameFor(fullName);
  }

  public List<String> getImports() {
    return typeTable.getImports();
  }
}
