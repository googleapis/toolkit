/* Copyright 2017 Google LLC
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
package com.google.api.codegen.clientconfig.viewmodel;

import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.auto.value.AutoValue;
import java.util.List;

/** Represents the client config being generated. */
@AutoValue
public abstract class ClientConfigView implements ViewModel {
  @Override
  public abstract String templateFileName();

  @Override
  public abstract String outputPath();

  public abstract String name();

  public abstract boolean hasVariable();

  public abstract List<RetryCodeDefView> retryCodesDef();

  public abstract List<RetryParamDefView> retryParamsDef();

  public abstract List<MethodView> methods();

  @Override
  public String resourceRoot() {
    return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;
  }

  public static Builder newBuilder() {
    return new AutoValue_ClientConfigView.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder templateFileName(String val);

    public abstract Builder outputPath(String val);

    public abstract Builder name(String val);

    public abstract Builder hasVariable(boolean val);

    public abstract Builder retryCodesDef(List<RetryCodeDefView> val);

    public abstract Builder retryParamsDef(List<RetryParamDefView> val);

    public abstract Builder methods(List<MethodView> val);

    public abstract ClientConfigView build();
  }
}
