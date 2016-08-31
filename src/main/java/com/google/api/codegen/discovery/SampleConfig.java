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
package com.google.api.codegen.discovery;

import java.util.ArrayList;
import java.util.List;

import com.google.api.codegen.ApiaryConfig;
import com.google.auto.value.AutoValue;
import com.google.protobuf.Method;

@AutoValue
public abstract class SampleConfig {

  public static SampleConfig createSampleConfig(Method method, ApiaryConfig apiaryConfig) {
    Builder sampleConfig = newBuilder();
    sampleConfig.apiTitle(apiaryConfig.getServiceTitle());
    sampleConfig.apiName(apiaryConfig.getServiceCanonicalName());
    sampleConfig.apiVersion(apiaryConfig.getServiceVersion());

    MethodInfo.Builder methodInfo = MethodInfo.newBuilder();
    String methodName = method.getName();
    methodInfo.methodName(methodName);
    methodInfo.resources(apiaryConfig.getResources(methodName));

    List<FieldInfo> fields = new ArrayList<>();
    for (String param : apiaryConfig.getMethodParams(methodName)) {
      fields.add(
          FieldInfo.newBuilder()
              .name(param)
              .doc(apiaryConfig.getDescription(method.getRequestTypeUrl(), param))
              .build());
    }

    methodInfo.fields(fields);

    sampleConfig.methodInfo(methodInfo.build());
    return sampleConfig.build();
  }

  public abstract String apiTitle();

  public abstract String apiName();

  public abstract String apiVersion();

  public abstract MethodInfo methodInfo();

  public static Builder newBuilder() {
    return new AutoValue_SampleConfig.Builder();
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder apiTitle(String val);

    public abstract Builder apiName(String val);

    public abstract Builder apiVersion(String val);

    public abstract Builder methodInfo(MethodInfo val);

    public abstract SampleConfig build();
  }
}
