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

import com.google.api.codegen.util.TypeName;
import com.google.api.codegen.util.TypedValue;
import com.google.protobuf.Field;

/**
 * Maps Type instances to TypeName instances.
 */
public interface TypeNameConverter {

  /**
   * Provides a TypeName for the given Field.Kind.
   */
  TypeName getTypeName(Field.Kind kind);

  /**
   * Provides a TypedValue containing the zero value of the given Field.Kind.
   */
  TypedValue getZeroValue(Field.Kind kind);

  /**
   * Renders the given value if it is a primitive type.
   */
  String renderPrimitiveValue(Field.Kind kind, String value);
}
