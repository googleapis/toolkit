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
package com.google.api.codegen.configgen.transformer;

import com.google.api.codegen.ResourceNameTreatment;
import com.google.api.codegen.config.MethodModel;
import com.google.api.codegen.configgen.PagingParameters;
import com.google.api.codegen.configgen.viewmodel.PageStreamingResponseView;
import javax.annotation.Nullable;

/**
 * Interface for functions that may have different implementations for different API source types.
 * This is called upon by {@link com.google.api.codegen.configgen.transformer.MethodTransformer}.
 */
public interface MethodHelperTransformer {
  @Nullable
  ResourceNameTreatment getResourceNameTreatment(MethodModel methodModel);

  @Nullable
  PageStreamingResponseView generatePageStreamingResponse(
      PagingParameters pagingParameters, MethodModel method);
}
