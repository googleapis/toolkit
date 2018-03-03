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
package com.google.api.codegen.transformer;

import com.google.api.codegen.config.ApiModel;
import com.google.api.codegen.config.InterfaceModel;
import com.google.api.codegen.config.ProductConfig;
import com.google.api.codegen.viewmodel.testing.MockGrpcMethodView;
import com.google.api.codegen.viewmodel.testing.MockServiceUsageView;
import java.util.List;
import java.util.Map;

/** MockServiceTransformer contains helper methods useful for creating mock views. */
public interface MockServiceTransformer {
  List<InterfaceModel> getGrpcInterfacesToMock(ApiModel model, ProductConfig productConfig);

  Map<String, InterfaceModel> getGrpcInterfacesForService(
      ApiModel model, ProductConfig productConfig, InterfaceModel apiInterface);

  List<MockGrpcMethodView> createMockGrpcMethodViews(InterfaceContext context);

  List<MockServiceUsageView> createMockServices(
      SurfaceNamer namer, ApiModel model, ProductConfig productConfig);
}
