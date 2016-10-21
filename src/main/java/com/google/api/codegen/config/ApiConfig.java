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
package com.google.api.codegen.config;

import com.google.api.codegen.ConfigProto;
import com.google.api.codegen.InterfaceConfigProto;
import com.google.api.codegen.LanguageSettingsProto;
import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.DiagCollector;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.SimpleLocation;
import com.google.api.tools.framework.model.SymbolTable;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nullable;

/** ApiConfig represents the code-gen config for an API library. */
@AutoValue
public abstract class ApiConfig {
  abstract ImmutableMap<String, InterfaceConfig> getInterfaceConfigMap();

  /**
   * Returns the package name.
   */
  public abstract String getPackageName();

  /**
   * Returns the location of the domain layer, if any.
   */
  public abstract String getDomainLayerLocation();

  /**
   * Creates an instance of ApiConfig based on ConfigProto, linking up API interface configurations
   * with specified interfaces in interfaceConfigMap. On errors, null will be returned, and
   * diagnostics are reported to the model.
   */
  @Nullable
  public static ApiConfig createApiConfig(Model model, ConfigProto configProto) {
    ImmutableMap<String, InterfaceConfig> interfaceConfigMap =
        createInterfaceConfigMap(model.getDiagCollector(), configProto, model.getSymbolTable());
    LanguageSettingsProto settings =
        configProto.getLanguageSettings().get(configProto.getLanguage());
    if (settings == null) {
      settings = LanguageSettingsProto.getDefaultInstance();
    }
    if (interfaceConfigMap == null) {
      return null;
    } else {
      return new AutoValue_ApiConfig(
          interfaceConfigMap, settings.getPackageName(), settings.getDomainLayerLocation());
    }
  }

  /**
   * Creates an ApiConfig with no content. Exposed for testing.
   */
  @VisibleForTesting
  public static ApiConfig createDummyApiConfig() {
    return new AutoValue_ApiConfig(ImmutableMap.<String, InterfaceConfig>builder().build(), "", "");
  }

  /** Creates an ApiConfig with fixed content. Exposed for testing. */
  @VisibleForTesting
  public static ApiConfig createDummyApiConfig(
      ImmutableMap<String, InterfaceConfig> interfaceConfigMap,
      String packageName,
      String domainLayerLocation) {
    return new AutoValue_ApiConfig(interfaceConfigMap, packageName, domainLayerLocation);
  }

  private static ImmutableMap<String, InterfaceConfig> createInterfaceConfigMap(
      DiagCollector diagCollector, ConfigProto configProto, SymbolTable symbolTable) {
    ImmutableMap.Builder<String, InterfaceConfig> interfaceConfigMap =
        ImmutableMap.<String, InterfaceConfig>builder();
    for (InterfaceConfigProto interfaceConfigProto : configProto.getInterfacesList()) {
      Interface iface = symbolTable.lookupInterface(interfaceConfigProto.getName());
      if (iface == null || !iface.isReachable()) {
        diagCollector.addDiag(
            Diag.error(
                SimpleLocation.TOPLEVEL,
                "interface not found: %s",
                interfaceConfigProto.getName()));
        continue;
      }
      InterfaceConfig interfaceConfig =
          InterfaceConfig.createInterfaceConfig(
              diagCollector, configProto.getLanguage(), interfaceConfigProto, iface);
      if (interfaceConfig == null) {
        continue;
      }
      interfaceConfigMap.put(interfaceConfigProto.getName(), interfaceConfig);
    }

    if (diagCollector.getErrorCount() > 0) {
      return null;
    } else {
      return interfaceConfigMap.build();
    }
  }

  /**
   * Returns the InterfaceConfig for the given API interface.
   */
  public InterfaceConfig getInterfaceConfig(Interface iface) {
    return getInterfaceConfigMap().get(iface.getFullName());
  }
}
