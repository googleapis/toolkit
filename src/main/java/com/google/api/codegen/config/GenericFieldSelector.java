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

import com.google.api.codegen.config.FieldType.ApiSource;

/**
 * Wrapper class around the protobuf Field class and the Discovery-doc Schema class.
 *
 * <p>Each instance of this class contains exactly one of {Field, Schema}. This class abstracts the
 * format (protobuf, discovery, etc) of the source from a resource type definition.
 */
public interface GenericFieldSelector {

  /* @return the type of source that this FieldType is based on. */
  ApiSource getApiSource();

  String getParamName();

  FieldType getLastField();
}
