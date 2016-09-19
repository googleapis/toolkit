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
package com.google.api.codegen;

import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.DiagCollector;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.SimpleLocation;

import java.util.ArrayList;
import java.util.List;

/** SmokeTestConfig represents the smoke test configuration for a method. */
public class SmokeTestConfig {
  private final Method method;
  private final List<String> initFieldNames;
  private final List<String> initFieldSpecs;

  private SmokeTestConfig(Method method, List<String> initFieldSpecs, List<String> initFieldNames) {
    this.initFieldSpecs = initFieldSpecs;
    this.initFieldNames = initFieldNames;
    this.method = method;
  }

  public static SmokeTestConfig createSmokeTestConfig(
      Interface service, SmokeTestConfigProto smokeTestConfigProto, DiagCollector diagCollector) {
    Method testedMethod = null;
    for (Method method : service.getMethods()) {
      if (method.getSimpleName().equals(smokeTestConfigProto.getMethod())) {
        testedMethod = method;
        break;
      }
    }

    if (testedMethod != null) {
      List<String> initFieldSpecs = smokeTestConfigProto.getInitFieldsList();
      ArrayList<String> initFieldNames = new ArrayList<>();
      for (String fieldSpec : initFieldSpecs) {
        String[] fieldSpecParts = fieldSpec.split("[<=>]");
        initFieldNames.add(fieldSpecParts[0]);
      }
      return new SmokeTestConfig(testedMethod, initFieldSpecs, initFieldNames);
    } else {
      diagCollector.addDiag(
          Diag.error(SimpleLocation.TOPLEVEL, "The configured smoke test method does not exist."));
      return null;
    }
  }

  /** Returns a list of initialized field names. */
  public List<String> getInitFieldNames() {
    return initFieldNames;
  }

  /** Returns a list of initialized fields configuration. */
  public List<String> getInitFieldSpecs() {
    return initFieldSpecs;
  }

  /** Returns the method that is used in the smoke test. */
  public Method getMethod() {
    return method;
  }
}
