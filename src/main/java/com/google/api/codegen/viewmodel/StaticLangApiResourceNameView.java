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
package com.google.api.codegen.viewmodel;

import com.google.auto.value.AutoValue;
import java.util.List;

/**
 * This ViewModel defines the view model structure of a generic message.
 *
 * <p>For example, this can be used to represent a Discovery Document's "schemas", "properties",
 * "additionalProperties", and "items".
 *
 * <p>This contains a subset of properties in the JSON Schema
 * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7.
 */
@AutoValue
public abstract class StaticLangApiResourceNameView
    implements Comparable<StaticLangApiResourceNameView> {

  // The possibly-transformed ID of the schema from the Discovery Doc
  public abstract String name();

  // The type name for this Schema when rendered as a field in its parent Schema, e.g. "List<Operation>".
  public abstract String typeName();

  // The name of the class that implements ResourceNameType.
  public abstract String nameTypeName();

  // The template for the path, e.g. "projects/{projects}/topic/{topic}"
  public abstract String pathTemplate();

  // The list of path parameter views.
  public abstract List<StaticMemberView> pathParams();

  public static Builder newBuilder() {
    return new AutoValue_StaticLangApiResourceNameView.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract StaticLangApiResourceNameView.Builder name(String val);

    public abstract StaticLangApiResourceNameView.Builder typeName(String val);

    public abstract StaticLangApiResourceNameView.Builder nameTypeName(String val);

    public abstract StaticLangApiResourceNameView.Builder pathTemplate(String val);

    public abstract StaticLangApiResourceNameView.Builder pathParams(List<StaticMemberView> val);

    public abstract StaticLangApiResourceNameView build();
  }

  @Override
  public int compareTo(StaticLangApiResourceNameView o) {
    return this.name().compareTo(o.name());
  }
}
