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
package com.google.api.codegen.viewmodel.testing;

import com.google.api.codegen.config.GrpcStreamingConfig.GrpcStreamingType;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MockGrpcMethodView {
  public abstract String name();

  public abstract String requestTypeName();

  public abstract String responseTypeName();

  public abstract String streamHandle();

  public abstract GrpcStreamingType grpcStreamingType();

  public static Builder newBuilder() {
    return new AutoValue_MockGrpcMethodView.Builder()
        .grpcStreamingType(GrpcStreamingType.NonStreaming);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder name(String val);

    public abstract Builder requestTypeName(String val);

    public abstract Builder responseTypeName(String val);

    public abstract Builder streamHandle(String val);

    public abstract Builder grpcStreamingType(GrpcStreamingType val);

    public abstract MockGrpcMethodView build();
  }
}
