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
package com.google.api.codegen.metacode;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * InitCode represents the lines of code necessary to compose a structure.
 */
@AutoValue
public abstract class InitCode {
  public static InitCode create(
      List<InitCodeLine> lines,
      List<FieldSetting> argFields,
      Map<String, String> aliasingTypesMap) {
    return new AutoValue_InitCode(lines, argFields, aliasingTypesMap);
  }

  public abstract List<InitCodeLine> getLines();

  public abstract List<FieldSetting> getArgFields();

  @Nullable
  public abstract Map<String, String> getAliasingTypesMap();
}
