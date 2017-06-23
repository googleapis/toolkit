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

import com.google.api.gax.core.RetrySettings;
import com.google.api.tools.framework.model.Method;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.grpc.Status.Code;
import java.util.List;
import javax.annotation.Nullable;

/**
 * InterfaceConfig represents the client code-gen config for an API interface in an input-agnostic
 * way.
 */
public interface InterfaceConfig {
  String getName();

  String getSimpleName();

  @Nullable
  SmokeTestConfig getSmokeTestConfig();

  List<? extends MethodConfig> getMethodConfigs();

  ImmutableMap<String, ImmutableSet<Code>> getRetryCodesDefinition();

  ImmutableMap<String, RetrySettings> getRetrySettingsDefinition();

  ImmutableList<String> getRequiredConstructorParams();

  String getManualDoc();

  MethodConfig getMethodConfig(Method method);

  boolean hasPageStreamingMethods();

  boolean hasLongRunningOperations();

  boolean hasDefaultServiceAddress();

  boolean hasDefaultServiceScopes();

  boolean hasBatchingMethods();

  boolean hasGrpcStreamingMethods();

  boolean hasDefaultInstance();

  ImmutableList<SingleResourceNameConfig> getSingleResourceNameConfigs();

  boolean hasInterfaceNameOverride();
}
