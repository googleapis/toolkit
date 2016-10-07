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
package com.google.api.codegen.util;

/**
 * A TypeNameConverter maps Strings to TypeName instances.
 */
public interface TypeNameConverter {
  /**
   * Maps the given fullName to a TypeName.
   */
  TypeName getTypeName(String fullName);

  /**
   * Maps the given short name to a TypeName, using the default package.
   */
  TypeName getTypeNameInImplicitPackage(String shortName);

  /**
   * Maps the given fullName to a NamePath.
   */
  NamePath getNamePath(String fullName);

  /** Creates a TypeName for the container type and element type. */
  TypeName getContainerTypeName(String containerFullName, String... elementFullNames);
}
