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
package com.google.api.codegen.configgen.viewmodel;

import com.google.auto.value.AutoValue;
import java.util.List;

/** Represents the GAPIC config being generated. */
@AutoValue
public abstract class ConfigView {
  /** The type of the config's proto. */
  public abstract String type();

  /** The settings of generated code in a specific language. */
  public abstract List<LanguageSettingView> languageSettings();

  /** The configuration for the license header to put on generated files. */
  public abstract LicenseView license();

  /** The API interface configurations. */
  public abstract List<InterfaceView> interfaces();

  public static Builder newBuilder() {
    return new AutoValue_ConfigView.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder type(String val);

    public abstract Builder languageSettings(List<LanguageSettingView> val);

    public abstract Builder license(LicenseView val);

    public abstract Builder interfaces(List<InterfaceView> val);

    public abstract ConfigView build();
  }
}
