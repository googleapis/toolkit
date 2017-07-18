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
package com.google.api.codegen.transformer;

import com.google.api.codegen.config.FieldType;
import com.google.api.codegen.discovery.Schema;

/** Default implementation of SchemaTypeFormatter. */
public class SchemaTypeFormatterImpl implements SchemaTypeFormatter {
  private SchemaTypeNameConverter typeNameConverter;

  public SchemaTypeFormatterImpl(SchemaTypeNameConverter typeNameConverter) {
    this.typeNameConverter = typeNameConverter;
  }

  @Override
  public String getImplicitPackageFullNameFor(String shortName) {
    return typeNameConverter.getTypeNameInImplicitPackage(shortName).getFullName();
  }

  @Override
  public String getFullNameFor(Schema type) {
    return typeNameConverter.getTypeName(type).getFullName();
  }

  @Override
  public String getInnerTypeNameFor(Schema type) {
    return typeNameConverter.getTypeName(type).getInnerTypeNames().get(0).getNickname();
  }

  @Override
  public String renderPrimitiveValue(Schema type, String value) {
    return typeNameConverter.renderPrimitiveValue(type, value);
  }

  @Override
  public String getNicknameFor(Schema type) {
    return typeNameConverter.getTypeName(type).getNickname();
  }

  @Override
  public String getFullNameFor(FieldType type) {
    return getFullNameFor(type.getSchemaField());
  }

  @Override
  public String getFullNameForElementType(FieldType type) {
    return getFullNameFor(type);
  }

  @Override
  public String renderPrimitiveValue(FieldType type, String value) {
    return renderPrimitiveValue(type.getSchemaField(), value);
  }

  /** Returns the nickname for the given type (without adding the full name to the import set). */
  @Override
  public String getNicknameFor(FieldType type) {
    return getNicknameFor(type.getSchemaField());
  }
}
