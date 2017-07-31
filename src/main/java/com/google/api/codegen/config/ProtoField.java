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
package com.google.api.codegen.config;

import static com.google.api.codegen.config.FieldType.ApiSource.PROTO;

import com.google.api.codegen.discovery.Schema;
import com.google.api.tools.framework.model.Field;
import com.google.api.tools.framework.model.Oneof;
import com.google.api.tools.framework.model.TypeRef;
import com.google.api.tools.framework.model.TypeRef.Cardinality;
import com.google.common.base.Preconditions;

/** Created by andrealin on 7/31/17. */
public class ProtoField implements FieldType {
  private final Field protoField;
  private final ApiSource apiSource = PROTO;

  @Override
  /* @return the type of the underlying model resource. */
  public ApiSource getApiSource() {
    return apiSource;
  }

  /* Create a FieldType object from a non-null Field object. */
  public ProtoField(Field protoField) {
    Preconditions.checkNotNull(protoField);
    this.protoField = protoField;
  }

  @Override
  public String getSimpleName() {
    return protoField.getSimpleName();
  }

  @Override
  public String getFullName() {
    return protoField.getFullName();
  }

  @Override
  /* @return if the underlying resource is a map type. */
  public boolean isMap() {
    return protoField.getType().isMap();
  }

  /* @return the resource type of the map key. */
  public ProtoField getMapKeyField() {
    return new ProtoField(protoField.getType().getMapKeyField());
  }

  /* @return the resource type of the map value. */
  public ProtoField getMapValueField() {
    return new ProtoField(protoField.getType().getMapValueField());
  }

  /* @return if the underlying resource is a proto Messsage. */
  public boolean isMessage() {
    return protoField.getType().isMessage();
  }

  /* @return if the underlying resource can be repeated in the parent resource. */
  public boolean isRepeated() {
    return protoField.isRepeated();
  }

  /* @return the full name of the parent. */
  public String getParentFullName() {
    return protoField.getParent().getFullName();
  }

  /* @return the cardinality of the resource. */
  public Cardinality getCardinality() {
    return protoField.getType().getCardinality();
  }

  /* @return if this resource is an enum. */
  public boolean isEnum() {
    return protoField.getType().isEnum();
  }

  /* @return if this is a primitive type. */
  public boolean isPrimitive() {
    return protoField.getType().isPrimitive();
  }

  @Override
  public String toString() {
    return String.format("Protobuf FieldType (%s): {%s}", apiSource, protoField.toString());
  }

  @Override
  public TypeRef getProtoTypeRef() {
    return protoField.getType();
  }

  @Override
  public Field getProtoField() {
    return protoField;
  }

  @Override
  public Schema getDiscoveryField() {
    throw new IllegalArgumentException("Protobuf model types have no Discovery Field types.");
  }

  @Override
  public Oneof getProtoOneof() {
    return protoField.getOneof();
  }

  @Override
  public boolean equals(Object o) {
    return o != null
        && o instanceof ProtoField
        && ((ProtoField) o).protoField.equals(this.protoField);
  }
}
