/* Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.transformer.java;

import com.google.api.codegen.config.FieldConfig;
import com.google.api.codegen.transformer.DefaultFeatureConfig;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JavaFeatureConfig extends DefaultFeatureConfig {

  @Override
  public abstract boolean enableStringFormatFunctions();

  @Override
  public boolean resourceNameTypesEnabled() {
    return true;
  }

  @Override
  public boolean useResourceNameFormatOption(FieldConfig fieldConfig) {
    return resourceNameTypesEnabled()
        && fieldConfig != null
        && fieldConfig.useResourceNameType()
        && !fieldConfig.getField().isRepeated();
  }

  @Override
  public boolean useResourceNameFormatOptionInSample(FieldConfig fieldConfig) {
    return resourceNameTypesEnabled()
        && fieldConfig != null
        && (fieldConfig.useResourceNameType() || fieldConfig.useResourceNameTypeInSampleOnly())
        && !fieldConfig.getField().isRepeated();
  }

  @Override
  public boolean useInheritanceForOneofs() {
    return true;
  }

  @Override
  public boolean enableMixins() {
    return true;
  }

  @Override
  public boolean enableRawOperationCallSettings() {
    return true;
  }

  public static Builder newBuilder() {
    return new AutoValue_JavaFeatureConfig.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder enableStringFormatFunctions(boolean value);

    public abstract JavaFeatureConfig build();
  }
}
